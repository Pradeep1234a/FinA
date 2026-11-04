// Interaction System
// Handles raycast-based interaction detection, highlighting, and prompts

import * as THREE from 'three'
import { usePlayerStore } from '@stores/playerStore'
import { useWorldStore } from '@stores/worldStore'
import { useGameStore } from '@stores/gameStore'
import type { Vector3, WorldEntity } from '@types'

export interface InteractionConfig {
  // Raycast settings
  maxDistance: number
  interactionRadius: number
  raycastLayers: number[]
  // Highlight settings
  highlightColor: THREE.Color
  highlightIntensity: number
  // Prompt settings
  promptOffset: THREE.Vector3
  promptFadeTime: number
}

const DEFAULT_CONFIG: InteractionConfig = {
  maxDistance: 3,
  interactionRadius: 0.5,
  raycastLayers: [0, 1], // default and interactable layers
  highlightColor: new THREE.Color(0x00d4aa),
  highlightIntensity: 1.5,
  promptOffset: new THREE.Vector3(0, 2, 0),
  promptFadeTime: 0.2,
}

export interface InteractableObject {
  entity: WorldEntity
  distance: number
  interactionPoint: THREE.Vector3
  interactionType: 'npc' | 'metro' | 'door' | 'item' | 'sign' | 'terminal' | 'custom'
  prompt: string
  promptKannada?: string
  onInteract: () => void | Promise<void>
  isAvailable: boolean
  cooldown?: number
  lastInteracted?: number
}

export class InteractionSystem {
  private config: InteractionConfig
  private camera: THREE.PerspectiveCamera | null = null
  private scene: THREE.Scene | null = null

  // Raycaster
  private raycaster: THREE.Raycaster
  private mousePosition: THREE.Vector2 = new THREE.Vector2(0, 0)

  // State
  private interactables: Map<string, InteractableObject> = new Map()
  private currentInteractable: InteractableObject | null = null
  private previousInteractable: InteractableObject | null = null
  private isPromptVisible = false
  private promptAlpha = 0

  // Highlight material
  private highlightMaterial: THREE.MeshBasicMaterial | null = null
  private originalMaterials: Map<THREE.Mesh, THREE.Material | THREE.Material[]> = new Map()

  // Cooldowns
  private interactionCooldowns: Map<string, number> = new Map()

  constructor(config: Partial<InteractionConfig> = {}) {
    this.config = { ...DEFAULT_CONFIG, ...config }
    this.raycaster = new THREE.Raycaster()
    this.raycaster.far = this.config.maxDistance
    this.raycaster.layers.enableAll()

    this.createHighlightMaterial()
  }

  private createHighlightMaterial(): void {
    this.highlightMaterial = new THREE.MeshBasicMaterial({
      color: this.config.highlightColor,
      transparent: true,
      opacity: 0.3,
      depthWrite: false,
      side: THREE.DoubleSide,
    })
  }

  // Initialize with camera and scene
  initialize(camera: THREE.PerspectiveCamera, scene: THREE.Scene): void {
    this.camera = camera
    this.scene = scene
  }

  // Register an interactable object
  registerInteractable(interactable: InteractableObject): void {
    this.interactables.set(interactable.entity.id, interactable)
  }

  // Unregister an interactable object
  unregisterInteractable(entityId: string): void {
    this.interactables.delete(entityId)
    if (this.currentInteractable?.entity.id === entityId) {
      this.clearCurrentInteractable()
    }
    if (this.previousInteractable?.entity.id === entityId) {
      this.previousInteractable = null
    }
  }

  // Update the interaction system
  update(deltaTime: number): void {
    if (!this.camera || !this.scene) return

    const gameState = useGameStore.getState().state
    if (gameState !== 'playing') {
      this.clearCurrentInteractable()
      return
    }

    // Update raycast from camera center
    this.raycaster.setFromCamera(new THREE.Vector2(0, 0), this.camera)

    // Find closest interactable
    let closestInteractable: InteractableObject | null = null
    let closestDistance = Infinity

    for (const interactable of this.interactables.values()) {
      if (!interactable.isAvailable) continue
      if (!interactable.entity) continue

      // Check cooldown
      const lastInteracted = this.interactionCooldowns.get(interactable.entity.id) || 0
      if (interactable.cooldown && Date.now() - lastInteracted < interactable.cooldown) {
        continue
      }

      // Raycast against entity
      const entityObject = this.scene?.getObjectByName(interactable.entity.id)
      if (!entityObject) continue

      this.raycaster.intersectObject(entityObject, true)

      // For now, use simple distance check
      const distance = this.camera.position.distanceTo(interactable.interactionPoint)

      if (distance <= this.config.maxDistance && distance < closestDistance) {
        closestDistance = distance
        closestInteractable = interactable
      }
    }

    // Update current interactable
    if (closestInteractable !== this.currentInteractable) {
      this.setCurrentInteractable(closestInteractable)
    }

    // Update prompt visibility
    this.updatePromptVisibility(deltaTime)
  }

  private setCurrentInteractable(interactable: InteractableObject | null): void {
    // Remove highlight from previous
    if (this.currentInteractable && this.currentInteractable !== interactable) {
      this.removeHighlight(this.currentInteractable)
    }

    this.previousInteractable = this.currentInteractable
    this.currentInteractable = interactable

    // Add highlight to new
    if (interactable) {
      this.addHighlight(interactable)
    }
  }

  private addHighlight(interactable: InteractableObject): void {
    if (!interactable.entity || !this.highlightMaterial) return

    const entityObject = this.scene?.getObjectByName(interactable.entity.id)
    if (!entityObject) return

    entityObject.traverse((child) => {
      if (child instanceof THREE.Mesh) {
        // Store original material
        if (!this.originalMaterials.has(child)) {
          this.originalMaterials.set(child, child.material)
        }
        // Apply highlight
        child.material = this.highlightMaterial!
      }
    })
  }

  private removeHighlight(interactable: InteractableObject): void {
    if (!interactable.entity) return

    const entityObject = this.scene?.getObjectByName(interactable.entity.id)
    if (!entityObject) return

    entityObject.traverse((child) => {
      if (child instanceof THREE.Mesh) {
        const original = this.originalMaterials.get(child)
        if (original) {
          child.material = original
          this.originalMaterials.delete(child)
        }
      }
    })
  }

  private updatePromptVisibility(deltaTime: number): void {
    const targetVisible = this.currentInteractable !== null
    const fadeSpeed = 1 / this.config.promptFadeTime

    if (targetVisible) {
      this.promptAlpha = Math.min(1, this.promptAlpha + fadeSpeed * deltaTime)
    } else {
      this.promptAlpha = Math.max(0, this.promptAlpha - fadeSpeed * deltaTime)
    }

    if (this.promptAlpha > 0.01) {
      this.isPromptVisible = true
    } else {
      this.isPromptVisible = false
    }
  }

  private clearCurrentInteractable(): void {
    if (this.currentInteractable) {
      this.removeHighlight(this.currentInteractable)
      this.currentInteractable = null
    }
  }

  // Execute interaction
  async interact(): Promise<boolean> {
    if (!this.currentInteractable || !this.currentInteractable.isAvailable) {
      return false
    }

    // Check cooldown
    const lastInteracted = this.interactionCooldowns.get(this.currentInteractable.entity.id) || 0
    if (this.currentInteractable.cooldown && Date.now() - lastInteracted < this.currentInteractable.cooldown) {
      return false
    }

    try {
      await this.currentInteractable.onInteract()
      this.interactionCooldowns.set(this.currentInteractable.entity.id, Date.now())
      return true
    } catch (error) {
      console.error('Interaction failed:', error)
      return false
    }
  }

  // Get current interactable for UI
  getCurrentInteractable(): InteractableObject | null {
    return this.currentInteractable
  }

  getPromptAlpha(): number {
    return this.promptAlpha
  }

  isPromptVisibleCheck(): boolean {
    return this.isPromptVisible
  }

  // Set interactable availability
  setInteractableAvailable(entityId: string, available: boolean): void {
    const interactable = this.interactables.get(entityId)
    if (interactable) {
      interactable.isAvailable = available
    }
  }

  // Set cooldown
  setCooldown(entityId: string, cooldownMs: number): void {
    const interactable = this.interactables.get(entityId)
    if (interactable) {
      interactable.cooldown = cooldownMs
    }
  }

  // Config updates
  setMaxDistance(distance: number): void {
    this.config.maxDistance = distance
    this.raycaster.far = distance
  }

  setInteractionRadius(radius: number): void {
    this.config.interactionRadius = radius
  }

  // Cleanup
  dispose(): void {
    this.clearCurrentInteractable()
    this.interactables.clear()
    this.originalMaterials.clear()
    this.interactionCooldowns.clear()
    if (this.highlightMaterial) {
      this.highlightMaterial.dispose()
      this.highlightMaterial = null
    }
  }
}

let interactionSystemInstance: InteractionSystem | null = null

export function createInteractionSystem(config?: Partial<InteractionConfig>): InteractionSystem {
  if (interactionSystemInstance) {
    interactionSystemInstance.dispose()
  }
  interactionSystemInstance = new InteractionSystem(config)
  return interactionSystemInstance
}

export function getInteractionSystem(): InteractionSystem | null {
  return interactionSystemInstance
}