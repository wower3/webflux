import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: true,
        ws: true,
        configure: (proxy) => {
          proxy.on('proxyRes', (_proxyRes) => {
            delete _proxyRes.headers['content-length']
            _proxyRes.headers['Transfer-Encoding'] = 'chunked'
          })
        }
      },
      '/api/ai': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
        configure: (proxy) => {
          proxy.on('proxyRes', (_proxyRes) => {
            delete _proxyRes.headers['content-length']
            _proxyRes.headers['Transfer-Encoding'] = 'chunked'
          })
        }
      }
    }
  }
})
