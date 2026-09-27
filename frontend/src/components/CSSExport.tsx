import { useState } from 'react'
import type { Palette } from '../types'
import { toCssVarName } from '../utils/colorUtils'

interface Props {
  palette: Palette
}

export function CSSExport({ palette }: Props) {
  const [copied, setCopied] = useState(false)

  const lines = palette.colors.map(c => `  ${toCssVarName(c.name)}: ${c.hex};`)
  const output = `:root {\n${lines.join('\n')}\n}`

  const copy = async () => {
    await navigator.clipboard.writeText(output)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="space-y-3">
      <div className="relative">
        <pre className="bg-stone-900 text-stone-100 text-xs font-mono p-5 rounded-xl overflow-x-auto leading-relaxed">
          {output}
        </pre>
        <button
          onClick={copy}
          className="absolute top-3 right-3 text-xs px-3 py-1 rounded-lg bg-stone-700 text-stone-200 hover:bg-stone-600 transition-colors"
        >
          {copied ? '✓ Copied' : 'Copy'}
        </button>
      </div>
      <div className="flex flex-wrap gap-2">
        {palette.colors.map(c => (
          <div key={c.hex} className="flex items-center gap-2 bg-white border border-stone-200 rounded-lg px-3 py-2">
            <div className="w-4 h-4 rounded-sm flex-shrink-0" style={{ backgroundColor: c.hex }} />
            <code className="text-xs text-stone-700">{toCssVarName(c.name)}</code>
          </div>
        ))}
      </div>
      <p className="text-xs text-stone-600">Paste into your stylesheet to use as CSS custom properties.</p>
    </div>
  )
}
