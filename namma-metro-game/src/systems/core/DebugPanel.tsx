// Debug Panel
// Development-only overlay for monitoring game state, performance, and debugging

import { useEffect, useRef, useState } from 'react'
import { useGameStore } from '@stores/gameStore'
import { usePlayerStore } from '@stores/playerStore'
import { useWorldStore } from '@stores/worldStore'
import { useNarrativeStore } from '@stores/narrativeStore'
import { getSceneManager } from '@systems/core/SceneManager'
import { getAssetLoader } from '@systems/core/AssetLoader'
import { getInputManager } from '@systems/core/InputManager'
import { gameLoop } from '@systems/core/GameLoop'
import type { Vector3 } from '@types'

interface DebugPanelProps {
  isOpen?: boolean
  onToggle?: () => void
}

export function DebugPanel({ isOpen = false, onToggle }: DebugPanelProps) {
  const [fps, setFps] = useState(0)
  const [stats, setStats] = useState<Record<string, unknown>>({})
  const [playerPos, setPlayerPos] = useState<Vector3>({ x: 0, y: 0, z: 0 })
  const [cameraMode, setCameraMode] = useState('')
  const [movementState, setMovementState] = useState('')
  const [timeOfDay, setTimeOfDay] = useState(0)
  const [weather, setWeather] = useState('')
  const [loadedChunks, setLoadedChunks] = useState(0)
  const [activeQuests, setActiveQuests] = useState(0)
  const [dialogueActive, setDialogueActive] = useState(false)

  const frameRef = useRef(0)

  useEffect(() => {
    if (!isOpen) return

    const updateLoop = () => {
      // FPS
      setFps(gameLoop.getFPS())

      // Scene stats
      const sceneManager = getSceneManager()
      if (sceneManager) {
        setStats(sceneManager.getStats())
      }

      // Player state
      const playerState = usePlayerStore.getState()
      setPlayerPos(playerState.position)
      setCameraMode(playerState.cameraMode)
      setMovementState(playerState.movementState)

      // World state
      const worldState = useWorldStore.getState()
      setTimeOfDay(worldState.timeOfDay)
      setWeather(worldState.weather)
      setLoadedChunks(worldState.loadedChunkIds.size)

      // Narrative state
      const narrativeState = useNarrativeStore.getState()
      setActiveQuests(narrativeState.completedQuests.length + narrativeState.failedQuests.length)
      setDialogueActive(!!narrativeState.currentDialogueId)

      frameRef.current = requestAnimationFrame(updateLoop)
    }

    frameRef.current = requestAnimationFrame(updateLoop)

    return () => {
      if (frameRef.current) {
        cancelAnimationFrame(frameRef.current)
      }
    }
  }, [isOpen])

  if (!isOpen) {
    return (
      <button
        className="fixed bottom-4 right-4 z-50 px-3 py-1.5 text-xs font-mono bg-ui-panel border border-ui-border rounded-lg hover:bg-ui-border transition-colors"
        onClick={onToggle}
        aria-label="Open debug panel"
      >
        🐛 Debug
      </button>
    )
  }

  return (
    <div className="debug-panel fixed bottom-4 right-4 z-50 w-96 max-h-[60vh] overflow-y-auto">
      <div className="flex items-center justify-between mb-3 pb-2 border-b border-ui-border">
        <h3 className="font-mono text-sm font-bold text-ui-accent">DEBUG PANEL</h3>
        <button
          className="text-ui-text-muted hover:text-ui-text transition-colors"
          onClick={onToggle}
          aria-label="Close debug panel"
        >
          ✕
        </button>
      </div>

      <div className="space-y-3 text-xs font-mono">
        {/* Performance */}
        <details className="bg-ui-panel/50 rounded-lg p-3 border border-ui-border/50">
          <summary className="cursor-pointer text-ui-accent font-bold">⚡ Performance</summary>
          <div className="mt-2 space-y-1 text-ui-text-muted">
            <div className="flex justify-between">
              <span>FPS:</span>
              <span className={fps >= 55 ? 'text-green-400' : fps >= 30 ? 'text-yellow-400' : 'text-red-400'}>
                {fps}
              </span>
            </div>
            <div className="flex justify-between">
              <span>Frame Time:</span>
              <span>{fps > 0 ? (1000 / fps).toFixed(2) : '0'} ms</span>
            </div>
            {Object.entries(stats).map(([key, value]) => (
              <div key={key} className="flex justify-between">
                <span>{key}:</span>
                <span>{String(value)}</span>
              </div>
            ))}
          </div>
        </details>

        {/* Player */}
        <details className="bg-ui-panel/50 rounded-lg p-3 border border-ui-border/50">
          <summary className="cursor-pointer text-ui-accent font-bold">🎮 Player</summary>
          <div className="mt-2 space-y-1 text-ui-text-muted">
            <div className="flex justify-between">
              <span>Position:</span>
              <span>{playerPos.x.toFixed(2)}, {playerPos.y.toFixed(2)}, {playerPos.z.toFixed(2)}</span>
            </div>
            <div className="flex justify-between">
              <span>Camera:</span>
              <span className="text-ui-text">{cameraMode}</span>
            </div>
            <div className="flex justify-between">
              <span>Movement:</span>
              <span className="text-ui-text">{movementState}</span>
            </div>
            <div className="flex justify-between">
              <span>Health:</span>
              <span>{usePlayerStore.getState().health}</span>
            </div>
            <div className="flex justify-between">
              <span>Stamina:</span>
              <span>{usePlayerStore.getState().stamina.toFixed(1)} / {usePlayerStore.getState().maxStamina}</span>
            </div>
          </div>
        </details>

        {/* World */}
        <details className="bg-ui-panel/50 rounded-lg p-3 border border-ui-border/50">
          <summary className="cursor-pointer text-ui-accent font-bold">🌍 World</summary>
          <div className="mt-2 space-y-1 text-ui-text-muted">
            <div className="flex justify-between">
              <span>Time:</span>
              <span>{timeOfDay.toFixed(2)}h ({useWorldStore.getState().getTimeOfDayLabel()})</span>
            </div>
            <div className="flex justify-between">
              <span>Weather:</span>
              <span className="text-ui-text capitalize">{weather}</span>
            </div>
            <div className="flex justify-between">
              <span>Loaded Chunks:</span>
              <span>{loadedChunks}</span>
            </div>
            <div className="flex justify-between">
              <span>Entities:</span>
              <span>{useWorldStore.getState().entities.size}</span>
            </div>
            <div className="flex justify-between">
              <span>Stations:</span>
              <span>{useWorldStore.getState().stations.size}</span>
            </div>
            <div className="flex justify-between">
              <span>Trains:</span>
              <span>{useWorldStore.getState().trains.size}</span>
            </div>
          </div>
        </details>

        {/* Narrative */}
        <details className="bg-ui-panel/50 rounded-lg p-3 border border-ui-border/50">
          <summary className="cursor-pointer text-ui-accent font-bold">📖 Narrative</summary>
          <div className="mt-2 space-y-1 text-ui-text-muted">
            <div className="flex justify-between">
              <span>Chapter / Act:</span>
              <span>{useNarrativeStore.getState().chapter} / {useNarrativeStore.getState().act}</span>
            </div>
            <div className="flex justify-between">
              <span>Active Dialogue:</span>
              <span>{dialogueActive ? 'Yes' : 'No'}</span>
            </div>
            <div className="flex justify-between">
              <span>Completed Quests:</span>
              <span>{useNarrativeStore.getState().completedQuests.length}</span>
            </div>
            <div className="flex justify-between">
              <span>Failed Quests:</span>
              <span>{useNarrativeStore.getState().failedQuests.length}</span>
            </div>
            <div className="flex justify-between">
              <span>Flags:</span>
              <span>{useNarrativeStore.getState().flags.size}</span>
            </div>
            <div className="flex justify-between">
              <span>Play Time:</span>
              <span>{(useNarrativeStore.getState().playTime / 1000 / 60).toFixed(1)} min</span>
            </div>
          </div>
        </details>

        {/* Asset Loader */}
        <details className="bg-ui-panel/50 rounded-lg p-3 border border-ui-border/50">
          <summary className="cursor-pointer text-ui-accent font-bold">📦 Assets</summary>
          <div className="mt-2 space-y-1 text-ui-text-muted">
            {(() => {
              const loader = getAssetLoader()
              if (!loader) return <span>Not initialized</span>
              const stats = loader.getStats()
              return (
                <>
                  <div className="flex justify-between">
                    <span>Queued:</span>
                    <span>{stats.queued}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Loading:</span>
                    <span>{stats.loading}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Loaded:</span>
                    <span>{stats.loaded}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Cached:</span>
                    <span>{stats.cached}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Progress:</span>
                    <span>{(stats.progress * 100).toFixed(1)}%</span>
                  </div>
                </>
              )
            })()}
          </div>
        </details>

        {/* Input */}
        <details className="bg-ui-panel/50 rounded-lg p-3 border border-ui-border/50">
          <summary className="cursor-pointer text-ui-accent font-bold">🎮 Input</summary>
          <div className="mt-2 space-y-1 text-ui-text-muted">
            {(() => {
              const input = getInputManager()
              if (!input) return <span>Not initialized</span>
              const state = input.getInputState()
              return (
                <>
                  <div className="flex justify-between">
                    <span>Pointer Locked:</span>
                    <span>{input.isPointerLockedEnabled() ? 'Yes' : 'No'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Move:</span>
                    <span>{[state.moveForward, state.moveBackward, state.moveLeft, state.moveRight].filter(Boolean).join(', ') || 'None'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Actions:</span>
                    <span>{[state.jump, state.sprint, state.crouch, state.interact].filter(Boolean).join(', ') || 'None'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Mouse Delta:</span>
                    <span>{state.mouseDelta.x.toFixed(1)}, {state.mouseDelta.y.toFixed(1)}</span>
                  </div>
                </>
              )
            })()}
          </div>
        </details>

        {/* Actions */}
        <details className="bg-ui-panel/50 rounded-lg p-3 border border-ui-border/50">
          <summary className="cursor-pointer text-ui-accent font-bold">🔧 Actions</summary>
          <div className="mt-2 space-y-2">
            <button
              className="w-full px-2 py-1 text-xs font-mono bg-ui-border hover:bg-ui-text-muted/20 rounded text-left transition-colors"
              onClick={() => {
                usePlayerStore.getState().setPosition({ x: 0, y: 1.7, z: 0 })
                usePlayerStore.getState().setRotation({ x: 0, y: 0, z: 0, w: 1 })
              }}
            >
              Reset Player Position
            </button>
            <button
              className="w-full px-2 py-1 text-xs font-mono bg-ui-border hover:bg-ui-text-muted/20 rounded text-left transition-colors"
              onClick={() => {
                useWorldStore.getState().setTimeOfDay(12)
                useWorldStore.getState().setWeather('clear')
              }}
            >
              Reset Time/Weather
            </button>
            <button
              className="w-full px-2 py-1 text-xs font-mono bg-ui-border hover:bg-ui-text-muted/20 rounded text-left transition-colors"
              onClick={() => {
                const loader = getAssetLoader()
                loader?.clearCache()
              }}
            >
              Clear Asset Cache
            </button>
            <button
              className="w-full px-2 py-1 text-xs font-mono bg-ui-danger/20 hover:bg-ui-danger/30 rounded text-left transition-colors border border-ui-danger/30"
              onClick={() => {
                if (confirm('Reset all game state?')) {
                  useGameStore.getState().setState('main-menu')
                  usePlayerStore.getState().reset()
                  useNarrativeStore.setState({
                    flags: new Map(),
                    completedQuests: [],
                    failedQuests: [],
                    choices: new Map(),
                    endings: [],
                    playTime: 0,
                    chapter: 1,
                    act: 1,
                    quests: new Map(),
                    activeQuestId: null,
                    dialogueTree: new Map(),
                    currentDialogueId: null,
                    currentDialogueNodeId: null,
                    dialogueHistory: [],
                  })
                }
              }}
            >
              🗑️ Reset All State
            </button>
          </div>
        </details>

        {/* Game State */}
        <details className="bg-ui-panel/50 rounded-lg p-3 border border-ui-border/50">
          <summary className="cursor-pointer text-ui-accent font-bold">🎯 Game State</summary>
          <div className="mt-2 space-y-1 text-ui-text-muted">
            <div className="flex justify-between">
              <span>State:</span>
              <span className="text-ui-text">{useGameStore.getState().state}</span>
            </div>
            <div className="flex justify-between">
              <span>Previous:</span>
              <span>{useGameStore.getState().previousState}</span>
            </div>
            <div className="flex justify-between">
              <span>Initialized:</span>
              <span>{useGameStore.getState().isInitialized ? 'Yes' : 'No'}</span>
            </div>
            <div className="flex justify-between">
              <span>Version:</span>
              <span>{useGameStore.getState().version}</span>
            </div>
          </div>
        </details>
      </div>
    </div>
  )
}

// Hook for easy access
export function useDebugPanel() {
  const [isOpen, setIsOpen] = useState(false)

  const toggle = () => setIsOpen(!isOpen)

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'F3' || (e.ctrlKey && e.shiftKey && e.key === 'D')) {
        e.preventDefault()
        toggle()
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [])

  return { isOpen, toggle, setIsOpen }
}