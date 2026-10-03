# Kasane

A reference browser for traditional Japanese color combinations ("kasane no irome")
and the individual colors that make them up.

- **Backend** — Java 21, Spring Boot 4, Gradle. Read-only REST API (`/api/colors`,
  `/api/palettes`) backed by Postgres (Docker Compose, dev and prod alike), seeded
  from CSV on first boot. See [`backend/AGENTS.md`](backend/AGENTS.md).
- **Frontend** — React 18, TypeScript, Vite, TanStack Query, Tailwind CSS. See
  [`frontend/AGENTS.md`](frontend/AGENTS.md).
- Full data flow, environment variables, CORS, and the shared-types convention
  between the two: [`AGENTS.md`](AGENTS.md).

## Quickstart

Requires Docker running locally (for Postgres), Java 21, and Node/Yarn.

```bash
# 1. Start Postgres (once per session - persists across restarts in a named volume)
docker compose up -d

# 2. Optional: export your own key to exercise the palette assistant feature.
#    Everything else works without it (the assistant degrades gracefully instead
#    of failing). Re-export in each new terminal - this isn't read from a .env file.
export ANTHROPIC_API_KEY=<your key>

# 3. Backend, in its own terminal
cd backend && ./gradlew bootRun

# 4. Frontend, in another terminal
yarn dev
```

Backend runs on `localhost:8080`, frontend on `localhost:5173` (proxies `/api` to
the backend). No `.env` file is needed for this everyday flow - see
[`AGENTS.md`](AGENTS.md#environment-variables) for when one actually applies
(testing the containerized backend build via `docker compose --profile full`, and
prod).

## Data & attribution

The color and palette CSVs (`data/*.csv`) are sourced from
[colorcombinations.org](https://colorcombinations.org/data/), released under
[CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). That dataset traces back
to Sanzo Wada's *A Dictionary of Color Combinations* (1933; reprinted by Seigensha
Art Publishing, 2010), with 348 of the 378 palettes originally compiled in the
[mattdesl/dictionary-of-colour-combinations](https://github.com/mattdesl/dictionary-of-colour-combinations)
GitHub repository (MIT).

This attribution covers the CSV data only — the code in this repository is
separately licensed under MIT (see [`LICENSE`](LICENSE)).

## Deployment

Planned target: a single VPS running the backend and Postgres together via
`docker-compose.yml`'s `full` profile (`docker compose --profile full up -d --build`,
see [`backend/Dockerfile`](backend/Dockerfile)), with the frontend built statically
and deployed separately to Vercel or Netlify's free tier. Required environment
variables are documented in [`AGENTS.md`](AGENTS.md#environment-variables).