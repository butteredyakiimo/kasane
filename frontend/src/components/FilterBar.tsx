import { useState } from 'react'
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
  const activeChipCount = [filters.type, filters.hue, filters.era].filter(Boolean).length

  // Mobile only: the ~18 chips wrap to 5-6 rows inside a sticky header, eating most of
  // a phone screen, so below `sm` they're collapsed behind a toggle. On `sm`+ the
  // wrappers below use `sm:contents`, which removes them from layout entirely, so the
  // desktop bar renders as the same single flex-wrap row as before.
  const [open, setOpen] = useState(false)

  return (
    <div className="bg-[#FAFAF8]/90 backdrop-blur-sm border-b border-stone-200 px-4 sm:px-6 py-3">
      <div className="flex flex-wrap gap-2 items-center">
        <div className="flex gap-2 w-full sm:contents">
          <input
            type="search"
            placeholder="Search palettes…"
            value={filters.q ?? ''}
            onChange={e => set('q', e.target.value || undefined)}
            className="text-sm border border-stone-200 rounded-full px-4 py-1 outline-none focus:border-stone-400 bg-white flex-1 min-w-0 sm:flex-none sm:w-44 transition-colors"
          />
          <button
            className={`sm:hidden ${chip(open || activeChipCount > 0)}`}
            onClick={() => setOpen(o => !o)}
            aria-expanded={open}
            aria-controls="filter-chips"
          >
            Filters{activeChipCount > 0 && ` · ${activeChipCount}`} {open ? '▴' : '▾'}
          </button>
        </div>

        <div id="filter-chips" className={`${open ? 'flex' : 'hidden'} flex-col gap-2 w-full pt-1 sm:contents`}>
          <Divider />

          <div className="flex flex-wrap gap-2 sm:contents">
            {([2, 3, 4] as const).map(t => (
              <button
                key={t}
                className={chip(filters.type === t)}
                onClick={() => set('type', filters.type === t ? undefined : t)}
              >
                {TYPE_LABEL[t]}
              </button>
            ))}
          </div>

          <Divider />

          <div className="flex flex-wrap gap-2 sm:contents">
            {meta.hues.map(h => (
              <button
                key={h}
                className={chip(filters.hue === h)}
                onClick={() => set('hue', filters.hue === h ? undefined : h)}
              >
                {HUE_EMOJI[h] ? `${HUE_EMOJI[h]} ${h}` : h}
              </button>
            ))}
          </div>

          <Divider />

          <div className="flex flex-wrap gap-2 sm:contents">
            {meta.eras.filter(Boolean).map(e => (
              <button
                key={e}
                className={chip(filters.era === e)}
                onClick={() => set('era', filters.era === e ? undefined : e)}
              >
                {e}
              </button>
            ))}
          </div>

          {hasActiveFilter && (
            <>
              <Divider />
              <button
                className="self-start text-xs text-stone-600 hover:text-stone-700 transition-colors underline-offset-2 hover:underline"
                onClick={() => onChange({})}
              >
                Clear all
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

function Divider() {
  // Vertical rule between groups on desktop; on mobile each group is already its own row.
  return <div className="hidden sm:block w-px h-5 bg-stone-200 mx-0.5 flex-shrink-0" />
}
