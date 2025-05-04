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
  define: {
    // This ensures import.meta is properly handled by all tools
    __VUE_OPTIONS_API__: true,
    __VUE_PROD_DEVTOOLS__: false
  },
  server: {
    port: 8000,
    proxy: {
      // 将所有以 /api 开头的请求转发到后端
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => {
          console.log(`Vite proxy rewriting path: ${path}`);
          // 修改重写规则: 将/api前缀替换为/archive
          return path.replace(/^\/api/, '/archive');
        }
      }
    }
  }
}) 


