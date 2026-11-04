import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import type {
  Quest,
  QuestObjective,
  DialogueNode,
  DialogueChoice,
  StoryFlag,
  StoryState,
  QuestReward,
} from '@types'

interface NarrativeStore extends StoryState {
  // Quest management
  quests: Map<string, Quest>
  activeQuestId: string | null
  dialogueTree: Map<string, DialogueNode>
  currentDialogueId: string | null
  currentDialogueNodeId: string | null
  dialogueHistory: Array<{ nodeId: string; choiceId?: string; timestamp: number }>

  // Actions
  // Quests
  registerQuest: (quest: Quest) => void
  getQuest: (questId: string) => Quest | undefined
  getQuestsByType: (type: Quest['type']) => Quest[]
  getAvailableQuests: (location?: string) => Quest[]
  startQuest: (questId: string) => boolean
  completeQuest: (questId: string) => void
  failQuest: (questId: string) => void
  updateObjective: (questId: string, objectiveId: string, count: number) => void
  getActiveQuest: () => Quest | null
  getActiveObjectives: () => QuestObjective[]
  isQuestPrerequisitesMet: (questId: string) => boolean
  getQuestProgress: (questId: string) => number

  // Dialogue
  registerDialogueTree: (treeId: string, nodes: DialogueNode[]) => void
  startDialogue: (treeId: string, startNodeId?: string) => void
  endDialogue: () => void
  makeChoice: (choiceId: string) => void
  getCurrentNode: () => DialogueNode | null
  getAvailableChoices: () => DialogueChoice[]
  evaluateConditions: (conditions: DialogueNode['conditions']) => boolean

  // Story flags
  setFlag: (flagId: string, value: boolean | number | string) => void
  getFlag: (flagId: string) => StoryFlag | undefined
  hasFlag: (flagId: string) => boolean
  clearFlag: (flagId: string) => void

  // Story progression
  setChapter: (chapter: number) => void
  setAct: (act: number) => void
  addPlayTime: (time: number) => void
  recordChoice: (choiceId: string, selectedOption: string) => void
  getEnding: () => string | null

  // Rewards
  grantReward: (reward: QuestReward) => void

  // Save/Load
  loadFromSave: (save: StoryState) => void
}

const initialState: StoryState = {
  flags: new Map(),
  completedQuests: [],
  failedQuests: [],
  choices: new Map(),
  endings: [],
  playTime: 0,
  chapter: 1,
  act: 1,
}

export const useNarrativeStore = create<NarrativeStore>()(
  immer((set, get) => ({
    ...initialState,
    quests: new Map(),
    activeQuestId: null,
    dialogueTree: new Map(),
    currentDialogueId: null,
    currentDialogueNodeId: null,
    dialogueHistory: [],

    // Quests
    registerQuest: (quest: Quest) => set((state) => {
      state.quests.set(quest.id, quest)
    }),

    getQuest: (questId: string) => {
      return get().quests.get(questId)
    },

    getQuestsByType: (type: Quest['type']) => {
      return Array.from(get().quests.values()).filter(q => q.type === type)
    },

    getAvailableQuests: (location?: string) => {
      return Array.from(get().quests.values()).filter(quest => {
        if (quest.status !== 'available' && quest.status !== 'locked') return false
        if (quest.status === 'locked') {
          return get().isQuestPrerequisitesMet(quest.id)
        }
        if (location && quest.location !== location) return false
        return true
      })
    },

    startQuest: (questId: string) => {
      const quest = get().quests.get(questId)
      if (!quest) return false
      if (quest.status !== 'available' && quest.status !== 'locked') return false
      if (!get().isQuestPrerequisitesMet(questId)) return false

      set((state) => {
        quest.status = 'active'
        state.activeQuestId = questId
        // Initialize objectives
        for (const obj of quest.objectives) {
          obj.currentCount = 0
        }
      })

      get().emit('quest:start', { questId })
      return true
    },

    completeQuest: (questId: string) => {
      const quest = get().quests.get(questId)
      if (!quest || quest.status !== 'active') return

      set((state) => {
        quest.status = 'completed'
        state.completedQuests.push(questId)
        if (state.activeQuestId === questId) {
          state.activeQuestId = null
        }
        // Grant rewards
        for (const reward of quest.rewards) {
          get().grantReward(reward)
        }
      })

      get().emit('quest:complete', { questId })
    },

    failQuest: (questId: string) => {
      const quest = get().quests.get(questId)
      if (!quest || quest.status !== 'active') return

      set((state) => {
        quest.status = 'failed'
        state.failedQuests.push(questId)
        if (state.activeQuestId === questId) {
          state.activeQuestId = null
        }
      })

      get().emit('quest:fail', { questId })
    },

    updateObjective: (questId: string, objectiveId: string, count: number) => {
      const quest = get().quests.get(questId)
      if (!quest || quest.status !== 'active') return

      const objective = quest.objectives.find(o => o.id === objectiveId)
      if (!objective) return

      set((state) => {
        objective.currentCount = Math.min(count, objective.count)
        // Check if all objectives complete
        const allComplete = quest.objectives.every(o => o.currentCount >= o.count)
        if (allComplete) {
          get().completeQuest(questId)
        } else {
          get().emit('quest:objective-update', { questId, objectiveId, count: objective.currentCount })
        }
      })
    },

    getActiveQuest: () => {
      const { activeQuestId, quests } = get()
      if (!activeQuestId) return null
      return quests.get(activeQuestId) || null
    },

    getActiveObjectives: () => {
      const quest = get().getActiveQuest()
      return quest ? quest.objectives.filter(o => o.currentCount < o.count) : []
    },

    isQuestPrerequisitesMet: (questId: string) => {
      const quest = get().quests.get(questId)
      if (!quest) return false
      return quest.prerequisites.every(prereq => get().completedQuests.includes(prereq))
    },

    getQuestProgress: (questId: string) => {
      const quest = get().quests.get(questId)
      if (!quest) return 0
      const total = quest.objectives.reduce((sum, o) => sum + o.count, 0)
      const current = quest.objectives.reduce((sum, o) => sum + o.currentCount, 0)
      return total > 0 ? current / total : 1
    },

    // Dialogue
    registerDialogueTree: (treeId: string, nodes: DialogueNode[]) => set((state) => {
      for (const node of nodes) {
        const key = `${treeId}:${node.id}`
        state.dialogueTree.set(key, node)
      }
    }),

    startDialogue: (treeId: string, startNodeId = 'start') => {
      const startNode = get().dialogueTree.get(`${treeId}:${startNodeId}`)
      if (!startNode) return

      set((state) => {
        state.currentDialogueId = treeId
        state.currentDialogueNodeId = startNodeId
        state.dialogueHistory = [{ nodeId: startNodeId, timestamp: Date.now() }]
      })

      get().emit('dialogue:start', { treeId, nodeId: startNodeId })
    },

    endDialogue: () => {
      const { currentDialogueId, currentDialogueNodeId } = get()
      set((state) => {
        state.currentDialogueId = null
        state.currentDialogueNodeId = null
        state.dialogueHistory = []
      })
      get().emit('dialogue:end', { treeId: currentDialogueId, nodeId: currentDialogueNodeId })
    },

    makeChoice: (choiceId: string) => {
      const { currentDialogueId, currentDialogueNodeId } = get()
      if (!currentDialogueId || !currentDialogueNodeId) return

      const currentNode = get().dialogueTree.get(`${currentDialogueId}:${currentDialogueNodeId}`)
      if (!currentNode) return

      const choice = currentNode.choices.find(c => c.id === choiceId)
      if (!choice) return

      // Evaluate choice conditions
      if (!get().evaluateConditions(choice.conditions)) return

      // Execute choice actions
      for (const action of choice.actions) {
        get().executeDialogueAction(action)
      }

      // Move to next node
      const nextNode = get().dialogueTree.get(`${currentDialogueId}:${choice.nextNodeId}`)
      if (nextNode) {
        set((state) => {
          state.currentDialogueNodeId = choice.nextNodeId
          state.dialogueHistory.push({ nodeId: choice.nextNodeId, choiceId, timestamp: Date.now() })
        })
        get().emit('dialogue:choice', { treeId: currentDialogueId, nodeId: choice.nextNodeId, choiceId })
      } else if (choice.nextNodeId === 'end') {
        get().endDialogue()
      }
    },

    getCurrentNode: () => {
      const { currentDialogueId, currentDialogueNodeId } = get()
      if (!currentDialogueId || !currentDialogueNodeId) return null
      return get().dialogueTree.get(`${currentDialogueId}:${currentDialogueNodeId}`) || null
    },

    getAvailableChoices: () => {
      const node = get().getCurrentNode()
      if (!node) return []
      return node.choices.filter(choice => get().evaluateConditions(choice.conditions))
    },

    evaluateConditions: (conditions: DialogueNode['conditions']) => {
      for (const condition of conditions) {
        let result = false
        switch (condition.type) {
          case 'quest-state': {
            const quest = get().quests.get(condition.value as string)
            if (!quest) return false
            result = quest.status === condition.value
            break
          }
          case 'flag': {
            const flag = get().flags.get(condition.value as string)
            if (!flag) return false
            switch (condition.operator) {
              case 'equals': result = flag.value === condition.value; break
              case 'not-equals': result = flag.value !== condition.value; break
              case 'contains': result = String(flag.value).includes(String(condition.value)); break
              case 'not-contains': result = !String(flag.value).includes(String(condition.value)); break
              default: return false
            }
            break
          }
          case 'item': {
            const { usePlayerStore } = require('./playerStore')
            const hasItem = usePlayerStore.getState().inventory.some(i => i.id === condition.value)
            result = condition.operator === 'equals' ? hasItem : !hasItem
            break
          }
          case 'metro-card-level': {
            const { usePlayerStore } = require('./playerStore')
            const level = usePlayerStore.getState().metroCard.level
            switch (condition.operator) {
              case 'equals': result = level === condition.value; break
              case 'greater': result = level > (condition.value as number); break
              case 'less': result = level < (condition.value as number); break
              default: return false
            }
            break
          }
          case 'time-of-day': {
            const { useWorldStore } = require('./worldStore')
            const timeLabel = useWorldStore.getState().getTimeOfDayLabel()
            result = condition.operator === 'equals' ? timeLabel === condition.value : timeLabel !== condition.value
            break
          }
          case 'weather': {
            const { useWorldStore } = require('./worldStore')
            const weather = useWorldStore.getState().weather
            result = condition.operator === 'equals' ? weather === condition.value : weather !== condition.value
            break
          }
          case 'location': {
            // TODO: Check player location
            result = true
            break
          }
        }
        if (!result) return false
      }
      return true
    },

    executeDialogueAction: (action: { type: string; params: Record<string, unknown> }) => {
      switch (action.type) {
        case 'set-flag':
          get().setFlag(action.params.flagId as string, action.params.value as boolean | number | string)
          break
        case 'unset-flag':
          get().clearFlag(action.params.flagId as string)
          break
        case 'start-quest':
          get().startQuest(action.params.questId as string)
          break
        case 'complete-quest':
          get().completeQuest(action.params.questId as string)
          break
        case 'fail-quest':
          get().failQuest(action.params.questId as string)
          break
        case 'give-item': {
          const { usePlayerStore } = require('./playerStore')
          usePlayerStore.getState().addInventoryItem(action.params.item as any)
          break
        }
        case 'remove-item': {
          const { usePlayerStore } = require('./playerStore')
          usePlayerStore.getState().removeInventoryItem(action.params.itemId as string, action.params.quantity as number)
          break
        }
        case 'add-metro-card-xp': {
          const { usePlayerStore } = require('./playerStore')
          const xp = action.params.amount as number
          const card = usePlayerStore.getState().metroCard
          // TODO: Implement XP system
          break
        }
        case 'unlock-fast-travel': {
          const { useWorldStore } = require('./worldStore')
          useWorldStore.getState().unlockFastTravel(action.params.stationId as string)
          break
        }
        case 'play-sound':
          // TODO: Play sound
          break
        case 'trigger-cutscene':
          // TODO: Trigger cutscene
          break
        case 'teleport':
          // TODO: Teleport player
          break
        case 'set-time':
          const { useWorldStore } = require('./worldStore')
          useWorldStore.getState().setTimeOfDay(action.params.time as number)
          break
      }
    },

    // Story flags
    setFlag: (flagId: string, value: boolean | number | string) => set((state) => {
      state.flags.set(flagId, { id: flagId, value, timestamp: Date.now() })
    }),

    getFlag: (flagId: string) => {
      return get().flags.get(flagId)
    },

    hasFlag: (flagId: string) => {
      return get().flags.has(flagId)
    },

    clearFlag: (flagId: string) => set((state) => {
      state.flags.delete(flagId)
    }),

    // Story progression
    setChapter: (chapter: number) => set((state) => {
      state.chapter = chapter
    }),

    setAct: (act: number) => set((state) => {
      state.act = act
    }),

    addPlayTime: (time: number) => set((state) => {
      state.playTime += time
    }),

    recordChoice: (choiceId: string, selectedOption: string) => set((state) => {
      state.choices.set(choiceId, selectedOption)
    }),

    getEnding: () => {
      const { endings } = get()
      return endings.length > 0 ? endings[endings.length - 1] : null
    },

    // Rewards
    grantReward: (reward: QuestReward) => {
      switch (reward.type) {
        case 'xp':
        case 'metro-card-xp':
          // TODO: Grant XP
          break
        case 'balance': {
          const { usePlayerStore } = require('./playerStore')
          usePlayerStore.getState().updateMetroCard({ balance: usePlayerStore.getState().metroCard.balance + (reward.value as number) })
          break
        }
        case 'item': {
          const { usePlayerStore } = require('./playerStore')
          usePlayerStore.getState().addInventoryItem(reward.value as any)
          break
        }
        case 'lore': {
          const { usePlayerStore } = require('./playerStore')
          usePlayerStore.getState().updateMetroCard({ loreUnlocked: [...usePlayerStore.getState().metroCard.loreUnlocked, reward.value as string] })
          break
        }
        case 'fast-travel': {
          const { useWorldStore } = require('./worldStore')
          useWorldStore.getState().unlockFastTravel(reward.value as string)
          break
        }
        case 'outfit':
          // TODO: Unlock outfit
          break
      }
    },

    // Save/Load
    loadFromSave: (save: StoryState) => set((state) => {
      state.flags = save.flags
      state.completedQuests = save.completedQuests
      state.failedQuests = save.failedQuests
      state.choices = save.choices
      state.endings = save.endings
      state.playTime = save.playTime
      state.chapter = save.chapter
      state.act = save.act

      // Restore quest states
      for (const questId of save.completedQuests) {
        const quest = state.quests.get(questId)
        if (quest) quest.status = 'completed'
      }
      for (const questId of save.failedQuests) {
        const quest = state.quests.get(questId)
        if (quest) quest.status = 'failed'
      }
    }),
  }))
)

// Helper to emit events (would need gameStore)
declare global {
  interface Window {
    __NARRATIVE_EMIT__?: (type: string, payload: unknown) => void
  }
}

// Selector hooks
export const useQuests = () => useNarrativeStore((state) => state.quests)
export const useActiveQuest = () => useNarrativeStore((state) => state.activeQuestId)
export const useCurrentDialogue = () => useNarrativeStore((state) => ({
  treeId: state.currentDialogueId,
  nodeId: state.currentDialogueNodeId,
  node: state.getCurrentNode(),
  choices: state.getAvailableChoices(),
}))
export const useStoryFlags = () => useNarrativeStore((state) => state.flags)
export const useChapter = () => useNarrativeStore((state) => state.chapter)
export const useAct = () => useNarrativeStore((state) => state.act)
export const useNarrativeActions = () => useNarrativeStore((state) => ({
  registerQuest: state.registerQuest,
  startQuest: state.startQuest,
  completeQuest: state.completeQuest,
  failQuest: state.failQuest,
  updateObjective: state.updateObjective,
  registerDialogueTree: state.registerDialogueTree,
  startDialogue: state.startDialogue,
  endDialogue: state.endDialogue,
  makeChoice: state.makeChoice,
  setFlag: state.setFlag,
  getFlag: state.getFlag,
  hasFlag: state.hasFlag,
  setChapter: state.setChapter,
  setAct: state.setAct,
}))