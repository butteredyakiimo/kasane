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

export interface AssistantMessage {
  role: 'user' | 'assistant'
  content: string
  /** Required to resend an "assistant" turn as history - proves it genuinely came from
   * the server. Absent on locally-synthesized messages (e.g. a fetch-failure fallback),
   * which must be excluded when rebuilding the history to send. */
  signature?: string
}

export interface AssistantResponse {
  reply: string
  palettes: Palette[]
  signature: string
}

export interface AssistantTurn {
  message: AssistantMessage
  palettes?: Palette[]
}
