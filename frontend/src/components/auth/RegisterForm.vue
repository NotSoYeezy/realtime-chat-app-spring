<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/axios'
import ConfirmAccount from '@/api/confirmAccount.js'
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
const success = ref(false)

const showResendOption = ref(false)

const resendLoading = ref(false)
const resendSuccess = ref(false)
const resendError = ref('')

const handleSubmit = async () => {
  error.value = ''
  showResendOption.value = false
  resendSuccess.value = false
  resendError.value = ''

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

    success.value = true
  } catch (err) {
    if (err.response?.data?.message === "User is not confirmed!") {
      error.value = "Account with this email exist, but was not activated."
      showResendOption.value = true
    } else {
      error.value = err.response?.data?.message || 'Registration error'
    }
  } finally {
    loading.value = false
  }
}

const handleResend = async () => {
  resendLoading.value = true
  resendError.value = ''

  try {
    await ConfirmAccount.resend(email.value)
    resendSuccess.value = true
  } catch (err) {
    resendError.value = "Could not send email. Please try again later."
  } finally {
    resendLoading.value = false
  }
}
</script>

<template>
  <div
    v-if="success"
    class="flex flex-col items-center justify-center text-center space-y-6 py-4 animate-in fade-in zoom-in duration-300"
  >
    <div class="h-20 w-20 bg-green-100 text-green-600 rounded-full flex items-center justify-center">
      <span class="material-symbols-outlined text-4xl">mark_email_read</span>
    </div>
    <div>
      <h3 class="text-xl font-bold text-[var(--color-text-primary)] mb-2">Check your inbox!</h3>
      <p class="text-[var(--color-text-secondary)]">
        We have sent an activation link to:<br />
        <span class="font-medium text-[var(--color-text-primary)]">{{ email }}</span>
      </p>
    </div>
    <BaseButton @click="router.push('/login')" :fullWidth="true">Go to Login</BaseButton>

    <div class="pt-4 w-full border-t border-[var(--color-border)]">
      <div v-if="!resendSuccess">
        <p class="text-xs text-[var(--color-text-secondary)] mb-2">Did not receive the email?</p>
        <button @click="handleResend" :disabled="resendLoading" class="text-sm font-medium text-[var(--color-primary)] hover:underline disabled:opacity-50">
          {{ resendLoading ? 'Sending...' : 'Resend confirmation link' }}
        </button>
        <p v-if="resendError" class="text-xs text-[var(--color-danger-text)] mt-1">{{ resendError }}</p>
      </div>
      <div v-else class="flex items-center justify-center gap-2 text-green-600 text-sm font-medium animate-in fade-in">
        <span class="material-symbols-outlined text-lg">check</span>
        New link sent!
      </div>
    </div>
  </div>

  <form v-else @submit.prevent="handleSubmit" novalidate class="flex flex-col gap-5">

    <div
      v-if="error || (showResendOption && resendSuccess)"
      class="px-4 py-3 rounded-lg text-sm border transition-colors duration-300"
      :class="resendSuccess
        ? 'bg-green-50 text-green-700 border-green-200'
        : 'bg-[var(--color-danger-bg)] text-[var(--color-danger-text)] border-[var(--color-danger-border)]'"
    >
      <div v-if="!resendSuccess">
        <div class="flex flex-col sm:flex-row justify-between gap-2">
          <span>{{ error }}</span>

          <button
            v-if="showResendOption"
            type="button"
            @click="handleResend"
            :disabled="resendLoading"
            class="font-bold underline shrink-0 hover:opacity-80 text-left sm:text-right"
          >
            {{ resendLoading ? 'Sending...' : 'Resend Email' }}
          </button>
        </div>

        <p v-if="resendError" class="mt-2 font-bold">{{ resendError }}</p>
      </div>

      <div v-else class="flex items-center gap-2 font-medium">
        <span class="material-symbols-outlined text-lg">check_circle</span>
        Activation link sent successfully!
      </div>
    </div>

    <FormInput v-model="name" label="Name" placeholder="Enter your name" required />
    <FormInput v-model="surname" label="Surname" placeholder="Enter your surname" required />
    <FormInput v-model="email" type="email" label="Email" placeholder="Enter your email" required />
    <PasswordInput v-model="password" label="Password" placeholder="Enter your password" required />
    <PasswordInput v-model="confirmPassword" label="Confirm password" placeholder="Repeat password" required />

    <BaseButton type="submit" :fullWidth="true" :disabled="loading">
      {{ loading ? 'Creating account...' : 'Register' }}
    </BaseButton>
  </form>
</template>
