# Kasane

A reference browser for traditional Japanese color combinations ("kasane no irome")
and the individual colors that make them up.

- **Backend** — Java 21, Spring Boot 3, Gradle. Read-only REST API (`/api/colors`,
  `/api/palettes`) backed by H2 in dev / Postgres (RDS) in prod, seeded from CSV on
  boot. See [`backend/AGENTS.md`](backend/AGENTS.md).
- **Frontend** — React 18, TypeScript, Vite, TanStack Query, Tailwind CSS. See
  [`frontend/AGENTS.md`](frontend/AGENTS.md).
- Full data flow, environment variables, CORS, and the shared-types convention
  between the two: [`AGENTS.md`](AGENTS.md).

## Quickstart

```bash
cd backend && ./gradlew bootRun
cd frontend && yarn dev
```

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

Planned target: S3 + CloudFront (static frontend build), EC2/Elastic Beanstalk
(dockerized backend, see [`backend/Dockerfile`](backend/Dockerfile)), RDS Postgres.
Required environment variables are documented in [`AGENTS.md`](AGENTS.md#environment-variables).
