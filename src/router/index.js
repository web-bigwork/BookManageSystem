import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/LayoutView'

// 读取本地用户角色，兼容 sessionStorage/localStorage
const getUserRole = () => {
  const raw = sessionStorage.getItem('user') || localStorage.getItem('user')
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw)
    const role = parsed.role == null ? null : Number(parsed.role)
    if (Number.isNaN(role)) return 0  // 默认普通用户
    return role == null ? 0 : role
  } catch (e) {
    return 0
  }
}

const routes = [
  {
    path: '/',
    name: 'Layout',
    redirect: '/login',
    component: Layout,
    children: [
      {
        path: 'user',
        name: 'UserView',
        component: () => import('@/views/UserView')
      },
      {
        path: 'book',
        name: 'BookManageView',
        component: () => import('@/views/BookManageView'),
        beforeEnter: (to, from, next) => {
          const role = getUserRole()
          if (role === 1) {
            next()
            return
          }
          next({ path: '/booksearch' })
        }
      },
      {
        path: 'booksearch',
        name: 'BookSearchView',
        component: () => import('@/views/BookSearchView'),
        beforeEnter: (to, from, next) => {
          const role = getUserRole()
          if (role === 0) {
            next()
            return
          }
          if (role === 1) {
            next({ path: '/book' })
            return
          }
          next('/login')
        }
      },
      
      {
        path: 'dashboard',
        name: 'DashboardView',
        component: () => import('@/views/DashboardView')
      },
      {
        path: 'borrow-manage',
        name: 'BorrowManageView',
        component: () => import('@/views/BorrowManageView'),
        beforeEnter: (to, from, next) => {
          const role = getUserRole()
          if (role === 1) {
            next()
            return
          }
          if (role === 0) {
            next({ path: '/borrow-search' })
            return
          }
          next('/login')
        }
      },
      {
        path: 'borrow-search',
        name: 'BorrowSearchView',
        component: () => import('@/views/BorrowSearchView'),
        beforeEnter: (to, from, next) => {
          const role = getUserRole()
          if (role === 0) {
            next()
            return
          }
          if (role === 1) {
            next({ path: '/borrow-manage' })
            return
          }
          next('/login')
        }
      },
      {
        path: 'blacklist',
        name: 'BlacklistView',
        component: () => import('@/views/BlacklistView'),
        beforeEnter: (to, from, next) => {
          const role = getUserRole()
          if (role === 1) {
            next()
            return
          }
          next('/login')
        }
      }
    ]
  },
  {
    path: '/login',
    name: 'LoginView',
    component: () => import('@/views/LoginView')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
