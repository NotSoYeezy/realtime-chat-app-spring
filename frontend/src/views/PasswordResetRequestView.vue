<script setup>
import { ref } from 'vue'
import AuthLayout from '@/components/layout/AuthLayout.vue'
import AuthCard from '@/components/auth/AuthCard.vue'
import AuthFooter from '@/components/auth/AuthFooter.vue'
import FormInput from '@/components/ui/FormInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import api from '@/api/axios'

const email = ref('')
const loading = ref(false)
const success = ref(false)
const error = ref('')

const handleSubmit = async () => {
  error.value = ''
  success.value = false

  if (!email.value) {
    error.value = 'Please enter your email.'
    return
  }

  loading.value = true
  try {
    await api.post('/auth/password-reset/request', {
      email: email.value
    })

    success.value = true
  } catch (err) {
    error.value =
      err.response?.data?.message ||
      'Something went wrong. Please try again later.'
  } finally {
    loading.value = false
  }
}
</script>
<template>
  <AuthLayout>
    <AuthCard
      title="Reset password"
      subtitle="Enter your email to reset your password"
    >
      <form
        v-if="!success"
        @submit.prevent="handleSubmit"
        novalidate
        class="space-y-4"
      >
        <FormInput
          v-model="email"
          type="email"
          placeholder="Enter your email"
        />

        <p v-if="error" class="text-sm text-center text-[var(--color-danger-text)]">
          {{ error }}
        </p>

        <BaseButton
          type="submit"
          class="w-full"
          :disabled="loading"
        >
          {{ loading ? 'Sending...' : 'Send reset link' }}
        </BaseButton>
      </form>

      <!-- SUCCESS -->
      <div v-else class="text-center space-y-4 py-4 animate-in fade-in">
        <div class="text-green-500">
          <span class="material-symbols-outlined text-6xl">
            mail
          </span>
        </div>

        <p class="text-sm text-[var(--color-text-secondary)]">
          If an account with this email exists,<br />
          a password reset link has been sent.
        </p>
      </div>

      <template #footer>
        <AuthFooter
          linkText="Go back to login page"
          to="/login"
        />
      </template>
    </AuthCard>
  </AuthLayout>
</template>
