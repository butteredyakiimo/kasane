import { useState } from 'react'
import { FilterBar } from '../components/FilterBar'
import { PaletteCard } from '../components/PaletteCard'
import { useFilterMeta, usePalettes } from '../hooks/usePalettes'
import type { PaletteFilters } from '../types'

export function BrowsePage() {
  const [filters, setFilters] = useState<PaletteFilters>({})
  const { data, isLoading, isError } = usePalettes(filters)
  const { data: meta } = useFilterMeta()

  return (
    <>
      {meta && <FilterBar filters={filters} meta={meta} onChange={setFilters} />}

      <div className="px-6 py-6">
        {isLoading && (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
            {Array.from({ length: 24 }).map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="h-28 rounded-xl bg-stone-200 mb-3" />
                <div className="h-3 bg-stone-200 rounded-full w-3/4 mb-1.5" />
                <div className="h-3 bg-stone-100 rounded-full w-1/2" />
              </div>
            ))}
          </div>
        )}

        {isError && (
          <div className="text-center py-24 text-stone-600">
            <p className="text-4xl mb-4">⚠️</p>
            <p className="text-base font-medium text-stone-700 mb-1">Could not reach the API</p>
            <p className="text-sm">Make sure the backend is running on port 8080.</p>
            <code className="text-xs bg-stone-100 px-3 py-1.5 rounded mt-3 inline-block">
              cd kasane/backend && ./gradlew bootRun
            </code>
          </div>
        )}

        {data && (
          <>
            <p className="text-xs text-stone-600 mb-5">
              {data.totalElements.toLocaleString()} palette{data.totalElements !== 1 ? 's' : ''}
            </p>

            {data.content.length === 0 ? (
              <div className="text-center py-24 text-stone-600">
                <p className="text-base">No palettes match these filters.</p>
              </div>
            ) : (
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
                {data.content.map(p => (
                  <PaletteCard key={p.slug} palette={p} />
                ))}
              </div>
            )}

            {data.totalPages > 1 && (
              <div className="flex justify-center items-center gap-3 mt-12">
                <button
                  disabled={!filters.page}
                  onClick={() => setFilters(f => ({ ...f, page: (f.page ?? 0) - 1 }))}
                  className="px-4 py-2 text-sm border border-stone-200 rounded-lg disabled:opacity-30 hover:border-stone-400 transition-colors bg-white"
                >
                  ← Previous
                </button>
                <span className="text-sm text-stone-700">
                  {(filters.page ?? 0) + 1} / {data.totalPages}
                </span>
                <button
                  disabled={data.last}
                  onClick={() => setFilters(f => ({ ...f, page: (f.page ?? 0) + 1 }))}
                  className="px-4 py-2 text-sm border border-stone-200 rounded-lg disabled:opacity-30 hover:border-stone-400 transition-colors bg-white"
                >
                  Next →
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </>
  )
}
