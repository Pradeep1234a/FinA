// Player Controller with Rapier3D Physics
// Handles character movement, collision, and physics simulation

import * as THREE from 'three'
import * as RAPIER from '@dimforge/rapier3d'
import { usePlayerStore } from '@stores/playerStore'
import { useWorldStore } from '@stores/worldStore'
import { useGameStore } from '@stores/gameStore'
import type { Vector3, Quaternion, MovementState } from '@types'

export interface PlayerControllerConfig {
  // Physics settings
  gravity: number
  capsuleRadius: number
  capsuleHeight: number
  maxSlopeAngle: number
  jumpImpulse: number
  moveSpeed: number
  sprintSpeed: number
  crouchSpeed: number
  airControl: number
  groundFriction: number
  airFriction: number
}

const DEFAULT_CONFIG: PlayerControllerConfig = {
  gravity: -9.81,
  capsuleRadius: 0.4,
  capsuleHeight: 1.8,
  maxSlopeAngle: Math.PI / 3,
  jumpImpulse: 7,
  moveSpeed: 4,
  sprintSpeed: 8,
  crouchSpeed: 2,
  airControl: 0.3,
  groundFriction: 10,
  airFriction: 0.5,
}

export class PlayerController {
  private config: PlayerControllerConfig
  private world: RAPIER.World | null = null
  private characterController: RAPIER.CharacterController | null = null
  private rigidBody: RAPIER.RigidBody | null = null
  private collider: RAPIER.Collider | null = null

  private isInitialized = false
  private lastPosition: Vector3 = { x: 0, y: 1.7, z: 0 }
  private lastRotation: Quaternion = { x: 0, y: 0, z: 0, w: 1 }
  private velocity: Vector3 = { x: 0, y: 0, z: 0 }
  private isGrounded = false
  private wasGrounded = false
  private jumpRequested = false
  private movementInput: { x: number; z: number } = { x: 0, z: 0 }
  private cameraYaw = 0
  private cameraPitch = 0

  private groundCheckRay: RAPIER.Ray | null = null
  private groundCheckFilter: RAPIER.QueryFilter | null = null

  constructor(config: Partial<PlayerControllerConfig> = {}) {
    this.config = { ...DEFAULT_CONFIG, ...config }
  }

  async initialize(): Promise<void> {
    await RAPIER.init()

    this.world = new RAPIER.World(new RAPIER.Vector3(0, this.config.gravity, 0))

    this.characterController = this.world.createCharacterController(this.config.capsuleRadius)
    this.characterController.setApplyImpulsesToDynamicBodies(true)
    this.characterController.setMaxSlopeClimbAngle(this.config.maxSlopeAngle)
    this.characterController.setMinSlopeSlideAngle(this.config.maxSlopeAngle)
    this.characterController.setAutostepHeight(0.5)
    this.characterController.setAutostepWidth(0.3)
    this.characterController.setAutostepIncludesDynamicBodies(false)

    const rbDesc = RAPIER.RigidBodyDesc.kinematicPositionBased()
      .setTranslation(this.lastPosition.x, this.lastPosition.y, this.lastPosition.z)
      .setRotation(new RAPIER.Quaternion(
        this.lastRotation.x,
        this.lastRotation.y,
        this.lastRotation.z,
        this.lastRotation.w
      ))
      .setCcdEnabled(true)

    this.rigidBody = this.world.createRigidBody(rbDesc)

    const colliderDesc = RAPIER.ColliderDesc.capsuleY(
      this.config.capsuleHeight / 2,
      this.config.capsuleRadius
    )
      .setFriction(0.0)
      .setRestitution(0.0)
      .setActiveEvents(RAPIER.ActiveEvents.COLLISION_EVENTS)

    this.collider = this.world.createCollider(colliderDesc, this.rigidBody)

    this.groundCheckRay = new RAPIER.Ray(
      new RAPIER.Vector3(0, 0, 0),
      new RAPIER.Vector3(0, -1, 0)
    )
    this.groundCheckFilter = new RAPIER.QueryFilter()
      .setExcludeCollider(this.collider.handle)
      .setFlags(RAPIER.QueryFilterFlags.EXCLUDE_SENSORS | RAPIER.QueryFilterFlags.EXCLUDE_RIGID_BODIES)

    this.isInitialized = true
  }

  update(deltaTime: number, input: {
    moveForward: boolean
    moveBackward: boolean
    moveLeft: boolean
    moveRight: boolean
    jump: boolean
    sprint: boolean
    crouch: boolean
    cameraYaw: number
    cameraPitch: number
  }): void {
    if (!this.isInitialized || !this.world || !this.characterController || !this.rigidBody) return

    const gameState = useGameStore.getState().state
    if (gameState !== 'playing') return

    this.cameraYaw = input.cameraYaw
    this.cameraPitch = input.cameraPitch

    this.movementInput.x = 0
    this.movementInput.z = 0

    if (input.moveForward) this.movementInput.z -= 1
    if (input.moveBackward) this.movementInput.z += 1
    if (input.moveLeft) this.movementInput.x -= 1
    if (input.moveRight) this.movementInput.x += 1

    const inputLen = Math.sqrt(this.movementInput.x ** 2 + this.movementInput.z ** 2)
    if (inputLen > 0) {
      this.movementInput.x /= inputLen
      this.movementInput.z /= inputLen
    }

    if (input.jump && this.isGrounded) {
      this.jumpRequested = true
    }

    this.world.step()

    this.updateCharacterController(deltaTime, input, inputLen)

    this.syncToStore()
  }

  private updateCharacterController(deltaTime: number, input: {
    moveForward: boolean
    moveBackward: boolean
    moveLeft: boolean
    moveRight: boolean
    jump: boolean
    sprint: boolean
    crouch: boolean
    cameraYaw: number
    cameraPitch: number
  }, inputLen: number): void {
    if (!this.characterController || !this.rigidBody || !this.world) return

    const playerStore = usePlayerStore.getState()
    const currentMovementState = playerStore.movementState

    let targetSpeed = this.config.moveSpeed
    let newMovementState: MovementState = 'idle'

    if (inputLen > 0) {
      if (playerStore.stamina > 10 && (currentMovementState === 'sprint' || currentMovementState === 'run')) {
        targetSpeed = this.config.sprintSpeed
        newMovementState = 'sprint'
      } else {
        targetSpeed = this.config.moveSpeed
        newMovementState = 'run'
      }
    } else {
      newMovementState = 'idle'
    }

    if (playerStore.isCrouching) {
      targetSpeed = this.config.crouchSpeed
      newMovementState = 'crouch'
    }

    const forward = new THREE.Vector3(0, 0, -1).applyAxisAngle(new THREE.Vector3(0, 1, 0), this.cameraYaw)
    const right = new THREE.Vector3(1, 0, 0).applyAxisAngle(new THREE.Vector3(0, 1, 0), this.cameraYaw)

    const moveDirection = new THREE.Vector3()
    moveDirection.addScaledVector(forward, this.movementInput.z)
    moveDirection.addScaledVector(right, this.movementInput.x)

    const rapierMove = new RAPIER.Vector3(
      moveDirection.x * targetSpeed,
      0,
      moveDirection.z * targetSpeed
    )

    if (this.jumpRequested) {
      rapierMove.y = this.config.jumpImpulse
      this.jumpRequested = false
      newMovementState = 'jump'
    }

    this.characterController.computeColliderMovement(
      this.collider!,
      rapierMove
    )

    const correctedMovement = this.characterController.computedMovement()
    this.characterController.setNextKinematicTranslation(
      this.rigidBody.translation().add(correctedMovement)
    )

    this.checkGrounded()

    if (newMovementState !== currentMovementState) {
      playerStore.setMovementState(newMovementState)
    }

    if (newMovementState === 'sprint') {
      playerStore.consumeStamina(deltaTime * 15)
    } else if (newMovementState !== 'jump' && playerStore.stamina < playerStore.maxStamina) {
      playerStore.restoreStamina(deltaTime * 10)
    }
  }

  private checkGrounded(): void {
    if (!this.world || !this.rigidBody || !this.groundCheckRay || !this.groundCheckFilter) return

    const position = this.rigidBody.translation()
    this.groundCheckRay.origin = new RAPIER.Vector3(position.x, position.y + 0.1, position.z)

    const hit = this.world.castRay(
      this.groundCheckRay,
      this.config.capsuleHeight / 2 + 0.2,
      true,
      this.groundCheckFilter
    )

    this.wasGrounded = this.isGrounded
    this.isGrounded = hit !== null

    const playerStore = usePlayerStore.getState()
    playerStore.setGrounded(this.isGrounded)

    if (!this.wasGrounded && this.isGrounded) {
      playerStore.setMovementState('idle')
    }
  }

  private syncToStore(): void {
    if (!this.rigidBody) return

    const position = this.rigidBody.translation()
    const rotation = this.rigidBody.rotation()

    this.lastPosition = { x: position.x, y: position.y, z: position.z }
    this.lastRotation = { x: rotation.x, y: rotation.y, z: rotation.z, w: rotation.w }

    const playerStore = usePlayerStore.getState()
    playerStore.setPosition(this.lastPosition)
    playerStore.setRotation(this.lastRotation)
    playerStore.setVelocity(this.velocity)
  }

  setPosition(position: Vector3): void {
    this.lastPosition = position
    if (this.rigidBody) {
      this.rigidBody.setTranslation(new RAPIER.Vector3(position.x, position.y, position.z), true)
    }
  }

  setRotation(rotation: Quaternion): void {
    this.lastRotation = rotation
    if (this.rigidBody) {
      this.rigidBody.setRotation(new RAPIER.Quaternion(rotation.x, rotation.y, rotation.z, rotation.w), true)
    }
  }

  getPosition(): Vector3 {
    return this.lastPosition
  }

  getRotation(): Quaternion {
    return this.lastRotation
  }

  isGroundedCheck(): boolean {
    return this.isGrounded
  }

  getVelocity(): Vector3 {
    return this.velocity
  }

  addImpulse(force: Vector3): void {
    if (this.rigidBody) {
      this.rigidBody.applyImpulse(new RAPIER.Vector3(force.x, force.y, force.z), true)
    }
  }

  teleport(position: Vector3): void {
    this.setPosition(position)
    if (this.characterController) {
      this.characterController.setNextKinematicTranslation(new RAPIER.Vector3(position.x, position.y, position.z))
    }
  }

  getPhysicsWorld(): RAPIER.World | null {
    return this.world
  }

  addStaticCollider(desc: RAPIER.ColliderDesc): RAPIER.Collider | null {
    if (!this.world) return null
    return this.world.createCollider(desc)
  }

  raycast(origin: Vector3, direction: Vector3, maxDistance: number, filter?: RAPIER.QueryFilter): RAPIER.RaycastHit | null {
    if (!this.world) return null

    const ray = new RAPIER.Ray(
      new RAPIER.Vector3(origin.x, origin.y, origin.z),
      new RAPIER.Vector3(direction.x, direction.y, direction.z)
    )

    return this.world.castRay(ray, maxDistance, true, filter || this.groundCheckFilter!)
  }

  dispose(): void {
    if (this.collider && this.world) {
      this.world.removeCollider(this.collider.handle, true)
    }
    if (this.rigidBody && this.world) {
      this.world.removeRigidBody(this.rigidBody.handle)
    }
    if (this.world) {
      this.world.free()
    }
    this.isInitialized = false
  }
}

let playerControllerInstance: PlayerController | null = null

export function createPlayerController(config?: Partial<PlayerControllerConfig>): PlayerController {
  if (playerControllerInstance) {
    playerControllerInstance.dispose()
  }
  playerControllerInstance = new PlayerController(config)
  return playerControllerInstance
}

export function getPlayerController(): PlayerController | null {
  return playerControllerInstance
}