import { Link, Outlet } from 'react-router-dom'

export function Layout() {
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
        <Outlet />
      </main>
    </div>
  )
}
