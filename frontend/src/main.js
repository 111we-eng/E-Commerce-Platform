import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { enableMock } from './api/product'

// 开发环境默认启用 Mock 数据，无需后端即可运行
if (import.meta.env.DEV) {
  enableMock(true)
}

const app = createApp(App)
app.use(ElementPlus, { size: 'default', zIndex: 3000 })
app.use(router)
app.mount('#app')
