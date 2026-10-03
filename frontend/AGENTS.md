# Kasane Frontend — Agent Reference

Read this before exploring the codebase — it's a small app and this doc should make
further hierarchy/routing/state spelunking unnecessary.

No Redux/Context store — server state lives in React Query, UI state is local
`useState`. Backend is a separate Spring Boot app (`kasane/backend`) proxied at
`/api` in dev (see `vite.config.ts`).

## Folder structure

Flat, **type-based** (not feature-based) — everything is grouped by role, not by
domain, since there's only one domain (palettes/colors).

If the app grows more domains (e.g. a "colors" browse page beyond the existing
`api.getColors`/`getColor` stubs), consider migrating to feature folders
(`features/palettes/`, `features/colors/`) — but as of now everything fits in
type-based buckets above.

## Routing (react-router-dom v6, `App.tsx`)

```
<Layout>                       "/"              -> BrowsePage
  (header/nav + <Outlet/>)     "/palettes/:slug" -> DetailPage
                               "/about"          -> AboutPage
```

- `Layout` is a pathless parent route — pure UI shell (Kasane header, "Palettes" /
  "About" nav links), no data fetching. Renders children via `<Outlet/>`.
- `AboutPage` is static (no data fetching) and holds the dataset's attribution: CC BY
  4.0 for colorcombinations.org, the full MIT notice for mattdesl's dataset, and the
  Sanzo Wada source. Keep it in sync with `README.md`'s "Data & attribution" section.
- No nested/lazy routes, no route loaders/actions — data fetching happens inside
  page components via hooks, not via router loaders.
- Navigation: `PaletteCard` uses `useNavigate()` on click; `Layout`/`DetailPage` use
  `<Link>`. `DetailPage` reads the slug via `useParams<{ slug: string }>()`.

## Component hierarchy

`components/` items are all leaf/presentational — they take data + callbacks as
props and don't call hooks/`usePalettes` themselves. Only `pages/` call the data
hooks.

## State management strategy

No global client-state library (no Redux/Zustand/Context store). Two kinds of state:

1. **Server state → TanStack Query** (`hooks/usePalettes.ts`), backed by a single
   `QueryClient` created in `App.tsx`:
   - `usePalettes(filters)` — `['palettes', filters]`, `staleTime: 5min`
   - `usePalette(slug)` — `['palette', slug]`, `staleTime: 10min`, `enabled: !!slug`
   - `useFilterMeta()` — `['filter-meta']`, `staleTime: Infinity` (hues/eras/moods
     list, effectively static)
   - All actual HTTP calls go through `services/api.ts` (`fetch` against `/api/...`,
     proxied to `localhost:8080` in dev). Hooks never call `fetch` directly.

2. **UI state → local `useState`**, lifted only as far as needed, no prop-drilling
   beyond one level:
   - `BrowsePage`: `filters: PaletteFilters` (search/hue/era/type/page) — owns
     filter state, passes `filters` + `onChange` down to `FilterBar`; `usePalettes`
     re-fetches whenever `filters` changes (it's part of the query key).
   - `DetailPage`: `tab` (active detail tab) and `copied` (which hex was just
     copied, for the "✓ Copied" flash) — both page-local, not shared.
   - `CSSExport`: its own local `copied` boolean for its own copy button.

There is no client-side caching/state beyond what React Query provides; no
persisted state (no localStorage use anywhere in `src/`).

## Conventions worth knowing

- Components are named exports (`export function X`), not default exports (except
  `App.tsx`, which the entrypoint imports as default).
- Styling is Tailwind utility classes inline; no CSS modules/styled-components.
- Two-tone type system: `Palette`/`Color` (DB-shaped, from `types/index.ts`) vs.
  `PaletteFilters`/`FilterMeta`/`PagedResponse<T>` (API request/response shapes).
- Pure color math is isolated in `utils/colorUtils.ts` — no component computes
  luminance/contrast/darkness itself, they all import from there.
