// Core game types

export interface Vector3 {
  x: number
  y: number
  z: number
}

export interface Vector2 {
  x: number
  y: number
}

export interface Quaternion {
  x: number
  y: number
  z: number
  w: number
}

export interface Euler {
  x: number
  y: number
  z: number
  order?: string
}

export interface Matrix4 {
  elements: number[]
}

export interface Color {
  r: number
  g: number
  b: number
}

export interface BoundingBox {
  min: Vector3
  max: Vector3
}

export interface BoundingSphere {
  center: Vector3
  radius: number
}

// Game state types
export type GameState = 'loading' | 'main-menu' | 'playing' | 'paused' | 'dialogue' | 'cutscene' | 'map' | 'inventory'

export type MovementState = 'idle' | 'walk' | 'run' | 'sprint' | 'crouch' | 'jump' | 'climb' | 'swim' | 'ride' | 'interact'

export type CameraMode = 'third-person' | 'first-person' | 'cinematic' | 'metro-ride'

export type WeatherType = 'clear' | 'rain' | 'mist' | 'dust' | 'storm'

export type TimeOfDay = 'dawn' | 'morning' | 'noon' | 'afternoon' | 'evening' | 'dusk' | 'night'

// Input types
export interface InputState {
  moveForward: boolean
  moveBackward: boolean
  moveLeft: boolean
  moveRight: boolean
  jump: boolean
  sprint: boolean
  crouch: boolean
  interact: boolean
  cameraToggle: boolean
  mapToggle: boolean
  inventoryToggle: boolean
  pause: boolean
  mouseDelta: Vector2
  mousePosition: Vector2
  wheelDelta: number
  touch: TouchState | null
}

export interface TouchState {
  touches: TouchPoint[]
  gestures: GestureState[]
}

export interface TouchPoint {
  id: number
  x: number
  y: number
  force: number
  startX: number
  startY: number
  startTime: number
}

export interface GestureState {
  type: 'tap' | 'double-tap' | 'pan' | 'pinch' | 'rotate'
  startTime: number
  center: Vector2
  scale: number
  rotation: number
}

// Player types
export interface PlayerState {
  position: Vector3
  rotation: Quaternion
  velocity: Vector3
  movementState: MovementState
  cameraMode: CameraMode
  health: number
  stamina: number
  maxStamina: number
  isGrounded: boolean
  isInWater: boolean
  isOnLadder: boolean
  currentMetroTrain: string | null
  metroCard: MetroCardState
  inventory: InventoryItem[]
  currentQuest: string | null
}

export interface MetroCardState {
  level: number
  balance: number
  tapSpeedBonus: number
  fareDiscount: number
  loreUnlocked: string[]
}

export interface InventoryItem {
  id: string
  type: 'clue' | 'map' | 'photo' | 'voice-memo' | 'key' | 'tool' | 'consumable'
  name: string
  description: string
  icon: string
  quantity: number
  metadata?: Record<string, unknown>
}

// World types
export interface WorldChunk {
  id: string
  position: Vector2
  bounds: BoundingBox
  isLoaded: boolean
  lod: number
  entities: WorldEntity[]
}

export interface WorldEntity {
  id: string
  type: 'building' | 'prop' | 'vegetation' | 'npc' | 'vehicle' | 'metro-station' | 'metro-track' | 'landmark'
  position: Vector3
  rotation: Quaternion
  scale: Vector3
  modelUrl: string
  lodUrls?: string[]
  metadata?: Record<string, unknown>
}

export interface MetroStation {
  id: string
  name: string
  nameKannada: string
  line: 'purple' | 'green' | 'blue' | 'interchange'
  position: Vector3
  type: 'elevated' | 'underground' | 'at-grade'
  platforms: MetroPlatform[]
  concourse: Vector3
  entrances: Vector3[]
  landmarks: string[]
  fastTravelEnabled: boolean
  zone: string
}

export interface MetroPlatform {
  id: string
  stationId: string
  line: 'purple' | 'green' | 'blue'
  direction: 'northbound' | 'southbound' | 'eastbound' | 'westbound'
  position: Vector3
  length: number
  height: number
  hasScreenDoors: boolean
  benchPositions: Vector3[]
}

export interface MetroTrain {
  id: string
  line: 'purple' | 'green' | 'blue'
  cars: MetroCar[]
  position: Vector3
  rotation: Quaternion
  speed: number
  targetSpeed: number
  currentStation: string | null
  nextStation: string | null
  schedule: MetroScheduleEntry[]
  scheduleIndex: number
  isPlayerBoarding: boolean
  doorsOpen: boolean
}

export interface MetroCar {
  id: string
  trainId: string
  index: number
  type: 'motor' | 'trailer' | 'cab'
  position: Vector3
  rotation: Quaternion
  capacity: number
  occupancy: number
  doors: MetroDoor[]
  interior: MetroInterior
}

export interface MetroDoor {
  side: 'left' | 'right'
  isOpen: boolean
  openProgress: number
}

export interface MetroInterior {
  seats: Vector3[]
  handrails: Vector3[]
  cameraPosition: Vector3
  windowPositions: Vector3[]
}

export interface MetroScheduleEntry {
  stationId: string
  arrivalTime: number
  departureTime: number
  dwellTime: number
}

export interface MetroRoute {
  from: string
  to: string
  line: 'purple' | 'green' | 'blue'
  stations: string[]
  distance: number
  duration: number
  fare: number
  transfers: MetroTransfer[]
}

export interface MetroTransfer {
  stationId: string
  fromLine: string
  toLine: string
  walkTime: number
}

// Narrative types
export interface Quest {
  id: string
  title: string
  titleKannada: string
  description: string
  descriptionKannada: string
  type: 'main' | 'side' | 'station-story' | 'commuter-chronicle' | 'urban-legend'
  status: 'locked' | 'available' | 'active' | 'completed' | 'failed'
  prerequisites: string[]
  objectives: QuestObjective[]
  rewards: QuestReward[]
  chapter: number
  act: number
  location?: string
  npcId?: string
}

export interface QuestObjective {
  id: string
  description: string
  descriptionKannada: string
  type: 'go-to' | 'interact' | 'talk' | 'find' | 'photograph' | 'ride' | 'wait' | 'solve'
  targetId: string
  targetPosition?: Vector3
  count: number
  currentCount: number
  isOptional: boolean
  isHidden: boolean
}

export interface QuestReward {
  type: 'xp' | 'metro-card-xp' | 'balance' | 'item' | 'lore' | 'fast-travel' | 'outfit'
  value: number | string
  metadata?: Record<string, unknown>
}

export interface DialogueNode {
  id: string
  speakerId: string
  speakerName: string
  speakerNameKannada: string
  text: string
  textKannada: string
  voiceId?: string
  portrait?: string
  conditions: DialogueCondition[]
  choices: DialogueChoice[]
  actions: DialogueAction[]
  isEnd: boolean
  autoAdvance: boolean
  advanceDelay: number
}

export interface DialogueCondition {
  type: 'quest-state' | 'flag' | 'item' | 'metro-card-level' | 'time-of-day' | 'weather' | 'location'
  operator: 'equals' | 'not-equals' | 'greater' | 'less' | 'contains' | 'not-contains'
  value: string | number | boolean
}

export interface DialogueChoice {
  id: string
  text: string
  textKannada: string
  conditions: DialogueCondition[]
  nextNodeId: string
  actions: DialogueAction[]
  isDefault: boolean
}

export interface DialogueAction {
  type: 'set-flag' | 'unset-flag' | 'start-quest' | 'complete-quest' | 'fail-quest' | 'give-item' | 'remove-item' | 'add-metro-card-xp' | 'unlock-fast-travel' | 'play-sound' | 'trigger-cutscene' | 'teleport' | 'set-time'
  params: Record<string, unknown>
}

export interface StoryFlag {
  id: string
  value: boolean | number | string
  timestamp: number
}

export interface StoryState {
  flags: Map<string, StoryFlag>
  completedQuests: string[]
  failedQuests: string[]
  choices: Map<string, string>
  endings: string[]
  playTime: number
  chapter: number
  act: number
}

// NPC types
export interface NPC {
  id: string
  name: string
  nameKannada: string
  archetype: 'commuter' | 'vendor' | 'guard' | 'official' | 'story' | 'crowd'
  personality: NPCPersonality
  schedule: NPCScheduleEntry[]
  currentBehavior: NPCBehavior
  dialogueTree: string
  position: Vector3
  rotation: Quaternion
  modelUrl: string
  animationSet: string
  isInteractable: boolean
  interactionRadius: number
}

export interface NPCPersonality {
  friendliness: number
  curiosity: number
  urgency: number
  talkativeness: number
  knowledge: string[]
}

export interface NPCScheduleEntry {
  timeStart: number
  timeEnd: number
  behavior: NPCBehavior
  location: Vector3
  activity: string
}

export type NPCBehavior = 'idle' | 'walk' | 'wait' | 'work' | 'patrol' | 'commute' | 'flee' | 'follow' | 'interact'

export interface CrowdAgent {
  id: string
  position: Vector3
  velocity: Vector3
  target: Vector3
  state: 'walking' | 'waiting' | 'boarding' | 'alighting' | 'idle'
  modelVariant: number
}

// Audio types
export interface AudioSource {
  id: string
  type: 'ambient' | 'sfx' | 'music' | 'voice' | 'ui'
  position?: Vector3
  isPositional: boolean
  volume: number
  pitch: number
  loop: boolean
  maxDistance: number
  rolloffFactor: number
  coneInnerAngle: number
  coneOuterAngle: number
  coneOuterGain: number
}

export interface AudioLayer {
  id: string
  name: string
  tracks: AudioTrack[]
  volume: number
  isActive: boolean
  crossfadeDuration: number
}

export interface AudioTrack {
  id: string
  url: string
  volume: number
  loop: boolean
  fadeIn: number
  fadeOut: number
  startDelay: number
  conditions: AudioCondition[]
}

export interface AudioCondition {
  type: 'time-of-day' | 'weather' | 'location' | 'quest-state' | 'proximity' | 'camera-mode'
  operator: 'equals' | 'not-equals' | 'in-range'
  value: string | number | { min: number; max: number }
}

// Settings types
export interface GameSettings {
  graphics: GraphicsSettings
  audio: AudioSettings
  controls: ControlSettings
  accessibility: AccessibilitySettings
  gameplay: GameplaySettings
}

export interface GraphicsSettings {
  quality: 'low' | 'medium' | 'high' | 'ultra'
  resolutionScale: number
  vsync: boolean
  fov: number
  motionBlur: boolean
  bloom: boolean
  ssao: boolean
  shadows: boolean
  shadowQuality: 'low' | 'medium' | 'high'
  lodBias: number
  particleDensity: number
  crowdDensity: number
}

export interface AudioSettings {
  masterVolume: number
  musicVolume: number
  sfxVolume: number
  voiceVolume: number
  ambientVolume: number
  uiVolume: number
  spatialAudio: boolean
  dynamicMusic: boolean
}

export interface ControlSettings {
  mouseSensitivity: number
  gamepadSensitivity: number
  invertY: boolean
  toggleSprint: boolean
  toggleCrouch: boolean
  cameraDistance: number
  cameraHeight: number
  keybindings: Keybindings
}

export interface Keybindings {
  moveForward: string
  moveBackward: string
  moveLeft: string
  moveRight: string
  jump: string
  sprint: string
  crouch: string
  interact: string
  cameraToggle: string
  mapToggle: string
  inventoryToggle: string
  pause: string
  photoMode: string
  screenshot: string
}

export interface AccessibilitySettings {
  highContrast: boolean
  largeText: boolean
  reducedMotion: boolean
  screenReader: boolean
  subtitles: boolean
  subtitleSize: 'small' | 'medium' | 'large'
  subtitleBackground: boolean
  colorBlindMode: 'none' | 'protanopia' | 'deuteranopia' | 'tritanopia'
  holdToInteract: boolean
  autoMove: boolean
}

export interface GameplaySettings {
  difficulty: 'story' | 'normal' | 'hard'
  autoSave: boolean
  showQuestMarkers: boolean
  showCompass: boolean
  showMetroMap: boolean
  fastTravelConfirmation: boolean
  photoModeEnabled: boolean
}

// Save data types
export interface SaveData {
  version: number
  timestamp: number
  playTime: number
  player: PlayerState
  world: WorldSaveState
  narrative: StoryState
  settings: GameSettings
  metadata: SaveMetadata
}

export interface WorldSaveState {
  loadedChunks: string[]
  entityStates: Map<string, EntitySaveState>
  metroStates: Map<string, MetroTrainSaveState>
  timeOfDay: number
  weather: WeatherType
  weatherIntensity: number
}

export interface EntitySaveState {
  id: string
  position: Vector3
  rotation: Quaternion
  state: Record<string, unknown>
}

export interface MetroTrainSaveState {
  id: string
  position: Vector3
  scheduleIndex: number
  doorsOpen: boolean
}

export interface SaveMetadata {
  gameVersion: string
  saveName: string
  screenshot?: string
  location: string
  chapter: number
  act: number
}

// Asset types
export interface AssetManifest {
  version: string
  models: AssetEntry[]
  textures: AssetEntry[]
  audio: AssetEntry[]
  data: AssetEntry[]
  shaders: AssetEntry[]
}

export interface AssetEntry {
  id: string
  url: string
  type: 'model' | 'texture' | 'audio' | 'data' | 'shader'
  priority: 'critical' | 'high' | 'normal' | 'low'
  size: number
  hash: string
  dependencies: string[]
  lods?: string[]
  compressed?: boolean
}

// Event types
export interface GameEvent {
  type: string
  payload: unknown
  timestamp: number
}

export type GameEventType =
  | 'game:init'
  | 'game:start'
  | 'game:pause'
  | 'game:resume'
  | 'game:save'
  | 'game:load'
  | 'player:move'
  | 'player:interact'
  | 'player:board-metro'
  | 'player:alight-metro'
  | 'player:fast-travel'
  | 'player:level-up'
  | 'quest:start'
  | 'quest:complete'
  | 'quest:fail'
  | 'quest:objective-update'
  | 'dialogue:start'
  | 'dialogue:end'
  | 'dialogue:choice'
  | 'npc:spawn'
  | 'npc:despawn'
  | 'npc:interact'
  | 'metro:train-arrive'
  | 'metro:train-depart'
  | 'metro:doors-open'
  | 'metro:doors-close'
  | 'world:chunk-load'
  | 'world:chunk-unload'
  | 'time:change'
  | 'weather:change'
  | 'settings:change'
  | 'photo:capture'
  | 'achievement:unlock'