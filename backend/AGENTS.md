# AGENTS.md

This file provides guidance to AI coding agents (Claude Code, Codex, Cursor, etc.) when working with code in this repository.

## What this is

`kasane-backend` is a read-only Spring Boot REST API serving a reference dataset of traditional Japanese colors ("kasane no irome") and color palettes.

Dev DB is H2 in-memory, auto-seeded on every startup from CSV (see below); there's nothing to migrate or persist between runs. H2 console is enabled at `/h2-console` in dev only.

## Architecture

Standard layered Spring MVC, one layer per package under `com.kasane`. Controllers hold no logic; services do manual entity↔DTO mapping.

There are two independent resources — Colors and Palettes — with no association between the `Color` and `Palette` entities. `Palette` embeds up to 4 colors directly as flat columns (`hex1..hex4`, `colorName1..colorName4`, `colorNameJa1..colorNameJa4`) rather than via a join table or child entity; `PaletteService.toDto()` assembles these into a `List<PaletteColorDto>` based on `colorCount`.

### API design

- All endpoints are `GET`, under `/api/colors` and `/api/palettes` — no write endpoints exist.
- Pagination: `page`/`size` query params, standard Spring `PageRequest`, response wrapped in `PagedResponse<T>` (content, page, size, totalElements, totalPages, last).
- Palette filtering (`GET /api/palettes`) is dynamic via `PaletteSpec.withFilters(hue, era, colorCount, mood, q)`, a JPA `Specification` combining optional predicates (exact match on hue/era/colorCount, `LIKE` on mood and free-text `q` across title/summary/color names).
- `GET /api/palettes/meta` returns available filter values (`FilterMetaDto`: hues and eras pulled distinct from the DB, moods hardcoded as a fixed list in `PaletteService.ALL_MOODS`).
- Lookup-by-slug endpoints (`GET /api/colors/{slug}`, `GET /api/palettes/{slug}`) return 404 via `ResponseStatusException` when not found.

### Authentication

None. There is no Spring Security dependency, no auth middleware, no filters/interceptors of any kind. The only access control is `CorsConfig`, whose allowed origins come from `app.cors.allowed-origins` (localhost defaults in `application.yml`, `CORS_ALLOWED_ORIGINS` in prod).

### Data seeding

`DataLoader` (an `ApplicationRunner`) loads `src/main/resources/data/colors.csv` and `palettes.csv` into the DB on every startup, but only if the `colors` table is empty — so seeding is a one-time no-op after first boot in a given DB instance. This is the source of truth for entity fields; check the CSV headers alongside `Color`/`Palette` when changing schema.

### Environments

- Dev (`application.yml`): H2 in-memory (`ddl-auto: create-drop`, wiped every restart), H2 console on.
- Prod (`application-prod.yml`): Postgres via `DATABASE_URL`/`DATABASE_USER`/`DATABASE_PASSWORD` env vars, `ddl-auto: update`, `open-in-view: false`, H2 console off.
