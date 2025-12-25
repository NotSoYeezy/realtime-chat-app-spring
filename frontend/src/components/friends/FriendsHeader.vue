<template>
  <div class="p-4 border-b border-[var(--color-border)]">
    <h2 class="text-sm font-semibold text-[var(--color-text-primary)] mb-2">
      Friends
    </h2>

    <input
      v-model="query"
      placeholder="Search users…"
      class="w-full px-3 py-2 text-sm rounded-md
           bg-[var(--surface-panel-strong)]
           border border-[var(--color-border)]
           focus:outline-none"
    />
  </div>

</template>
<script setup lang="ts">
</script>
