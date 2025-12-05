<script setup>
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
</script>

<template>
  <div class="space-y-6 animate-in fade-in duration-300">
    <div>
      <h3 class="text-lg font-semibold mb-1">Display Options</h3>
      <p class="text-sm text-[var(--color-text-secondary)] mb-4">
        Customize how the application looks on your device.
      </p>

      <div class="flex items-center justify-between p-4 bg-[var(--surface-panel-strong)] rounded-lg border border-[var(--color-border)]">
        <div class="flex flex-col">
          <label for="dark-mode-toggle" class="font-medium">Dark Mode</label>
          <span class="text-xs text-[var(--color-text-secondary)]">Switch between light and dark themes</span>
        </div>
        <ThemeToggle id="dark-mode-toggle" />
      </div>
    </div>
  </div>
</template>
