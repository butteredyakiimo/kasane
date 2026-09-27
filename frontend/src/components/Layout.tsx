import { useState } from 'react'
import { Link, Outlet } from 'react-router-dom'
import type { AssistantTurn, PaletteFilters } from '../types'

export type BrowseMode = 'browse' | 'ask'

export interface LayoutContext {
  mode: BrowseMode
  setMode: (mode: BrowseMode) => void
  assistantTurns: AssistantTurn[]
  setAssistantTurns: React.Dispatch<React.SetStateAction<AssistantTurn[]>>
  filters: PaletteFilters
  setFilters: React.Dispatch<React.SetStateAction<PaletteFilters>>
  browseScrollY: number
  setBrowseScrollY: (y: number) => void
}

export function Layout() {
  // Owned here, not in BrowsePage/PaletteAssistant, so it survives navigating to
  // DetailPage and back - Layout is the one component that stays mounted across routes.
  const [mode, setMode] = useState<BrowseMode>('browse')
  const [assistantTurns, setAssistantTurns] = useState<AssistantTurn[]>([])
  const [filters, setFilters] = useState<PaletteFilters>({})
  const [browseScrollY, setBrowseScrollY] = useState(0)

  const context: LayoutContext = {
    mode, setMode, assistantTurns, setAssistantTurns, filters, setFilters, browseScrollY, setBrowseScrollY,
  }

  return (
    <div className="min-h-screen bg-[#FAFAF8] text-stone-900">
      <header className="border-b border-stone-200 px-6 py-4 flex items-center gap-6">
        <Link to="/" className="flex items-baseline gap-2 hover:opacity-70 transition-opacity">
          <span className="text-xl font-semibold tracking-tight">Kasane</span>
          <span className="text-sm text-stone-600 font-japanese">かさね</span>
        </Link>
        <nav className="flex gap-4 text-sm text-stone-600">
          <Link to="/" className="hover:text-stone-700 transition-colors">Palettes</Link>
        </nav>
      </header>
      <main>
        <Outlet context={context} />
      </main>
    </div>
  )
}
