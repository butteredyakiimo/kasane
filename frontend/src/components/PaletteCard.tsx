import { useNavigate } from 'react-router-dom'
import type { Palette } from '../types'

interface Props {
  palette: Palette
}

export function PaletteCard({ palette }: Props) {
  const navigate = useNavigate()

  return (
    <article
      className="cursor-pointer group"
      onClick={() => navigate(`/palettes/${palette.slug}`)}
    >
      <div className="rounded-xl overflow-hidden flex h-28 mb-3 shadow-sm group-hover:shadow-md transition-all duration-200">
        {palette.colors.map((color) => (
          <div key={color.position} className="flex-1" style={{ backgroundColor: color.hex }} />
        ))}
      </div>
      <div>
        <p className="text-sm font-medium text-stone-800 leading-tight truncate group-hover:text-stone-600 transition-colors">
          {palette.title}
        </p>
        <p className="text-xs text-stone-600 font-japanese mt-0.5">{palette.titleJa}</p>
        <div className="flex items-center gap-1.5 mt-1.5 flex-wrap">
          {palette.era && (
            <span className="text-xs text-stone-600 capitalize">{palette.era}</span>
          )}
          {palette.era && <span className="text-stone-500 text-xs">·</span>}
          <span className="text-xs text-stone-600">{palette.colorCount} colours</span>
          {palette.moods.slice(0, 2).map(m => (
            <span key={m} className="text-xs text-stone-500">· {m}</span>
          ))}
        </div>
      </div>
    </article>
  )
}
