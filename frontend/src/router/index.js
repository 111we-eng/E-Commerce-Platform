/**
 * Vue Router 路由配置
 *
 * 路由守卫原理：
 * 1. beforeEach → 检查目标路由是否需要认证（meta.requiresAuth）
 * 2. 需要认证但未登录 → 重定向到 /login
 * 3. 已登录访问 /login → 重定向到首页
 * 4. Token 由 Axios 请求拦截器自动附加
 */
import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('../views/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('../views/Cart.vue'),
        meta: { title: '购物车', requiresAuth: true }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('../views/Checkout.vue'),
        meta: { title: '结算', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('../views/OrderList.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('../views/OrderDetail.vue'),
        meta: { title: '订单详情', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'api-test',
        name: 'ApiTest',
        component: () => import('../views/ApiTest.vue'),
        meta: { title: 'API测试', requiresAuth: false }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ===== 全局前置守卫 =====
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? to.meta.title + ' - 电商平台' : '电商购物平台'

  const authenticated = isAuthenticated()

  if (to.meta.requiresAuth && !authenticated) {
    // 需要登录但未登录 → 跳转登录页
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (authenticated && (to.path === '/login' || to.path === '/register')) {
    // 已登录访问登录页 → 跳转首页
    next('/home')
  } else {
    next()
  }
})

export default router
