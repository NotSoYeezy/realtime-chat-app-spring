<script setup>
import { ref, onMounted } from 'vue'
import getUser from '@/api/getUser.js'
import SettingsTab from '@/components/settings/SettingsTab.vue'
import SettingsButton from '@/components/settings/SettingsButton.vue'

defineEmits(['updateProfile'])

const user = ref(null)
const loading = ref(true)
const error = ref(null)

const fetchUser = async () => {
  loading.value = true
  error.value = null

  try {
    const idResponse = await getUser.getMyId()
    const myId = idResponse.data

    if (!myId) throw new Error('Backend returned no ID')

    const profileResponse = await getUser.getUser(myId)
    user.value = profileResponse.data
  } catch (err) {
    console.error('Failed to load profile:', err)
    error.value = 'Could not load user data.'
  } finally {
    loading.value = false
  }
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
        <div class="space-y-4">
          <div>
            <label
              class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] mb-1"
            >
              Email
            </label>
            <div class="text-base font-medium">
              {{ user.email }}
            </div>
          </div>

          <div>
            <label
              class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] mb-1"
            >
              Username
            </label>
            <div class="text-base font-medium">
              {{ user.username }}
            </div>
          </div>
        </div>

        <div class="pt-2">
          <SettingsButton>Update profile</SettingsButton>
        </div>
      </div>
    </div>
  </SettingsTab>
</template>
