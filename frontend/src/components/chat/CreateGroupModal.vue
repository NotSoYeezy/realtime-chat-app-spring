<script setup>
import { ref, computed } from 'vue'
import BaseButton from '@/components/ui/BaseButton.vue'

const props = defineProps({
  users: {
    type: Array,
    default: () => []
  },
  loading: Boolean
})

const emit = defineEmits(['close', 'create'])

const groupName = ref('')
const searchQuery = ref('')
const selectedUserIds = ref(new Set())

const filteredUsers = computed(() => {
  if (!searchQuery.value) return props.users
  const q = searchQuery.value.toLowerCase()
  return props.users.filter(u =>
    u.name.toLowerCase().includes(q) ||
    u.surname.toLowerCase().includes(q) ||
    u.username.toLowerCase().includes(q)
  )
})

const toggleUser = (userId) => {
  if (selectedUserIds.value.has(userId)) {
    selectedUserIds.value.delete(userId)
  } else {
    selectedUserIds.value.add(userId)
  }
}

const handleCreate = () => {
  if (!groupName.value.trim()) return

  emit('create', {
    name: groupName.value,
    memberIds: Array.from(selectedUserIds.value)
  })
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <div
      class="absolute inset-0 bg-black/40 backdrop-blur-sm transition-opacity"
      @click="$emit('close')"
    ></div>

    <div class="relative w-full max-w-md bg-[var(--surface-panel)] rounded-xl shadow-2xl border border-[var(--color-border)] overflow-hidden flex flex-col max-h-[80vh] animate-in zoom-in-95 duration-200">

      <div class="p-4 border-b border-[var(--color-border)] flex justify-between items-center bg-[var(--surface-panel-strong)]/50">
        <h3 class="text-lg font-bold text-[var(--color-text-primary)]">New Group Chat</h3>
        <button
          @click="$emit('close')"
          class="text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors"
        >
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>

      <div class="p-6 space-y-6 overflow-y-auto">

        <div class="space-y-2">
          <label class="text-xs font-bold uppercase text-[var(--color-text-secondary)] tracking-wider">
            Group Name
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <span class="material-symbols-outlined text-[var(--color-text-secondary)]">tag</span>
            </div>
            <input
              v-model="groupName"
              type="text"
              placeholder="e.g. Project Alpha Team"
              class="w-full pl-10 pr-4 py-2.5 bg-[var(--color-bg-input)] border border-[var(--color-border)] rounded-lg focus:ring-2 focus:ring-[var(--color-primary)] focus:outline-none text-[var(--color-text-primary)] placeholder:text-[var(--color-text-secondary)]"
              autofocus
            />
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-xs font-bold uppercase text-[var(--color-text-secondary)] tracking-wider flex justify-between">
            <span>Add Members</span>
            <span class="text-[var(--color-primary)]" v-if="selectedUserIds.size > 0">
                {{ selectedUserIds.size }} selected
              </span>
          </label>

          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search people..."
            class="w-full px-3 py-2 text-sm bg-[var(--surface-panel-strong)] border border-[var(--color-border)] rounded-lg focus:outline-none focus:border-[var(--color-text-secondary)] text-[var(--color-text-primary)] placeholder:text-[var(--color-text-secondary)] mb-2"
          />

          <div class="h-48 overflow-y-auto border border-[var(--color-border)] rounded-lg bg-[var(--color-bg-input)]">
            <div v-if="filteredUsers.length === 0" class="p-4 text-center text-sm text-[var(--color-text-secondary)]">
              No users found.
            </div>

            <div
              v-for="user in filteredUsers"
              :key="user.id || user.username"
              @click="toggleUser(user.id)"
              class="flex items-center gap-3 p-2 hover:bg-[var(--surface-hover)] cursor-pointer transition-colors border-b border-[var(--color-border)] last:border-0"
              :class="{ 'bg-[var(--color-primary-bg-soft)]': selectedUserIds.has(user.id) }"
            >
              <div
                class="w-5 h-5 rounded border flex items-center justify-center transition-colors"
                :class="selectedUserIds.has(user.id) ? 'bg-[var(--color-primary)] border-[var(--color-primary)]' : 'border-[var(--color-text-secondary)]'"
              >
                <span v-if="selectedUserIds.has(user.id)" class="material-symbols-outlined text-white text-[14px] font-bold">check</span>
              </div>

              <div class="h-8 w-8 rounded-full bg-[var(--surface-panel-strong)] flex items-center justify-center text-xs font-bold">
                {{ user.name?.charAt(0) }}
              </div>
              <div class="flex flex-col">
                   <span class="text-sm font-medium text-[var(--color-text-primary)]">
                     {{ user.name }} {{ user.surname }}
                   </span>
                <span class="text-xs text-[var(--color-text-secondary)]">
                     @{{ user.username }}
                   </span>
              </div>
            </div>
          </div>
        </div>

      </div>

      <div class="p-4 border-t border-[var(--color-border)] bg-[var(--surface-panel)] flex justify-end gap-3">
        <button
          @click="$emit('close')"
          class="px-4 py-2 text-sm font-medium text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors"
        >
          Cancel
        </button>
        <BaseButton
          @click="handleCreate"
          :disabled="!groupName.trim() || loading"
          class="!h-9 !text-sm !px-6"
        >
          Create Group
        </BaseButton>
      </div>

    </div>
  </div>
</template>
