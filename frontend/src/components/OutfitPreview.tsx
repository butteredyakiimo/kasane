import type { PaletteColor } from '../types'

interface Props {
  colors: PaletteColor[]
}

const LABELS = ['Top', 'Bottom', 'Outer', 'Accessory']

export function OutfitPreview({ colors }: Props) {
  const [c1, c2, c3, c4] = colors

  return (
    <div className="flex items-center justify-center gap-10">
      <svg
        viewBox="0 0 180 300"
        width="180"
        height="300"
        xmlns="http://www.w3.org/2000/svg"
        className="flex-shrink-0"
      >
        {/* Outer jacket (color 3) */}
        {c3 && (
          <path
            d="M28,90 L8,110 L8,185 L48,185 L48,135 L132,135 L132,185 L172,185 L172,110 L152,90 L118,102 L90,97 L62,102 Z"
            fill={c3.hex}
          />
        )}

        {/* Top / shirt (color 1) */}
        <path
          d="M48,88 L18,108 L18,168 L58,168 L58,135 L122,135 L122,168 L162,168 L162,108 L132,88 L103,100 L90,95 L77,100 Z"
          fill={c1.hex}
        />
        {/* Collar V */}
        <path d="M77,88 L90,106 L103,88" fill={c1.hex} stroke="rgba(0,0,0,0.08)" strokeWidth="1" />

        {/* Trousers / skirt (color 2) */}
        <path
          d="M52,166 L52,278 L84,278 L90,235 L96,278 L128,278 L128,166 Z"
          fill={c2.hex}
        />

        {/* Bag / accessory (color 4) */}
        {c4 && (
          <g>
            <rect x="138" y="210" width="34" height="28" rx="4" fill={c4.hex} />
            <path d="M146,210 Q156,198 166,210" stroke={c4.hex} strokeWidth="3" fill="none" strokeLinecap="round" />
          </g>
        )}
      </svg>

      <div className="space-y-3">
        {colors.map((c, i) => (
          <div key={i} className="flex items-center gap-3">
            <div
              className="w-8 h-8 rounded-lg flex-shrink-0 shadow-sm"
              style={{ backgroundColor: c.hex }}
            />
            <div>
              <p className="text-xs font-medium text-stone-700 leading-tight">{LABELS[i]}</p>
              <p className="text-xs text-stone-400 leading-tight">{c.name}</p>
              {c.nameJa && (
                <p className="text-xs text-stone-300 font-japanese leading-tight">{c.nameJa}</p>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
