import { useEffect, useRef } from 'react'
import { useOutletContext } from 'react-router-dom'
import type { LayoutContext } from '../components/Layout'
import { FilterBar } from '../components/FilterBar'
import { PaletteAssistant } from '../components/PaletteAssistant'
import { PaletteCard } from '../components/PaletteCard'
import { useFilterMeta, usePalettes } from '../hooks/usePalettes'

export function BrowsePage() {
  const {
    mode, setMode, assistantTurns, setAssistantTurns, filters, setFilters, browseScrollY, setBrowseScrollY,
  } = useOutletContext<LayoutContext>()
  const { data, isLoading, isError, refetch, isFetching } = usePalettes(filters)
  const { data: meta } = useFilterMeta()

  // Captures scroll position the instant this page is navigated away from (e.g. into
  // DetailPage) so it can be restored below, rather than always landing back at the top.
  useEffect(() => {
    return () => {
      if (mode === 'browse') setBrowseScrollY(window.scrollY)
    }
  }, [mode, setBrowseScrollY])

  // Restores it exactly once per mount - i.e. only right after navigating back, not on
  // every later tab switch within the same still-mounted session.
  const hasRestoredScroll = useRef(false)
  useEffect(() => {
    if (mode === 'browse' && data && !hasRestoredScroll.current) {
      hasRestoredScroll.current = true
      if (browseScrollY > 0) window.scrollTo({ top: browseScrollY })
    }
  }, [mode, data, browseScrollY])

  const goToPage = (page: number) => {
    setFilters(f => ({ ...f, page }))
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <>
      <div className="sticky top-0 z-10 bg-[#FAFAF8]">
        <div className="flex gap-2 px-6 py-4 border-b border-stone-200">
          {(['browse', 'ask'] as const).map(m => (
            <button
              key={m}
              onClick={() => setMode(m)}
              className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors ${
                mode === m ? 'bg-stone-800 text-white' : 'bg-white border border-stone-200 text-stone-700 hover:border-stone-400'
              }`}
            >
              {m === 'browse' ? 'Browse' : 'Ask'}
            </button>
          ))}
        </div>

        {mode === 'browse' && meta && <FilterBar filters={filters} meta={meta} onChange={setFilters} />}
      </div>

      {mode === 'ask' ? (
        <PaletteAssistant turns={assistantTurns} onTurnsChange={setAssistantTurns} />
      ) : (
        <div className="px-6 py-6">
          {isLoading && (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
              {Array.from({ length: 48 }).map((_, i) => (
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
              <p className="text-base font-medium text-stone-700 mb-1">Palettes couldn't be loaded</p>
              <p className="text-sm">Something went wrong on our end. Please try again in a moment.</p>
              <button
                onClick={() => refetch()}
                disabled={isFetching}
                className="mt-5 px-4 py-2 text-sm font-medium rounded-lg bg-white border border-stone-200 text-stone-700 hover:border-stone-400 transition-colors disabled:opacity-50"
              >
                {isFetching ? 'Retrying…' : 'Try again'}
              </button>
              {/* Dev-only hint - stripped from production builds by Vite. */}
              {import.meta.env.DEV && (
                <p className="text-xs text-stone-500 mt-6">
                  Dev: is the backend running? <code>cd backend && ./gradlew bootRun</code>
                </p>
              )}
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
                    onClick={() => goToPage((filters.page ?? 0) - 1)}
                    className="px-4 py-2 text-sm border border-stone-200 rounded-lg disabled:opacity-30 hover:border-stone-400 transition-colors bg-white"
                  >
                    ← Previous
                  </button>
                  <span className="text-sm text-stone-700">
                    {(filters.page ?? 0) + 1} / {data.totalPages}
                  </span>
                  <button
                    disabled={data.last}
                    onClick={() => goToPage((filters.page ?? 0) + 1)}
                    className="px-4 py-2 text-sm border border-stone-200 rounded-lg disabled:opacity-30 hover:border-stone-400 transition-colors bg-white"
                  >
                    Next →
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </>
  )
}
