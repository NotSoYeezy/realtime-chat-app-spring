<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  const accessToken = route.query.token
  const refreshToken = route.query.refreshToken

  if (accessToken && refreshToken) {
    authStore.setTokens(accessToken, refreshToken)

    router.replace('/')
  } else {
    console.error("Missing tokens in OAuth callback")
    router.push('/login?error=oauth_failed')
  }
})
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-[var(--color-background)]">
    <div class="w-12 h-12 border-4 border-[var(--color-primary)] border-t-transparent rounded-full animate-spin mb-4"></div>
    <span class="text-[var(--color-text-secondary)] font-medium">Finalizing secure login...</span>
  </div>
</template>
