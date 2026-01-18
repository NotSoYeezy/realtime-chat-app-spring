<script setup>
import { ref, onMounted, computed } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useFriendsStore } from '@/stores/friendsStore'
import api from '@/api/axios'
import CreateGroupModal from '@/components/chat/CreateGroupModal.vue'

const chatStore = useChatStore()
const friendsStore = useFriendsStore()
const isModalOpen = ref(false)

const searchQuery = ref('')

onMounted(async () => {
  if (friendsStore.friends.content.length === 0) {
    await friendsStore.fetchFriends()
  }
})

const filteredGroups = computed(() => {
  const query = searchQuery.value.toLowerCase().trim()

  if (!query) {
    return chatStore.sortedGroups
  }

  return chatStore.sortedGroups.filter(group =>
    group.name.toLowerCase().includes(query)
  )
})

const handleCreateGroup = async (payload) => {
  try {
    const formData = new FormData();

    const groupData = {
      name: payload.name,
      memberIds: payload.memberIds
    };

    formData.append('data', new Blob([JSON.stringify(groupData)], {
      type: 'application/json'
    }));

    if (payload.image) {
      formData.append('image', payload.image);
    }

    const response = await api.post('/groups', formData, {
      headers: {
        'Content-Type': undefined
      }
    });
    chatStore.addGroup(response.data);
    isModalOpen.value = false;

  } catch (e) {
    console.error(e)
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
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
          v-model="searchQuery"
          placeholder="Search chats..."
          class="w-full bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)] text-sm rounded-lg pl-9 pr-3 py-2 border border-transparent focus:border-[var(--color-border)] focus:bg-[var(--color-bg-input)] focus:outline-none transition-all"
        />

        <button
          v-if="searchQuery"
          @click="searchQuery = ''"
          class="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)]"
        >
          <span class="material-symbols-outlined text-[16px]">close</span>
        </button>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto p-2 space-y-1 custom-scrollbar">
      <button
        v-for="group in filteredGroups"
        :key="group.id"
        @click="chatStore.setActiveGroup(group.id)"
        class="w-full text-left p-3 rounded-xl flex items-center gap-3 transition-all duration-200 group relative overflow-hidden"
        :class="[
          chatStore.activeGroupId === group.id
            ? 'bg-[var(--color-primary)] text-white shadow-md'
            : (group.unreadCount > 0 && !group.muted)
              ? 'bg-[var(--color-primary-bg-soft)] text-[var(--color-text-primary)] border-l-4 border-[var(--color-primary)] rounded-l-none'
              : 'hover:bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)]'
        ]"
      >

        <div class="h-10 w-10 shrink-0 relative">
          <img
            v-if="group.imageUrl"
            :src="group.imageUrl"
            alt="Group Image"
            class="h-full w-full rounded-full object-cover border border-[var(--color-border)]"
          />

          <div
            v-else
            class="h-full w-full rounded-full flex items-center justify-center font-bold uppercase text-sm border-2 transition-colors"
            :class="chatStore.activeGroupId === group.id
      ? 'bg-white/20 border-transparent text-white'
      : 'bg-[var(--surface-panel-strong)] border-[var(--surface-panel)] text-[var(--color-text-secondary)] group-hover:bg-[var(--surface-panel)] group-hover:border-[var(--color-border)]'"
          >
            {{ group.name.charAt(0) }}
          </div>
        </div>

        <div class="flex-1 min-w-0">
          <div class="flex justify-between items-baseline">
            <span
              class="truncate text-sm flex items-center gap-1"
              :class="(group.unreadCount > 0 && !group.muted && chatStore.activeGroupId !== group.id) ? 'font-bold' : 'font-semibold'"
            >
               {{ group.name }}
               <span v-if="group.muted" class="material-symbols-outlined text-[12px] opacity-70">notifications_off</span>
            </span>
            <span v-if="chatStore.activeGroupId !== group.id" class="text-[10px] opacity-60 shrink-0">{{ formatTime(group.lastMessageTime) }}</span>
          </div>

          <div class="flex justify-between items-center mt-0.5 gap-2">
            <div
              class="text-xs truncate flex-1"
              :class="(group.unreadCount > 0 && !group.muted && chatStore.activeGroupId !== group.id) ? 'opacity-100 font-medium' : 'opacity-70'"
            >
              {{ group.lastMessage || 'No messages yet' }}
            </div>

            <span
              v-if="group.unreadCount > 0 && !group.muted"
              class="min-w-[18px] h-[18px] flex items-center justify-center bg-red-500 text-white text-[10px] font-bold px-1 rounded-full shrink-0"
              :class="chatStore.activeGroupId === group.id ? 'ring-2 ring-[var(--color-primary)]' : ''"
            >
                {{ group.unreadCount }}
            </span>
          </div>
        </div>
      </button>

      <button
        v-if="chatStore.groupsHasMore"
        @click="chatStore.loadMoreGroups()"
        class="w-full text-center text-xs text-[var(--color-primary)] hover:underline py-2"
      >
        Load More
      </button>

      <div v-if="filteredGroups.length === 0 && searchQuery" class="text-center p-4 text-[var(--color-text-secondary)] text-sm">
        No chats found.
      </div>
    </div>

    <Teleport to="body">
      <CreateGroupModal
        v-if="isModalOpen"
        :users="friendsStore.friends.content"
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
