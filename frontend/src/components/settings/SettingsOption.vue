<template>
  <div class="p-4 bg-[var(--surface-panel-strong)] rounded-lg border border-[var(--color-border)] flex items-center justify-between">
    <div>
      <p class="font-medium">{{ label }}</p>
      <p v-if="description" class="text-sm text-[var(--color-text-secondary)]">{{ description }}</p>
    </div>

    <slot />
  </div>
</template>

<script setup>
defineProps({
  label: String,
  description: String
})
</script>
