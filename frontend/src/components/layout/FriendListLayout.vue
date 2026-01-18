<script setup>
import { onMounted } from 'vue'
import { useFriendsStore } from '@/stores/friendsStore'
import DefaultUsersList from '@/components/user/DefaultUsersList.vue'

defineProps({
  onlineUsers: Object
})

defineEmits([
  'openChat',
  'openFriends'
])

const friendsStore = useFriendsStore()

onMounted(() => {
  if (!friendsStore.friends.content.length) {
    friendsStore.fetchFriends()
  }
})
</script>

<template>
  <div class="flex-1 flex flex-col overflow-hidden">

    <!-- HEADER -->
    <div
      class="flex items-center justify-between
             px-3 py-2
             border-b border-[var(--color-border)]"
    >
      <span
        class="text-xs font-semibold uppercase tracking-wide
               text-[var(--color-text-secondary)]"
      >
        Friends
      </span>

      <button
        class="h-6 w-6 flex items-center justify-center
               rounded-md text-[16px]
               text-[var(--color-text-secondary)]
               hover:text-[var(--color-text-primary)]
               hover:bg-[var(--surface-panel-strong)]
               transition"
        title="Manage friends"
        @click="$emit('openFriends')"
      >
        &#129730;
      </button>
    </div>

    <!-- FRIENDS LIST (DEFAULT) -->
    <DefaultUsersList
      class="flex-1"
      :users="friendsStore.friends.content"
      :loading="friendsStore.loading"
      :onlineUsers="onlineUsers"
      empty-text="Press here to add your first friend"
      @rowClick="$emit('openChat', $event)"
    />

    <div v-if="friendsStore.friends.hasMore" class="p-2 text-center border-t border-[var(--color-border)]">
      <button
        @click="friendsStore.loadMoreFriends()"
        class="text-xs text-[var(--color-primary)] hover:underline"
      >
        Load More
      </button>
    </div>
  </div>
</template>
