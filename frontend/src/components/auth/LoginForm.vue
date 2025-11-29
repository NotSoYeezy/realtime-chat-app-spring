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

    await router.push('/')
    console.log("After push")
  } catch (err) {
    error.value = err.response?.data?.message || 'Nieprawidłowy email lub hasło.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthCard
    title="Welcome Back"
    subtitle="Sign in to your account to continue."
  >
    <template #logo>
      <AppLogo />
    </template>

    <form @submit.prevent="handleSubmit" class="flex flex-col gap-5">
      <div v-if="error" class="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-600 dark:text-red-400 px-4 py-3 rounded-lg text-sm">
        {{ error }}
      </div>

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

      <div class="flex items-center justify-between text-sm">
        <label class="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            class="w-4 h-4 text-primary border-slate-300 rounded focus:ring-primary"
          />
          <span class="text-slate-600 dark:text-slate-400">Remember me</span>
        </label>
        <a href="#" class="font-medium text-primary hover:underline">
          Forgot password?
        </a>
      </div>

      <BaseButton
        type="submit"
        :fullWidth="true"
        :disabled="loading"
        class="mt-2"
      >
        {{ loading ? 'Signing in...' : 'Sign In' }}
      </BaseButton>
    </form>

    <template #footer>
      <p class="text-sm text-slate-600 dark:text-slate-400 text-center">
        Don't have an account?
        <router-link to="/register" class="font-medium text-primary hover:underline">
          Sign Up
        </router-link>
      </p>
    </template>
  </AuthCard>
</template>
