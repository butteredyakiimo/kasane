import { isDark } from '../utils/colorUtils'

interface Props {
  hex: string
  name?: string
  nameJa?: string
  size?: 'sm' | 'md' | 'lg'
  onClick?: () => void
}

const heights = { sm: 'h-16', md: 'h-28', lg: 'h-44' }

export function ColorSwatch({ hex, name, nameJa, size = 'md', onClick }: Props) {
  const dark = isDark(hex)
  const textColor = dark ? 'text-white/75' : 'text-black/55'

  return (
    <div
      className={`${heights[size]} rounded-lg relative ${onClick ? 'cursor-pointer' : ''}`}
      style={{ backgroundColor: hex }}
      onClick={onClick}
      title={onClick ? `Click to copy ${hex}` : undefined}
    >
      {(name || nameJa) && (
        <div className={`absolute bottom-2.5 left-3 right-3 ${textColor}`}>
          {nameJa && <p className="text-xs font-japanese leading-snug">{nameJa}</p>}
          {name && <p className="text-xs font-medium leading-snug truncate">{name}</p>}
          <p className="text-xs opacity-60 font-mono mt-0.5">{hex}</p>
        </div>
      )}
    </div>
  )
}
