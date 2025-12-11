import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/register',
      name: 'Register',
      component: () => import("@/views/RegisterView.vue"),
      meta: { requiresGuest: true } // Tylko dla niezalogowanych
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import("@/views/LoginView.vue"),
      meta: { requiresGuest: true } // Tylko dla niezalogowanych
    },
    {
      path: "/",
      name: 'index',
      component: () => import("@/views/HomeView.vue"),
      meta: { requiresAuth: true } // Tylko dla zalogowanych
    },
    {
      path: '/auth/callback',
      name: 'AuthCallback',
      component: () => import("@/views/AuthCallback.vue"),
    }
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login' })
  }
  else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next({ name: 'index' })
  }
  else {
    next()
  }
})

export default router
