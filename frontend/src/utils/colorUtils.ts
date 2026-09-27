import type { Palette } from '../types'

const ABBREVIATED_TITLE = /\s\+\d+$/

/**
 * Some source titles abbreviate 3rd/4th colors as "+1"/"+2" instead of naming them
 * (e.g. "Red Orange & Pale Lemon Yellow +2"). Their prefix always matches color_1 &
 * color_2 verbatim, so reconstructing the full name from `palette.colors` is a
 * faithful un-abbreviation. Curated titles (e.g. "Cherry Blossom & Young Bamboo")
 * don't follow this pattern and use names that don't match the raw color fields at
 * all - those are left untouched.
 */
export function fullTitle(palette: Palette): string {
  if (ABBREVIATED_TITLE.test(palette.title)) {
    return palette.colors.map(c => c.name).join(' & ')
  }
  return palette.title
}

function toLinear(val: number): number {
  const s = val / 255
  return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4)
}

export function relativeLuminance(hex: string): number {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return 0.2126 * toLinear(r) + 0.7152 * toLinear(g) + 0.0722 * toLinear(b)
}

export function contrastRatio(hex1: string, hex2: string): number {
  const l1 = relativeLuminance(hex1)
  const l2 = relativeLuminance(hex2)
  const lighter = Math.max(l1, l2)
  const darker = Math.min(l1, l2)
  return (lighter + 0.05) / (darker + 0.05)
}

export function wcagLevel(ratio: number) {
  return {
    aa: ratio >= 4.5,
    aaLarge: ratio >= 3,
    aaa: ratio >= 7,
    aaaLarge: ratio >= 4.5,
  }
}

export function isDark(hex: string): boolean {
  return relativeLuminance(hex) < 0.179
}

export function toCssVarName(name: string): string {
  return '--color-' + name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')
}
