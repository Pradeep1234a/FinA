/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        metro: {
          purple: '#6A1B9A',
          'purple-light': '#9C27B0',
          'purple-dark': '#4A148C',
          green: '#2E7D32',
          'green-light': '#4CAF50',
          'green-dark': '#1B5E20',
          gold: '#FFD700',
          'gold-light': '#FFEB3B',
        },
        bangalore: {
          sky: '#87CEEB',
          'sky-dawn': '#FFB6C1',
          'sky-dusk': '#E6E6FA',
          garden: '#228B22',
          stone: '#8B7355',
          tech: '#00BFFF',
        },
        ui: {
          bg: '#0A0A0F',
          panel: '#14141F',
          border: '#2A2A3E',
          text: '#E8E8F0',
          'text-muted': '#8888A0',
          accent: '#00D4AA',
          danger: '#FF4757',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
        kannada: ['Noto Sans Kannada', 'sans-serif'],
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'spin-slow': 'spin 2s linear infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
      },
    },
  },
  plugins: [],
}