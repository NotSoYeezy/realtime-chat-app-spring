<script setup>
import { ref, onMounted } from 'vue'
import getUser from '@/api/getUser.js'
import SettingsTab from '@/components/settings/SettingsTab.vue'
import SettingsButton from '@/components/settings/SettingsButton.vue'
import CheckPassword from '@/api/checkPassword.js'
import UpdateProfileLayout from '@/components/settings/layout/updateProfileLayout.vue'

defineEmits(['updateProfile'])

const user = ref(null)
const loading = ref(true)
const error = ref(null)
const verifyError = ref(' ')

const showPassword = ref(false)
const showVerify = ref(false)
const showUpdate = ref(false)
const currentPassword = ref('')

const fetchUser = async () => {
  loading.value = true
  error.value = null

  try {
    const idResponse = await getUser.getMyId()
    const myId = idResponse.data

    if (!myId) throw new Error('no ID')

    const profileResponse = await getUser.getUser(myId)
    user.value = profileResponse.data
  } catch (err) {
    console.error('Failed to load profile:', err)
    error.value = 'Could not load user data.'
  } finally {
    loading.value = false
  }
}

const handleVerify = async () => {
  verifyError.value = ' '

  if (!currentPassword.value) {
    verifyError.value = "Please enter your password.";
    return;
  }

  try {
    const response = await CheckPassword.checkPassword(currentPassword.value);
    if(response.status === 200) {
      showUpdate.value = true
      showVerify.value = false
      currentPassword.value = ''
    }
  } catch (err) {
    if (err.response && err.response.status === 400) {
      verifyError.value = "Incorrect password.";
    } else {
      verifyError.value = "Something went wrong.";
    }
  }
}

const handleUpdateSuccess = () => {
  showUpdate.value = false;
  fetchUser();
}

onMounted(fetchUser)
</script>

<template>
  <SettingsTab title="Account Information" description="View and manage your account details.">
    <div class="space-y-6 animate-in fade-in duration-300">

      <div v-if="loading" class="text-sm text-[var(--color-text-secondary)]">
        Loading profile data...
      </div>

      <div v-else-if="error" class="text-red-500 text-sm">
        {{ error }}
      </div>

      <div
        v-else-if="user"
        class="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--surface-panel-strong)]/40 space-y-6"
      >

        <UpdateProfileLayout
          v-if="showUpdate"
          :user="user"
          @cancel="showUpdate = false"
          @saved="handleUpdateSuccess"
        />

        <div v-else class="space-y-6">

          <div class="space-y-4">
            <div>
              <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] mb-1">
                Email
              </label>
              <div class="text-base font-medium">
                {{ user.email }}
              </div>
            </div>

            <div>
              <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] mb-1">
                Username
              </label>
              <div class="text-base font-medium">
                {{ user.username }}
              </div>
            </div>
          </div>

          <div class="pt-2">
            <div v-if="!showVerify">
              <SettingsButton @click="showVerify = true">
                Update profile
              </SettingsButton>
            </div>

            <div v-else class="space-y-4 pt-4 border-t border-[var(--color-border)] animate-in slide-in-from-top-2 fade-in duration-200">
              <p class="text-sm text-[var(--color-text-secondary)]">
                To make changes, please confirm your current password.
              </p>

              <div>
                <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] mb-1 font-semibold">
                  Current Password
                </label>

                <div class="relative w-full max-w-md">
                  <input
                    v-model="currentPassword"
                    :type="showPassword ? 'text' : 'password'"
                    placeholder="Enter password"
                    class="w-full px-4 py-2 pr-10 rounded-lg bg-[var(--surface-panel)] border border-[var(--color-border)] text-[var(--color-text-primary)] placeholder-[var(--color-text-secondary)]/50 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] focus:border-transparent transition-all"
                    :class="{ verifyError }"
                  />

                  <button
                    type="button"
                    @click="showPassword = !showPassword"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors focus:outline-none"
                  >
                    <svg v-if="!showPassword" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                      <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>

                    <svg v-else xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
                    </svg>
                  </button>
                </div>

                <p v-if="verifyError" class="text-red-500 text-xs mt-1">
                  {{ verifyError }}
                </p>
              </div>

              <div class="flex items-center gap-3">
                <SettingsButton @click="handleVerify">
                  Verify
                </SettingsButton>
                <button
                  @click="showVerify = false; currentPassword = ''"
                  class="px-4 py-2 text-sm font-medium text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors"
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div> </div>
    </div>
  </SettingsTab>
</template>
