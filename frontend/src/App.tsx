import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Analytics } from '@vercel/analytics/react'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { AboutPage } from './pages/AboutPage'
import { BrowsePage } from './pages/BrowsePage'
import { DetailPage } from './pages/DetailPage'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1 } },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<BrowsePage />} />
            <Route path="/palettes/:slug" element={<DetailPage />} />
            <Route path="/about" element={<AboutPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
      {/* Vercel Web Analytics - auto-tracks client-side route changes via the History
          API, so React Router navigations count as page views with no extra wiring.
          No-op outside Vercel (logs to console in dev instead of sending). */}
      <Analytics />
    </QueryClientProvider>
  )
}
