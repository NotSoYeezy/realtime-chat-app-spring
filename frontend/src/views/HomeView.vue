<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/axios.js'

const router = useRouter()
const authStore = useAuthStore()

const data = ref('')
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  loading.value = true

  try {
    console.log('Making request to /test')
    console.log('Token:', authStore.accessToken)

    const response = await api.get('/test')

    console.log('Response:', response.data)
    data.value = response.data
  } catch (err) {
    console.error('Error:', err)
    console.error('Error response:', err.response)
    error.value = err.response?.data?.message || err.message
  } finally {
    loading.value = false
    console.log('Request finished')
  }
})

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div>
    <h1>Dashboard</h1>
    <p>Witaj! Jesteś zalogowany.</p>
    <p v-if="loading">Ładowanie...</p>
    <p v-else-if="error">Błąd: {{ error }}</p>
    <p v-else>{{ data }}</p>
    <button @click="handleLogout">Wyloguj</button>
  </div>
</template>
