import type { Color, FilterMeta, PagedResponse, Palette, PaletteFilters } from '../types'

const BASE = import.meta.env.VITE_API_BASE_URL ?? '/api'

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`)
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`)
  return res.json()
}

function qs(params: Record<string, string | number | undefined>): string {
  const entries = Object.entries(params).filter(([, v]) => v !== undefined && v !== '')
  if (!entries.length) return ''
  return '?' + entries.map(([k, v]) => `${k}=${encodeURIComponent(String(v))}`).join('&')
}

export const api = {
  getPalettes: (f: PaletteFilters = {}): Promise<PagedResponse<Palette>> =>
    get(`/palettes${qs({ page: f.page ?? 0, size: 24, hue: f.hue, era: f.era, type: f.type, mood: f.mood, q: f.q })}`),

  getPalette: (slug: string): Promise<Palette> =>
    get(`/palettes/${slug}`),

  getFilterMeta: (): Promise<FilterMeta> =>
    get('/palettes/meta'),

  getColors: (page = 0): Promise<PagedResponse<Color>> =>
    get(`/colors${qs({ page, size: 24 })}`),

  getColor: (slug: string): Promise<Color> =>
    get(`/colors/${slug}`),
}
