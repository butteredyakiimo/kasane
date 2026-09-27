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
- Dev: H2 in-memory, `ddl-auto: create-drop` → full reseed on every restart.
- Prod: RDS Postgres, `ddl-auto: update` → seeds once ever, on first boot against a
  fresh database. Schema then evolves via Hibernate auto-DDL on later boots (no
  Flyway/Liquibase — an existing tradeoff, not something to "fix" casually).

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

**Transport, prod (AWS).** Static React build lives in S3, served through CloudFront
— CloudFront is a CDN in front of S3 only, it does **not** proxy `/api` to the
backend. Spring Boot runs dockerized on EC2 (or Elastic Beanstalk, which is EC2
underneath), backed by RDS Postgres. Because CloudFront and the API are different
origins, the browser calls the EC2/EB origin directly and cross-origin — CORS is
what makes this actually work in prod, not just a dev-time safety net.

## Environment variables

| Var | Read by | Dev | Prod |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Spring Boot | unset (uses `application.yml`) | `prod` — **must be set explicitly** in the EC2/EB environment, or `application-prod.yml` never activates |
| `DATABASE_URL` / `DATABASE_USER` / `DATABASE_PASSWORD` | `application-prod.yml` | n/a (H2, no env vars needed) | point at the RDS instance |
| `CORS_ALLOWED_ORIGINS` | `application-prod.yml` → `CorsConfig` | n/a (`application.yml` defaults to `http://localhost:5173,http://localhost:3000`) | comma-separated list — the CloudFront/S3 domain (or custom domain) |
| `VITE_API_BASE_URL` | `frontend/src/services/api.ts` | unset — falls back to `/api`, handled by the Vite proxy | absolute URL of the EC2/EB API, e.g. `https://api.<your-domain>.com` |
| `ANTHROPIC_API_KEY` | `AssistantConfig` → `AnthropicOkHttpClient.fromEnv()` | required to actually exercise the palette assistant; without it the endpoint degrades gracefully (canned reply, no palettes) rather than failing | same var, set in the EC2/EB environment |

**Important:** Vite inlines `import.meta.env.VITE_*` values **at build time**, not
read at runtime. Since the frontend ships as a static S3 bundle, changing
`VITE_API_BASE_URL` later means rebuilding and re-uploading to S3 — it's not a
config flip like the backend's env vars are.

See `frontend/.env.example` for the frontend-side var.

## CORS

`backend/src/main/java/com/kasane/config/CorsConfig.java` binds allowed origins from
`app.cors.allowed-origins` instead of hardcoding them — `GET`-only, matching the
read-only API. Dev defaults to the two localhost origins in `application.yml`; prod
requires `CORS_ALLOWED_ORIGINS` to be set (`application-prod.yml` has no default, so
it fails fast if missing).

## Dev tooling

No monorepo tool (no Turbo/Nx/yarn workspaces) — backend is Gradle/Java, and
frontend is the only JS package, so there's nothing to hoist or build-graph. The root
`package.json` is just a script runner:

```
yarn dev              # frontend only (vite) - run the backend yourself in a separate
                       # terminal (cd backend && ./gradlew bootRun) so env vars like
                       # ANTHROPIC_API_KEY and backend logs are unambiguous
yarn dev:all          # backend + frontend together via concurrently, the old `yarn dev` behavior
yarn build:backend    # ./gradlew build
yarn build:frontend   # vite build
```

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
