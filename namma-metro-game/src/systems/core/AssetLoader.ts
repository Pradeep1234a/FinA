// Asset Loader
// Progressive loading with priority queue, caching, and DRACO/GLTF support

import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { DRACOLoader } from 'three/examples/jsm/loaders/DRACOLoader.js'
import { KTX2Loader } from 'three/examples/jsm/loaders/KTX2Loader.js'
import { MeshoptDecoder } from 'three/examples/jsm/libs/meshopt_decoder.module.js'
import { useGameStore } from '@stores/gameStore'
import type { AssetManifest, AssetEntry, Vector3 } from '@types'

export type AssetPriority = 'critical' | 'high' | 'normal' | 'low'
export type AssetType = 'model' | 'texture' | 'audio' | 'data' | 'shader' | 'cubemap'

export interface LoadRequest {
  id: string
  url: string
  type: AssetType
  priority: AssetPriority
  onProgress?: (progress: number) => void
  onComplete?: (asset: unknown) => void
  onError?: (error: Error) => void
}

export interface LoadedAsset {
  id: string
  url: string
  type: AssetType
  data: unknown
  timestamp: number
  size: number
}

const PRIORITY_WEIGHTS: Record<AssetPriority, number> = {
  critical: 0,
  high: 1,
  normal: 2,
  low: 3,
}

export class AssetLoader {
  private loadingManager: THREE.LoadingManager
  private gltfLoader: GLTFLoader
  private dracoLoader: DRACOLoader
  private ktx2Loader: KTX2Loader
  private textureLoader: THREE.TextureLoader
  private audioLoader: THREE.AudioLoader
  private fileLoader: THREE.FileLoader

  private loadQueue: LoadRequest[] = []
  private loading: Set<string> = new Set()
  private loadedAssets: Map<string, LoadedAsset> = new Map()
  private cache: Map<string, unknown> = new Map()
  private maxConcurrentLoads = 4
  private activeLoads = 0
  private totalQueueSize = 0
  private loadedQueueSize = 0

  // Callbacks
  private onGlobalProgress?: (progress: number, loaded: number, total: number) => void
  private onGlobalComplete?: () => void

  constructor() {
    this.loadingManager = new THREE.LoadingManager(
      this.onLoadComplete.bind(this),
      this.onLoadProgress.bind(this),
      this.onLoadError.bind(this)
    )

    this.setupLoaders()
  }

  private setupLoaders(): void {
    // GLTF Loader with DRACO and Meshopt
    this.gltfLoader = new GLTFLoader(this.loadingManager)
    this.dracoLoader = new DRACOLoader()
    this.dracoLoader.setDecoderPath('/draco/')
    this.dracoLoader.setDecoderConfig({ type: 'js' })
    this.gltfLoader.setDRACOLoader(this.dracoLoader)
    this.gltfLoader.setMeshoptDecoder(MeshoptDecoder)

    // KTX2 Loader for compressed textures
    this.ktx2Loader = new KTX2Loader(this.loadingManager)
    this.ktx2Loader.setTranscoderPath('/basis/')
    this.ktx2Loader.detectSupport(this.loadingManager.renderer as THREE.WebGLRenderer)

    // Standard loaders
    this.textureLoader = new THREE.TextureLoader(this.loadingManager)
    this.audioLoader = new THREE.AudioLoader(this.loadingManager)
    this.fileLoader = new THREE.FileLoader(this.loadingManager)
    this.fileLoader.setResponseType('json')
  }

  // Public API

  // Load a single asset
  load(request: LoadRequest): Promise<unknown> {
    return new Promise((resolve, reject) => {
      request.onComplete = (asset) => resolve(asset)
      request.onError = (error) => reject(error)
      this.enqueue(request)
    })
  }

  // Load multiple assets with progress tracking
  loadManifest(manifest: AssetManifest, onProgress?: (progress: number) => void): Promise<Map<string, LoadedAsset>> {
    this.onGlobalProgress = onProgress
    this.totalQueueSize = manifest.models.length + manifest.textures.length + manifest.audio.length + manifest.data.length + manifest.shaders.length
    this.loadedQueueSize = 0

    const requests: LoadRequest[] = []

    for (const entry of manifest.models) {
      requests.push({ id: entry.id, url: entry.url, type: 'model', priority: entry.priority })
    }
    for (const entry of manifest.textures) {
      requests.push({ id: entry.id, url: entry.url, type: 'texture', priority: entry.priority })
    }
    for (const entry of manifest.audio) {
      requests.push({ id: entry.id, url: entry.url, type: 'audio', priority: entry.priority })
    }
    for (const entry of manifest.data) {
      requests.push({ id: entry.id, url: entry.url, type: 'data', priority: entry.priority })
    }
    for (const entry of manifest.shaders) {
      requests.push({ id: entry.id, url: entry.url, type: 'shader', priority: entry.priority })
    }

    // Sort by priority
    requests.sort((a, b) => PRIORITY_WEIGHTS[a.priority] - PRIORITY_WEIGHTS[b.priority])

    for (const request of requests) {
      this.enqueue(request)
    }

    return new Promise((resolve) => {
      const checkComplete = () => {
        if (this.loadedQueueSize >= this.totalQueueSize) {
          this.onGlobalProgress = undefined
          this.onGlobalComplete = undefined
          resolve(this.loadedAssets)
        } else {
          requestAnimationFrame(checkComplete)
        }
      }
      checkComplete()
    })
  }

  // Preload critical assets
  preloadCritical(entries: AssetEntry[]): Promise<void> {
    const critical = entries.filter(e => e.priority === 'critical')
    return this.loadManifest({
      version: '1.0',
      models: critical.filter(e => e.type === 'model'),
      textures: critical.filter(e => e.type === 'texture'),
      audio: critical.filter(e => e.type === 'audio'),
      data: critical.filter(e => e.type === 'data'),
      shaders: critical.filter(e => e.type === 'shader'),
    })
  }

  // Get loaded asset
  getAsset(id: string): LoadedAsset | undefined {
    return this.loadedAssets.get(id)
  }

  // Check if asset is loaded
  isLoaded(id: string): boolean {
    return this.loadedAssets.has(id)
  }

  // Get cached asset
  getCached(url: string): unknown {
    return this.cache.get(url)
  }

  // Clear cache
  clearCache(): void {
    this.cache.clear()
  }

  // Remove asset
  unload(id: string): void {
    const asset = this.loadedAssets.get(id)
    if (asset) {
      this.disposeAsset(asset)
      this.loadedAssets.delete(id)
    }
  }

  // Get loading stats
  getStats() {
    return {
      queued: this.loadQueue.length,
      loading: this.loading.size,
      loaded: this.loadedAssets.size,
      cached: this.cache.size,
      progress: this.totalQueueSize > 0 ? this.loadedQueueSize / this.totalQueueSize : 1,
    }
  }

  // Set max concurrent loads
  setMaxConcurrentLoads(max: number): void {
    this.maxConcurrentLoads = Math.max(1, max)
    this.processQueue()
  }

  // Private methods

  private enqueue(request: LoadRequest): void {
    this.loadQueue.push(request)
    this.loadQueue.sort((a, b) => PRIORITY_WEIGHTS[a.priority] - PRIORITY_WEIGHTS[b.priority])
    this.processQueue()
  }

  private processQueue(): void {
    while (this.activeLoads < this.maxConcurrentLoads && this.loadQueue.length > 0) {
      const request = this.loadQueue.shift()!
      this.executeLoad(request)
    }
  }

  private executeLoad(request: LoadRequest): void {
    if (this.loading.has(request.id)) return

    // Check cache first
    const cached = this.cache.get(request.url)
    if (cached) {
      this.finishLoad(request, cached)
      return
    }

    this.loading.add(request.id)
    this.activeLoads++

    let loadPromise: Promise<unknown>

    switch (request.type) {
      case 'model':
        loadPromise = this.loadModel(request.url)
        break
      case 'texture':
        loadPromise = this.loadTexture(request.url)
        break
      case 'audio':
        loadPromise = this.loadAudio(request.url)
        break
      case 'data':
        loadPromise = this.loadData(request.url)
        break
      case 'shader':
        loadPromise = this.loadShader(request.url)
        break
      case 'cubemap':
        loadPromise = this.loadCubemap(request.url)
        break
      default:
        loadPromise = Promise.reject(new Error(`Unknown asset type: ${request.type}`))
    }

    loadPromise
      .then(data => {
        this.finishLoad(request, data)
      })
      .catch(error => {
        this.handleError(request, error)
      })
  }

  private finishLoad(request: LoadRequest, data: unknown): void {
    this.loading.delete(request.id)
    this.activeLoads--

    const asset: LoadedAsset = {
      id: request.id,
      url: request.url,
      type: request.type,
      data,
      timestamp: Date.now(),
      size: this.estimateSize(data),
    }

    this.loadedAssets.set(request.id, asset)
    this.cache.set(request.url, data)
    this.loadedQueueSize++

    request.onComplete?.(data)

    // Update global progress
    if (this.onGlobalProgress) {
      this.onGlobalProgress(this.loadedQueueSize / this.totalQueueSize, this.loadedQueueSize, this.totalQueueSize)
    }

    this.processQueue()
  }

  private handleError(request: LoadRequest, error: Error): void {
    this.loading.delete(request.id)
    this.activeLoads--

    console.error(`Failed to load asset: ${request.id} (${request.url})`, error)
    request.onError?.(error)

    this.processQueue()
  }

  private onLoadComplete(): void {
    // Called when all items in loading manager complete
  }

  private onLoadProgress(url: string, loaded: number, total: number): void {
    // Find request by URL and call progress callback
    for (const request of this.loadQueue) {
      if (request.url === url) {
        request.onProgress?.(total > 0 ? loaded / total : 0)
        break
      }
    }
    // Also check active loads
    // (Would need to track URL -> request mapping)
  }

  private onLoadError(url: string): void {
    console.error(`Loading error for: ${url}`)
  }

  // Type-specific loaders

  private loadModel(url: string): Promise<THREE.Group> {
    return new Promise((resolve, reject) => {
      this.gltfLoader.load(
        url,
        (gltf) => {
          const group = gltf.scene
          group.name = url
          // Store animations, materials, etc. in userData
          group.userData = {
            animations: gltf.animations,
            cameras: gltf.cameras,
            parser: gltf.parser,
            asset: gltf.asset,
          }
          resolve(group)
        },
        undefined,
        reject
      )
    })
  }

  private loadTexture(url: string): Promise<THREE.Texture> {
    return new Promise((resolve, reject) => {
      // Check if it's a compressed format (KTX2 or Basis Universal)
      // KTX2Loader handles both .ktx2 and .basis files
      if (url.endsWith('.ktx2') || url.endsWith('.basis')) {
        this.ktx2Loader.load(url, resolve, undefined, reject)
      } else {
        this.textureLoader.load(url, resolve, undefined, reject)
      }
    })
  }

  private loadAudio(url: string): Promise<THREE.AudioBuffer> {
    return new Promise((resolve, reject) => {
      this.audioLoader.load(url, resolve, undefined, reject)
    })
  }

  private loadData(url: string): Promise<unknown> {
    return new Promise((resolve, reject) => {
      this.fileLoader.load(url, resolve, undefined, reject)
    })
  }

  private loadShader(url: string): Promise<string> {
    return new Promise((resolve, reject) => {
      fetch(url)
        .then(res => {
          if (!res.ok) throw new Error(`Failed to load shader: ${res.status}`)
          return res.text()
        })
        .then(resolve)
        .catch(reject)
    })
  }

  private loadCubemap(urls: string[]): Promise<THREE.CubeTexture> {
    return new Promise((resolve, reject) => {
      const loader = new THREE.CubeTextureLoader(this.loadingManager)
      loader.load(urls, resolve, undefined, reject)
    })
  }

  // Utility

  private estimateSize(data: unknown): number {
    if (data instanceof THREE.Texture) {
      const image = data.image
      if (image) {
        return (image.width * image.height * 4) // RGBA
      }
    }
    if (data instanceof THREE.Group) {
      let size = 0
      data.traverse(obj => {
        if (obj instanceof THREE.Mesh) {
          if (obj.geometry) size += obj.geometry.attributes.position?.count * 12 || 0
          if (obj.material) size += 1024 // Rough estimate
        }
      })
      return size
    }
    if (typeof data === 'string') {
      return data.length * 2 // UTF-16
    }
    if (data instanceof ArrayBuffer) {
      return data.byteLength
    }
    return JSON.stringify(data).length * 2
  }

  private disposeAsset(asset: LoadedAsset): void {
    const { data } = asset
    if (data instanceof THREE.Texture) {
      data.dispose()
    } else if (data instanceof THREE.Group) {
      data.traverse(obj => {
        if (obj instanceof THREE.Mesh) {
          obj.geometry?.dispose()
          if (Array.isArray(obj.material)) {
            obj.material.forEach(m => m.dispose())
          } else {
            obj.material?.dispose()
          }
        }
      })
    } else if (data instanceof THREE.AudioBuffer) {
      // AudioBuffer doesn't need explicit disposal
    }
  }

  // Dispose everything
  dispose(): void {
    for (const asset of this.loadedAssets.values()) {
      this.disposeAsset(asset)
    }
    this.loadedAssets.clear()
    this.cache.clear()
    this.loadQueue = []
    this.loading.clear()
    this.activeLoads = 0
    this.dracoLoader.dispose()
    this.ktx2Loader.dispose()
  }
}

// Singleton
let assetLoaderInstance: AssetLoader | null = null

export function createAssetLoader(): AssetLoader {
  if (assetLoaderInstance) {
    assetLoaderInstance.dispose()
  }
  assetLoaderInstance = new AssetLoader()
  return assetLoaderInstance
}

export function getAssetLoader(): AssetLoader | null {
  return assetLoaderInstance
}

// React hook
export function useAssetLoader() {
  const loader = getAssetLoader() || createAssetLoader()
  return {
    load: loader.load.bind(loader),
    loadManifest: loader.loadManifest.bind(loader),
    preloadCritical: loader.preloadCritical.bind(loader),
    getAsset: loader.getAsset.bind(loader),
    isLoaded: loader.isLoaded.bind(loader),
    unload: loader.unload.bind(loader),
    getStats: loader.getStats.bind(loader),
  }
}