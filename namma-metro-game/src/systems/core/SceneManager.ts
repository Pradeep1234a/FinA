// Three.js Scene Manager
// Handles renderer, scene, camera, resize, and render pipeline
// Simplified version without post-processing for stability

import * as THREE from 'three'
import { useGameStore } from '@stores/gameStore'
import { useWorldStore } from '@stores/worldStore'
import type { Vector3 } from '@types'

export interface SceneManagerConfig {
  canvas: HTMLCanvasElement
  antialias?: boolean
  alpha?: boolean
  preserveDrawingBuffer?: boolean
  powerPreference?: 'default' | 'high-performance' | 'low-power'
}

export class SceneManager {
  private config: SceneManagerConfig
  public renderer: THREE.WebGLRenderer
  public scene: THREE.Scene
  public camera: THREE.PerspectiveCamera

  private cameraStates: Map<string, { position: THREE.Vector3; target: THREE.Vector3; fov: number }> = new Map()
  private currentCameraState: string = 'default'

  private shadowsEnabled = true
  private shadowMapSize = 2048

  private resizeObserver: ResizeObserver | null = null

  constructor(config: SceneManagerConfig) {
    this.config = {
      antialias: true,
      alpha: false,
      preserveDrawingBuffer: false,
      powerPreference: 'high-performance',
      ...config,
    }

    this.renderer = this.createRenderer()
    this.scene = this.createScene()
    this.camera = this.createCamera()

    this.setupResizeHandler()
    this.setupDefaultCameraStates()
  }

  private createRenderer(): THREE.WebGLRenderer {
    const renderer = new THREE.WebGLRenderer({
      canvas: this.config.canvas,
      antialias: this.config.antialias,
      alpha: this.config.alpha,
      preserveDrawingBuffer: this.config.preserveDrawingBuffer,
      powerPreference: this.config.powerPreference,
    })

    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    renderer.setSize(this.config.canvas.clientWidth, this.config.canvas.clientHeight)
    renderer.outputColorSpace = THREE.SRGBColorSpace
    renderer.toneMapping = THREE.ACESFilmicToneMapping
    renderer.toneMappingExposure = 1
    renderer.shadowMap.enabled = this.shadowsEnabled
    renderer.shadowMap.type = THREE.PCFSoftShadowMap
    renderer.shadowMap.autoUpdate = true
    renderer.physicallyCorrectLights = true

    return renderer
  }

  private createScene(): THREE.Scene {
    const scene = new THREE.Scene()
    scene.background = new THREE.Color(0x87CEEB)
    return scene
  }

  private createCamera(): THREE.PerspectiveCamera {
    const aspect = this.config.canvas.clientWidth / this.config.canvas.clientHeight
    const camera = new THREE.PerspectiveCamera(75, aspect, 0.1, 50000)
    camera.position.set(0, 1.7, 5)
    return camera
  }

  private setupResizeHandler(): void {
    const handleResize = () => {
      const width = this.config.canvas.clientWidth
      const height = this.config.canvas.clientHeight

      this.camera.aspect = width / height
      this.camera.updateProjectionMatrix()

      this.renderer.setSize(width, height)
    }

    this.resizeObserver = new ResizeObserver(handleResize)
    this.resizeObserver.observe(this.config.canvas)
    handleResize()
  }

  private setupDefaultCameraStates(): void {
    this.cameraStates.set('default', {
      position: new THREE.Vector3(0, 1.7, 5),
      target: new THREE.Vector3(0, 1.7, 0),
      fov: 75,
    })
    this.cameraStates.set('first-person', {
      position: new THREE.Vector3(0, 1.65, 0),
      target: new THREE.Vector3(0, 1.65, -1),
      fov: 90,
    })
    this.cameraStates.set('metro-ride', {
      position: new THREE.Vector3(0, 1.5, 3),
      target: new THREE.Vector3(0, 1.5, -10),
      fov: 70,
    })
    this.cameraStates.set('cinematic', {
      position: new THREE.Vector3(0, 5, 10),
      target: new THREE.Vector3(0, 0, 0),
      fov: 50,
    })
  }

  render(): void {
    this.renderer.render(this.scene, this.camera)
  }

  setCameraState(stateName: string, duration = 0): void {
    const state = this.cameraStates.get(stateName)
    if (!state) return
    this.currentCameraState = stateName
    if (duration > 0) {
      useGameStore.getState().emit('camera:transition', { stateName, duration })
    } else {
      this.camera.position.copy(state.position)
      this.camera.lookAt(state.target)
      this.camera.fov = state.fov
      this.camera.updateProjectionMatrix()
    }
  }

  getCameraState(stateName: string) {
    return this.cameraStates.get(stateName)
  }

  addCameraState(name: string, position: Vector3, target: Vector3, fov = 75): void {
    this.cameraStates.set(name, {
      position: new THREE.Vector3(position.x, position.y, position.z),
      target: new THREE.Vector3(target.x, target.y, target.z),
      fov,
    })
  }

  updateCameraPosition(position: Vector3): void {
    this.camera.position.set(position.x, position.y, position.z)
  }

  updateCameraTarget(target: Vector3): void {
    this.camera.lookAt(target.x, target.y, target.z)
  }

  setFOV(fov: number): void {
    this.camera.fov = fov
    this.camera.updateProjectionMatrix()
  }

  setShadowsEnabled(enabled: boolean): void {
    this.shadowsEnabled = enabled
    this.renderer.shadowMap.enabled = enabled
  }

  setShadowMapSize(size: number): void {
    this.shadowMapSize = size
  }

  addToScene(object: THREE.Object3D): void {
    this.scene.add(object)
  }

  removeFromScene(object: THREE.Object3D): void {
    this.scene.remove(object)
    this.disposeObject(object)
  }

  clearScene(): void {
    while (this.scene.children.length > 0) {
      this.disposeObject(this.scene.children[0])
      this.scene.remove(this.scene.children[0])
    }
  }

  private disposeObject(object: THREE.Object3D): void {
    if (object instanceof THREE.Mesh) {
      if (object.geometry) object.geometry.dispose()
      if (object.material) {
        if (Array.isArray(object.material)) {
          object.material.forEach(m => m.dispose())
        } else {
          object.material.dispose()
        }
      }
    }
    for (const child of object.children) {
      this.disposeObject(child)
    }
  }

  setBackground(color: THREE.Color | THREE.Texture): void {
    this.scene.background = color
  }

  setFog(fog: THREE.Fog | THREE.FogExp2 | null): void {
    this.scene.fog = fog
  }

  applyGraphicsSettings(): void {
    const settings = useGameStore.getState().settings.graphics
    this.setShadowsEnabled(settings.shadows)
    if (settings.shadows) {
      const sizeMap = { low: 1024, medium: 2048, high: 4096, ultra: 8192 }
      this.setShadowMapSize(sizeMap[settings.shadowQuality])
    }
    this.camera.fov = settings.fov
    this.camera.updateProjectionMatrix()
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio * settings.resolutionScale, 2))
  }

  getStats() {
    return {
      triangles: this.renderer.info.render.triangles,
      draws: this.renderer.info.render.calls,
      frames: this.renderer.info.render.frame,
      geometries: this.renderer.info.memory.geometries,
      textures: this.renderer.info.memory.textures,
    }
  }

  dispose(): void {
    if (this.resizeObserver) {
      this.resizeObserver.disconnect()
    }
    this.clearScene()
    this.renderer.dispose()
  }
}

let sceneManagerInstance: SceneManager | null = null

export function createSceneManager(config: SceneManagerConfig): SceneManager {
  if (sceneManagerInstance) {
    sceneManagerInstance.dispose()
  }
  sceneManagerInstance = new SceneManager(config)
  return sceneManagerInstance
}

export function getSceneManager(): SceneManager | null {
  return sceneManagerInstance
}