<script setup>
import { ref, watch, onMounted } from 'vue'

const loadDarkModePreference = () => {
  return localStorage.getItem('darkMode') === 'true'
}

const saveDarkModePreference = (value) => {
  localStorage.setItem('darkMode', value)
}

const isDarkMode = ref(loadDarkModePreference())
watch(isDarkMode, (newValue) => {
  saveDarkModePreference(newValue)
  if (newValue) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
})

onMounted(() => {
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark')
  }
})

</script>

<template>
  <div class="p-6 bg-white dark:bg-slate-900 min-h-screen">
    <div class="p-6">
      <div class="flex justify-between items-center mb-6 border-b pb-4 dark:border-slate-700">
        <h1 class="text-3xl font-bold text-slate-800 dark:text-white">Settings</h1>
        <button @click="$emit('back')" class="dark:text-red-600 hover:text-slate-600 dark:hover:text-slate-300 transition-colors p-1 rounded-full">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
    </div>
  </div>
</template>
