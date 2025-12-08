<script setup lang="ts">
import ThemeToggle from '@/components/ui/ThemeToggle.vue'

defineEmits(['changeRoom'])
const rooms = ['general', 'random', 'help', 'news']
</script>

<template>
  <aside
    class="w-64 hidden md:flex flex-col bg-[var(--surface-panel)] border-r border-[var(--color-border)]"
  >
    <div class="flex p-4 border-b border-[var(--color-border)]">
      <h2 class="font-bold text-[var(--color-text-primary)] text-lg">Channels</h2>
    </div>
    <nav class="flex-1 overflow-y-auto p-2">
      <button
        v-for="room in rooms"
        :key="room"
        @click="$emit('changeRoom', room)"
        class="w-full text-left px-4 py-2 rounded hover:bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)]"
      >
        # {{ room }}
      </button>
    </nav>
  </aside>
</template>
