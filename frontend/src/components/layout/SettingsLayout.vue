<script setup>
// Removed unused imports: ref, watch, onMounted

import ThemeToggle from '@/components/ui/ThemeToggle.vue' // <-- 1. Keep the ThemeToggle import

defineEmits(['close'])
defineProps({
    currentUser: String
  }
)

// 2. REMOVED: isDarkMode ref, toggleTheme function, watch, and onMounted hook
</script>

<template>
  <div
    class="fixed inset-0 z-50 flex items-center justify-center
           bg-black bg-opacity-50 backdrop-blur-sm"
    @click.self="$emit('close')"
  >
    <div
      class="bg-[var(--surface-panel)] rounded-xl shadow-2xl w-full max-w-2xl h-3/4
             flex flex-col text-[var(--color-text-primary)] overflow-hidden"
    >
      <header class="p-4 border-b border-[var(--color-border)] flex items-center justify-between">
        <h2 class="text-xl font-bold">User Settings</h2>
        <button
          @click="$emit('close')"
          class="text-[var(--color-text-secondary)] hover:text-red-500 transition-colors"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </header>

      <div class="flex-1 p-6 overflow-y-auto">
        <h3 class="font-semibold mb-3">Display Options</h3>
        <div class="space-y-4">

          <div class="flex items-center justify-between p-3 bg-[var(--surface-panel-strong)] rounded-md">
            <label for="dark-mode-toggle" class="text-sm">Enable Dark Mode</label>

            <ThemeToggle id="dark-mode-toggle" />

          </div>
          <h3 class="font-semibold mb-3">Account Information</h3>
          <div class="p-3 bg-[var(--surface-panel-strong)] rounded-md">
            <p class="text-sm text-[var(--color-text-secondary)]">Current Username: {{currentUser}}</p>
            <button class="mt-2 text-sm font-medium text-[var(--color-primary)] hover:underline">
              Update Profile
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
