import { useQuery } from '@tanstack/react-query'
import { api } from '../services/api'
import type { PaletteFilters } from '../types'

export function usePalettes(filters: PaletteFilters) {
  return useQuery({
    queryKey: ['palettes', filters],
    queryFn: () => api.getPalettes(filters),
    staleTime: 5 * 60 * 1000,
  })
}

export function usePalette(slug: string) {
  return useQuery({
    queryKey: ['palette', slug],
    queryFn: () => api.getPalette(slug),
    staleTime: 10 * 60 * 1000,
    enabled: !!slug,
  })
}

export function useFilterMeta() {
  return useQuery({
    queryKey: ['filter-meta'],
    queryFn: api.getFilterMeta,
    staleTime: Infinity,
  })
}
