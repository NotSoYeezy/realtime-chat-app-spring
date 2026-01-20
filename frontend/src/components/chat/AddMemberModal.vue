<script setup>
import { ref, computed } from 'vue'
import BaseButton from '@/components/ui/BaseButton.vue'

const props = defineProps({
  friends: Array,
  currentMembers: Array
})

const emit = defineEmits(['close', 'add'])

const searchQuery = ref('')
const selectedIds = ref(new Set())

const availableFriends = computed(() => {
  const q = searchQuery.value.toLowerCase().trim()
  const memberIds = new Set(props.currentMembers.map(m => m.id))

  return props.friends.filter(f => {
    const matchesSearch = !q ||
      f.name.toLowerCase().includes(q) ||
      f.surname.toLowerCase().includes(q) ||
      f.username.toLowerCase().includes(q)

    return matchesSearch && !memberIds.has(f.id)
  })
})

const toggleSelection = (userId) => {
  if (selectedIds.value.has(userId)) {
    selectedIds.value.delete(userId)
  } else {
    selectedIds.value.add(userId)
  }
}

const handleAdd = () => {
  if (selectedIds.value.size === 0) return
  emit('add', Array.from(selectedIds.value))
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="$emit('close')"></div>

    <div class="relative w-full max-w-sm bg-[var(--surface-panel)] rounded-xl shadow-2xl border border-[var(--color-border)] overflow-hidden flex flex-col max-h-[70vh] animate-in zoom-in-95 duration-200">

      <div class="p-4 border-b border-[var(--color-border)] flex justify-between items-center bg-[var(--surface-panel-strong)]/50">
        <h3 class="font-bold text-[var(--color-text-primary)]">
          Add Members <span v-if="selectedIds.size > 0" class="text-[var(--color-primary)]">({{ selectedIds.size }})</span>
        </h3>
        <button @click="$emit('close')" class="text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)]">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>

      <div class="p-3 bg-[var(--surface-panel)]">
        <input
          v-model="searchQuery"
          placeholder="Search friends..."
          class="w-full px-3 py-2 text-sm bg-[var(--surface-panel-strong)] border border-[var(--color-border)] rounded-lg focus:outline-none focus:border-[var(--color-primary)] text-[var(--color-text-primary)]"
        />
      </div>

      <div class="flex-1 overflow-y-auto p-2 space-y-1">
        <div v-if="availableFriends.length === 0" class="text-center text-sm text-[var(--color-text-secondary)] py-8">
          No friends available to add.
        </div>

        <div
          v-for="user in availableFriends"
          :key="user.id"
          @click="toggleSelection(user.id)"
          class="w-full flex items-center gap-3 p-2 rounded-lg cursor-pointer transition-colors border border-transparent"
          :class="selectedIds.has(user.id) ? 'bg-[var(--color-primary-bg-soft)] border-[var(--color-primary)]/30' : 'hover:bg-[var(--surface-hover)]'"
        >
          <div
            class="w-5 h-5 rounded border flex items-center justify-center transition-colors"
            :class="selectedIds.has(user.id) ? 'bg-[var(--color-primary)] border-[var(--color-primary)]' : 'border-[var(--color-text-secondary)]'"
          >
            <span v-if="selectedIds.has(user.id)" class="material-symbols-outlined text-white text-[14px] font-bold">check</span>
          </div>

          <div class="h-8 w-8 rounded-full bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)] flex items-center justify-center font-bold text-xs">
            {{ user.name?.charAt(0) }}
          </div>
          <div>
            <div class="text-sm font-medium text-[var(--color-text-primary)]">
              {{ user.name }} {{ user.surname }}
            </div>
            <div class="text-xs text-[var(--color-text-secondary)]">@{{ user.username }}</div>
          </div>
        </div>
      </div>

      <div class="p-4 border-t border-[var(--color-border)] bg-[var(--surface-panel)]">
        <BaseButton
          @click="handleAdd"
          :fullWidth="true"
          :disabled="selectedIds.size === 0"
        >
          Add Selected
        </BaseButton>
      </div>
    </div>
  </div>
</template>
