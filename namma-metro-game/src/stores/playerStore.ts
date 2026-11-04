import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import type {
  PlayerState,
  MovementState,
  CameraMode,
  MetroCardState,
  InventoryItem,
  Vector3,
  Quaternion,
} from '@types'

interface PlayerStore extends PlayerState {
  // Actions
  setPosition: (position: Vector3) => void
  setRotation: (rotation: Quaternion) => void
  setVelocity: (velocity: Vector3) => void
  setMovementState: (state: MovementState) => void
  setCameraMode: (mode: CameraMode) => void
  setGrounded: (grounded: boolean) => void
  setInWater: (inWater: boolean) => void
  setOnLadder: (onLadder: boolean) => void
  setCurrentMetroTrain: (trainId: string | null) => void
  updateMetroCard: (updates: Partial<MetroCardState>) => void
  addInventoryItem: (item: InventoryItem) => void
  removeInventoryItem: (itemId: string, quantity?: number) => void
  updateInventoryItem: (itemId: string, updates: Partial<InventoryItem>) => void
  setCurrentQuest: (questId: string | null) => void
  consumeStamina: (amount: number) => void
  restoreStamina: (amount: number) => void
  takeDamage: (amount: number) => void
  heal: (amount: number) => void
  reset: () => void
  loadFromSave: (save: PlayerState) => void
}

const initialMetroCard: MetroCardState = {
  level: 1,
  balance: 50,
  tapSpeedBonus: 0,
  fareDiscount: 0,
  loreUnlocked: [],
}

const initialState: PlayerState = {
  position: { x: 0, y: 1.7, z: 0 },
  rotation: { x: 0, y: 0, z: 0, w: 1 },
  velocity: { x: 0, y: 0, z: 0 },
  movementState: 'idle',
  cameraMode: 'third-person',
  health: 100,
  stamina: 100,
  maxStamina: 100,
  isGrounded: true,
  isInWater: false,
  isOnLadder: false,
  currentMetroTrain: null,
  metroCard: initialMetroCard,
  inventory: [],
  currentQuest: null,
}

export const usePlayerStore = create<PlayerStore>()(
  immer((set, get) => ({
    ...initialState,

    setPosition: (position: Vector3) => set((state) => { state.position = position }),
    setRotation: (rotation: Quaternion) => set((state) => { state.rotation = rotation }),
    setVelocity: (velocity: Vector3) => set((state) => { state.velocity = velocity }),
    setMovementState: (movementState: MovementState) => set((state) => { state.movementState = movementState }),
    setCameraMode: (cameraMode: CameraMode) => set((state) => { state.cameraMode = cameraMode }),
    setGrounded: (isGrounded: boolean) => set((state) => { state.isGrounded = isGrounded }),
    setInWater: (isInWater: boolean) => set((state) => { state.isInWater = isInWater }),
    setOnLadder: (isOnLadder: boolean) => set((state) => { state.isOnLadder = isOnLadder }),
    setCurrentMetroTrain: (currentMetroTrain: string | null) => set((state) => { state.currentMetroTrain = currentMetroTrain }),

    updateMetroCard: (updates: Partial<MetroCardState>) => set((state) => {
      Object.assign(state.metroCard, updates)
    }),

    addInventoryItem: (item: InventoryItem) => set((state) => {
      const existing = state.inventory.find(i => i.id === item.id)
      if (existing) {
        existing.quantity += item.quantity
      } else {
        state.inventory.push(item)
      }
    }),

    removeInventoryItem: (itemId: string, quantity = 1) => set((state) => {
      const index = state.inventory.findIndex(i => i.id === itemId)
      if (index !== -1) {
        state.inventory[index].quantity -= quantity
        if (state.inventory[index].quantity <= 0) {
          state.inventory.splice(index, 1)
        }
      }
    }),

    updateInventoryItem: (itemId: string, updates: Partial<InventoryItem>) => set((state) => {
      const item = state.inventory.find(i => i.id === itemId)
      if (item) {
        Object.assign(item, updates)
      }
    }),

    setCurrentQuest: (currentQuest: string | null) => set((state) => { state.currentQuest = currentQuest }),

    consumeStamina: (amount: number) => set((state) => {
      state.stamina = Math.max(0, state.stamina - amount)
    }),

    restoreStamina: (amount: number) => set((state) => {
      state.stamina = Math.min(state.maxStamina, state.stamina + amount)
    }),

    takeDamage: (amount: number) => set((state) => {
      state.health = Math.max(0, state.health - amount)
    }),

    heal: (amount: number) => set((state) => {
      state.health = Math.min(100, state.health + amount)
    }),

    reset: () => set((state) => {
      Object.assign(state, initialState)
    }),

    loadFromSave: (save: PlayerState) => set((_state) => {
      Object.assign(_state, save)
    }),
  }))
)

// Selector hooks
export const usePlayerPosition = () => usePlayerStore((state) => state.position)
export const usePlayerRotation = () => usePlayerStore((state) => state.rotation)
export const usePlayerMovementState = () => usePlayerStore((state) => state.movementState)
export const usePlayerCameraMode = () => usePlayerStore((state) => state.cameraMode)
export const usePlayerHealth = () => usePlayerStore((state) => state.health)
export const usePlayerStamina = () => usePlayerStore((state) => state.stamina)
export const usePlayerMetroCard = () => usePlayerStore((state) => state.metroCard)
export const usePlayerInventory = () => usePlayerStore((state) => state.inventory)
export const usePlayerCurrentQuest = () => usePlayerStore((state) => state.currentQuest)
export const usePlayerActions = () => usePlayerStore((state) => ({
  setPosition: state.setPosition,
  setRotation: state.setRotation,
  setVelocity: state.setVelocity,
  setMovementState: state.setMovementState,
  setCameraMode: state.setCameraMode,
  setGrounded: state.setGrounded,
  setInWater: state.setInWater,
  setOnLadder: state.setOnLadder,
  setCurrentMetroTrain: state.setCurrentMetroTrain,
  updateMetroCard: state.updateMetroCard,
  addInventoryItem: state.addInventoryItem,
  removeInventoryItem: state.removeInventoryItem,
  updateInventoryItem: state.updateInventoryItem,
  setCurrentQuest: state.setCurrentQuest,
  consumeStamina: state.consumeStamina,
  restoreStamina: state.restoreStamina,
  takeDamage: state.takeDamage,
  heal: state.heal,
}))