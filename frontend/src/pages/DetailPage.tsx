import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ColorSwatch } from '../components/ColorSwatch'
import { ContrastChecker } from '../components/ContrastChecker'
import { CSSExport } from '../components/CSSExport'
import { OutfitPreview } from '../components/OutfitPreview'
import { usePalette } from '../hooks/usePalettes'

type Tab = 'outfit' | 'contrast' | 'css'

const TABS: { key: Tab; label: string }[] = [
  { key: 'outfit', label: 'Outfit Preview' },
  { key: 'contrast', label: 'Contrast' },
  { key: 'css', label: 'CSS Export' },
]

export function DetailPage() {
  const { slug } = useParams<{ slug: string }>()
  const { data: palette, isLoading, isError } = usePalette(slug!)
  const [tab, setTab] = useState<Tab>('outfit')
  const [copied, setCopied] = useState<string | null>(null)

  if (isLoading) {
    return (
      <div className="max-w-3xl mx-auto px-6 py-12 animate-pulse">
        <div className="h-4 bg-stone-200 rounded w-24 mb-8" />
        <div className="h-8 bg-stone-200 rounded w-64 mb-2" />
        <div className="h-4 bg-stone-100 rounded w-32 mb-8" />
        <div className="flex gap-3 h-44">
          {[1, 2, 3].map(i => <div key={i} className="flex-1 rounded-lg bg-stone-200" />)}
        </div>
      </div>
    )
  }

  if (isError || !palette) {
    return (
      <div className="max-w-3xl mx-auto px-6 py-20 text-center text-stone-600">
        <p className="text-lg mb-2">Palette not found</p>
        <Link to="/" className="text-sm underline hover:text-stone-700 transition-colors">
          Back to browse
        </Link>
      </div>
    )
  }

  const copyHex = async (hex: string) => {
    await navigator.clipboard.writeText(hex)
    setCopied(hex)
    setTimeout(() => setCopied(null), 1500)
  }

  return (
    <div className="max-w-3xl mx-auto px-6 py-8">
      <Link
        to="/"
        className="text-sm text-stone-600 hover:text-stone-700 transition-colors mb-8 inline-flex items-center gap-1"
      >
        ← All palettes
      </Link>

      <div className="mt-6 mb-8">
        <h1 className="text-3xl font-semibold text-stone-900 leading-tight">{palette.title}</h1>
        <p className="text-lg text-stone-600 font-japanese mt-1">{palette.titleJa}</p>

        <div className="flex gap-2 flex-wrap mt-4">
          {palette.era && (
            <span className="text-xs px-3 py-1 rounded-full border border-stone-200 text-stone-700 capitalize bg-white">
              {palette.era}
            </span>
          )}
          {palette.moods.map(m => (
            <span
              key={m}
              className="text-xs px-3 py-1 rounded-full border border-stone-200 text-stone-700 capitalize bg-white"
            >
              {m}
            </span>
          ))}
        </div>
      </div>

      {/* Colour swatches */}
      <div
        className="grid gap-3 mb-4"
        style={{ gridTemplateColumns: `repeat(${palette.colorCount}, minmax(0, 1fr))` }}
      >
        {palette.colors.map(c => (
          <div key={c.position}>
            <ColorSwatch
              hex={c.hex}
              name={c.name}
              nameJa={c.nameJa}
              size="lg"
              onClick={() => copyHex(c.hex)}
            />
            <button
              onClick={() => copyHex(c.hex)}
              className="w-full text-xs font-mono text-stone-600 hover:text-stone-700 transition-colors mt-1.5 text-center"
            >
              {copied === c.hex ? '✓ Copied' : c.hex}
            </button>
          </div>
        ))}
      </div>

      {palette.summary && (
        <blockquote className="text-sm text-stone-700 leading-relaxed my-8 border-l-2 border-stone-200 pl-4 italic">
          {palette.summary}
        </blockquote>
      )}

      {/* Feature tabs */}
      <div className="border border-stone-200 rounded-2xl overflow-hidden mt-8 bg-white">
        <div className="flex border-b border-stone-100">
          {TABS.map(t => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`flex-1 py-3.5 text-sm font-medium transition-colors ${
                tab === t.key
                  ? 'text-stone-900 bg-white border-b-2 border-stone-800'
                  : 'text-stone-600 bg-stone-50 hover:text-stone-700'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>
        <div className="p-6">
          {tab === 'outfit' && <OutfitPreview colors={palette.colors} />}
          {tab === 'contrast' && <ContrastChecker colors={palette.colors} />}
          {tab === 'css' && <CSSExport palette={palette} />}
        </div>
      </div>
    </div>
  )
}
