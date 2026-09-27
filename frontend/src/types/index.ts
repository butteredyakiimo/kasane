export interface PaletteColor {
  position: number
  hex: string
  name: string
  nameJa: string
}

export interface Palette {
  id: number
  slug: string
  title: string
  titleJa: string
  summary: string
  dominantHue: string
  moods: string[]
  era: string
  colorCount: number
  colors: PaletteColor[]
}

export interface Color {
  id: number
  slug: string
  name: string
  nameJa: string
  meaning: string
  hex: string
  rgbR: number
  rgbG: number
  rgbB: number
  hue: string
  paletteCount: number
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface FilterMeta {
  hues: string[]
  eras: string[]
  moods: string[]
}

export interface PaletteFilters {
  hue?: string
  era?: string
  type?: number
  mood?: string
  q?: string
  page?: number
}
