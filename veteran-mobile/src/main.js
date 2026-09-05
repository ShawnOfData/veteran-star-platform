/**
 * 应用入口
 * 挂载 Vant、Pinia、Router、flexible 适配
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 移动端 rem 自适应
import 'amfe-flexible'

// Vant 全局样式（基础样式重置）
import 'vant/lib/index.css'

// 全局样式
import './styles/global.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
