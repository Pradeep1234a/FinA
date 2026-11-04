// Fixed timestep game loop with interpolation
// Based on "Fix Your Timestep" by Glenn Fiedler

import type { GameEvent, GameEventType } from '@types'
import { useGameStore } from '@stores/gameStore'

export interface GameLoopConfig {
  fixedTimestep: number // ms (default: 1000/60 = 16.666ms)
  maxSubSteps: number
  interpolation: boolean
}

const DEFAULT_CONFIG: GameLoopConfig = {
  fixedTimestep: 1000 / 60, // 60 Hz
  maxSubSteps: 5,
  interpolation: true,
}

type UpdateCallback = (deltaTime: number, fixedDeltaTime: number) => void
type RenderCallback = (interpolationAlpha: number) => void

export class GameLoop {
  private config: GameLoopConfig
  private updateCallbacks: Set<UpdateCallback> = new Set()
  private renderCallbacks: Set<RenderCallback> = new Set()
  private isRunning = false
  private lastTime = 0
  private accumulator = 0
  private frameId: number | null = null
  private frameCount = 0
  private fps = 0
  private lastFpsUpdate = 0

  constructor(config: Partial<GameLoopConfig> = {}) {
    this.config = { ...DEFAULT_CONFIG, ...config }
  }

  // Register update callback (runs at fixed timestep)
  onUpdate(callback: UpdateCallback): () => void {
    this.updateCallbacks.add(callback)
    return () => this.updateCallbacks.delete(callback)
  }

  // Register render callback (runs every frame with interpolation)
  onRender(callback: RenderCallback): () => void {
    this.renderCallbacks.add(callback)
    return () => this.renderCallbacks.delete(callback)
  }

  start(): void {
    if (this.isRunning) return
    this.isRunning = true
    this.lastTime = performance.now()
    this.accumulator = 0
    this.tick()
  }

  stop(): void {
    this.isRunning = false
    if (this.frameId !== null) {
      cancelAnimationFrame(this.frameId)
      this.frameId = null
    }
  }

  private tick = (): void => {
    if (!this.isRunning) return

    const currentTime = performance.now()
    const frameDelta = currentTime - this.lastTime
    this.lastTime = currentTime

    // Clamp frame delta to prevent spiral of death
    const clampedDelta = Math.min(frameDelta, 250)

    this.accumulator += clampedDelta

    // Fixed timestep updates
    let subSteps = 0
    while (this.accumulator >= this.config.fixedTimestep && subSteps < this.config.maxSubSteps) {
      this.fixedUpdate(this.config.fixedTimestep)
      this.accumulator -= this.config.fixedTimestep
      subSteps++
    }

    // Calculate interpolation alpha
    const alpha = this.config.interpolation
      ? this.accumulator / this.config.fixedTimestep
      : 1

    // Render with interpolation
    this.render(alpha)

    // FPS calculation
    this.frameCount++
    if (currentTime - this.lastFpsUpdate >= 1000) {
      this.fps = this.frameCount
      this.frameCount = 0
      this.lastFpsUpdate = currentTime
    }

    this.frameId = requestAnimationFrame(this.tick)
  }

  private fixedUpdate(deltaTime: number): void {
    const fixedDeltaTime = this.config.fixedTimestep
    for (const callback of this.updateCallbacks) {
      try {
        callback(deltaTime, fixedDeltaTime)
      } catch (error) {
        console.error('Error in fixed update:', error)
      }
    }

    // Emit game tick event
    useGameStore.getState().emit('game:tick', { deltaTime, fixedDeltaTime })
  }

  private render(alpha: number): void {
    for (const callback of this.renderCallbacks) {
      try {
        callback(alpha)
      } catch (error) {
        console.error('Error in render:', error)
      }
    }

    // Emit render event
    useGameStore.getState().emit('game:render', { alpha, fps: this.fps })
  }

  getFPS(): number {
    return this.fps
  }

  getConfig(): GameLoopConfig {
    return { ...this.config }
  }

  setConfig(config: Partial<GameLoopConfig>): void {
    this.config = { ...this.config, ...config }
  }

  isActive(): boolean {
    return this.isRunning
  }
}

// Singleton instance
export const gameLoop = new GameLoop()

// React hook for using the game loop
export function useGameLoop() {
  return {
    onUpdate: gameLoop.onUpdate.bind(gameLoop),
    onRender: gameLoop.onRender.bind(gameLoop),
    start: gameLoop.start.bind(gameLoop),
    stop: gameLoop.stop.bind(gameLoop),
    getFPS: gameLoop.getFPS.bind(gameLoop),
  }
}