import type { FilterMeta, PaletteFilters } from '../types'

interface Props {
  filters: PaletteFilters
  meta: FilterMeta
  onChange: (f: PaletteFilters) => void
}

const HUE_EMOJI: Record<string, string> = {
  red: '🔴', pink: '🌸', orange: '🟠', yellow: '🟡',
  green: '🟢', blue: '🔵', purple: '🟣', brown: '🤎', neutral: '⚪',
}

const TYPE_LABEL: Record<number, string> = { 2: 'Duet', 3: 'Trio', 4: 'Quad' }

export function FilterBar({ filters, meta, onChange }: Props) {
  const set = (key: keyof PaletteFilters, val: string | number | undefined) =>
    onChange({ ...filters, [key]: val, page: 0 })

  const chip = (active: boolean) =>
    `px-3 py-1 rounded-full text-xs font-medium border transition-all cursor-pointer select-none whitespace-nowrap ${
      active
        ? 'bg-stone-800 text-white border-stone-800'
        : 'bg-white text-stone-700 border-stone-200 hover:border-stone-400'
    }`

  const hasActiveFilter = !!(filters.hue || filters.era || filters.type || filters.mood || filters.q)

  return (
    <div className="bg-[#FAFAF8]/90 backdrop-blur-sm border-b border-stone-200 px-6 py-3">
      <div className="flex flex-wrap gap-2 items-center">
        <input
          type="search"
          placeholder="Search palettes…"
          value={filters.q ?? ''}
          onChange={e => set('q', e.target.value || undefined)}
          className="text-sm border border-stone-200 rounded-full px-4 py-1 outline-none focus:border-stone-400 bg-white w-44 transition-colors"
        />

        <Divider />

        {([2, 3, 4] as const).map(t => (
          <button
            key={t}
            className={chip(filters.type === t)}
            onClick={() => set('type', filters.type === t ? undefined : t)}
          >
            {TYPE_LABEL[t]}
          </button>
        ))}

        <Divider />

        {meta.hues.map(h => (
          <button
            key={h}
            className={chip(filters.hue === h)}
            onClick={() => set('hue', filters.hue === h ? undefined : h)}
          >
            {HUE_EMOJI[h] ? `${HUE_EMOJI[h]} ${h}` : h}
          </button>
        ))}

        <Divider />

        {meta.eras.filter(Boolean).map(e => (
          <button
            key={e}
            className={chip(filters.era === e)}
            onClick={() => set('era', filters.era === e ? undefined : e)}
          >
            {e}
          </button>
        ))}

        {hasActiveFilter && (
          <>
            <Divider />
            <button
              className="text-xs text-stone-600 hover:text-stone-700 transition-colors underline-offset-2 hover:underline"
              onClick={() => onChange({})}
            >
              Clear all
            </button>
          </>
        )}
      </div>
    </div>
  )
}

function Divider() {
  return <div className="w-px h-5 bg-stone-200 mx-0.5 flex-shrink-0" />
}
