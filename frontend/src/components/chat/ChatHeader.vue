<script setup>
defineProps({
  currentName: String,
  currentSurname: String
})
</script>

<template>
  <header class="p-4 border-b border-[var(--color-border)] flex justify-between items-center">
    <div class="flex items-center gap-3">
      <div
        class="h-10 w-10 bg-[var(--color-primary-bg-soft)] text-[var(--color-primary)] rounded-lg flex items-center justify-center"
      >
        <span class="material-symbols-outlined">tag</span>
      </div>

      <p class="text-xs text-[var(--color-text-secondary)]">
        Logged in as:
        <span class="font-medium text-[var(--color-primary)]">{{ currentName }} {{currentSurname }}</span>
      </p>
    </div>
  </header>
</template>
