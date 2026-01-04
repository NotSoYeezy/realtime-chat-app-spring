<script setup>
import { computed } from 'vue'

const props = defineProps({
  activeGroup: {
    type: Object,
    default: null
  },
  currentUser: String
})

const emit = defineEmits(['openInfo'])

const memberCount = computed(() => props.activeGroup?.members?.length || 0)

</script>

<template>
  <header class="h-16 px-4 border-b border-[var(--color-border)] flex justify-between items-center bg-[var(--surface-panel)] shrink-0">

    <div class="flex items-center gap-3 overflow-hidden">
      <div class="h-10 w-10 shrink-0 relative">
        <img
          v-if="activeGroup?.imageUrl"
          :src="activeGroup.imageUrl"
          class="h-full w-full rounded-full object-cover border border-[var(--color-border)]"
        />
        <div
          v-else
          class="h-full w-full rounded-full flex items-center justify-center font-bold text-[var(--color-primary)] bg-[var(--color-primary-bg-soft)]"
        >
          <span v-if="activeGroup?.type === 'PRIVATE'" class="material-symbols-outlined text-[20px]">person</span>
          <span v-else class="material-symbols-outlined text-[20px]">groups</span>
        </div>
      </div>

      <div class="min-w-0">
        <h2 class="text-sm font-bold text-[var(--color-text-primary)] truncate">
          {{ activeGroup?.name || 'Chat' }}
        </h2>
        <p v-if="activeGroup?.type === 'GROUP'" class="text-xs text-[var(--color-text-secondary)]">
          {{ memberCount }} members
        </p>
      </div>
    </div>

    <button
      @click="$emit('openInfo')"
      class="h-9 w-9 flex items-center justify-center rounded-lg text-[var(--color-text-secondary)] hover:bg-[var(--surface-panel-strong)] hover:text-[var(--color-text-primary)] transition-colors"
      title="Group Info"
    >
      <span class="material-symbols-outlined">info</span>
    </button>

  </header>
</template>
