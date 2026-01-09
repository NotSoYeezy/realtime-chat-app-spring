<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AuthLayout from '@/components/layout/AuthLayout.vue'
import AuthCard from '@/components/auth/AuthCard.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import FormInput from '@/components/ui/FormInput.vue'
import PasswordReset from '@/api/passwordReset'

const route = useRoute()
const router = useRouter()

const status = ref('loading')
const message = ref('Verifying reset link...')

const password = ref('')
const confirmPassword = ref('')
const submitLoading = ref(false)
const submitError = ref('')

const resendEmail = ref('')
const resendLoading = ref(false)
const resendSuccess = ref(false)
const resendError = ref('')

const token = route.params.token

onMounted(async () => {
  if (!token) {
    status.value = 'error'
    message.value = 'No reset token provided.'
    return
  }

  // opcjonalnie: backend validate-token
  status.value = 'ready'
})

const handleResetPassword = async () => {
  submitError.value = ''

  if (!password.value || password.value.length < 8) {
    submitError.value = 'Password must be at least 8 characters.'
    return
  }

  if (password.value !== confirmPassword.value) {
    submitError.value = 'Passwords do not match.'
    return
  }

  submitLoading.value = true
  try {
    await PasswordReset.confirm(token, password.value)

    status.value = 'success'
    message.value = 'Password successfully reset!'

    setTimeout(() => {
      router.push('/login')
    }, 4000)
  } catch (err) {
    submitError.value =
      err.response?.data?.message ||
      'The reset link is invalid or has expired.'
  } finally {
    submitLoading.value = false
  }
}

const handleResend = async () => {
  if (!resendEmail.value) return

  resendLoading.value = true
  resendError.value = ''

  try {
    await PasswordReset.request(resendEmail.value)
    resendSuccess.value = true
  } catch (err) {
    resendError.value =
      err.response?.data?.message ||
      'An error occurred while sending the email.'
  } finally {
    resendLoading.value = false
  }
}

</script>
<template>
  <AuthLayout>
    <AuthCard
      :title="
        status === 'success'
          ? 'Password Reset'
          : status === 'error'
            ? 'Link Expired'
            : 'Reset Password'
      "
      :subtitle="status === 'loading' ? 'Please wait...' : 'Set your new password'"
    >

      <!-- CONTENT -->
      <div class="flex flex-col gap-6 py-4 animate-in fade-in">

        <!-- LOADING -->
        <div
          v-if="status === 'loading'"
          class="flex justify-center text-[var(--color-primary)]"
        >
          <span class="material-symbols-outlined text-6xl animate-spin">
            sync
          </span>
        </div>

        <!-- FORM -->
        <div v-else-if="status === 'ready'" class="space-y-4">

          <FormInput
            v-model="password"
            type="password"
            placeholder="New password"
          />

          <FormInput
            v-model="confirmPassword"
            type="password"
            placeholder="Confirm new password"
          />

          <p
            v-if="submitError"
            class="text-sm text-center text-[var(--color-danger-text)]"
          >
            {{ submitError }}
          </p>

          <BaseButton
            :fullWidth="true"
            :disabled="submitLoading"
            @click="handleResetPassword"
          >
            {{ submitLoading ? 'Resetting...' : 'Reset password' }}
          </BaseButton>
        </div>

        <!-- SUCCESS -->
        <div v-else-if="status === 'success'" class="text-center space-y-4">
          <div class="text-green-500">
            <span class="material-symbols-outlined text-6xl">
              check_circle
            </span>
          </div>

          <p class="text-lg font-medium">
            {{ message }}
          </p>

          <p class="text-sm text-[var(--color-text-secondary)]">
            Redirecting to login...
          </p>
        </div>

        <!-- ERROR -->
        <div v-else class="space-y-4 text-center">

          <div class="text-[var(--color-danger)]">
            <span class="material-symbols-outlined text-6xl">
              link_off
            </span>
          </div>

          <p class="text-[var(--color-text-secondary)]">
            {{ message }}
          </p>

          <hr class="border-[var(--color-border)]" />

          <div v-if="!resendSuccess" class="space-y-4 text-left">

            <p class="text-sm font-medium">
              Send a new reset link:
            </p>

            <FormInput
              v-model="resendEmail"
              type="email"
              placeholder="Enter your email"
            />

            <p
              v-if="resendError"
              class="text-xs text-center text-[var(--color-danger-text)]"
            >
              {{ resendError }}
            </p>

            <BaseButton
              :fullWidth="true"
              :disabled="resendLoading || !resendEmail"
              @click="handleResend"
            >
              {{ resendLoading ? 'Sending...' : 'Resend link' }}
            </BaseButton>
          </div>

          <div
            v-else
            class="bg-green-50 text-green-700 p-4 rounded-lg text-sm border border-green-200"
          >
            A new reset link has been sent!
          </div>
        </div>
      </div>
    </AuthCard>
  </AuthLayout>
</template>

