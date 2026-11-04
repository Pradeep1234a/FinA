import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import { persist, createJSONStorage } from 'zustand/middleware'
import type {
  GameState,
  GameSettings,
  SaveData,
  GameEvent,
  GameEventType,
} from '@types'

interface GameStore {
  // Core state
  state: GameState
  previousState: GameState
  isInitialized: boolean
  version: string

  // Settings
  settings: GameSettings

  // Event system
  eventListeners: Map<GameEventType, Set<(event: GameEvent) => void>>

  // Actions
  initialize: () => Promise<void>
  setState: (newState: GameState) => void
  togglePause: () => void
  updateSettings: (settings: Partial<GameSettings>) => void
  resetSettings: () => void

  // Event system
  emit: (type: GameEventType, payload: unknown) => void
  on: (type: GameEventType, listener: (event: GameEvent) => void) => () => void
  off: (type: GameEventType, listener: (event: GameEvent) => void) => void

  // Save/Load
  saveGame: (slot?: string) => Promise<SaveData>
  loadGame: (saveData: SaveData) => Promise<void>
  deleteSave: (slot: string) => Promise<void>
  listSaves: () => Promise<SaveData[]>
}

const defaultSettings: GameSettings = {
  graphics: {
    quality: 'high',
    resolutionScale: 1,
    vsync: true,
    fov: 75,
    motionBlur: false,
    bloom: true,
    ssao: true,
    shadows: true,
    shadowQuality: 'high',
    lodBias: 0,
    particleDensity: 1,
    crowdDensity: 1,
  },
  audio: {
    masterVolume: 0.8,
    musicVolume: 0.7,
    sfxVolume: 0.8,
    voiceVolume: 1,
    ambientVolume: 0.6,
    uiVolume: 0.5,
    spatialAudio: true,
    dynamicMusic: true,
  },
  controls: {
    mouseSensitivity: 1,
    gamepadSensitivity: 1,
    invertY: false,
    toggleSprint: false,
    toggleCrouch: false,
    cameraDistance: 5,
    cameraHeight: 1.7,
    keybindings: {
      moveForward: 'KeyW',
      moveBackward: 'KeyS',
      moveLeft: 'KeyA',
      moveRight: 'KeyD',
      jump: 'Space',
      sprint: 'ShiftLeft',
      crouch: 'ControlLeft',
      interact: 'KeyE',
      cameraToggle: 'KeyC',
      mapToggle: 'KeyM',
      inventoryToggle: 'KeyI',
      pause: 'Escape',
      photoMode: 'KeyP',
      screenshot: 'F12',
    },
  },
  accessibility: {
    highContrast: false,
    largeText: false,
    reducedMotion: false,
    screenReader: false,
    subtitles: true,
    subtitleSize: 'medium',
    subtitleBackground: true,
    colorBlindMode: 'none',
    holdToInteract: false,
    autoMove: false,
  },
  gameplay: {
    difficulty: 'normal',
    autoSave: true,
    showQuestMarkers: true,
    showCompass: true,
    showMetroMap: true,
    fastTravelConfirmation: true,
    photoModeEnabled: true,
  },
}

const defaultGameState: Omit<GameStore, 'actions'> = {
  state: 'loading',
  previousState: 'loading',
  isInitialized: false,
  version: '0.1.0',
  settings: defaultSettings,
  eventListeners: new Map(),
}

export const useGameStore = create<GameStore>()(
  immer(
    persist(
      (set, get) => ({
        ...defaultGameState,

        initialize: async () => {
          // Initialize game systems
          set((state) => {
            state.isInitialized = true
            state.state = 'main-menu'
          })
        },

        setState: (newState: GameState) => {
          set((state) => {
            state.previousState = state.state
            state.state = newState
          })
          get().emit('game:state-change', { newState, previousState: get().previousState })
        },

        togglePause: () => {
          const { state } = get()
          if (state === 'playing') {
            get().setState('paused')
          } else if (state === 'paused') {
            get().setState('playing')
          }
        },

        updateSettings: (newSettings: Partial<GameSettings>) => {
          set((state) => {
            const mergeSettings = (target: Record<string, unknown>, source: Record<string, unknown>) => {
              for (const key of Object.keys(source)) {
                if (typeof source[key] === 'object' && source[key] !== null && !Array.isArray(source[key])) {
                  target[key] = target[key] || {}
                  mergeSettings(target[key] as Record<string, unknown>, source[key] as Record<string, unknown>)
                } else {
                  target[key] = source[key]
                }
              }
            }
            mergeSettings(state.settings, newSettings)
          })
          get().emit('settings:change', newSettings)
        },

        resetSettings: () => {
          set((state) => {
            state.settings = defaultSettings
          })
          get().emit('settings:change', defaultSettings)
        },

        // Event system
        emit: (type: GameEventType, payload: unknown) => {
          const listeners = get().eventListeners.get(type)
          if (listeners) {
            const event: GameEvent = { type, payload, timestamp: Date.now() }
            listeners.forEach(listener => listener(event))
          }
        },

        on: (type: GameEventType, listener: (event: GameEvent) => void) => {
          set((state) => {
            if (!state.eventListeners.has(type)) {
              state.eventListeners.set(type, new Set())
            }
            state.eventListeners.get(type)!.add(listener)
          })
          return () => get().off(type, listener)
        },

        off: (type: GameEventType, listener: (event: GameEvent) => void) => {
          set((state) => {
            const listeners = state.eventListeners.get(type)
            if (listeners) {
              listeners.delete(listener)
              if (listeners.size === 0) {
                state.eventListeners.delete(type)
              }
            }
          })
        },

        // Save/Load
        saveGame: async (slot = 'autosave') => {
          const { state, settings } = get()
          // Import here to avoid circular dependency
          const { usePlayerStore } = await import('./playerStore')
          const { useWorldStore } = await import('./worldStore')
          const { useNarrativeStore } = await import('./narrativeStore')

          const saveData: SaveData = {
            version: 1,
            timestamp: Date.now(),
            playTime: 0, // TODO: track play time
            player: usePlayerStore.getState(),
            world: useWorldStore.getState(),
            narrative: useNarrativeStore.getState(),
            settings,
            metadata: {
              gameVersion: get().version,
              saveName: slot,
              location: 'Unknown',
              chapter: useNarrativeStore.getState().chapter,
              act: useNarrativeStore.getState().act,
            },
          }

          // Save to IndexedDB
          const db = await import('idb').then(m => m.openDB('namma-metro-saves', 1, {
            upgrade(db) {
              db.createObjectStore('saves')
            }
          }))
          await db.put('saves', saveData, slot)

          get().emit('game:save', { slot, saveData })
          return saveData
        },

        loadGame: async (saveData: SaveData) => {
          const { usePlayerStore } = await import('./playerStore')
          const { useWorldStore } = await import('./worldStore')
          const { useNarrativeStore } = await import('./narrativeStore')

          usePlayerStore.getState().loadFromSave(saveData.player)
          useWorldStore.getState().loadFromSave(saveData.world)
          useNarrativeStore.getState().loadFromSave(saveData.narrative)
          get().updateSettings(saveData.settings)

          get().setState('playing')
          get().emit('game:load', { saveData })
        },

        deleteSave: async (slot: string) => {
          const db = await import('idb').then(m => m.openDB('namma-metro-saves', 1))
          await db.delete('saves', slot)
        },

        listSaves: async () => {
          const db = await import('idb').then(m => m.openDB('namma-metro-saves', 1))
          const keys = await db.getAllKeys('saves')
          const saves: SaveData[] = []
          for (const key of keys) {
            const save = await db.get('saves', key)
            if (save) saves.push(save)
          }
          return saves.sort((a, b) => b.timestamp - a.timestamp)
        },
      }),
      {
        name: 'namma-metro-settings',
        storage: createJSONStorage(() => localStorage),
        partialize: (state) => ({
          settings: state.settings,
          version: state.version,
        }),
      }
    )
  )
)

// Selector hooks for common use cases
export const useGameState = () => useGameStore((state) => state.state)
export const useGameSettings = () => useGameStore((state) => state.settings)
export const useGameActions = () => useGameStore((state) => ({
  setState: state.setState,
  togglePause: state.togglePause,
  updateSettings: state.updateSettings,
  resetSettings: state.resetSettings,
  saveGame: state.saveGame,
  loadGame: state.loadGame,
}))