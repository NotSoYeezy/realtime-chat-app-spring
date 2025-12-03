<script setup>
import { ref, onMounted } from 'vue'

const isDark = ref(false)

function applyTheme(dark) {
  const html = document.documentElement
  html.classList.toggle('dark', dark)
  isDark.value = dark
}

function toggleTheme() {
  const newValue = !isDark.value
  localStorage.theme = newValue ? 'dark' : 'light'
  applyTheme(newValue)
}

onMounted(() => {
  // 1. Stored preference
  if (localStorage.theme === 'dark') return applyTheme(true)
  if (localStorage.theme === 'light') return applyTheme(false)

  // 2. System default
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  applyTheme(prefersDark)
})
</script>

<template>
  <button
    @click="toggleTheme"
    class="relative inline-flex h-7 w-12 items-center rounded-full transition-colors duration-300 focus:outline-none
           bg-[var(--color-border)]"
    :class="{
      'bg-[var(--color-primary)]': isDark
    }"
  >
    <!-- knob -->
    <span
      class="absolute h-5 w-5 rounded-full bg-[var(--surface-panel)] shadow transform transition-all duration-300"
      :class="{
        'translate-x-6': isDark,
        'translate-x-1': !isDark
      }"
    />
    <!-- icons -->
    <span
      class="absolute left-1 text-xs pointer-events-none text-[var(--color-text-secondary)]"
      v-if="!isDark"
    >
    </span>
    <span
      class="absolute right-1 text-xs pointer-events-none text-[var(--color-text-on-primary)]"
      v-else
    >
    </span>
  </button>
</template>
