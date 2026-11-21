import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      manifest: {
        name: 'SecureProof',
        short_name: 'SecureProof',
        description: 'Secure Document Protection and Traceability Platform',
        theme_color: '#0f172a',
        background_color: '#ffffff',
        display: 'standalone',
        icons: []
      },
      workbox: {
        // Cache only static UI shell — never API responses or PDF downloads (SEC-WEB-06)
        globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
        navigateFallbackDenylist: [/^\/api\//],
        runtimeCaching: [
          {
            urlPattern: /^\/api\/.*/i,
            handler: 'NetworkOnly'
          }
        ]
      }
    })
  ],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
