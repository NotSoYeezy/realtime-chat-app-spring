<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ConfirmAccount from '@/api/confirmAccount.js'
import AuthLayout from '@/components/layout/AuthLayout.vue'
import AuthCard from '@/components/auth/AuthCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import FormInput from '@/components/ui/FormInput.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const status = ref('loading')
const message = ref('Verifying activation link...')

const resendEmail = ref('')
const resendLoading = ref(false)
const resendSuccess = ref(false)
const resendError = ref('')

onMounted(async () => {
  const token = route.params.token

  if (!token) {
    status.value = 'error'
    message.value = 'No verification token provided.'
    return
  }

  try {
    const response = await ConfirmAccount.confirm(token)

    const { token: accessToken, refreshToken } = response.data

    authStore.setTokens(accessToken, refreshToken)

    status.value = 'success'
    message.value = 'Account successfully verified!'

    setTimeout(() => {
      router.push('/')
    }, 2000)

  } catch (err) {
    console.error(err)
    status.value = 'error'
    if (err.response && err.response.data && err.response.data.message) {
      message.value = err.response.data.message
    } else {
      message.value = 'The verification link is invalid or has expired.'
    }
  }
})

const handleResend = async () => {
  if (!resendEmail.value) return

  resendLoading.value = true
  resendError.value = ''

  try {
    await ConfirmAccount.resend(resendEmail.value)
    resendSuccess.value = true
  } catch (err) {
    resendError.value = err.response?.data.message || "An error occurred while sending the email."
  } finally {
    resendLoading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <AuthLayout>
    <AuthCard
      :title="status === 'success' ? 'Success!' : (status === 'error' ? 'Link Expired' : 'Verification')"
      :subtitle="status === 'loading' ? 'Please wait...' : ''"
    >
      <div class="flex flex-col items-center justify-center gap-6 py-4 animate-in fade-in duration-300">

        <div v-if="status === 'loading'" class="text-[var(--color-primary)]">
          <span class="material-symbols-outlined text-6xl animate-spin">sync</span>
        </div>

        <div v-else-if="status === 'success'" class="text-center space-y-4">
          <div class="text-green-500">
            <span class="material-symbols-outlined text-6xl">check_circle</span>
          </div>
          <p class="text-[var(--color-text-primary)] font-medium text-lg">
            {{ message }}
          </p>
          <p class="text-sm text-[var(--color-text-secondary)]">
            You will be redirected automatically...
          </p>
        </div>

        <div v-else-if="status === 'error'" class="w-full text-center space-y-4">

          <div class="text-[var(--color-danger)] mb-2">
            <span class="material-symbols-outlined text-6xl">link_off</span>
          </div>

          <p class="text-[var(--color-text-secondary)]">
            {{ message }}
          </p>

          <hr class="border-[var(--color-border)] my-4" />

          <div v-if="!resendSuccess" class="text-left w-full space-y-4 animate-in fade-in">
            <p class="text-sm font-medium text-[var(--color-text-primary)]">
              Send a new activation link:
            </p>

            <FormInput
              v-model="resendEmail"
              type="email"
              label=""
              placeholder="Enter your email"
            />

            <p v-if="resendError" class="text-xs text-center text-[var(--color-danger-text)]">
              {{ resendError }}
            </p>

            <BaseButton
              @click="handleResend"
              :fullWidth="true"
              :disabled="resendLoading || !resendEmail"
            >
              {{ resendLoading ? 'Sending...' : 'Resend Link' }}
            </BaseButton>
          </div>

          <div v-else class="bg-green-50 text-green-700 p-4 rounded-lg text-sm border border-green-200">
            A new link has been sent! Check your inbox.
          </div>

          <div class="pt-2">
            <button
              @click="goToLogin"
              class="text-sm text-[var(--color-text-secondary)] hover:text-[var(--color-primary)] hover:underline transition-colors"
            >
              Back to sign in
            </button>
          </div>
        </div>

      </div>
    </AuthCard>
  </AuthLayout>
</template>
