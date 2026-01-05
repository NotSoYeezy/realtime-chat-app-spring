<script setup>
import { ref, onMounted } from 'vue'
import getUser from '@/api/getUser.js'
import SettingsTab from '@/components/settings/SettingsTab.vue'
import SettingsButton from '@/components/settings/SettingsButton.vue'
import CheckPassword from '@/api/checkPassword.js'
import UpdateProfileLayout from '@/components/settings/layout/UpdateProfileLayout.vue'
import PasswordInput from "@/components/auth/PasswordInput.vue";
import UpdateUser from "@/api/updateUser.js";

const emit = defineEmits(['updateProfile', 'logout'])

const user = ref(null)
const loading = ref(true)
const error = ref(null)

const hasPassword = ref(true)
const showVerify = ref(false)
const showUpdate = ref(false)
const showCreatePassword = ref(false)

const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

const verifyError = ref(' ')
const createError = ref(' ')

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

const checkPasswordStatus = async () => {
  try {
    const response = await CheckPassword.checkPassword(null);

    if (response.status === 200) {
      hasPassword.value = true;
    }
  } catch (err) {
    if (err.response && err.response.status === 501) {
      hasPassword.value = false;
    } else {
      console.error("Error checking password status:", err);
    }
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


const handleCreatePassword = async () => {
  createError.value = ' '
  const payload = {}

  if (!newPassword.value || !confirmPassword.value) {
    createError.value = "Please fill in both fields.";
    return;
  }

  if (newPassword.value !== confirmPassword.value) {
    createError.value = "Passwords do not match.";
    return;
  }

  try {
    payload.password = newPassword.value;
    const response = await UpdateUser.updateUser(payload)
    if(response.status === 200) {
      showCreatePassword.value = false;
      hasPassword.value = true;
      newPassword.value = '';
      confirmPassword.value = '';
    }
  } catch (err) {
    createError.value = err.load.data.message;
  }
}

const handleCancel = () => {
  showUpdate.value = false
  fetchUser()
}

const handleUpdateSuccess = () => {
  showUpdate.value = false;
  fetchUser();
  emit('logout');
}

onMounted( async () => {
  await fetchUser();
  await checkPasswordStatus();
})
</script>

<template>
  <SettingsTab title="Account Information" description="View and manage your account details.">
    <div class="space-y-6 animate-in fade-in duration-300">

      <div v-if="loading" class="text-sm text-[var(--color-text-secondary)]">
        Loading profile data&#8230;
      </div>

      <div v-else-if="error" class="text-[var(--color-danger-text)] text-sm">
        {{ error }}
      </div>

      <div
        v-else-if="user"
        class="p-6 rounded-xl border border-[var(--color-border)] bg-[var(--surface-panel-strong)]/40 space-y-6"
      >

        <UpdateProfileLayout
          v-if="showUpdate"
          :user="user"
          @cancel="handleCancel"
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

            <div>
              <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] mb-1">
                Name
              </label>
              <div class="text-base font-medium">
                {{ user.name }} {{ user.surname }}
              </div>
            </div>
          </div>

          <div class="pt-2 border-t border-[var(--color-border)]">

            <div v-if="hasPassword">
              <div v-if="!showVerify" class="pt-4">
                <SettingsButton @click="showVerify = true">
                  Update profile
                </SettingsButton>
              </div>

              <div v-else class="space-y-4 pt-4 animate-in slide-in-from-top-2 fade-in duration-200">
                <p class="text-sm text-[var(--color-text-secondary)]">
                  To make changes, please confirm your current password.
                </p>

                <div>
                  <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] mb-1 font-semibold">
                    Current Password
                  </label>
                  <PasswordInput
                    v-model="currentPassword"
                    :type="'password'"
                    placeholder="Enter password"
                    :class="{ verifyError }"
                  />
                  <p v-if="verifyError.trim()" class="text-[var(--color-danger-text)] text-xs mt-1">
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

            <div v-else>
              <div v-if="!showCreatePassword" class="pt-4 space-y-4">
                <p class="text-sm text-[var(--color-text-secondary)]">
                  You are logged in via Google. Set a password to enable profile editing.
                </p>
                <div>
                  <SettingsButton @click="showCreatePassword = true">
                    Create Password
                  </SettingsButton>
                </div>
              </div>
              <div v-else class="space-y-4 pt-4 animate-in slide-in-from-top-2 fade-in duration-200">
                <div class="grid gap-4">
                  <div>
                    <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] mb-1 font-semibold">
                      New Password
                    </label>
                    <PasswordInput
                      v-model="newPassword"
                      type="password"
                      placeholder="Enter new password"
                    />
                  </div>

                  <div>
                    <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] mb-1 font-semibold">
                      Confirm Password
                    </label>
                    <PasswordInput
                      v-model="confirmPassword"
                      type="password"
                      placeholder="Confirm new password"
                    />
                  </div>
                </div>

                <p v-if="createError.trim()" class="text-[var(--color-danger-text)] text-xs">
                  {{ createError }}
                </p>

                <div class="flex items-center gap-3">
                  <SettingsButton @click="handleCreatePassword">
                    Set Password
                  </SettingsButton>
                  <button
                    @click="showCreatePassword = false; newPassword = ''; confirmPassword = ''"
                    class="px-4 py-2 text-sm font-medium text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  </SettingsTab>
</template>
