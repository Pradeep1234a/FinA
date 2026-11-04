// Game Provider Component
// Context provider for game stores (Zustand doesn't need providers, but we can use this for initialization)

import { useEffect } from 'react'
import { useGameStore } from '@stores/gameStore'
import { usePlayerStore } from '@stores/playerStore'
import { useWorldStore } from '@stores/worldStore'
import { useNarrativeStore } from '@stores/narrativeStore'

interface GameProviderProps {
  children: React.ReactNode
}

export function GameProvider({ children }: GameProviderProps) {
  const initialize = useGameStore((state) => state.initialize)

  useEffect(() => {
    initialize()
  }, [initialize])

  return <>{children}</>
}

// Hook to access all stores at once (convenience)
export function useGameStores() {
  const game = useGameStore()
  const player = usePlayerStore()
  const world = useWorldStore()
  const narrative = useNarrativeStore()

  return { game, player, world, narrative }
}