<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/axios'

import FormInput from '@/components/ui/FormInput.vue'
import PasswordInput from '@/components/auth/PasswordInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleSubmit = async () => {
  error.value = ''
  loading.value = true

  try {
    const response = await api.post('/auth/login', {
      email: email.value,
      password: password.value
    })

    const { token, refreshToken } = response.data
    authStore.setTokens(token, refreshToken)
    router.push('/')
  } catch (err) {
    error.value = err.response?.data?.message || 'Incorrect email or password.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <form @submit.prevent="handleSubmit" class="flex flex-col gap-5">

    <!-- ERROR MESSAGE -->
    <div
      v-if="error"
      class="px-4 py-3 rounded-lg text-sm
             bg-[var(--color-danger-bg)]
             text-[var(--color-danger-text)]
             border border-[var(--color-danger-border)]"
    >
      {{ error }}
    </div>

    <FormInput
      v-model="email"
      type="email"
      label="Email"
      placeholder="Enter your email"
      required
    />

    <PasswordInput
      v-model="password"
      label="Password"
      placeholder="Enter your password"
      required
    />

    <BaseButton type="submit" :fullWidth="true" :disabled="loading">
      {{ loading ? 'Signing in&#8230;' : 'Sign In' }}
    </BaseButton>

  </form>
</template>
