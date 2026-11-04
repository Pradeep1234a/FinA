// Movement State Machine
// Manages player movement states and transitions

import { usePlayerStore } from '@stores/playerStore'
import { useGameStore } from '@stores/gameStore'
import type { MovementState, Vector3 } from '@types'

export interface MovementStateConfig {
  // Speed multipliers
  walkSpeed: number
  runSpeed: number
  sprintSpeed: number
  crouchSpeed: number
  swimSpeed: number
  climbSpeed: number
  // Acceleration
  acceleration: number
  airAcceleration: number
  deceleration: number
  // Stamina
  sprintStaminaCost: number
  staminaRegenRate: number
  staminaRegenDelay: number
  // Jump
  jumpForce: number
  coyoteTime: number
  jumpBufferTime: number
  // Crouch
  crouchHeight: number
  standHeight: number
  crouchTransitionTime: number
  // Swimming
  waterDetectionHeight: number
  swimBuoyancy: number
  // Climbing
  climbDetectionDistance: number
  climbCheckInterval: number
}

const DEFAULT_CONFIG: MovementStateConfig = {
  walkSpeed: 4,
  runSpeed: 6,
  sprintSpeed: 10,
  crouchSpeed: 2,
  swimSpeed: 3,
  climbSpeed: 2,
  acceleration: 15,
  airAcceleration: 5,
  deceleration: 20,
  sprintStaminaCost: 15,
  staminaRegenRate: 10,
  staminaRegenDelay: 1,
  jumpForce: 7,
  coyoteTime: 0.1,
  jumpBufferTime: 0.1,
  crouchHeight: 1.0,
  standHeight: 1.7,
  crouchTransitionTime: 0.2,
  waterDetectionHeight: 0.5,
  swimBuoyancy: 0.5,
  climbDetectionDistance: 1.5,
  climbCheckInterval: 0.5,
}

export type MovementStateType =
  | 'idle'
  | 'walk'
  | 'run'
  | 'sprint'
  | 'crouch'
  | 'jump'
  | 'fall'
  | 'climb'
  | 'swim'
  | 'ride'

export interface StateTransition {
  from: MovementStateType
  to: MovementStateType
  condition: (context: MovementContext) => boolean
  priority: number
}

export interface MovementContext {
  // Input
  moveForward: boolean
  moveBackward: boolean
  moveLeft: boolean
  moveRight: boolean
  jump: boolean
  sprint: boolean
  crouch: boolean
  // Physics
  isGrounded: boolean
  isInWater: boolean
  isOnLadder: boolean
  velocity: { x: number; y: number; z: number }
  // State
  stamina: number
  maxStamina: number
  movementState: MovementStateType
  // Time
  deltaTime: number
  timeSinceGrounded: number
  timeSinceJumpPressed: number
  // Config
  config: MovementStateConfig
}

export class MovementStateMachine {
  private config: MovementStateConfig
  private currentState: MovementStateType = 'idle'
  private previousState: MovementStateType = 'idle'
  private stateTime = 0

  // Transition tracking
  private coyoteTimer = 0
  private jumpBufferTimer = 0
  private staminaRegenTimer = 0
  private crouchTransitionTimer = 0
  private climbCheckTimer = 0

  // State enter/exit callbacks
  private onStateEnter: Map<MovementStateType, (context: MovementContext) => void> = new Map()
  private onStateExit: Map<MovementStateType, (context: MovementContext) => void> = new Map()
  private onStateUpdate: Map<MovementStateType, (context: MovementContext) => void> = new Map()

  // Transitions (ordered by priority)
  private transitions: StateTransition[] = []

  constructor(config: Partial<MovementStateConfig> = {}) {
    this.config = { ...DEFAULT_CONFIG, ...config }
    this.setupDefaultTransitions()
    this.setupDefaultCallbacks()
  }

  private setupDefaultTransitions(): void {
    // Priority: higher = checked first
    this.transitions = [
      // Death/knockback (highest priority)
      // Not implemented yet

      // Water transitions
      { from: '*', to: 'swim', condition: ctx => ctx.isInWater && !ctx.isGrounded, priority: 100 },
      { from: 'swim', to: 'idle', condition: ctx => !ctx.isInWater && ctx.isGrounded, priority: 90 },
      { from: 'swim', to: 'jump', condition: ctx => ctx.isInWater && ctx.jump && ctx.isGrounded, priority: 90 },

      // Climbing transitions
      { from: '*', to: 'climb', condition: ctx => ctx.isOnLadder && (ctx.moveForward || ctx.moveBackward), priority: 80 },
      { from: 'climb', to: 'idle', condition: ctx => !ctx.isOnLadder, priority: 70 },

      // Crouch transitions
      { from: '*', to: 'crouch', condition: ctx => ctx.crouch && ctx.isGrounded, priority: 60 },
      { from: 'crouch', to: 'idle', condition: ctx => !ctx.crouch, priority: 50 },

      // Jump/fall transitions
      { from: '*', to: 'jump', condition: ctx => ctx.jump && (ctx.isGrounded || ctx.coyoteTimer > 0), priority: 50 },
      { from: 'jump', to: 'fall', condition: ctx => !ctx.isGrounded && ctx.velocity.y < 0, priority: 40 },
      { from: 'fall', to: 'idle', condition: ctx => ctx.isGrounded && ctx.velocity.y <= 0, priority: 30 },

      // Sprint transitions
      { from: ['idle', 'walk', 'run'], to: 'sprint', condition: ctx => ctx.sprint && ctx.stamina > 10 && ctx.isGrounded && ctx.hasMovementInput, priority: 20 },
      { from: 'sprint', to: 'run', condition: ctx => !ctx.sprint || ctx.stamina <= 0 || !ctx.isGrounded || !ctx.hasMovementInput, priority: 15 },

      // Run transitions
      { from: ['idle', 'walk', 'crouch'], to: 'run', condition: ctx => ctx.hasMovementInput && ctx.isGrounded && !ctx.sprint, priority: 10 },
      { from: 'run', to: 'walk', condition: ctx => ctx.crouch || !ctx.hasMovementInput, priority: 5 },

      // Walk transitions
      { from: ['idle', 'run', 'sprint'], to: 'walk', condition: ctx => ctx.hasMovementInput && ctx.isGrounded && (ctx.crouch || !ctx.sprint), priority: 5 },
      { from: 'walk', to: 'idle', condition: ctx => !ctx.hasMovementInput || !ctx.isGrounded, priority: 1 },

      // Default idle
      { from: '*', to: 'idle', condition: ctx => true, priority: 0 },
    ]
  }

  private setupDefaultCallbacks(): void {
    // State enter callbacks
    this.onStateEnter.set('jump', ctx => {
      // Apply jump force
      // Handled by physics system
    })
    this.onStateEnter.set('sprint', ctx => {
      // Start stamina drain
    })
    this.onStateEnter.set('crouch', ctx => {
      // Start crouch transition
    })
    this.onStateEnter.set('swim', ctx => {
      // Enable swim physics
    })
    this.onStateEnter.set('climb', ctx => {
      // Enable climb physics
    })

    // State exit callbacks
    this.onStateExit.set('sprint', ctx => {
      // Stop stamina drain
    })
    this.onStateExit.set('crouch', ctx => {
      // Start stand transition
    })
    this.onStateExit.set('swim', ctx => {
      // Disable swim physics
    })
    this.onStateExit.set('climb', ctx => {
      // Disable climb physics
    })

    // State update callbacks
    this.onStateUpdate.set('sprint', ctx => {
      ctx.stamina = Math.max(0, ctx.stamina - ctx.config.sprintStaminaCost * ctx.deltaTime)
    })
    this.onStateUpdate.set('swim', ctx => {
      // Apply buoyancy
    })
    this.onStateUpdate.set('climb', ctx => {
      // Vertical movement on ladder
    })
  }

  // Public API

  update(context: MovementContext): MovementStateType {
    this.stateTime += context.deltaTime

    // Update timers
    if (context.isGrounded) {
      this.coyoteTimer = context.config.coyoteTime
    } else {
      this.coyoteTimer = Math.max(0, this.coyoteTimer - context.deltaTime)
    }

    if (context.jump) {
      this.jumpBufferTimer = context.config.jumpBufferTime
    } else {
      this.jumpBufferTimer = Math.max(0, this.jumpBufferTimer - context.deltaTime)
    }

    // Stamina regen
    if (context.stamina < context.maxStamina) {
      this.staminaRegenTimer += context.deltaTime
      if (this.staminaRegenTimer >= context.config.staminaRegenDelay) {
        // Regen handled by update callbacks
      }
    } else {
      this.staminaRegenTimer = 0
    }

    // Crouch transition
    if (context.crouch) {
      this.crouchTransitionTimer = Math.min(context.config.crouchTransitionTime, this.crouchTransitionTimer + context.deltaTime)
    } else {
      this.crouchTransitionTimer = Math.max(0, this.crouchTransitionTimer - context.deltaTime)
    }

    // Check transitions
    const nextState = this.evaluateTransitions(context)
    if (nextState !== this.currentState) {
      this.transitionTo(nextState, context)
    }

    // Run current state update
    const updateCallback = this.onStateUpdate.get(this.currentState)
    if (updateCallback) {
      updateCallback(context)
    }

    // Update player store
    usePlayerStore.getState().setMovementState(this.currentState)

    this.stateTime += context.deltaTime

    return this.currentState
  }

  private evaluateTransitions(context: MovementContext): MovementStateType {
    // Add computed properties to context
    const enhancedContext = {
      ...context,
      hasMovementInput: context.moveForward || context.moveBackward || context.moveLeft || context.moveRight,
      coyoteTimer: this.coyoteTimer,
      jumpBufferTimer: this.jumpBufferTimer,
    } as MovementContext & { hasMovementInput: boolean; coyoteTimer: number; jumpBufferTimer: number }

    // Sort transitions by priority (highest first)
    const sortedTransitions = [...this.transitions].sort((a, b) => b.priority - a.priority)

    for (const transition of sortedTransitions) {
      const fromMatches = transition.from === '*' ||
        (Array.isArray(transition.from) ? transition.from.includes(this.currentState) : transition.from === this.currentState)

      if (fromMatches && transition.condition(enhancedContext)) {
        return transition.to
      }
    }

    return this.currentState
  }

  private transitionTo(newState: MovementStateType, context: MovementContext): void {
    if (newState === this.currentState) return

    // Exit old state
    const exitCallback = this.onStateExit.get(this.currentState)
    if (exitCallback) exitCallback(context)

    this.previousState = this.currentState
    this.currentState = newState
    this.stateTime = 0

    // Enter new state
    const enterCallback = this.onStateEnter.get(this.currentState)
    if (enterCallback) enterCallback(context)

    // Emit event
    useGameStore.getState().emit('player:state-change', {
      from: this.previousState,
      to: this.currentState,
      time: Date.now(),
    })
  }

  // Public API

  getCurrentState(): MovementStateType {
    return this.currentState
  }

  getPreviousState(): MovementStateType {
    return this.previousState
  }

  getStateTime(): number {
    return this.stateTime
  }

  forceState(state: MovementStateType, context: MovementContext): void {
    this.transitionTo(state, context)
  }

  canJump(): boolean {
    return this.coyoteTimer > 0 || this.jumpBufferTimer > 0
  }

  consumeJumpBuffer(): void {
    this.jumpBufferTimer = 0
  }

  // Config updates
  setConfig(config: Partial<MovementStateConfig>): void {
    this.config = { ...this.config, ...config }
  }

  // Add custom transition
  addTransition(transition: StateTransition): void {
    this.transitions.push(transition)
    this.transitions.sort((a, b) => b.priority - a.priority)
  }

  // Remove transition
  removeTransition(from: MovementStateType | '*', to: MovementStateType): void {
    this.transitions = this.transitions.filter(t =>
      !(t.from === from && t.to === to)
    )
  }

  // Callbacks
  onEnter(state: MovementStateType, callback: (context: MovementContext) => void): void {
    this.onStateEnter.set(state, callback)
  }

  onExit(state: MovementStateType, callback: (context: MovementContext) => void): void {
    this.onStateExit.set(state, callback)
  }

  onUpdate(state: MovementStateType, callback: (context: MovementContext) => void): void {
    this.onStateUpdate.set(state, callback)
  }

  getConfig(): MovementStateConfig {
    return { ...this.config }
  }

  dispose(): void {
    this.onStateEnter.clear()
    this.onStateExit.clear()
    this.onStateUpdate.clear()
    this.transitions = []
  }
}

let movementStateMachineInstance: MovementStateMachine | null = null

export function createMovementStateMachine(config?: Partial<MovementStateConfig>): MovementStateMachine {
  if (movementStateMachineInstance) {
    movementStateMachineInstance.dispose()
  }
  movementStateMachineInstance = new MovementStateMachine(config)
  return movementStateMachineInstance
}

export function getMovementStateMachine(): MovementStateMachine | null {
  return movementStateMachineInstance
}