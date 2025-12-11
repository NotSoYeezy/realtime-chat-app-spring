<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/axios'
import PasswordInput from '@/components/auth/PasswordInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import FormInput from '@/components/ui/FormInput.vue'

const router = useRouter()
const authStore = useAuthStore()

const name = ref('')
const surname = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')
const loading = ref(false)

const handleSubmit = async () => {
  error.value = ''

  if (password.value !== confirmPassword.value) {
    error.value = 'Passwords do not match.'
    return
  }

  loading.value = true

  try {
    const response = await api.post('/auth/register', {
      name: name.value,
      surname: surname.value,
      email: email.value,
      password: password.value,
    })

    if (response.data.token) {
      authStore.setTokens(response.data.token, response.data.refreshToken)
      router.push('/')
    } else {
      router.push('/login')
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Registration error'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <form @submit.prevent="handleSubmit" novalidate class="flex flex-col gap-5">
    <!-- ERROR MESSAGE -->
    <div
      v-if="error"
      class="px-4 py-3 rounded-lg text-sm bg-[var(--color-danger-bg)] text-[var(--color-danger-text)] border border-[var(--color-danger-border)]"
    >
      {{ error }}
    </div>

    <FormInput v-model="name" label="Name" placeholder="Enter your name" required />

    <FormInput v-model="surname" label="Surname" placeholder="Enter your surname" required />

    <FormInput v-model="email" type="email" label="Email" placeholder="Enter your email" required />

    <PasswordInput v-model="password" label="Password" placeholder="Enter your password" required />

    <PasswordInput
      v-model="confirmPassword"
      label="Confirm password"
      placeholder="Repeat password"
      required
    />

    <BaseButton type="submit" :fullWidth="true" :disabled="loading">
      {{ loading ? 'Creating account...' : 'Register' }}
    </BaseButton>
  </form>
</template>
