import { useEffect, useRef, useState, Component, ReactNode } from 'react'
import { GameProvider } from '@components/game/GameProvider'
import { GameCanvas } from '@components/game/GameCanvas'
import { UIOverlay } from '@components/ui/UIOverlay'
import { LoadingScreen } from '@components/ui/LoadingScreen'

// Error Boundary to catch render errors
class ErrorBoundary extends Component<{ children: ReactNode; fallback: ReactNode }, { hasError: boolean; error?: Error }> {
  state = { hasError: false, error: undefined }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('🔴 ErrorBoundary caught:', error, errorInfo)
  }

  render() {
    if (this.state.hasError) {
      return this.props.fallback
    }
    return this.props.children
  }
}

function App() {
  const [isLoading, setIsLoading] = useState(true)
  const [loadProgress, setLoadProgress] = useState(0)
  const [initError, setInitError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    // Simulate asset loading for now
    const simulateLoading = async () => {
      try {
        for (let i = 0; i <= 100; i += 10) {
          if (cancelled) return
          await new Promise(resolve => setTimeout(resolve, 50))
          setLoadProgress(i)
        }
        if (!cancelled) setIsLoading(false)
      } catch (err) {
        console.error('Loading error:', err)
        if (!cancelled) setInitError(String(err))
      }
    }
    simulateLoading()
    return () => { cancelled = true }
  }, [])

  if (initError) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-ui-bg text-ui-text p-4">
        <div className="bg-ui-panel border border-ui-danger rounded-xl p-6 max-w-md text-center">
          <h2 className="text-xl font-bold text-ui-danger mb-2">Initialization Error</h2>
          <pre className="text-sm text-left bg-ui-bg p-4 rounded overflow-auto whitespace-pre-wrap">{initError}</pre>
          <button className="mt-4 px-4 py-2 bg-metro-purple rounded-lg" onClick={() => window.location.reload()}>
            Reload
          </button>
        </div>
      </div>
    )
  }

  if (isLoading) {
    return <LoadingScreen progress={loadProgress} />
  }

  return (
    <ErrorBoundary
      fallback={
        <div className="fixed inset-0 flex items-center justify-center bg-ui-bg text-ui-text p-4">
          <div className="bg-ui-panel border border-ui-danger rounded-xl p-6 max-w-md text-center">
            <h2 className="text-xl font-bold text-ui-danger mb-2">Runtime Error</h2>
            <p className="text-sm mb-4">Game failed to initialize. Check console (F12) for details.</p>
            <button className="px-4 py-2 bg-metro-purple rounded-lg" onClick={() => window.location.reload()}>
              Reload
            </button>
          </div>
        </div>
      }
    >
      <GameProvider>
        <div className="relative w-full h-full">
          <canvas id="game-canvas" className="w-full h-full" />
          <div className="ui-overlay">
            <UIOverlay />
          </div>
        </div>
      </GameProvider>
    </ErrorBoundary>
  )
}

export default App