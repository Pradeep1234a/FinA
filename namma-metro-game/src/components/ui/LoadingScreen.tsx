// Loading Screen Component
// Animated loading screen with progress bar and tips

import { useEffect, useState } from 'react'

interface LoadingScreenProps {
  progress: number // 0-100
  message?: string
}

const loadingTips = [
  "Tip: Press M to open the metro map and fast travel between stations.",
  "Tip: Your Metro Card earns XP when you ride trains. Level up for fare discounts!",
  "Tip: Bangalore weather changes dynamically. Rain affects visibility and NPC behavior.",
  "Tip: Each metro station has unique stories. Talk to commuters to discover them.",
  "Tip: Press C to toggle between third-person and first-person camera views.",
  "Tip: Sprint (Shift) consumes stamina. Walk to recover stamina automatically.",
  "Tip: Photo mode (P) lets you capture beautiful moments. Share them with friends!",
  "Tip: The Purple Line runs east-west. The Green Line runs north-south.",
  "Tip: Interchange stations like Majestic connect both lines for easy transfers.",
  "Tip: Time of day affects NPC schedules. Visit stations at night for different stories.",
  "Tip: Your choices in dialogue affect the story. Choose wisely!",
  "Tip: Fast travel costs money but saves time. Balance your Metro Card balance.",
  "Tip: Kannada voice-overs are available. Enable them in settings for immersion.",
  "Tip: Accessibility options include high contrast, large text, and reduced motion.",
  "Tip: The game auto-saves at stations and quest milestones. No manual saving needed!",
]

export function LoadingScreen({ progress, message = "Loading Namma Metro..." }: LoadingScreenProps) {
  const [tip, setTip] = useState(loadingTips[0])
  const [dots, setDots] = useState('')

  // Animate loading dots
  useEffect(() => {
    const interval = setInterval(() => {
      setDots(prev => prev.length >= 3 ? '' : prev + '.')
    }, 400)
    return () => clearInterval(interval)
  }, [])

  // Rotate tips
  useEffect(() => {
    const interval = setInterval(() => {
      setTip(prev => {
        const index = loadingTips.indexOf(prev)
        return loadingTips[(index + 1) % loadingTips.length]
      })
    }, 8000)
    return () => clearInterval(interval)
  }, [])

  return (
    <div className="loading-screen flex flex-col items-center justify-center">
      {/* Logo / Title */}
      <div className="flex flex-col items-center gap-4 mb-8">
        <div className="relative w-32 h-32">
          <svg className="w-full h-full text-metro-purple animate-pulse-slow" viewBox="0 0 100 100">
            <defs>
              <linearGradient id="metroGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stopColor="#6A1B9A" />
                <stop offset="100%" stopColor="#9C27B0" />
              </linearGradient>
            </defs>
            <circle cx="50" cy="50" r="45" fill="none" stroke="url(#metroGradient)" strokeWidth="4" strokeDasharray="283" strokeDashoffset="283">
              <animate attributeName="strokeDashoffset" from="283" to="0" dur="2s" repeatCount="indefinite" />
            </circle>
            <text x="50" y="58" textAnchor="middle" fontFamily="monospace" fontSize="24" fontWeight="bold" fill="url(#metroGradient)">M</text>
          </svg>
        </div>
        <div className="text-center">
          <h1 className="text-3xl font-bold text-ui-text tracking-tight">Namma Metro</h1>
          <p className="text-ui-text-muted mt-1">Open World Story Game</p>
        </div>
      </div>

      {/* Progress Bar */}
      <div className="w-96 max-w-[90vw]">
        <div className="flex justify-between text-xs text-ui-text-muted mb-2">
          <span>{message}</span>
          <span className="font-mono">{progress}%</span>
        </div>
        <div className="loading-bar">
          <div
            className="loading-bar-fill"
            style={{ width: `${Math.min(progress, 100)}%` }}
          />
        </div>
      </div>

      {/* Loading Tip */}
      <div className="mt-8 w-96 max-w-[90vw] text-center">
        <div className="bg-ui-panel/50 border border-ui-border/30 rounded-xl p-4 animate-fade-in">
          <p className="text-xs text-metro-purple-light uppercase tracking-wide font-medium mb-1">Loading Tip</p>
          <p className="text-sm text-ui-text">{tip}</p>
        </div>
      </div>

      {/* Bangalore ASCII Art */}
      <div className="mt-8 text-ui-text-muted/30 font-mono text-xs select-none">
        <pre>{`
    ██████╗ ██████╗ ███╗   ██╗███████╗███████╗██████╗
    ██╔══██╗██╔══██╗████╗  ██║██╔════╝██╔════╝██╔══██╗
    ██████╔╝██████╔╝██╔██╗ ██║█████╗  ███████╗██████╔╝
    ██╔══██╗██╔══██╗██║╚██╗██║██╔══╝  ╚════██║██╔═══╝
    ██║  ██║██║  ██║██║ ╚████║███████╗███████║██║
    ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚══════╝╚══════╝╚═╝
        `}</pre>
      </div>

      {/* Version */}
      <div className="fixed bottom-4 right-4 text-xs text-ui-text-muted/50 font-mono">
        v0.1.0-alpha
      </div>
    </div>
  )
}