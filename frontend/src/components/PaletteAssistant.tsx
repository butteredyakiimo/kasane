import { useState } from 'react'
import ReactMarkdown from 'react-markdown'
import { useAssistantChat } from '../hooks/usePalettes'
import { PaletteCard } from './PaletteCard'
import type { AssistantTurn } from '../types'

// react-markdown parses markdown into React elements directly - it never touches
// innerHTML, so raw HTML in the model's output (e.g. a stray <script> tag) renders as
// inert text rather than executing. Do NOT add the rehype-raw plugin - that opts back
// into raw HTML passthrough and reopens the XSS risk this is here to avoid.
const markdownComponents = {
  p: ({ ...props }) => <p className="mb-2 last:mb-0" {...props} />,
  strong: ({ ...props }) => <strong className="font-semibold" {...props} />,
  ul: ({ ...props }) => <ul className="list-disc pl-5 space-y-0.5 mb-2 last:mb-0" {...props} />,
  ol: ({ ...props }) => <ol className="list-decimal pl-5 space-y-0.5 mb-2 last:mb-0" {...props} />,
  a: ({ ...props }) => (
    <a className="underline hover:no-underline" target="_blank" rel="noopener noreferrer" {...props} />
  ),
  code: ({ ...props }) => <code className="bg-stone-200 rounded px-1 text-xs" {...props} />,
}

// Mirrors PaletteAssistantService's limits on the backend - kept in sync manually since
// there's no shared schema between the two. The backend enforces these regardless; this
// is purely about giving the user a clear reason instead of a raw failed request.
const MAX_MESSAGE_LENGTH = 1000
const MAX_HISTORY_TURNS = 20

interface Props {
  turns: AssistantTurn[]
  onTurnsChange: React.Dispatch<React.SetStateAction<AssistantTurn[]>>
}

export function PaletteAssistant({ turns, onTurnsChange }: Props) {
  const [input, setInput] = useState('')
  const mutation = useAssistantChat()

  const trimmed = input.trim()
  const historyFull = turns.length >= MAX_HISTORY_TURNS
  const canSend = !!trimmed && trimmed.length <= MAX_MESSAGE_LENGTH && !historyFull && !mutation.isPending

  const send = () => {
    if (!canSend) return

    // Assistant turns without a signature never came from the server (e.g. a local
    // fetch-failure fallback below) - exclude them, or the next request would 400.
    const history = turns
      .map(t => t.message)
      .filter(m => m.role === 'user' || !!m.signature)
    onTurnsChange(t => [...t, { message: { role: 'user', content: trimmed } }])
    setInput('')

    mutation.mutate(
      { message: trimmed, history },
      {
        onSuccess: (res) => {
          onTurnsChange(t => [...t, {
            message: { role: 'assistant', content: res.reply, signature: res.signature },
            palettes: res.palettes,
          }])
        },
        onError: () => {
          onTurnsChange(t => [...t, {
            message: { role: 'assistant', content: 'Something went wrong reaching the assistant - please try again.' },
          }])
        },
      }
    )
  }

  return (
    <div className="px-6 py-6 max-w-2xl mx-auto">
      <div className="space-y-6 mb-6">
        {turns.length === 0 && (
          <p className="text-sm text-stone-600 text-center py-8">
            Describe a mood, season, or scene - e.g. "something for a rainy autumn evening" -
            and I'll find matching palettes from the collection.
          </p>
        )}

        {turns.map((t, i) => (
          <div key={i} className={t.message.role === 'user' ? 'text-right' : ''}>
            <div
              className={`inline-block text-left text-sm rounded-2xl px-4 py-2.5 max-w-[85%] ${
                t.message.role === 'user'
                  ? 'bg-stone-800 text-white whitespace-pre-wrap'
                  : 'bg-stone-100 text-stone-800'
              }`}
            >
              {t.message.role === 'assistant' ? (
                <ReactMarkdown components={markdownComponents}>{t.message.content}</ReactMarkdown>
              ) : (
                t.message.content
              )}
            </div>

            {t.palettes && t.palettes.length > 0 && (
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 mt-4">
                {t.palettes.map(p => (
                  <PaletteCard key={p.slug} palette={p} />
                ))}
              </div>
            )}
          </div>
        ))}

        {mutation.isPending && (
          <p className="text-sm text-stone-500 italic">Thinking…</p>
        )}
      </div>

      {historyFull ? (
        <p className="text-sm text-stone-600 text-center py-3 border-t border-stone-100">
          This conversation has gotten long.{' '}
          <button
            onClick={() => onTurnsChange([])}
            className="underline hover:text-stone-800 transition-colors"
          >
            Start a new one
          </button>{' '}
          to keep chatting.
        </p>
      ) : (
        <>
          <div className="flex gap-2 sticky bottom-6">
            <input
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && send()}
              placeholder="Ask for a palette…"
              disabled={mutation.isPending}
              maxLength={MAX_MESSAGE_LENGTH}
              className="flex-1 text-sm border border-stone-200 rounded-lg px-4 py-2.5 bg-white shadow-sm focus:outline-none focus:border-stone-400 disabled:opacity-50"
            />
            <button
              onClick={send}
              disabled={!canSend}
              className="px-4 py-2.5 text-sm font-medium bg-stone-800 text-white rounded-lg disabled:opacity-30 hover:bg-stone-700 transition-colors"
            >
              Send
            </button>
          </div>
          {input.length > MAX_MESSAGE_LENGTH * 0.8 && (
            <p className={`text-xs mt-1.5 text-right ${
              input.length >= MAX_MESSAGE_LENGTH ? 'text-red-600' : 'text-stone-500'
            }`}>
              {input.length}/{MAX_MESSAGE_LENGTH}
            </p>
          )}
        </>
      )}
    </div>
  )
}
