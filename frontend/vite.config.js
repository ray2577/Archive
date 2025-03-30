import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import vueJsx from '@vitejs/plugin-vue-jsx'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 8000,
    proxy: {
      // 将所有以 /api 开头的请求转发到后端
      // 例如:
      // 前端请求 /api/auth/login
      // 会被转发到 http://localhost:8080/archive/auth/login
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => {
          console.log(`Vite proxy rewriting path: ${path} to ${path.replace(/^\/api/, '/archive')}`);
          return path.replace(/^\/api/, '/archive');
        }
      }
    }
  }
}) 


