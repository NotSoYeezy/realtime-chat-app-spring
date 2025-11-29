import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('jwt_token') || null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token
  },

  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('jwt_token', token)
    },

    clearToken() {
      this.token = null
      localStorage.removeItem('jwt_token')
    },
    async logout() {
      try {
        const { default: api } = await import('@/api/axios')
        await api.post('/auth/logout')
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.clearToken()
      }
    }
  }
})
