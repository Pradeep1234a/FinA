import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import type {
  WorldChunk,
  WorldEntity,
  MetroStation,
  MetroTrain,
  MetroRoute,
  WeatherType,
  TimeOfDay,
  Vector3,
  Vector2,
  BoundingBox,
} from '@types'

interface WorldStore {
  // Chunk system
  chunks: Map<string, WorldChunk>
  loadedChunkIds: Set<string>
  playerChunkPosition: Vector2

  // Entities
  entities: Map<string, WorldEntity>

  // Metro system
  stations: Map<string, MetroStation>
  trains: Map<string, MetroTrain>
  routes: Map<string, MetroRoute>
  fastTravelUnlocked: Set<string>

  // Environment
  timeOfDay: number // 0-24 hours
  timeScale: number
  weather: WeatherType
  weatherIntensity: number
  weatherTransition: number

  // Actions
  // Chunks
  registerChunk: (chunk: WorldChunk) => void
  loadChunk: (chunkId: string) => void
  unloadChunk: (chunkId: string) => void
  updatePlayerChunk: (position: Vector2) => void
  getLoadedChunks: () => WorldChunk[]
  getChunkAt: (position: Vector2) => WorldChunk | undefined

  // Entities
  registerEntity: (entity: WorldEntity) => void
  unregisterEntity: (entityId: string) => void
  updateEntity: (entityId: string, updates: Partial<WorldEntity>) => void
  getEntitiesInRadius: (position: Vector3, radius: number) => WorldEntity[]
  getEntitiesInChunk: (chunkId: string) => WorldEntity[]

  // Metro
  registerStation: (station: MetroStation) => void
  registerTrain: (train: MetroTrain) => void
  registerRoute: (route: MetroRoute) => void
  updateTrain: (trainId: string, updates: Partial<MetroTrain>) => void
  getStation: (stationId: string) => MetroStation | undefined
  getNearbyStations: (position: Vector3, radius: number) => MetroStation[]
  getRoute: (from: string, to: string) => MetroRoute | undefined
  unlockFastTravel: (stationId: string) => void
  isFastTravelUnlocked: (stationId: string) => boolean

  // Environment
  setTimeOfDay: (time: number) => void
  setTimeScale: (scale: number) => void
  setWeather: (weather: WeatherType, intensity?: number, transition?: number) => void
  getTimeOfDayLabel: () => TimeOfDay
  getSunPosition: () => Vector3

  // Save/Load
  loadFromSave: (save: {
    loadedChunks: string[]
    entityStates: Map<string, { position: Vector3; rotation: { x: number; y: number; z: number; w: number }; state: Record<string, unknown> }>
    metroStates: Map<string, { position: Vector3; scheduleIndex: number; doorsOpen: boolean }>
    timeOfDay: number
    weather: WeatherType
    weatherIntensity: number
  }) => void
}

const initialState = {
  chunks: new Map<string, WorldChunk>(),
  loadedChunkIds: new Set<string>(),
  playerChunkPosition: { x: 0, y: 0 },
  entities: new Map<string, WorldEntity>(),
  stations: new Map<string, MetroStation>(),
  trains: new Map<string, MetroTrain>(),
  routes: new Map<string, MetroRoute>(),
  fastTravelUnlocked: new Set<string>(),
  timeOfDay: 12,
  timeScale: 1,
  weather: 'clear' as WeatherType,
  weatherIntensity: 0,
  weatherTransition: 0,
}

export const useWorldStore = create<WorldStore>()(
  immer((set, get) => ({
    ...initialState,

    // Chunks
    registerChunk: (chunk: WorldChunk) => set((state) => {
      state.chunks.set(chunk.id, chunk)
    }),

    loadChunk: (chunkId: string) => set((state) => {
      const chunk = state.chunks.get(chunkId)
      if (chunk) {
        chunk.isLoaded = true
        state.loadedChunkIds.add(chunkId)
      }
    }),

    unloadChunk: (chunkId: string) => set((state) => {
      const chunk = state.chunks.get(chunkId)
      if (chunk) {
        chunk.isLoaded = false
        state.loadedChunkIds.delete(chunkId)
      }
    }),

    updatePlayerChunk: (position: Vector2) => set((state) => {
      state.playerChunkPosition = position
      // Auto-load surrounding chunks (3x3)
      const chunkSize = 500 // 500m chunks
      const playerChunkX = Math.floor(position.x / chunkSize)
      const playerChunkY = Math.floor(position.y / chunkSize)

      for (let dx = -1; dx <= 1; dx++) {
        for (let dy = -1; dy <= 1; dy++) {
          const chunkX = playerChunkX + dx
          const chunkY = playerChunkY + dy
          const chunkId = `chunk_${chunkX}_${chunkY}`
          if (state.chunks.has(chunkId) && !state.loadedChunkIds.has(chunkId)) {
            get().loadChunk(chunkId)
          }
        }
      }

      // Unload distant chunks
      for (const loadedId of state.loadedChunkIds) {
        const chunk = state.chunks.get(loadedId)
        if (chunk) {
          const distX = Math.abs(chunk.position.x - playerChunkX)
          const distY = Math.abs(chunk.position.y - playerChunkY)
          if (distX > 2 || distY > 2) {
            get().unloadChunk(loadedId)
          }
        }
      }
    }),

    getLoadedChunks: () => {
      const { loadedChunkIds, chunks } = get()
      return Array.from(loadedChunkIds).map(id => chunks.get(id)!).filter(Boolean)
    },

    getChunkAt: (position: Vector2) => {
      const chunkSize = 500
      const chunkX = Math.floor(position.x / chunkSize)
      const chunkY = Math.floor(position.y / chunkSize)
      const chunkId = `chunk_${chunkX}_${chunkY}`
      return get().chunks.get(chunkId)
    },

    // Entities
    registerEntity: (entity: WorldEntity) => set((state) => {
      state.entities.set(entity.id, entity)
    }),

    unregisterEntity: (entityId: string) => set((state) => {
      state.entities.delete(entityId)
    }),

    updateEntity: (entityId: string, updates: Partial<WorldEntity>) => set((state) => {
      const entity = state.entities.get(entityId)
      if (entity) {
        Object.assign(entity, updates)
      }
    }),

    getEntitiesInRadius: (position: Vector3, radius: number) => {
      const { entities } = get()
      const radiusSq = radius * radius
      return Array.from(entities.values()).filter(entity => {
        const dx = entity.position.x - position.x
        const dy = entity.position.y - position.y
        const dz = entity.position.z - position.z
        return dx * dx + dy * dy + dz * dz <= radiusSq
      })
    },

    getEntitiesInChunk: (chunkId: string) => {
      const { entities, chunks } = get()
      const chunk = chunks.get(chunkId)
      if (!chunk) return []
      return Array.from(entities.values()).filter(entity => {
        return entity.position.x >= chunk.bounds.min.x &&
               entity.position.x <= chunk.bounds.max.x &&
               entity.position.z >= chunk.bounds.min.z &&
               entity.position.z <= chunk.bounds.max.z
      })
    },

    // Metro
    registerStation: (station: MetroStation) => set((state) => {
      state.stations.set(station.id, station)
    }),

    registerTrain: (train: MetroTrain) => set((state) => {
      state.trains.set(train.id, train)
    }),

    registerRoute: (route: MetroRoute) => set((state) => {
      const key = `${route.from}-${route.to}-${route.line}`
      state.routes.set(key, route)
    }),

    updateTrain: (trainId: string, updates: Partial<MetroTrain>) => set((state) => {
      const train = state.trains.get(trainId)
      if (train) {
        Object.assign(train, updates)
      }
    }),

    getStation: (stationId: string) => {
      return get().stations.get(stationId)
    },

    getNearbyStations: (position: Vector3, radius: number) => {
      const { stations } = get()
      const radiusSq = radius * radius
      return Array.from(stations.values()).filter(station => {
        const dx = station.position.x - position.x
        const dy = station.position.y - position.y
        const dz = station.position.z - position.z
        return dx * dx + dy * dy + dz * dz <= radiusSq
      })
    },

    getRoute: (from: string, to: string) => {
      const { routes } = get()
      // Try direct route first
      for (const route of routes.values()) {
        if ((route.from === from && route.to === to) ||
            (route.from === to && route.to === from)) {
          return route
        }
      }
      // TODO: Implement multi-line routing with transfers
      return undefined
    },

    unlockFastTravel: (stationId: string) => set((state) => {
      state.fastTravelUnlocked.add(stationId)
    }),

    isFastTravelUnlocked: (stationId: string) => {
      return get().fastTravelUnlocked.has(stationId)
    },

    // Environment
    setTimeOfDay: (time: number) => set((state) => {
      state.timeOfDay = time % 24
    }),

    setTimeScale: (scale: number) => set((state) => {
      state.timeScale = Math.max(0, scale)
    }),

    setWeather: (weather: WeatherType, intensity = 1, transition = 5) => set((state) => {
      state.weather = weather
      state.weatherIntensity = intensity
      state.weatherTransition = transition
    }),

    getTimeOfDayLabel: () => {
      const time = get().timeOfDay
      if (time >= 5 && time < 6) return 'dawn'
      if (time >= 6 && time < 12) return 'morning'
      if (time >= 12 && time < 14) return 'noon'
      if (time >= 14 && time < 17) return 'afternoon'
      if (time >= 17 && time < 19) return 'evening'
      if (time >= 19 && time < 21) return 'dusk'
      return 'night'
    },

    getSunPosition: () => {
      const time = get().timeOfDay
      // Convert 24h time to sun angle
      // 6am = 0° (east), 12pm = 90° (zenith), 6pm = 180° (west)
      const hourAngle = ((time - 6) / 24) * Math.PI * 2
      const elevation = Math.sin(hourAngle) * Math.PI / 2
      const azimuth = hourAngle

      const radius = 10000
      return {
        x: Math.cos(elevation) * Math.sin(azimuth) * radius,
        y: Math.sin(elevation) * radius,
        z: Math.cos(elevation) * Math.cos(azimuth) * radius,
      }
    },

    // Save/Load
    loadFromSave: (save) => set((state) => {
      // Restore loaded chunks
      state.loadedChunkIds.clear()
      for (const chunkId of save.loadedChunks) {
        const chunk = state.chunks.get(chunkId)
        if (chunk) {
          chunk.isLoaded = true
          state.loadedChunkIds.add(chunkId)
        }
      }

      // Restore entity states
      for (const [entityId, entityState] of save.entityStates) {
        const entity = state.entities.get(entityId)
        if (entity) {
          entity.position = entityState.position
          entity.rotation = entityState.rotation
          // Restore custom state
        }
      }

      // Restore metro states
      for (const [trainId, trainState] of save.metroStates) {
        const train = state.trains.get(trainId)
        if (train) {
          train.position = trainState.position
          train.scheduleIndex = trainState.scheduleIndex
          train.doorsOpen = trainState.doorsOpen
        }
      }

      state.timeOfDay = save.timeOfDay
      state.weather = save.weather
      state.weatherIntensity = save.weatherIntensity
    }),
  }))
)

// Selector hooks
export const useWorldChunks = () => useWorldStore((state) => state.chunks)
export const useLoadedChunks = () => useWorldStore((state) => state.loadedChunkIds)
export const useWorldEntities = () => useWorldStore((state) => state.entities)
export const useMetroStations = () => useWorldStore((state) => state.stations)
export const useMetroTrains = () => useWorldStore((state) => state.trains)
export const useMetroRoutes = () => useWorldStore((state) => state.routes)
export const useTimeOfDay = () => useWorldStore((state) => state.timeOfDay)
export const useWeather = () => useWorldStore((state) => ({ weather: state.weather, intensity: state.weatherIntensity }))
export const useWorldActions = () => useWorldStore((state) => ({
  registerChunk: state.registerChunk,
  loadChunk: state.loadChunk,
  unloadChunk: state.unloadChunk,
  updatePlayerChunk: state.updatePlayerChunk,
  registerEntity: state.registerEntity,
  unregisterEntity: state.unregisterEntity,
  updateEntity: state.updateEntity,
  registerStation: state.registerStation,
  registerTrain: state.registerTrain,
  registerRoute: state.registerRoute,
  updateTrain: state.updateTrain,
  getStation: state.getStation,
  getNearbyStations: state.getNearbyStations,
  getRoute: state.getRoute,
  unlockFastTravel: state.unlockFastTravel,
  isFastTravelUnlocked: state.isFastTravelUnlocked,
  setTimeOfDay: state.setTimeOfDay,
  setTimeScale: state.setTimeScale,
  setWeather: state.setWeather,
  getTimeOfDayLabel: state.getTimeOfDayLabel,
  getSunPosition: state.getSunPosition,
}))