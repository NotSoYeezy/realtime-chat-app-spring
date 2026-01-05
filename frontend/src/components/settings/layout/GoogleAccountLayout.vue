<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'

defineProps({
  currentUser: String
})

const isConnected = ref(false)
const allowStatusSync = ref(false)

onMounted(() => {
  isConnected.value = localStorage.getItem('google_calendar') === 'true'
  allowStatusSync.value = localStorage.getItem('google_calendar_sync_status') === 'true'
})

watch(allowStatusSync, (newValue) => {
  localStorage.setItem('google_calendar_sync_status', newValue)
  console.log('Status sync updated:', newValue)
})

const handleConnect = async () => {
  try {
    const token = localStorage.getItem('access_token');

    await axios.post('http://localhost:8080/api/google/google-link', {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      withCredentials: true
    });

    window.location.href = 'http://localhost:8080/oauth2/authorization/google';

  } catch (error) {
    console.error("Failed to initiate linking:", error);
  }
}
</script>

<template>
  <div class="animate-fade-in">
    <div class="mb-8">
      <h3 class="text-lg font-semibold mb-1">Google Account Integration</h3>
      <p class="text-[var(--color-text-secondary)] text-sm">
        Connect your Google Account to access features like Calendar sync.
      </p>
    </div>

    <div class="bg-[var(--surface-panel-strong)]/30 border border-[var(--color-border)] rounded-lg p-5 flex items-center justify-between mb-6">
      <div class="flex items-center gap-4">
        <div
          class="w-10 h-10 rounded-full flex items-center justify-center transition-colors"
          :class="isConnected ? 'bg-green-500/20 text-green-400' : 'bg-blue-500/20 text-blue-400'"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
        </div>
        <div>
          <h4 class="font-medium">Google Account</h4>
          <p class="text-xs transition-colors" :class="isConnected ? 'text-green-400' : 'text-[var(--color-text-secondary)]'">
            {{ isConnected ? 'Connected' : 'Not connected' }}
          </p>
        </div>
      </div>

      <button
        v-if="!isConnected"
        @click="handleConnect"
        class="px-4 py-2 bg-[var(--surface-panel-strong)] hover:bg-[var(--surface-panel-strong)]/80 border border-[var(--color-border)] rounded-lg text-sm transition-colors"
      >
        Connect
      </button>

    </div>

    <div v-if="isConnected" class="border-t border-[var(--color-border)] pt-6">
      <div class="flex items-center justify-between">
        <div class="flex flex-col">
          <span class="font-medium text-[var(--color-text-primary)]">Sync Availability</span>
          <span class="text-sm text-[var(--color-text-secondary)]">Allow Google Calendar to modify your status</span>
        </div>

        <button
          @click="allowStatusSync = !allowStatusSync"
          class="relative inline-flex h-7 w-12 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] focus:ring-offset-2 focus:ring-offset-[var(--surface-panel)]"
          :class="allowStatusSync ? 'bg-[var(--color-primary)]' : 'bg-gray-600'"
        >
          <span class="sr-only">Allow calendar to modify your status</span>
          <span
            class="inline-block h-5 w-5 transform rounded-full bg-white transition-transform duration-200 ease-in-out"
            :class="allowStatusSync ? 'translate-x-6' : 'translate-x-1'"
          />
        </button>
      </div>
    </div>

  </div>
</template>
