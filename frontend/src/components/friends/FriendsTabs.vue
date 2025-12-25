<template>
  <div class="flex gap-2 px-4 py-2 border-b border-[var(--color-border)]">
    <button
      v-for="tab in tabs"
      :key="tab"
      @click="activeTab = tab"
      class="text-xs px-3 py-1.5 rounded-md transition"
      :class="tab === activeTab
      ? 'bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)]'
      : 'text-[var(--color-text-secondary)] hover:bg-[var(--surface-panel-strong)]'"
    >
      {{ tab }}
    </button>
  </div>
</template>
<script setup lang="ts">
</script>
