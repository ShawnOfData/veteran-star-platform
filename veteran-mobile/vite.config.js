import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'
import path from 'path'

// Vite 配置 — 移动端 Vue3 项目
export default defineConfig({
  // 部署到 /mobile/ 路径下，开发环境设为 '/'
  base: process.env.NODE_ENV === 'production' ? '/mobile/' : '/',
  plugins: [
    vue(),
    // Vant 按需自动引入
    Components({
      resolvers: [VantResolver()]
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3001,
    // 开发环境代理 /api 到后端
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 上传文件（头像、证明材料），本地开发代理到后端 uploads 目录
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    // 分包优化
    rollupOptions: {
      output: {
        manualChunks: {
          vant: ['vant'],
          echarts: ['echarts'],
          vendor: ['vue', 'vue-router', 'pinia', 'axios']
        }
      }
    }
  }
})
