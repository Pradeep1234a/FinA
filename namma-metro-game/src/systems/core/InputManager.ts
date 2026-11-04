// Input Manager
// Handles keyboard, mouse, gamepad, and touch input

import type { InputState, Vector2, TouchState, TouchPoint, GestureState } from '@types'
import { useGameStore } from '@stores/gameStore'
import { usePlayerStore } from '@stores/playerStore'

export interface InputManagerConfig {
  canvas: HTMLCanvasElement
  enableGamepad?: boolean
  enableTouch?: boolean
}

const DEFAULT_KEYBINDINGS: Record<string, string> = {
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
}

export class InputManager {
  private config: InputManagerConfig
  private canvas: HTMLCanvasElement
  private keybindings: Record<string, string>
  private keyState: Map<string, boolean> = new Map()
  private mousePosition: Vector2 = { x: 0, y: 0 }
  private mouseDelta: Vector2 = { x: 0, y: 0 }
  private lastMousePosition: Vector2 = { x: 0, y: 0 }
  private wheelDelta = 0
  private isPointerLocked = false
  private touchState: TouchState | null = null
  private gamepads: Map<number, Gamepad> = new Map()
  private gamepadPollInterval: number | null = null

  // Input state for systems to read
  public inputState: InputState = {
    moveForward: false,
    moveBackward: false,
    moveLeft: false,
    moveRight: false,
    jump: false,
    sprint: false,
    crouch: false,
    interact: false,
    cameraToggle: false,
    mapToggle: false,
    inventoryToggle: false,
    pause: false,
    mouseDelta: { x: 0, y: 0 },
    mousePosition: { x: 0, y: 0 },
    wheelDelta: 0,
    touch: null,
  }

  // Callbacks
  private onInteractCallbacks: Set<() => void> = new Set()
  private onPauseCallbacks: Set<() => void> = new Set()
  private onCameraToggleCallbacks: Set<() => void> = new Set()
  private onMapToggleCallbacks: Set<() => void> = new Set()
  private onInventoryToggleCallbacks: Set<() => void> = new Set()
  private onPhotoModeCallbacks: Set<() => void> = new Set()

  constructor(config: InputManagerConfig) {
    this.config = {
      enableGamepad: true,
      enableTouch: true,
      ...config,
    }
    this.canvas = config.canvas
    this.keybindings = { ...DEFAULT_KEYBINDINGS }
    this.setupEventListeners()
    if (this.config.enableGamepad) this.setupGamepad()
  }

  private setupEventListeners(): void {
    // Keyboard
    window.addEventListener('keydown', this.handleKeyDown.bind(this))
    window.addEventListener('keyup', this.handleKeyUp.bind(this))

    // Mouse
    this.canvas.addEventListener('mousemove', this.handleMouseMove.bind(this))
    this.canvas.addEventListener('mousedown', this.handleMouseDown.bind(this))
    this.canvas.addEventListener('mouseup', this.handleMouseUp.bind(this))
    this.canvas.addEventListener('wheel', this.handleWheel.bind(this), { passive: false })
    this.canvas.addEventListener('contextmenu', e => e.preventDefault())

    // Pointer lock
    this.canvas.addEventListener('click', this.requestPointerLock.bind(this))

    // Touch
    if (this.config.enableTouch) {
      this.canvas.addEventListener('touchstart', this.handleTouchStart.bind(this), { passive: false })
      this.canvas.addEventListener('touchmove', this.handleTouchMove.bind(this), { passive: false })
      this.canvas.addEventListener('touchend', this.handleTouchEnd.bind(this))
      this.canvas.addEventListener('touchcancel', this.handleTouchEnd.bind(this))
    }

    // Visibility change
    document.addEventListener('visibilitychange', this.handleVisibilityChange.bind(this))

    // Gamepad events
    window.addEventListener('gamepadconnected', this.handleGamepadConnected.bind(this))
    window.addEventListener('gamepaddisconnected', this.handleGamepadDisconnected.bind(this))
  }

  private setupGamepad(): void {
    this.gamepadPollInterval = window.setInterval(() => {
      this.pollGamepads()
    }, 16) // ~60Hz
  }

  private pollGamepads(): void {
    const gamepads = navigator.getGamepads()
    for (const gamepad of gamepads) {
      if (gamepad) {
        this.gamepads.set(gamepad.index, gamepad)
      } else {
        // Remove disconnected gamepads
        for (const [index, gp] of this.gamepads) {
          if (!navigator.getGamepads()[index]) {
            this.gamepads.delete(index)
          }
        }
      }
    }
    this.updateGamepadState()
  }

  private updateGamepadState(): void {
    // Use first connected gamepad
    const gamepad = this.gamepads.values().next().value
    if (!gamepad) return

    const settings = useGameStore.getState().settings.controls
    const deadzone = 0.1
    const sensitivity = settings.gamepadSensitivity

    // Left stick - movement
    const lx = gamepad.axes[0] || 0
    const ly = gamepad.axes[1] || 0

    this.inputState.moveLeft = lx < -deadzone
    this.inputState.moveRight = lx > deadzone
    this.inputState.moveForward = ly < -deadzone
    this.inputState.moveBackward = ly > deadzone

    // Right stick - camera
    const rx = gamepad.axes[2] || 0
    const ry = gamepad.axes[3] || 0

    if (Math.abs(rx) > deadzone || Math.abs(ry) > deadzone) {
      this.mouseDelta.x += rx * sensitivity * 0.1
      this.mouseDelta.y += ry * sensitivity * 0.1
    }

    // Buttons
    const buttons = gamepad.buttons
    this.inputState.jump = buttons[0]?.pressed || false // A
    this.inputState.interact = buttons[1]?.pressed || false // B
    this.inputState.sprint = buttons[5]?.pressed || false // RB
    this.inputState.crouch = buttons[4]?.pressed || false // LB
    this.inputState.cameraToggle = buttons[3]?.pressed || false // Y
    this.inputState.mapToggle = buttons[2]?.pressed || false // X
    this.inputState.pause = buttons[9]?.pressed || false // Start
  }

  // Keyboard handlers
  private handleKeyDown(event: KeyboardEvent): void {
    // Ignore if typing in input field
    if (this.isTyping(event)) return

    const code = event.code
    this.keyState.set(code, true)
    this.updateInputState(code, true)

    // Prevent default for game keys
    if (Object.values(this.keybindings).includes(code)) {
      event.preventDefault()
    }
  }

  private handleKeyUp(event: KeyboardEvent): void {
    const code = event.code
    this.keyState.set(code, false)
    this.updateInputState(code, false)
  }

  private isTyping(event: KeyboardEvent): boolean {
    const target = event.target as HTMLElement
    return target.tagName === 'INPUT' ||
           target.tagName === 'TEXTAREA' ||
           target.isContentEditable
  }

  private updateInputState(code: string, pressed: boolean): void {
    const settings = useGameStore.getState().settings.controls.keybindings

    // Find action for this key
    for (const [action, key] of Object.entries(settings)) {
      if (key === code) {
        switch (action) {
          case 'moveForward': this.inputState.moveForward = pressed; break
          case 'moveBackward': this.inputState.moveBackward = pressed; break
          case 'moveLeft': this.inputState.moveLeft = pressed; break
          case 'moveRight': this.inputState.moveRight = pressed; break
          case 'jump': this.inputState.jump = pressed; break
          case 'sprint': this.inputState.sprint = pressed; break
          case 'crouch': this.inputState.crouch = pressed; break
          case 'interact':
            this.inputState.interact = pressed
            if (pressed) this.onInteractCallbacks.forEach(cb => cb())
            break
          case 'cameraToggle':
            if (pressed) this.onCameraToggleCallbacks.forEach(cb => cb())
            break
          case 'mapToggle':
            if (pressed) this.onMapToggleCallbacks.forEach(cb => cb())
            break
          case 'inventoryToggle':
            if (pressed) this.onInventoryToggleCallbacks.forEach(cb => cb())
            break
          case 'pause':
            if (pressed) this.onPauseCallbacks.forEach(cb => cb())
            break
          case 'photoMode':
            if (pressed) this.onPhotoModeCallbacks.forEach(cb => cb())
            break
        }
      }
    }
  }

  // Mouse handlers
  private handleMouseMove(event: MouseEvent): void {
    this.mousePosition.x = event.clientX
    this.mousePosition.y = event.clientY

    if (this.isPointerLocked) {
      this.mouseDelta.x = event.movementX
      this.mouseDelta.y = event.movementY
    } else {
      this.mouseDelta.x = this.mousePosition.x - this.lastMousePosition.x
      this.mouseDelta.y = this.mousePosition.y - this.lastMousePosition.y
    }

    this.lastMousePosition.x = this.mousePosition.x
    this.lastMousePosition.y = this.mousePosition.y

    this.inputState.mousePosition = { ...this.mousePosition }
    this.inputState.mouseDelta = { ...this.mouseDelta }
  }

  private handleMouseDown(event: MouseEvent): void {
    if (event.button === 0 && !this.isPointerLocked) {
      this.requestPointerLock()
    }
  }

  private handleMouseUp(_event: MouseEvent): void {
    // Handle mouse up if needed
  }

  private handleWheel(event: WheelEvent): void {
    event.preventDefault()
    this.wheelDelta = event.deltaY > 0 ? 1 : -1
    this.inputState.wheelDelta = this.wheelDelta
  }

  private requestPointerLock(): void {
    if (!this.isPointerLocked) {
      this.canvas.requestPointerLock()
    }
  }

  private handlePointerLockChange = (): void => {
    this.isPointerLocked = document.pointerLockElement === this.canvas
    if (!this.isPointerLocked) {
      this.mouseDelta = { x: 0, y: 0 }
    }
  }

  // Touch handlers
  private handleTouchStart(event: TouchEvent): void {
    event.preventDefault()
    const touches: TouchPoint[] = []

    for (let i = 0; i < event.changedTouches.length; i++) {
      const touch = event.changedTouches[i]
      touches.push({
        id: touch.identifier,
        x: touch.clientX,
        y: touch.clientY,
        force: touch.force || 1,
        startX: touch.clientX,
        startY: touch.clientY,
        startTime: Date.now(),
      })
    }

    this.updateTouchState(touches, 'start')
  }

  private handleTouchMove(event: TouchEvent): void {
    event.preventDefault()
    const touches: TouchPoint[] = []

    for (let i = 0; i < event.changedTouches.length; i++) {
      const touch = event.changedTouches[i]
      const existing = this.touchState?.touches.find(t => t.id === touch.identifier)
      touches.push({
        id: touch.identifier,
        x: touch.clientX,
        y: touch.clientY,
        force: touch.force || 1,
        startX: existing?.startX || touch.clientX,
        startY: existing?.startY || touch.clientY,
        startTime: existing?.startTime || Date.now(),
      })
    }

    this.updateTouchState(touches, 'move')
  }

  private handleTouchEnd(event: TouchEvent): void {
    event.preventDefault()
    const endedIds = Array.from(event.changedTouches).map(t => t.identifier)

    if (this.touchState) {
      this.touchState.touches = this.touchState.touches.filter(t => !endedIds.includes(t.id))
      this.detectGestures()
    }
  }

  private updateTouchState(newTouches: TouchPoint[], phase: 'start' | 'move'): void {
    if (!this.touchState) {
      this.touchState = { touches: [], gestures: [] }
    }

    for (const newTouch of newTouches) {
      const index = this.touchState.touches.findIndex(t => t.id === newTouch.id)
      if (index >= 0) {
        this.touchState.touches[index] = newTouch
      } else {
        this.touchState.touches.push(newTouch)
      }
    }

    this.inputState.touch = this.touchState
  }

  private detectGestures(): void {
    if (!this.touchState || this.touchState.touches.length === 0) return

    const touch = this.touchState.touches[0]
    const dx = touch.x - touch.startX
    const dy = touch.y - touch.startY
    const distance = Math.sqrt(dx * dx + dy * dy)
    const duration = Date.now() - touch.startTime

    // Tap detection
    if (distance < 10 && duration < 200) {
      this.touchState.gestures.push({
        type: 'tap',
        startTime: touch.startTime,
        center: { x: touch.x, y: touch.y },
        scale: 1,
        rotation: 0,
      })
      this.onInteractCallbacks.forEach(cb => cb())
    }

    // Pan detection
    if (distance > 10) {
      this.touchState.gestures.push({
        type: 'pan',
        startTime: touch.startTime,
        center: { x: touch.x, y: touch.y },
        scale: 1,
        rotation: 0,
      })
      this.mouseDelta.x = dx * 0.5
      this.mouseDelta.y = dy * 0.5
    }

    // Pinch detection (two fingers)
    if (this.touchState.touches.length >= 2) {
      const t1 = this.touchState.touches[0]
      const t2 = this.touchState.touches[1]
      const centerX = (t1.x + t2.x) / 2
      const centerY = (t1.y + t2.y) / 2
      const currentDist = Math.sqrt((t1.x - t2.x) ** 2 + (t1.y - t2.y) ** 2)
      const startDist = Math.sqrt((t1.startX - t2.startX) ** 2 + (t1.startY - t2.startY) ** 2)

      if (startDist > 0) {
        this.touchState.gestures.push({
          type: 'pinch',
          startTime: Math.min(t1.startTime, t2.startTime),
          center: { x: centerX, y: centerY },
          scale: currentDist / startDist,
          rotation: 0,
        })
      }
    }
  }

  // Gamepad events
  private handleGamepadConnected(event: GamepadEvent): void {
    console.log('Gamepad connected:', event.gamepad.id)
    this.gamepads.set(event.gamepad.index, event.gamepad)
  }

  private handleGamepadDisconnected(event: GamepadEvent): void {
    console.log('Gamepad disconnected:', event.gamepad.id)
    this.gamepads.delete(event.gamepad.index)
  }

  private handleVisibilityChange(): void {
    if (document.hidden) {
      // Release all keys when tab loses focus
      this.keyState.clear()
      this.resetInputState()
    }
  }

  // Public API

  getInputState(): InputState {
    // Reset mouse delta and wheel delta each frame
    const state = { ...this.inputState }
    this.mouseDelta = { x: 0, y: 0 }
    this.wheelDelta = 0
    this.inputState.mouseDelta = { x: 0, y: 0 }
    this.inputState.wheelDelta = 0
    return state
  }

  isKeyPressed(code: string): boolean {
    return this.keyState.get(code) === true
  }

  isActionPressed(action: keyof typeof DEFAULT_KEYBINDINGS): boolean {
    const key = this.keybindings[action]
    return this.keyState.get(key) === true
  }

  setKeybinding(action: string, key: string): void {
    this.keybindings[action] = key
  }

  getKeybinding(action: string): string {
    return this.keybindings[action] || ''
  }

  resetInputState(): void {
    this.inputState = {
      moveForward: false,
      moveBackward: false,
      moveLeft: false,
      moveRight: false,
      jump: false,
      sprint: false,
      crouch: false,
      interact: false,
      cameraToggle: false,
      mapToggle: false,
      inventoryToggle: false,
      pause: false,
      mouseDelta: { x: 0, y: 0 },
      mousePosition: { ...this.mousePosition },
      wheelDelta: 0,
      touch: this.touchState,
    }
  }

  // Callback registration
  onInteract(callback: () => void): () => void {
    this.onInteractCallbacks.add(callback)
    return () => this.onInteractCallbacks.delete(callback)
  }

  onPause(callback: () => void): () => void {
    this.onPauseCallbacks.add(callback)
    return () => this.onPauseCallbacks.delete(callback)
  }

  onCameraToggle(callback: () => void): () => void {
    this.onCameraToggleCallbacks.add(callback)
    return () => this.onCameraToggleCallbacks.delete(callback)
  }

  onMapToggle(callback: () => void): () => void {
    this.onMapToggleCallbacks.add(callback)
    return () => this.onMapToggleCallbacks.delete(callback)
  }

  onInventoryToggle(callback: () => void): () => void {
    this.onInventoryToggleCallbacks.add(callback)
    return () => this.onInventoryToggleCallbacks.delete(callback)
  }

  onPhotoMode(callback: () => void): () => void {
    this.onPhotoModeCallbacks.add(callback)
    return () => this.onPhotoModeCallbacks.delete(callback)
  }

  // Pointer lock
  isPointerLockedEnabled(): boolean {
    return this.isPointerLocked
  }

  exitPointerLock(): void {
    if (this.isPointerLocked) {
      document.exitPointerLock()
    }
  }

  // Cleanup
  dispose(): void {
    window.removeEventListener('keydown', this.handleKeyDown.bind(this))
    window.removeEventListener('keyup', this.handleKeyUp.bind(this))
    this.canvas.removeEventListener('mousemove', this.handleMouseMove.bind(this))
    this.canvas.removeEventListener('mousedown', this.handleMouseDown.bind(this))
    this.canvas.removeEventListener('mouseup', this.handleMouseUp.bind(this))
    this.canvas.removeEventListener('wheel', this.handleWheel.bind(this))
    this.canvas.removeEventListener('click', this.requestPointerLock.bind(this))
    document.removeEventListener('visibilitychange', this.handleVisibilityChange.bind(this))
    window.removeEventListener('gamepadconnected', this.handleGamepadConnected.bind(this))
    window.removeEventListener('gamepaddisconnected', this.handleGamepadDisconnected.bind(this))
    document.removeEventListener('pointerlockchange', this.handlePointerLockChange)

    if (this.config.enableTouch) {
      this.canvas.removeEventListener('touchstart', this.handleTouchStart.bind(this))
      this.canvas.removeEventListener('touchmove', this.handleTouchMove.bind(this))
      this.canvas.removeEventListener('touchend', this.handleTouchEnd.bind(this))
      this.canvas.removeEventListener('touchcancel', this.handleTouchEnd.bind(this))
    }

    if (this.gamepadPollInterval) {
      clearInterval(this.gamepadPollInterval)
    }
  }
}

// Singleton
let inputManagerInstance: InputManager | null = null

export function createInputManager(config: InputManagerConfig): InputManager {
  if (inputManagerInstance) {
    inputManagerInstance.dispose()
  }
  inputManagerInstance = new InputManager(config)
  return inputManagerInstance
}

export function getInputManager(): InputManager | null {
  return inputManagerInstance
}