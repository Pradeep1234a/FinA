// UI Overlay Component
// Main HUD overlay with health, stamina, compass, quest tracker, etc.
// Uses throttled selectors to prevent 60fps re-render loops

import { useEffect, useRef, useState } from 'react'
import { useGameStore } from '@stores/gameStore'
import { usePlayerStore } from '@stores/playerStore'
import { useWorldStore } from '@stores/worldStore'
import { useNarrativeStore } from '@stores/narrativeStore'
import type { Quest, QuestObjective } from '@/types'

// ============= PURE HELPER FUNCTIONS =============

function getTimeOfDayIcon(timeOfDay: string): string {
  const icons: Record<string, string> = {
    dawn: '🌅', morning: '☀️', noon: '☀️', afternoon: '☀️',
    evening: '🌆', dusk: '🌇', night: '🌙',
  }
  return icons[timeOfDay] || '🌤️'
}

function getWeatherIcon(weather: string): string {
  const icons: Record<string, string> = {
    clear: '☀️', rain: '🌧️', mist: '🌫️', dust: '🌪️', storm: '⛈️',
  }
  return icons[weather] || '🌤️'
}

function formatTime(hours: number): string {
  const h = Math.floor(hours) % 24
  const m = Math.floor((hours % 1) * 60)
  const period = h >= 12 ? 'PM' : 'AM'
  const displayHour = h % 12 || 12
  return `${displayHour}:${m.toString().padStart(2, '0')} ${period}`
}

function getTimeOfDayLabel(time: number): string {
  if (time >= 5 && time < 6) return 'dawn'
  if (time >= 6 && time < 12) return 'morning'
  if (time >= 12 && time < 14) return 'noon'
  if (time >= 14 && time < 17) return 'afternoon'
  if (time >= 17 && time < 19) return 'evening'
  if (time >= 19 && time < 21) return 'dusk'
  return 'night'
}

function getCardinalDirection(radians: number): string {
  const degrees = (radians * 180 / Math.PI + 360) % 360
  const directions = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW']
  return directions[Math.round(degrees / 45) % 8]
}

function getCameraModeLabel(mode: string): string {
  const labels: Record<string, string> = {
    'third-person': '3RD PERSON', 'first-person': '1ST PERSON',
    'cinematic': 'CINEMATIC', 'metro-ride': 'METRO VIEW',
  }
  return labels[mode] || mode.toUpperCase()
}

// ============= THROTTLED SELECTOR HOOK =============

interface StoreWithSubscribe<T> {
  getState: () => T
  subscribe: (listener: (state: any) => void) => () => void
}

function useThrottledSelector<T>(store: any, selector: (state: any) => any, fps = 10) {
  const [value, setValue] = useState(() => selector(store.getState()))
  const selectorRef = useRef(selector)
  const lastUpdateRef = useRef(0)

  useEffect(() => {
    selectorRef.current = selector
  }, [selector])

  useEffect(() => {
    const unsubscribe = store.subscribe((state: any) => {
      const now = performance.now()
      if (now - lastUpdateRef.current >= 1000 / 10) {
        lastUpdateRef.current = now
        const newValue = selectorRef.current(store.getState())
        setValue(newValue)
      }
    })

    // Initial value
    setValue(selectorRef.current(store.getState()))

    return () => {
      // unsubscribe is handled by the store
    }
  }, [])

  return value
}

// ============= CHILD COMPONENTS =============

function CompassRose({ playerRotation }: { playerRotation: number }) {
  const directions = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW']
  const angle = -playerRotation * (180 / Math.PI)
  const index = Math.round(((angle % 360) + 360) % 360 / 45) % 8

  return (
    <div className="relative w-16 h-16">
      <svg className="w-full h-full text-ui-text-muted" viewBox="0 0 64 64">
        <circle cx="32" cy="32" r="28" fill="none" stroke="currentColor" strokeWidth="1" />
        {directions.map((dir, i) => {
          const rad = (i * 45 - 90) * Math.PI / 180
          const x = 32 + Math.cos(rad) * 24
          const y = 32 + Math.sin(rad) * 24
          return (
            <text key={dir} x={x} y={y} textAnchor="middle" dominantBaseline="middle" fontSize="8" fontFamily="monospace" className={i === index ? 'text-metro-purple font-bold' : 'text-ui-text-muted'}>
              {dir}
            </text>
          )
        })}
        <polygon points="32,4 30,12 34,12" fill="currentColor" className="text-metro-purple" transform={`rotate(${angle}, 32, 32)`} />
      </svg>
    </div>
  )
}

function CompassWrapper() {
  // Throttle rotation updates to 20fps
  const { rotationY } = useThrottledSelector(
    usePlayerStore,
    (s) => ({ rotationY: s.rotation.y }),
    20
  )
  return (
    <div className="bg-ui-panel/80 backdrop-blur-sm border border-ui-border/50 rounded-xl px-6 py-3">
      <div className="flex items-center gap-4">
        <CompassRose playerRotation={rotationY} />
        <div className="text-center">
          <p className="text-xs text-ui-text-muted uppercase tracking-wide">Heading</p>
          <p className="text-lg font-bold font-mono text-ui-text">{getCardinalDirection(rotationY)}</p>
        </div>
      </div>
    </div>
  )
}

function QuestTracker() {
  const activeQuestId = useNarrativeStore((s) => s.activeQuestId)
  const questsMap = useNarrativeStore((s) => s.quests)

  const activeQuest = activeQuestId ? questsMap.get(activeQuestId) || null : null
  const objectives = activeQuest ? activeQuest.objectives.filter((o) => o.currentCount < o.count) : []

  if (!activeQuest) return null

  return (
    <div className="bg-ui-panel/90 backdrop-blur-sm border border-metro-purple/50 rounded-xl px-4 py-3 max-w-md pointer-events-auto">
      <div className="flex items-start justify-between gap-2 mb-2">
        <div>
          <p className="text-xs text-metro-purple-light uppercase tracking-wide font-medium">Active Quest</p>
          <p className="text-sm font-medium text-ui-text truncate max-w-[280px]">{activeQuest.title}</p>
        </div>
        <button className="p-1 text-ui-text-muted hover:text-ui-text transition-colors flex-shrink-0" aria-label="Open quest details">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" /></svg>
        </button>
      </div>
      <div className="space-y-1.5">
        {objectives.slice(0, 3).map(obj => (
          <div key={obj.id} className="flex items-center gap-2 text-sm">
            <div className={`w-4 h-4 rounded border-2 flex items-center justify-center flex-shrink-0 ${obj.currentCount >= obj.count ? 'bg-metro-green border-metro-green' : 'border-ui-border'}`}>
              {obj.currentCount >= obj.count && <svg className="w-3 h-3 text-ui-bg" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414 0l-4-4a1 1 0 011.414 0z" clipRule="evenodd" /></svg>}
            </div>
            <span className={`truncate ${obj.currentCount >= obj.count ? 'text-ui-text-muted line-through' : 'text-ui-text'}`}>{obj.description}</span>
            {obj.count > 1 && <span className="text-xs font-mono text-ui-text-muted ml-auto">{obj.currentCount} / {obj.count}</span>}
          </div>
        ))}
        {objectives.length > 3 && <p className="text-xs text-ui-text-muted text-center mt-1">+{objectives.length - 3} more objectives</p>}
      </div>
    </div>
  )
}

function DialogueIndicator() {
  const currentDialogueId = useNarrativeStore((s) => s.currentDialogueId)
  if (!currentDialogueId) return null
  return (
    <div className="fixed bottom-24 left-1/2 -translate-x-1/2 pointer-events-auto animate-slide-up">
      <div className="bg-metro-purple/95 backdrop-blur-sm border border-metro-purple-light/30 rounded-xl px-6 py-4 text-center max-w-xl">
        <div className="flex items-center justify-center gap-2 mb-2"><svg className="w-6 h-6 text-metro-gold" fill="currentColor" viewBox="0 0 24 24"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/></svg><span className="text-sm font-medium text-metro-gold">Dialogue Active</span></div>
        <p className="text-sm text-ui-text">Press E to continue...</p>
      </div>
    </div>
  )
}

function PauseOverlay() {
  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-40 pointer-events-auto">
      <div className="bg-ui-panel border border-ui-border rounded-2xl p-8 max-w-md w-full mx-4 text-center">
        <h2 className="text-2xl font-bold text-ui-text mb-2">Game Paused</h2>
        <p className="text-ui-text-muted mb-6">Press ESC to resume</p>
        <div className="flex flex-col gap-3">
          <button className="px-4 py-2 bg-metro-purple text-ui-text rounded-lg font-medium hover:bg-metro-purple-light transition-colors">Resume</button>
          <button className="px-4 py-2 bg-ui-border text-ui-text rounded-lg font-medium hover:bg-ui-text-muted/20 transition-colors">Settings</button>
          <button className="px-4 py-2 bg-ui-danger/20 text-ui-danger border border-ui-danger/30 rounded-lg font-medium hover:bg-ui-danger/30 transition-colors">Main Menu</button>
        </div>
      </div>
    </div>
  )
}

function MetroMapModal({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) {
  if (!isOpen) return null

  const stations = useWorldStore((s) => Array.from(s.stations.values()).map(st => ({
    id: st.id, name: st.name, nameKannada: st.nameKannada, line: st.line
  })))

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 pointer-events-auto">
      <div className="bg-ui-panel border border-ui-border rounded-2xl p-6 max-w-4xl max-h-[80vh] w-full mx-4 overflow-hidden pointer-events-auto">
        <div className="flex items-center justify-between mb-4"><h2 className="text-xl font-bold text-ui-text">Metro Map</h2><button className="p-2 text-ui-text-muted hover:text-ui-text transition-colors" onClick={onClose}><svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg></button></div>
        <div className="overflow-auto max-h-[60vh]">
          <p className="text-ui-text-muted text-center py-8">Metro Map View - {stations.length} stations loaded<br /><span className="text-xs">Full interactive map coming soon...</span></p>
          <div className="grid grid-cols-2 gap-2 mt-4">
            {stations.slice(0, 20).map(station => (
              <button key={station.id} className={`px-3 py-2 rounded-lg text-sm text-left transition-colors ${station.line === 'purple' ? 'bg-metro-purple/20 border border-metro-purple/30 hover:bg-metro-purple/30' : 'bg-metro-green/20 border border-metro-green/30 hover:bg-metro-green/30'}`}>
                <p className="font-medium">{station.name}</p>
                <p className="text-xs text-ui-text-muted">{station.nameKannada}</p>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

function InventoryModal({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) {
  if (!isOpen) return null

  const inventory = usePlayerStore((s) => s.inventory)

  if (inventory.length === 0) {
    return (
      <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 pointer-events-auto">
        <div className="bg-ui-panel border border-ui-border rounded-2xl p-6 max-w-2xl max-h-[80vh] w-full mx-4 overflow-hidden pointer-events-auto">
          <div className="flex items-center justify-between mb-4"><h2 className="text-xl font-bold text-ui-text">Inventory</h2><button className="p-2 text-ui-text-muted hover:text-ui-text transition-colors" onClick={onClose}><svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg></button></div>
          <div className="text-center py-12"><svg className="w-16 h-16 mx-auto text-ui-text-muted mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" /></svg><p className="text-ui-text-muted">Inventory is empty</p></div>
        </div>
      </div>
    )
  }

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 pointer-events-auto">
      <div className="bg-ui-panel border border-ui-border rounded-2xl p-6 max-w-2xl max-h-[80vh] w-full mx-4 overflow-hidden pointer-events-auto">
        <div className="flex items-center justify-between mb-4"><h2 className="text-xl font-bold text-ui-text">Inventory</h2><button className="p-2 text-ui-text-muted hover:text-ui-text transition-colors" onClick={onClose}><svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg></button></div>
        <div className="max-h-[60vh] overflow-auto">
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
            {inventory.map(item => (
              <div key={item.id} className="bg-ui-border/30 border border-ui-border/50 rounded-xl p-3 hover:border-ui-text-muted/30 transition-colors">
                <div className="flex items-start gap-3">
                  <div className="w-12 h-12 bg-ui-bg rounded-lg flex items-center justify-center flex-shrink-0"><span className="text-2xl">{item.icon || '📦'}</span></div>
                  <div className="flex-1 min-w-0"><p className="font-medium text-ui-text truncate">{item.name}</p><p className="text-xs text-ui-text-muted truncate">{item.description}</p><p className="text-xs text-metro-gold font-mono mt-1">Qty: {item.quantity}</p></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

// ============= MAIN UI OVERLAY (THROTTLED SELECTORS) =============

export function UIOverlay() {
  // Game state - primitive, rarely changes
  const gameState = useGameStore((s) => s.state)

  // Player state - throttled to 10fps for stable values, 20fps for rotation
  const playerStats = useThrottledSelector(
    usePlayerStore,
    (s) => ({
      health: s.health,
      stamina: s.stamina,
      maxStamina: s.maxStamina,
      movementState: s.movementState,
      cameraMode: s.cameraMode,
      metroCardBalance: s.metroCard.balance,
      metroCardLevel: s.metroCard.level,
      currentQuest: s.currentQuest,
    }),
    10 // 10fps for stats
  )

  const rotationY = useThrottledSelector(
    usePlayerStore,
    (s) => s.rotation.y,
    20 // 20fps for rotation
  )

  // World state - throttled to 5fps (timeOfDay changes slowly in terms of label)
  const worldState = useThrottledSelector(
    useWorldStore,
    (s) => ({
      timeOfDay: s.timeOfDay,
      weather: s.weather,
      timeOfDayLabel: getTimeOfDayLabel(s.timeOfDay),
    }),
    5
  )

  const { timeOfDay, weather, timeOfDayLabel } = worldState

  // Narrative state - primitive, rarely changes
  const currentDialogueId = useNarrativeStore((s) => s.currentDialogueId)

  // Local UI state
  const [showMetroMap, setShowMetroMap] = useState(false)
  const [showInventory, setShowInventory] = useState(false)

  // Early return for non-playing states
  if (gameState !== 'playing' && gameState !== 'paused') return null

  // Destructure for convenience
  const { health, stamina, maxStamina, movementState, cameraMode, metroCardBalance, metroCardLevel, currentQuest } = playerStats

  return (
    <div className="ui-overlay pointer-events-none">
      {/* Top Bar - Time, Weather, Metro Card */}
      <div className="fixed top-4 left-4 right-4 flex justify-between items-start pointer-events-auto">
        {/* Time & Weather */}
        <div className="flex items-center gap-3 bg-ui-panel/80 backdrop-blur-sm border border-ui-border/50 rounded-xl px-4 py-2">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-metro-purple/20 flex items-center justify-center">
              {getTimeOfDayIcon(timeOfDayLabel)}
            </div>
            <div>
              <p className="text-xs font-mono text-ui-text-muted uppercase tracking-wide">{timeOfDayLabel}</p>
              <p className="text-sm font-medium text-ui-text">{formatTime(timeOfDay)}</p>
            </div>
          </div>
          <div className="flex items-center gap-2 ml-4 border-l border-ui-border/30 pl-4">
            <div className="w-8 h-8 rounded-full bg-ui-border/50 flex items-center justify-center">{getWeatherIcon(weather)}</div>
            <span className="text-sm text-ui-text capitalize">{weather}</span>
          </div>
        </div>

        {/* Metro Card Balance */}
        <div className="flex items-center gap-2 bg-ui-panel/80 backdrop-blur-sm border border-ui-border/50 rounded-xl px-4 py-2">
          <div className="w-8 h-8 rounded-full bg-metro-gold/20 flex items-center justify-center">
            <svg className="w-5 h-5 text-metro-gold" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/></svg>
          </div>
          <div className="text-right"><p className="text-xs text-ui-text-muted">BALANCE</p><p className="text-lg font-bold text-metro-gold font-mono">₹{metroCardBalance}</p></div>
          <div className="ml-4 border-l border-ui-border/30 pl-4 text-right"><p className="text-xs text-ui-text-muted">LEVEL</p><p className="text-lg font-bold text-ui-text font-mono">{metroCardLevel}</p></div>
        </div>
      </div>

      {/* Bottom Left - Health & Stamina */}
      <div className="fixed bottom-4 left-4 pointer-events-auto">
        <div className="flex flex-col gap-3">
          <div className="bg-ui-panel/80 backdrop-blur-sm border border-ui-border/50 rounded-xl px-4 py-3 min-w-[200px]">
            <div className="flex items-center justify-between mb-1"><span className="text-xs font-medium text-ui-text-muted uppercase tracking-wide">Health</span><span className="text-sm font-mono text-ui-text">{health} / 100</span></div>
            <div className="h-2 bg-ui-border rounded-full overflow-hidden"><div className="h-full bg-gradient-to-r from-red-500 to-red-400 rounded-full transition-all duration-300" style={{ width: `${health}%` }} /></div>
          </div>
          <div className="bg-ui-panel/80 backdrop-blur-sm border border-ui-border/50 rounded-xl px-4 py-3 min-w-[200px]">
            <div className="flex items-center justify-between mb-1"><span className="text-xs font-medium text-ui-text-muted uppercase tracking-wide">Stamina</span><span className="text-sm font-mono text-ui-text">{stamina.toFixed(0)} / {maxStamina}</span></div>
            <div className="h-2 bg-ui-border rounded-full overflow-hidden"><div className="h-full bg-gradient-to-r from-yellow-500 to-yellow-400 rounded-full transition-all duration-300" style={{ width: `${(stamina / maxStamina) * 100}%` }} /></div>
            <p className="text-xs text-ui-text-muted mt-1">{movementState === 'sprint' ? '⚡ Sprinting' : movementState === 'run' ? '🏃 Running' : '🚶 Idle'}</p>
          </div>
        </div>
      </div>

      {/* Bottom Center - Compass & Quest Tracker */}
      <div className="fixed bottom-4 left-1/2 -translate-x-1/2 pointer-events-auto flex flex-col items-center gap-3">
        <CompassWrapper />
        <QuestTracker />
      </div>

      {/* Bottom Right - Interaction Prompt & Metro Status */}
      <div className="fixed bottom-4 right-4 pointer-events-auto flex flex-col items-end gap-3">
        {currentQuest && (
          <div className="bg-metro-purple/90 backdrop-blur-sm border border-metro-purple-light/30 rounded-xl px-4 py-3 text-center">
            <p className="text-xs text-metro-gold uppercase tracking-wide font-medium">On Metro</p>
            <p className="font-medium text-ui-text">{currentQuest}</p>
            <p className="text-xs text-metro-gold/80">Press E to alight</p>
          </div>
        )}
        {gameState === 'playing' && (
          <div className="bg-ui-panel/90 backdrop-blur-sm border border-ui-border/50 rounded-xl px-4 py-2 animate-fade-in">
            <p className="text-xs text-ui-text-muted uppercase tracking-wide">Controls</p>
            <div className="flex items-center gap-4 mt-1 text-sm flex-wrap justify-end">
              <kbd className="px-2 py-1 bg-ui-border rounded text-ui-text font-mono">WASD</kbd><span className="text-ui-text-muted">Move</span>
              <kbd className="px-2 py-1 bg-ui-border rounded text-ui-text font-mono">Shift</kbd><span className="text-ui-text-muted">Sprint</span>
              <kbd className="px-2 py-1 bg-ui-border rounded text-ui-text font-mono">Space</kbd><span className="text-ui-text-muted">Jump</span>
              <kbd className="px-2 py-1 bg-ui-border rounded text-ui-text font-mono">E</kbd><span className="text-ui-text-muted">Interact</span>
              <kbd className="px-2 py-1 bg-ui-border rounded text-ui-text font-mono">C</kbd><span className="text-ui-text-muted">Camera</span>
              <kbd className="px-2 py-1 bg-ui-border rounded text-ui-text font-mono">M</kbd><span className="text-ui-text-muted">Map</span>
              <kbd className="px-2 py-1 bg-ui-border rounded text-ui-text font-mono">I</kbd><span className="text-ui-text-muted">Inventory</span>
            </div>
          </div>
        )}
        <div className="bg-ui-panel/80 backdrop-blur-sm border border-ui-border/50 rounded-xl px-3 py-1.5">
          <span className="text-xs font-mono text-ui-text-muted uppercase tracking-wide">{getCameraModeLabel(cameraMode)}</span>
        </div>
      </div>

      {/* Dialogue Indicator */}
      <DialogueIndicator />
      {gameState === 'paused' && <PauseOverlay />}

      {/* Modals - subscribe to object/array stores ONLY when open */}
      <MetroMapModal isOpen={showMetroMap} onClose={() => setShowMetroMap(false)} />
      <InventoryModal isOpen={showInventory} onClose={() => setShowInventory(false)} />
    </div>
  )
}