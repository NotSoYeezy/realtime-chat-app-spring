<script setup>
import { ref } from 'vue'
import XButton from '@/components/ui/XButton.vue'
import FriendsList from "@/components/friends/layout/FriendListTab.vue";
import IncomingRequests from "@/components/friends/layout/IncomingRequestsTab.vue";
import AddFriend from "@/components/friends/layout/AddFriendTab.vue";
import BlockedUsers from "@/components/friends/layout/BlockedListTab.vue";

const activeTab = ref('friends')

defineEmits(['close'])

const tabLabel = (tab) => ({
  friends: 'Your Friends',
  adding: 'Add Friend',
  requests: 'Friend Requests',
  blocked: 'Blocked Users',
}[tab])

</script>

<template>
  <div
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/10 backdrop-blur-sm"
    @click.self="$emit('close')"
  >
    <div
      class="bg-[var(--surface-panel)] rounded-xl shadow-2xl w-full max-w-7xl h-3/4 flex flex-col text-[var(--color-text-primary)] overflow-hidden transition-all"
    >
      <header
        class="p-4 border-b border-[var(--color-border)] flex items-center justify-between shrink-0"
      >
        <h2 class="text-xl font-bold">Friends</h2>
        <XButton @close="$emit('close')"/>
      </header>
      <main class="flex flex-col h-full">
        <!-- Tabs -->
        <nav class="flex gap-2 border-b border-[var(--color-border)] px-4">
          <button
            v-for="tab in ['friends', 'adding', 'requests', 'blocked']"
            :key="tab"
            @click="activeTab = tab"
            class="px-3 py-2 text-sm font-medium transition-colors"
            :class="activeTab === tab
        ? 'border-b-2 border-[var(--color-accent)] text-[var(--color-text-primary)]'
        : 'text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)]'"
          >
            {{ tabLabel(tab) }}
          </button>
        </nav>

        <section class="flex-1 overflow-y-auto p-4">
          <FriendsList v-if="activeTab === 'friends'" />
          <AddFriend v-else-if="activeTab === 'adding'" />
          <IncomingRequests v-else-if="activeTab === 'requests'" />
          <BlockedUsers v-else />
        </section>
      </main>

      </div>
    </div>
</template>
