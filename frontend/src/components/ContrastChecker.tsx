import type { PaletteColor } from '../types'
import { contrastRatio, wcagLevel } from '../utils/colorUtils'

interface Props {
  colors: PaletteColor[]
}

export function ContrastChecker({ colors }: Props) {
  const pairs: Array<[PaletteColor, PaletteColor]> = []
  for (let i = 0; i < colors.length; i++) {
    for (let j = i + 1; j < colors.length; j++) {
      pairs.push([colors[i], colors[j]])
    }
  }

  return (
    <div className="space-y-4">
      {pairs.map(([a, b]) => {
        const ratio = contrastRatio(a.hex, b.hex)
        const lvl = wcagLevel(ratio)
        return (
          <div key={`${a.hex}-${b.hex}`} className="flex items-center gap-4">
            <div className="flex rounded-lg overflow-hidden flex-shrink-0 shadow-sm">
              <div className="w-12 h-12" style={{ backgroundColor: a.hex }} />
              <div className="w-12 h-12" style={{ backgroundColor: b.hex }} />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs text-stone-700 truncate">{a.name} / {b.name}</p>
              <p className="text-sm font-mono font-semibold text-stone-800 mt-0.5">
                {ratio.toFixed(2)}:1
              </p>
            </div>
            <div className="flex gap-1.5 flex-shrink-0">
              <Badge pass={lvl.aaLarge} label="AA Lg" />
              <Badge pass={lvl.aa} label="AA" />
              <Badge pass={lvl.aaa} label="AAA" />
            </div>
          </div>
        )
      })}
      <p className="text-xs text-stone-600 border-t border-stone-100 pt-3">
        WCAG 2.1 — AA ≥ 4.5:1 normal · ≥ 3:1 large text · AAA ≥ 7:1
      </p>
    </div>
  )
}

function Badge({ pass, label }: { pass: boolean; label: string }) {
  return (
    <span className={`text-xs px-2 py-0.5 rounded font-medium ${
      pass ? 'bg-emerald-100 text-emerald-700' : 'bg-stone-100 text-stone-600'
    }`}>
      {label}
    </span>
  )
}
