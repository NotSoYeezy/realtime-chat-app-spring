<script setup>
import { ref, computed } from 'vue'
import SettingsButton from '@/components/settings/SettingsButton.vue'
import UpdateUser from '@/api/updateUser.js'

defineProps({
  user: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['cancel', 'saved'])

const editEmail = ref(false)
const editUsername = ref(false)
const editPassword = ref(false)

const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const error = ref(null)

const updateSuccess = ref(false)

const form = ref({
  email: null,
  username: null,
  password: null,
  confirmPassword: null
})

const passwordsMatch = computed(() => {
  if (!form.value.password || !form.value.confirmPassword) return true
  return form.value.password === form.value.confirmPassword
})

const canSave = computed(() => {
  if (editPassword.value) {
    if (!form.value.password || !form.value.confirmPassword) return false
    if (form.value.password !== form.value.confirmPassword) return false
  }

  if (editEmail.value && (!form.value.email || form.value.email.trim() === '')) {
    return false
  }

  if (editUsername.value && (!form.value.username || form.value.username.trim() === '')) {
    return false
  }

  return editEmail.value || editUsername.value || editPassword.value
})

const toggleEmail = () => {
  if (editEmail.value) {
    form.value.email = null
  }
  editEmail.value = !editEmail.value
}

const toggleUsername = () => {
  if (editUsername.value) {
    form.value.username = null
  }
  editUsername.value = !editUsername.value
}

const togglePassword = () => {
  if (editPassword.value) {
    form.value.password = null
    form.value.confirmPassword = null
    showNewPassword.value = false
    showConfirmPassword.value = false
    error.value = null
  }
  editPassword.value = !editPassword.value
}

const handleGlobalCancel = () => {
  editEmail.value = false
  editUsername.value = false
  editPassword.value = false
  form.value.email = null
  form.value.username = null
  form.value.password = null
  form.value.confirmPassword = null
  error.value = null
  emit('cancel')
}

const handleSave = async() => {
  error.value = null

  try {
    const payload = {}

    if (editEmail.value && form.value.email) {
      payload.email = form.value.email
    }

    if (editUsername.value && form.value.username) {
      payload.username = form.value.username
    }

    if (editPassword.value && form.value.password) {
      if (form.value.password !== form.value.confirmPassword) {
        error.value = "Passwords do not match"
        return
      }
      payload.password = form.value.password
    }

    if (Object.keys(payload).length === 0) return

    const response = await UpdateUser.updateUser(payload)

    if (response.status === 200) {
      updateSuccess.value = true
    }
  }
  catch (err) {
    console.error(err)
    error.value = err.response.data.message
  }
}

const confirmLogout = () => {
  emit('saved')
}
</script>

<template>
  <div class="space-y-6 animate-in fade-in duration-300">

    <div v-if="updateSuccess" class="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-in fade-in duration-300">

      <div class="bg-[var(--surface-panel)] border border-[var(--color-border)] p-8 rounded-2xl shadow-2xl max-w-md w-full flex flex-col items-center text-center space-y-6 animate-in zoom-in-95 duration-300">

        <div class="w-16 h-16 rounded-full bg-green-500/10 flex items-center justify-center text-green-500">
          <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
        </div>

        <div class="space-y-2">
          <h4 class="text-xl font-bold text-[var(--color-text-primary)]">Profile Updated</h4>
          <p class="text-[var(--color-text-secondary)] text-sm leading-relaxed">
            Your account credentials have been changed successfully. For security reasons, you must log in again.
          </p>
        </div>

        <SettingsButton @click="confirmLogout" class="w-full">
          OK, Log me out
        </SettingsButton>
      </div>
    </div>

    <div class="flex items-center justify-between">
      <div>
        <h3 class="text-lg font-semibold">Update Profile</h3>
        <p class="text-sm text-[var(--color-text-secondary)]">
          Manage your account credentials.
        </p>
      </div>
      <button
        @click="handleGlobalCancel"
        class="text-sm font-medium text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors"
      >
        Cancel
      </button>
    </div>

    <div v-if="error" class="bg-red-500/10 border border-red-500/20 text-red-500 px-4 py-2 rounded-lg text-sm">
      {{ error }}
    </div>

    <div class="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--surface-panel-strong)]/40 space-y-8">

      <div class="space-y-2">
        <div class="flex items-center justify-between">
          <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] font-bold">
            Email Address
          </label>
          <button
            @click="toggleEmail"
            class="text-xs font-medium text-[var(--color-primary)] hover:text-[var(--color-primary)]/80 transition-colors"
          >
            {{ editEmail ? 'Cancel' : 'Change' }}
          </button>
        </div>

        <div class="text-base font-medium text-[var(--color-text-primary)]">
          {{ user.email }}
        </div>

        <div v-if="editEmail" class="animate-in slide-in-from-top-2 fade-in duration-200 pt-2">
          <input
            v-model="form.email"
            type="email"
            placeholder="Enter new email"
            class="w-full px-4 py-2 rounded-lg bg-[var(--surface-panel)] border border-[var(--color-border)] text-[var(--color-text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] transition-all"
          />
        </div>
      </div>

      <div class="space-y-2">
        <div class="flex items-center justify-between">
          <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] font-bold">
            Username
          </label>
          <button
            @click="toggleUsername"
            class="text-xs font-medium text-[var(--color-primary)] hover:text-[var(--color-primary)]/80 transition-colors"
          >
            {{ editUsername ? 'Cancel' : 'Change' }}
          </button>
        </div>

        <div class="text-base font-medium text-[var(--color-text-primary)]">
          {{ user.username }}
        </div>

        <div v-if="editUsername" class="animate-in slide-in-from-top-2 fade-in duration-200 pt-2">
          <input
            v-model="form.username"
            type="text"
            placeholder="Enter new username"
            class="w-full px-4 py-2 rounded-lg bg-[var(--surface-panel)] border border-[var(--color-border)] text-[var(--color-text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] transition-all"
          />
        </div>
      </div>

      <div class="space-y-2">
        <div class="flex items-center justify-between">
          <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] font-bold">
            Password
          </label>
          <button
            @click="togglePassword"
            class="text-xs font-medium text-[var(--color-primary)] hover:text-[var(--color-primary)]/80 transition-colors"
          >
            {{ editPassword ? 'Cancel' : 'Change' }}
          </button>
        </div>

        <div class="text-base font-medium text-[var(--color-text-primary)] tracking-widest">
          ••••••••••••
        </div>

        <div v-if="editPassword" class="space-y-3 animate-in slide-in-from-top-2 fade-in duration-200 pt-2">

          <div class="relative">
            <input
              v-model="form.password"
              :type="showNewPassword ? 'text' : 'password'"
              placeholder="Enter new password"
              class="w-full pl-4 pr-12 py-2 rounded-lg bg-[var(--surface-panel)] border border-[var(--color-border)] text-[var(--color-text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] transition-all"
            />
            <button
              type="button"
              @click="showNewPassword = !showNewPassword"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors p-1"
            >
              <svg v-if="!showNewPassword" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"/><path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"/><path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7c.44 0 .87-.03 1.28-.09"/><line x1="2" x2="22" y1="2" y2="22"/></svg>
            </button>
          </div>

          <div class="relative">
            <input
              v-model="form.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="Confirm new password"
              class="w-full pl-4 pr-12 py-2 rounded-lg bg-[var(--surface-panel)] border border-[var(--color-border)] text-[var(--color-text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] transition-all"
              :class="{ '!border-red-500 focus:!ring-red-500': !passwordsMatch }"
            />
            <button
              type="button"
              @click="showConfirmPassword = !showConfirmPassword"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors p-1"
            >
              <svg v-if="!showConfirmPassword" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"/><path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"/><path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7c.44 0 .87-.03 1.28-.09"/><line x1="2" x2="22" y1="2" y2="22"/></svg>
            </button>
          </div>

          <p v-if="!passwordsMatch" class="text-xs text-red-500 font-medium ml-1">
            Passwords do not match.
          </p>

        </div>
      </div>

      <div v-if="editEmail || editUsername || editPassword" class="pt-4 border-t border-[var(--color-border)] flex items-center gap-3 animate-in fade-in">
        <SettingsButton
          @click="handleSave"
          :disabled="!canSave"
          :class="{ 'opacity-50 cursor-not-allowed': !canSave }"
        >
          Save Changes
        </SettingsButton>
        <button
          @click="handleGlobalCancel"
          class="px-4 py-2 text-sm font-medium text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors"
        >
          Cancel
        </button>
      </div>

    </div>
  </div>
</template>
