<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

import FormInput from '@/components/ui/FormInput.vue'
import PasswordInput from '@/components/auth/PasswordInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'

const router = useRouter()

const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')

const error = ref('')
const loading = ref(false)

const handleSubmit = async () => {
  error.value = ''

  if (password.value !== confirmPassword.value) {
    error.value = 'Passwords do not match!'
    return
  }

  loading.value = true

  try {
    await api.post('/auth/register', {
      username: username.value,
      email: email.value,
      password: password.value
    })

    router.push('/login')
  } catch (err) {
    error.value = err.response?.data?.message || 'Registration error.'
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
             bg-[var(--color-error-bg)]
             text-[var(--color-error-text)]
             border border-[var(--color-error-border)]"
    >
      {{ error }}
    </div>

    <FormInput
      v-model="username"
      label="Username"
      placeholder="Enter your username"
      required
    />

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
