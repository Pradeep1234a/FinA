import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig } from 'vite'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@systems': path.resolve(__dirname, './src/systems'),
      '@components': path.resolve(__dirname, './src/components'),
      '@hooks': path.resolve(__dirname, './src/hooks'),
      '@stores': path.resolve(__dirname, './src/stores'),
      '@types': path.resolve(__dirname, './src/types'),
      '@utils': path.resolve(__dirname, './src/utils'),
      '@assets': path.resolve(__dirname, './public/assets'),
    },
  },
  build: {
    target: 'esnext',
    minify: 'esbuild',
    cssCodeSplit: true,
    assetsInlineLimit: 4096,
    rollupOptions: {
      output: {
        manualChunks: (id: string) => {
          if (id.includes('node_modules')) {
            if (id.includes('three')) return 'three'
            if (id.includes('@dimforge/rapier3d')) return 'rapier'
            if (id.includes('gsap')) return 'gsap'
            if (id.includes('howler')) return 'howler'
            if (id.includes('zustand') || id.includes('immer')) return 'zustand'
            return 'vendor'
          }
        },
      },
    },
  },
  server: {
    port: 3000,
    open: true,
  },
})
