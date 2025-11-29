<template>
  <div>
    <h1>Dashboard</h1>
    <p>Witaj! Jesteś zalogowany.</p>
    <button @click="handleLogout">Wyloguj</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>
