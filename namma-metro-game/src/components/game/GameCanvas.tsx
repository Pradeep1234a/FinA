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
  const [initError, setInitError] = useState<string | null>(null)
  const { isOpen: debugOpen, toggle: toggleDebug } = useDebugPanel()

  const sceneManagerRef = useRef<ReturnType<typeof createSceneManager> | null>(null)
  const inputManagerRef = useRef<ReturnType<typeof createInputManager> | null>(null)
  const assetLoaderRef = useRef<ReturnType<typeof createAssetLoader> | null>(null)

  useEffect(() => {
    if (!canvasRef.current) {
      console.error('❌ Canvas ref is null')
      setInitError('Canvas element not found')
      return
    }

    const canvas = canvasRef.current
    console.log('🎮 Canvas found:', canvas.width, canvas.height)

    // Check WebGL support
    try {
      const gl = canvas.getContext('webgl2') || canvas.getContext('webgl')
      if (!gl) {
        throw new Error('WebGL not supported')
      }
      console.log('✅ WebGL context created:', gl.getParameter(gl.VERSION))
    } catch (e) {
      console.error('❌ WebGL failed:', e)
      setInitError(`WebGL not supported: ${e}`)
      return
    }

    let sceneManager: ReturnType<typeof createSceneManager> | null = null
    let inputManager: ReturnType<typeof createInputManager> | null = null
    let assetLoader: ReturnType<typeof createAssetLoader> | null = null

    try {
      console.log('🔧 Initializing SceneManager...')
      sceneManager = createSceneManager({
        canvas,
        antialias: true,
        powerPreference: 'high-performance',
      })
      console.log('✅ SceneManager created')

      console.log('🔧 Initializing InputManager...')
      inputManager = createInputManager({
        canvas,
        enableGamepad: true,
        enableTouch: true,
      })
      console.log('✅ InputManager created')

      console.log('🔧 Initializing AssetLoader...')
      assetLoader = createAssetLoader()
      console.log('✅ AssetLoader created')

      // Store refs
      sceneManagerRef.current = sceneManager
      inputManagerRef.current = inputManager
      assetLoaderRef.current = assetLoader

      // Set up game loop callbacks
      const cleanupUpdate = gameLoop.onUpdate((deltaTime, fixedDeltaTime) => {
        updateGame(deltaTime, fixedDeltaTime)
      })

      const cleanupRender = gameLoop.onRender((alpha: number) => {
        renderGame(alpha)
      })

      // Initialize game store
      console.log('🔧 Initializing GameStore...')
      useGameStore.getState().initialize()
      console.log('✅ GameStore initialized')

      // Start game loop
      console.log('🚀 Starting game loop...')
      gameLoop.start()
      setIsInitialized(true)
      console.log('✅ Game initialized successfully!')

    } catch (error) {
      console.error('❌ Initialization failed:', error)
      setInitError(`Initialization failed: ${error instanceof Error ? error.message : String(error)}`)
      return
    }

    // Cleanup
    return () => {
      console.log('🧹 Cleaning up...')
      if (sceneManagerRef.current) sceneManagerRef.current.dispose()
      if (inputManagerRef.current) inputManagerRef.current.dispose()
      if (assetLoaderRef.current) assetLoaderRef.current.dispose()
      gameLoop.stop()
      sceneManagerRef.current = null
      inputManagerRef.current = null
      assetLoaderRef.current = null
    }
  }, [])

  const updateGame = (deltaTime: number, fixedDeltaTime: number) => {
    try {
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
    } catch (error) {
      console.error('❌ Update error:', error)
    }
  }

  const renderGame = (alpha: number) => {
    try {
      const sceneManager = sceneManagerRef.current
      if (!sceneManager) return

      // Apply graphics settings on first render
      if (!sceneManager.renderer.info.autoReset) {
        sceneManager.applyGraphicsSettings()
      }

      sceneManager.render()
    } catch (error) {
      console.error('❌ Render error:', error)
    }
  }

  // Show error overlay if initialization failed
  if (initError) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-ui-bg text-ui-text p-4">
        <div className="bg-ui-panel border border-ui-danger rounded-xl p-6 max-w-md text-center">
          <h2 className="text-xl font-bold text-ui-danger mb-2">Initialization Error</h2>
          <pre className="text-sm text-left bg-ui-bg p-4 rounded overflow-auto whitespace-pre-wrap text-ui-text">{initError}</pre>
          <button className="mt-4 px-4 py-2 bg-metro-purple rounded-lg" onClick={() => window.location.reload()}>
            Reload
          </button>
        </div>
      </div>
    )
  }

  if (!isInitialized) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-ui-bg text-ui-text">
        <div className="animate-spin w-12 h-12 border-4 border-metro-purple border-t-transparent rounded-full"></div>
      </div>
    )
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