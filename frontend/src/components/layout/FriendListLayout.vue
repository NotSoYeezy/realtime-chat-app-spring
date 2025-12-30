<script setup>
import { onMounted } from 'vue'
import { useFriendsStore } from '@/stores/friendsStore'
import DefaultUsersList from '@/components/user/DefaultUsersList.vue'

defineEmits([
  'openChat',
  'openFriends'
])

const friendsStore = useFriendsStore()

onMounted(() => {
  if (!friendsStore.friends.length) {
    friendsStore.fetchAll()
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
      :users="friendsStore.friends"
      :loading="friendsStore.loading"
      empty-text="Press here to add your first friend"
      @rowClick="$emit('openChat', $event)"
    />
  </div>
</template>
