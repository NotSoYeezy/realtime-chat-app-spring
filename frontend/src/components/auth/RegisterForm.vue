<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/axios'
import AuthCard from '@/components/auth/AuthCard.vue'
import AppLogo from '@/components/common/AppLogo.vue'
import FormInput from '@/components/common/FormInput.vue'
import PasswordInput from '@/components/auth/PasswordInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')
const loading = ref(false)

const handleSubmit = async () => {
  error.value = ''

  if (password.value !== confirmPassword.value) {
    error.value = 'Hasła nie są identyczne!'
    return
  }

  loading.value = true

  try {
    const response = await api.post('/auth/register', {
      username: username.value,
      email: email.value,
      password: password.value
    })

    if (response.data.token) {
      authStore.setToken(response.data.token)
      router.push('/')
    } else {
      router.push('/login')
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Błąd rejestracji. Spróbuj ponownie.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthCard
    title="Create an Account"
    subtitle="Join our platform and start connecting."
  >
    <template #logo>
      <AppLogo />
    </template>

    <form @submit.prevent="handleSubmit" class="flex flex-col gap-5">
      <div v-if="error" class="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-600 dark:text-red-400 px-4 py-3 rounded-lg text-sm">
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
        placeholder="Enter your email address"
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
        label="Confirm Password"
        placeholder="Confirm your password"
        required
      />

      <BaseButton
        type="submit"
        :fullWidth="true"
        :disabled="loading"
        class="mt-2"
      >
        {{ loading ? 'Creating account...' : 'Register' }}
      </BaseButton>
    </form>

    <template #footer>
      <p class="text-sm text-slate-600 dark:text-slate-400 text-center">
        Already have an account?
        <router-link to="/login" class="font-medium text-primary hover:underline">
          Sign In
        </router-link>
      </p>
    </template>
  </AuthCard>
</template>
