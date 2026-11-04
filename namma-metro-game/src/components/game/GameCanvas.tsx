// Game Canvas Component
// Main Three.js canvas wrapper that initializes and runs the game loop

import { useEffect, useRef, useState } from 'react'
import { createSceneManager } from '@systems/core/SceneManager'
import { createInputManager } from '@systems/core/InputManager'
import { createAssetLoader } from '@systems/core/AssetLoader'
import { gameLoop } from '@systems/core/GameLoop'
import { useGameStore } from '@stores/gameStore'
import { usePlayerStore } from '@stores/playerStore'
import { useWorldStore } from '@stores/worldStore'
import { DebugPanel } from '@systems/core/DebugPanel'
import { useDebugPanel } from '@systems/core/DebugPanel'

export function GameCanvas() {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const [isInitialized, setIsInitialized] = useState(false)
  const { isOpen: debugOpen, toggle: toggleDebug } = useDebugPanel()

  const sceneManagerRef = useRef<ReturnType<typeof createSceneManager> | null>(null)
  const inputManagerRef = useRef<ReturnType<typeof createInputManager> | null>(null)
  const assetLoaderRef = useRef<ReturnType<typeof createAssetLoader> | null>(null)

  useEffect(() => {
    if (!canvasRef.current) return

    // Initialize core systems
    const sceneManager = createSceneManager({
      canvas: canvasRef.current!,
      antialias: true,
      powerPreference: 'high-performance',
    })
    sceneManagerRef.current = sceneManager

    const inputManager = createInputManager({
      canvas: canvasRef.current!,
      enableGamepad: true,
      enableTouch: true,
    })
    inputManagerRef.current = inputManager

    const assetLoader = createAssetLoader()
    assetLoaderRef.current = assetLoader

    // Set up game loop callbacks
    const cleanupUpdate = gameLoop.onUpdate((deltaTime, fixedDeltaTime) => {
      updateGame(deltaTime, fixedDeltaTime)
    })

    const cleanupRender = gameLoop.onRender((alpha) => {
      renderGame(alpha)
    })

    // Initialize game store
    useGameStore.getState().initialize()

    // Start game loop
    gameLoop.start()
    setIsInitialized(true)

    // Cleanup
    return () => {
      cleanupUpdate()
      cleanupRender()
      gameLoop.stop()

      inputManager.dispose()
      assetLoader.dispose()
      sceneManager.dispose()

      sceneManagerRef.current = null
      inputManagerRef.current = null
      assetLoaderRef.current = null
    }
  }, [])

  const updateGame = (deltaTime: number, fixedDeltaTime: number) => {
    const gameState = useGameStore.getState().state
    if (gameState !== 'playing') return

    const inputManager = inputManagerRef.current
    const playerStore = usePlayerStore.getState()
    const worldStore = useWorldStore.getState()

    if (!inputManager) return

    // Get input state
    const input = inputManager.getInputState()

    // Update player position based on input (simplified for now)
    // In a real implementation, this would use Rapier3D physics
    const speed = input.sprint ? 8 : 4
    const moveDir = { x: 0, z: 0 }

    if (input.moveForward) moveDir.z -= 1
    if (input.moveBackward) moveDir.z += 1
    if (input.moveLeft) moveDir.x -= 1
    if (input.moveRight) moveDir.x += 1

    // Normalize diagonal movement
    const len = Math.sqrt(moveDir.x ** 2 + moveDir.z ** 2)
    if (len > 0) {
      moveDir.x /= len
      moveDir.z /= len
    }

    // Apply movement (simplified - no physics yet)
    const newPosition = {
      x: playerStore.position.x + moveDir.x * speed * (fixedDeltaTime / 1000),
      y: playerStore.position.y,
      z: playerStore.position.z + moveDir.z * speed * (fixedDeltaTime / 1000),
    }

    playerStore.setPosition(newPosition)
    playerStore.setMovementState(
      len > 0 ? (input.sprint ? 'sprint' : 'run') : 'idle'
    )

    // Update world streaming based on player position
    worldStore.updatePlayerChunk({ x: newPosition.x, y: newPosition.z })

    // Update time of day
    const timeScale = worldStore.timeScale
    worldStore.setTimeOfDay(worldStore.timeOfDay + (fixedDeltaTime / 1000 / 60) * timeScale) // 1 hour per minute at 1x

    // Handle camera rotation from mouse
    if (input.mouseDelta.x !== 0 || input.mouseDelta.y !== 0) {
      // Camera rotation handled by CameraController (to be implemented)
    }
  }

  const renderGame = (alpha: number) => {
    const sceneManager = sceneManagerRef.current
    if (!sceneManager) return

    // Apply graphics settings on first render
    if (!sceneManager.renderer.info.autoReset) {
      sceneManager.applyGraphicsSettings()
    }

    sceneManager.render()
  }

  return (
    <div className="relative w-full h-full">
      <canvas
        ref={canvasRef}
        id="game-canvas"
        className="w-full h-full block"
        tabIndex={0}
      />
      {isInitialized && <DebugPanel isOpen={debugOpen} onToggle={toggleDebug} />}
    </div>
  )
}