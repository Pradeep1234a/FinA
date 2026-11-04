// Camera Controller
// Handles 3rd person, 1st person, cinematic, and metro ride camera modes

import * as THREE from 'three'
import { usePlayerStore } from '@stores/playerStore'
import { useGameStore } from '@stores/gameStore'
import type { Vector3, Quaternion, CameraMode } from '@types'

export interface CameraControllerConfig {
  // Third person settings
  thirdPersonDistance: number
  thirdPersonHeight: number
  thirdPersonOffset: THREE.Vector3
  // First person settings
  firstPersonHeight: number
  // Cinematic settings
  cinematicDistance: number
  cinematicHeight: number
  cinematicLookAtHeight: number
  // Metro ride settings
  metroRideDistance: number
  metroRideHeight: number
  metroRideLookAhead: number
  // Transition settings
  transitionDuration: number
  // Collision settings
  collisionRadius: number
  collisionCheckDistance: number
}

const DEFAULT_CONFIG: CameraControllerConfig = {
  thirdPersonDistance: 5,
  thirdPersonHeight: 1.7,
  thirdPersonOffset: new THREE.Vector3(0, 0, 0),
  firstPersonHeight: 1.65,
  cinematicDistance: 10,
  cinematicHeight: 5,
  cinematicLookAtHeight: 0,
  metroRideDistance: 3,
  metroRideHeight: 1.5,
  metroRideLookAhead: 10,
  transitionDuration: 0.5,
  collisionRadius: 0.3,
  collisionCheckDistance: 10,
}

export class CameraController {
  private config: CameraControllerConfig
  private camera: THREE.PerspectiveCamera
  private target: THREE.Object3D | null = null
  private currentMode: CameraMode = 'third-person'
  private previousMode: CameraMode = 'third-person'

  // Transition state
  private isTransitioning = false
  private transitionStartTime = 0
  private transitionDuration = 0
  private transitionStartPos = new THREE.Vector3()
  private transitionStartTarget = new THREE.Vector3()
  private transitionStartFov = 75

  // Camera shake
  private shakeIntensity = 0
  private shakeDuration = 0
  private shakeStartTime = 0

  // Smoothing
  private smoothingEnabled = true
  private positionSmoothing = 0.15
  private rotationSmoothing = 0.1

  // Desired camera state
  private desiredPosition = new THREE.Vector3()
  private desiredTarget = new THREE.Vector3()
  private desiredFov = 75

  // Metro ride state
  private metroTrain: THREE.Object3D | null = null
  private metroLookAhead = 0

  constructor(camera: THREE.PerspectiveCamera, config: Partial<CameraControllerConfig> = {}) {
    this.camera = camera
    this.config = { ...DEFAULT_CONFIG, ...config }
    this.desiredFov = camera.fov
  }

  setTarget(target: THREE.Object3D | null): void {
    this.target = target
  }

  setMetroTrain(train: THREE.Object3D | null): void {
    this.metroTrain = train
  }

  setMode(mode: CameraMode, duration = 0): void {
    if (mode === this.currentMode && !this.isTransitioning) return

    this.previousMode = this.currentMode
    this.currentMode = mode

    if (duration > 0) {
      this.startTransition(duration)
    } else {
      this.applyModeImmediate(mode)
    }
  }

  private startTransition(duration: number): void {
    this.isTransitioning = true
    this.transitionStartTime = performance.now()
    this.transitionDuration = duration * 1000

    this.transitionStartPos.copy(this.camera.position)
    this.transitionStartTarget.copy(this.getModeTarget())
    this.transitionStartFov = this.camera.fov
  }

  private applyModeImmediate(mode: CameraMode): void {
    if (!this.target) return

    const targetPos = this.target.position
    const targetRot = this.target.rotation

    switch (mode) {
      case 'third-person': {
        const distance = this.config.thirdPersonDistance
        const height = this.config.thirdPersonHeight
        const offset = this.config.thirdPersonOffset

        this.desiredPosition.set(
          targetPos.x + offset.x,
          targetPos.y + height,
          targetPos.z + distance + offset.z
        )
        this.desiredTarget.set(targetPos.x, targetPos.y + height * 0.5, targetPos.z)
        this.desiredFov = 75
        break
      }

      case 'first-person': {
        const height = this.config.firstPersonHeight
        const forward = new THREE.Vector3(0, 0, -1).applyQuaternion(this.target.quaternion)

        this.desiredPosition.set(
          targetPos.x,
          targetPos.y + height,
          targetPos.z
        )
        this.desiredTarget.set(
          targetPos.x + forward.x * 10,
          targetPos.y + height,
          targetPos.z + forward.z * 10
        )
        this.desiredFov = 90
        break
      }

      case 'cinematic': {
        const distance = this.config.cinematicDistance
        const height = this.config.cinematicHeight
        const lookAtHeight = this.config.cinematicLookAtHeight

        this.desiredPosition.set(
          targetPos.x,
          targetPos.y + height,
          targetPos.z + distance
        )
        this.desiredTarget.set(targetPos.x, targetPos.y + lookAtHeight, targetPos.z)
        this.desiredFov = 50
        break
      }

      case 'metro-ride': {
        this.applyMetroRideMode()
        break
      }
    }
  }

  private applyMetroRideMode(): void {
    if (!this.metroTrain) {
      this.applyModeImmediate('third-person')
      return
    }

    const trainPos = this.metroTrain.position
    const trainForward = new THREE.Vector3(0, 0, -1).applyQuaternion(this.metroTrain.quaternion)
    const trainRight = new THREE.Vector3(1, 0, 0).applyQuaternion(this.metroTrain.quaternion)

    const distance = this.config.metroRideDistance
    const height = this.config.metroRideHeight
    const lookAhead = this.config.metroRideLookAhead

    this.desiredPosition.set(
      trainPos.x - trainForward.x * distance + trainRight.x * 1.5,
      trainPos.y + height,
      trainPos.z - trainForward.z * distance + trainRight.z * 1.5
    )

    this.desiredTarget.set(
      trainPos.x + trainForward.x * lookAhead,
      trainPos.y + 1.5,
      trainPos.z + trainForward.z * lookAhead
    )

    this.desiredFov = 70
  }

  private getModeTarget(): THREE.Vector3 {
    if (!this.target) return new THREE.Vector3()

    switch (this.previousMode) {
      case 'third-person': {
        const targetPos = this.target.position
        const height = this.config.thirdPersonHeight
        return new THREE.Vector3(targetPos.x, targetPos.y + height * 0.5, targetPos.z)
      }
      case 'first-person': {
        const targetPos = this.target.position
        const height = this.config.firstPersonHeight
        const forward = new THREE.Vector3(0, 0, -1).applyQuaternion(this.target.quaternion)
        return new THREE.Vector3(
          targetPos.x + forward.x * 10,
          targetPos.y + height,
          targetPos.z + forward.z * 10
        )
      }
      case 'cinematic': {
        const targetPos = this.target.position
        return new THREE.Vector3(targetPos.x, targetPos.y + this.config.cinematicLookAtHeight, targetPos.z)
      }
      case 'metro-ride': {
        if (!this.metroTrain) return new THREE.Vector3()
        const trainPos = this.metroTrain.position
        const trainForward = new THREE.Vector3(0, 0, -1).applyQuaternion(this.metroTrain.quaternion)
        return new THREE.Vector3(
          trainPos.x + trainForward.x * this.config.metroRideLookAhead,
          trainPos.y + 1.5,
          trainPos.z + trainForward.z * this.config.metroRideLookAhead
        )
      }
    }
    return new THREE.Vector3()
  }

  update(deltaTime: number): void {
    if (!this.target) return

    // Update transition
    if (this.isTransitioning) {
      const elapsed = performance.now() - this.transitionStartTime
      const progress = Math.min(elapsed / this.transitionDuration, 1)

      // Ease in-out cubic
      const eased = progress < 0.5
        ? 4 * progress * progress * progress
        : 1 - Math.pow(-2 * progress + 2, 3) / 2

      this.camera.position.lerpVectors(this.transitionStartPos, this.desiredPosition, eased)
      this.camera.lookAt(
        new THREE.Vector3().lerpVectors(this.transitionStartTarget, this.desiredTarget, eased)
      )
      this.camera.fov = THREE.MathUtils.lerp(this.transitionStartFov, this.desiredFov, eased)
      this.camera.updateProjectionMatrix()

      if (progress >= 1) {
        this.isTransitioning = false
      }
    } else {
      // Apply smoothing to position and target
      if (this.smoothingEnabled) {
        this.camera.position.lerp(this.desiredPosition, this.positionSmoothing)
        this.desiredTarget.lerp(this.getModeTarget(), this.rotationSmoothing)
        this.camera.lookAt(this.desiredTarget)
      } else {
        this.camera.position.copy(this.desiredPosition)
        this.camera.lookAt(this.getModeTarget())
      }

      // Smooth FOV
      this.camera.fov = THREE.MathUtils.lerp(this.camera.fov, this.desiredFov, 0.1)
      this.camera.updateProjectionMatrix()

      // Update desired position/target based on current mode
      this.updateDesiredState()

      // Camera shake
      if (this.shakeIntensity > 0) {
        const elapsed = performance.now() - this.shakeStartTime
        const progress = Math.min(elapsed / this.shakeDuration, 1)
        const currentIntensity = this.shakeIntensity * (1 - progress)

        if (progress < 1) {
          const shakeX = (Math.random() - 0.5) * currentIntensity
          const shakeY = (Math.random() - 0.5) * currentIntensity
          const shakeZ = (Math.random() - 0.5) * currentIntensity

          this.camera.position.x += shakeX
          this.camera.position.y += shakeY
          this.camera.position.z += shakeZ
        } else {
          this.shakeIntensity = 0
        }
      }
    }
  }

  private updateDesiredState(): void {
    if (!this.target) return

    switch (this.currentMode) {
      case 'third-person': {
        const targetPos = this.target.position
        const distance = this.config.thirdPersonDistance
        const height = this.config.thirdPersonHeight
        const offset = this.config.thirdPersonOffset

        this.desiredPosition.set(
          targetPos.x + offset.x,
          targetPos.y + height,
          targetPos.z + distance + offset.z
        )
        this.desiredTarget.set(targetPos.x, targetPos.y + height * 0.5, targetPos.z)
        this.desiredFov = 75
        break
      }

      case 'first-person': {
        const targetPos = this.target.position
        const height = this.config.firstPersonHeight
        const forward = new THREE.Vector3(0, 0, -1).applyQuaternion(this.target.quaternion)

        this.desiredPosition.set(
          targetPos.x,
          targetPos.y + height,
          targetPos.z
        )
        this.desiredTarget.set(
          targetPos.x + forward.x * 10,
          targetPos.y + height,
          targetPos.z + forward.z * 10
        )
        this.desiredFov = 90
        break
      }

      case 'cinematic': {
        const targetPos = this.target.position
        this.desiredPosition.set(
          targetPos.x,
          targetPos.y + this.config.cinematicHeight,
          targetPos.z + this.config.cinematicDistance
        )
        this.desiredTarget.set(targetPos.x, targetPos.y + this.config.cinematicLookAtHeight, targetPos.z)
        this.desiredFov = 50
        break
      }

      case 'metro-ride': {
        this.applyMetroRideMode()
        break
      }
    }
  }

  // Camera shake
  shake(intensity: number, duration: number): void {
    this.shakeIntensity = intensity
    this.shakeDuration = duration * 1000
    this.shakeStartTime = performance.now()
  }

  // Smoothing control
  setSmoothing(enabled: boolean, positionSmoothing?: number, rotationSmoothing?: number): void {
    this.smoothingEnabled = enabled
    if (positionSmoothing !== undefined) this.positionSmoothing = positionSmoothing
    if (rotationSmoothing !== undefined) this.rotationSmoothing = rotationSmoothing
  }

  // Config updates
  setConfig(config: Partial<CameraControllerConfig>): void {
    this.config = { ...this.config, ...config }
  }

  getMode(): CameraMode {
    return this.currentMode
  }

  isTransitioningCheck(): boolean {
    return this.isTransitioning
  }

  // Get camera state for saving
  getState() {
    return {
      mode: this.currentMode,
      position: this.camera.position.clone(),
      target: this.getModeTarget(),
      fov: this.camera.fov,
    }
  }

  // Restore camera state
  setState(state: { mode: CameraMode; position: THREE.Vector3; target: THREE.Vector3; fov: number }): void {
    this.currentMode = state.mode
    this.camera.position.copy(state.position)
    this.camera.lookAt(state.target)
    this.camera.fov = state.fov
    this.camera.updateProjectionMatrix()
    this.desiredPosition.copy(state.position)
    this.desiredTarget.copy(state.target)
    this.desiredFov = state.fov
  }

  dispose(): void {
    this.target = null
    this.metroTrain = null
  }
}

let cameraControllerInstance: CameraController | null = null

export function createCameraController(camera: THREE.PerspectiveCamera, config?: Partial<CameraControllerConfig>): CameraController {
  if (cameraControllerInstance) {
    cameraControllerInstance.dispose()
  }
  cameraControllerInstance = new CameraController(camera, config)
  return cameraControllerInstance
}

export function getCameraController(): CameraController | null {
  return cameraControllerInstance
}