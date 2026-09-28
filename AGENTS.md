# AGENTS.md

Full-stack overview tying together `frontend/AGENTS.md` and `backend/AGENTS.md`. Read
those two for per-side detail (routing/state on the FE, layers/API design on the BE);
this doc covers how the two sides actually connect: data flow, env vars, CORS, dev
tooling, and the type-sharing convention.

## End-to-end data flow

**Seed data.** `data/*.csv` at repo root is the hand-maintained source dataset. It's
manually copied into `backend/src/main/resources/data/*.csv`, which is what actually
ships in the backend build — there's no build step that syncs these, so remember to
copy both when editing the dataset.

**Seeding.** `DataLoader` (an `ApplicationRunner`) loads those CSVs into the `colors`
and `palettes` tables on every boot, but only if `colors` is empty:
- Dev: Postgres via Docker Compose (`docker compose up -d` at repo root), `ddl-auto:
  update` → seeds once, on first boot against the fresh container volume, then persists
  across restarts (`./gradlew bootRun` no longer wipes your data). To force a full
  reseed, `docker compose down -v && docker compose up -d`.
- Prod: Postgres running as the `db` service in the same `docker-compose.yml`, on the
  VPS, `ddl-auto: update` → seeds once ever, on first boot against a fresh volume.
  Schema then evolves via Hibernate auto-DDL on later boots (no Flyway/Liquibase — an
  existing tradeoff, not something to "fix" casually).
- Tests: `@DataJpaTest` runs against H2 in-memory (`src/test/resources/application.yml`,
  `ddl-auto: create-drop`), independent of whatever's in the dev Postgres container —
  H2 is a `testRuntimeOnly` dependency, not used by the running app anymore.

**Request path.** `Controller` (query params only, no logic) → `Service` (entity↔DTO
mapping, `PaletteSpec` builds dynamic JPA `Specification`s for filtering) →
`Repository` (Spring Data JPA) → DB.

**Frontend.** React Query hooks (`hooks/usePalettes.ts`) → `services/api.ts`
(`fetch`) → JSON deserialized straight into the `types/index.ts` interfaces, which
are hand-kept in sync with the backend DTOs (see "Shared types" below) → components.

**Transport, dev.** `vite.config.ts` proxies `/api/**` → `http://localhost:8080`, so
the browser sees same-origin requests. CORS exists in dev but isn't really exercised
through the proxy.

**Palette assistant.** `POST /api/assistant/chat` (`com.kasane.assistant.*`) is the one
write-shaped, non-read-only endpoint in the API — a conversational palette finder. It's
intentionally separate from the read-only `Controller → Service → Repository` path
above: `AssistantController` → `RateLimiterService` (per-IP token bucket, 429 on
exceeded) → `ScopeGuardService` (Haiku 4.5, structured output, fails open on API/network
errors) → `PaletteAssistantService` (manual Claude tool-use loop, Sonnet 5, tools =
`search_palettes`/`get_filter_meta` calling straight into the existing `PaletteService`
— no new query logic). The palette list returned to the frontend is always exactly
what `search_palettes` returned that turn, never something parsed out of the model's
prose — that's the hallucination guardrail. Both Anthropic exception types
(`AnthropicServiceException` for HTTP-level failures, `AnthropicIoException` for
network-level ones) share a common `AnthropicException` base; catch that, not the
narrower subclass, or a DNS blip / offline sandbox surfaces as a raw 500 instead of the
graceful fallback reply.

**Transport, prod (VPS + Vercel/Netlify).** Backend and DB are containerized together
on a single VPS via `docker-compose.yml`'s `full` profile (`app` + `db` services on
one Docker network, `app` reaching `db` by service name — see "Dev tooling" below).
The frontend is a static build deployed separately to Vercel or Netlify's free tier
(not part of this repo's deploy story beyond `yarn build:frontend` producing the
bundle they serve). Because Vercel/Netlify and the VPS are different origins, the
browser calls the VPS directly and cross-origin — CORS is what makes this actually
work in prod, not just a dev-time safety net. This is a deliberate downgrade from an
earlier AWS plan (S3/CloudFront/EC2/RDS) — dropped as overkill for a pet project; a
VPS is cheaper and simpler at this scale, at the cost of losing RDS's managed
backups/failover (own a `pg_dump` cron or similar if that data ever matters).

## Environment variables

| Var | Read by | Dev | Prod |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Spring Boot | unset (uses `application.yml`) | `prod` — set directly in `docker-compose.yml`'s `app` service (already done), or `application-prod.yml` never activates |
| `DATABASE_URL` / `DATABASE_USER` / `DATABASE_PASSWORD` | `application-prod.yml` | n/a — dev connection is hardcoded in `application.yml` (`localhost:5433`, Docker Compose) | `DATABASE_URL` points at the `db` compose service (`jdbc:postgresql://db:5432/kasane`); `DATABASE_PASSWORD` should be overridden via a gitignored `.env` on the VPS, not left at the committed local-dev default |
| `CORS_ALLOWED_ORIGINS` | `application-prod.yml` → `CorsConfig` | n/a (`application.yml` defaults to `http://localhost:5173,http://localhost:3000`) | comma-separated list — the Vercel/Netlify domain (or custom domain) |
| `VITE_API_BASE_URL` | `frontend/src/services/api.ts` | unset — falls back to `/api`, handled by the Vite proxy | absolute URL of the VPS backend, e.g. `https://api.<your-domain>.com` |
| `ANTHROPIC_API_KEY` | `AssistantConfig` → `AnthropicOkHttpClient.fromEnv()` | required to actually exercise the palette assistant; without it the endpoint degrades gracefully (canned reply, no palettes) rather than failing | same var, set on the VPS (host env, passed through by `docker-compose.yml`) |

**Important:** Vite inlines `import.meta.env.VITE_*` values **at build time**, not
read at runtime. Since the frontend ships as a static bundle to Vercel/Netlify,
changing `VITE_API_BASE_URL` later means rebuilding and redeploying it there — it's
not a config flip like the backend's env vars are.

**Where secrets actually come from — two different mechanisms, don't conflate them:**
- Plain `./gradlew bootRun` (everyday backend dev): Spring Boot has no `.env`
  support. `ANTHROPIC_API_KEY` must be a real exported shell variable before you
  launch it — a `.env` file does nothing here on its own.
- `docker compose` (both the default `db`-only run and `--profile full`): Compose
  natively auto-loads a `.env` file sitting next to `docker-compose.yml` and uses it
  for that file's `${VAR}` interpolation. Copy `.env.example` → `.env` (gitignored)
  at the repo root to set `ANTHROPIC_API_KEY` / `DATABASE_PASSWORD` /
  `CORS_ALLOWED_ORIGINS` for the `app` service this way.
- Prod (VPS): same Compose `.env` mechanism, but that file is created directly on
  the VPS's disk and never touches this repo or GitHub.

See `frontend/.env.example` for the frontend-side var.

## CORS

`backend/src/main/java/com/kasane/config/CorsConfig.java` binds allowed origins from
`app.cors.allowed-origins` instead of hardcoding them, on a single `/api/**` mapping
allowing `GET, POST` (one mapping, not two overlapping ones — see the comment in that
file for why splitting it by sub-path doesn't work the way you'd expect). Dev defaults
to the two localhost origins in `application.yml`; prod requires `CORS_ALLOWED_ORIGINS`
to be set to the Vercel/Netlify domain (`application-prod.yml` has no default, so it
fails fast if missing).

## Dev tooling

No monorepo tool (no Turbo/Nx/yarn workspaces) — backend is Gradle/Java, and
frontend is the only JS package, so there's nothing to hoist or build-graph. The root
`package.json` is just a script runner:

```
docker compose up -d              # starts only the dev Postgres container (localhost:5433) -
                                   # run this once before the backend; data persists across
                                   # restarts in a named volume until you `docker compose down -v`
yarn dev                          # frontend only (vite) - run the backend yourself in a separate
                                   # terminal (cd backend && ./gradlew bootRun) so env vars like
                                   # ANTHROPIC_API_KEY and backend logs are unambiguous
yarn dev:all                      # backend + frontend together via concurrently, the old `yarn dev` behavior
yarn build:backend                # ./gradlew build
yarn build:frontend               # vite build
docker compose --profile full up -d --build
                                   # builds and runs the actual containerized backend
                                   # (app + db, application-prod.yml) - the same shape that
                                   # runs on the VPS. Not part of the everyday dev loop; use
                                   # it to sanity-check the container build before deploying.
```

Requires Docker (Desktop or equivalent) running locally. `docker-compose.yml` is
deliberately the same file used in prod (`app` + `db` services, gated behind the
`full` profile so a plain `docker compose up -d` still just gives you `db` for fast
local iteration against `./gradlew bootRun`). Its Postgres version (`postgres:16`)
should track whatever the VPS actually runs — update both together if that changes.

## Shared types

No codegen (no OpenAPI/`openapi-typescript`) — `frontend/src/types/index.ts` is
hand-kept in sync with the backend DTOs. Mapping:

| Backend DTO | Frontend type |
|---|---|
| `ColorDto` | `Color` |
| `PaletteDto` + `PaletteColorDto` | `Palette` + `PaletteColor` |
| `PagedResponse<T>` | `PagedResponse<T>` |
| `FilterMetaDto` | `FilterMeta` |

**Rule:** a field change in any `dto/*.java` file must be mirrored in
`frontend/src/types/index.ts` in the same change.
