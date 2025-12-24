<script setup>
import { ref, onMounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import api from '@/api/axios'
import CreateGroupModal from '@/components/chat/CreateGroupModal.vue'

const chatStore = useChatStore()
const isModalOpen = ref(false)
const allUsers = ref([])

onMounted(async () => {
  await fetchAllUsers()
})

const fetchAllUsers = async () => {
  try {
    const response = await api.get('/users')
    allUsers.value = response.data
  } catch (e) {
    console.error(e)
  }
}

const handleCreateGroup = async (payload) => {
  try {
    const response = await api.post('/groups', {
      name: payload.name,
      memberIds: payload.memberIds
    })
    chatStore.addGroup(response.data)
    isModalOpen.value = false
  } catch (e) {
    console.error(e)
  }
}
</script>

<template>
  <aside class="w-64 hidden md:flex flex-col bg-[var(--surface-panel)] border-r border-[var(--color-border)] h-full">

    <div class="p-4 border-b border-[var(--color-border)] flex justify-between items-center shrink-0">
      <h2 class="font-bold text-[var(--color-text-primary)] text-lg tracking-tight">Messages</h2>

      <button
        @click="isModalOpen = true"
        class="h-8 w-8 flex items-center justify-center rounded-lg bg-[var(--color-primary)] text-white hover:brightness-110 shadow-lg shadow-indigo-500/20 transition-all active:scale-95"
      >
        <span class="material-symbols-outlined text-[20px]">add</span>
      </button>
    </div>

    <div class="p-3">
      <div class="relative">
        <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-[var(--color-text-secondary)] text-[18px]">search</span>
        <input
          placeholder="Search chats..."
          class="w-full bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)] text-sm rounded-lg pl-9 pr-3 py-2 border border-transparent focus:border-[var(--color-border)] focus:bg-[var(--color-bg-input)] focus:outline-none transition-all"
        />
      </div>
    </div>

    <div class="flex-1 overflow-y-auto p-2 space-y-1 custom-scrollbar">
      <button
        v-for="group in chatStore.sortedGroups"
        :key="group.id"
        @click="chatStore.setActiveGroup(group.id)"
        class="w-full text-left p-3 rounded-xl flex items-center gap-3 transition-all duration-200 group relative overflow-hidden"
        :class="chatStore.activeGroupId === group.id ? 'bg-[var(--color-primary)] text-white shadow-md' : 'hover:bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)]'"
      >
        <div
          class="h-10 w-10 shrink-0 rounded-full flex items-center justify-center font-bold uppercase text-sm border-2"
          :class="chatStore.activeGroupId === group.id ? 'bg-white/20 border-transparent text-white' : 'bg-[var(--surface-panel-strong)] border-[var(--surface-panel)] text-[var(--color-text-secondary)] group-hover:bg-[var(--surface-panel)] group-hover:border-[var(--color-border)]'"
        >
          {{ group.name.charAt(0) }}
        </div>

        <div class="flex-1 min-w-0">
          <div class="flex justify-between items-baseline">
            <span class="font-semibold truncate text-sm">{{ group.name }}</span>
            <span v-if="chatStore.activeGroupId !== group.id" class="text-[10px] opacity-60">12:30</span>
          </div>
          <div class="text-xs truncate opacity-70 mt-0.5">
            {{ group.lastMessage || 'No messages yet' }}
          </div>
        </div>

        <span
          v-if="group.unreadCount > 0"
          class="absolute right-3 top-1/2 -translate-y-1/2 min-w-[18px] h-[18px] flex items-center justify-center bg-red-500 text-white text-[10px] font-bold px-1 rounded-full ring-2 ring-[var(--surface-panel)]"
        >
            {{ group.unreadCount }}
        </span>
      </button>
    </div>

    <Teleport to="body">
      <CreateGroupModal
        v-if="isModalOpen"
        :users="allUsers"
        @close="isModalOpen = false"
        @create="handleCreateGroup"
      />
    </Teleport>

  </aside>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: var(--color-border);
  border-radius: 20px;
}
</style>
