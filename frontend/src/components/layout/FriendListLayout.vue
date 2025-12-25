<script setup>
import { useFriendsStore } from '@/stores/friendsStore'

const emit = defineEmits([
  'openChat',
  'openFriends',
  'removeFriend'
])

const friendsStore = useFriendsStore()
</script>

<template>
  <div class="flex-1 flex flex-col overflow-hidden">

    <!-- HEADER -->
    <div class="flex items-center justify-between px-3 py-2 border-b border-[var(--color-border)]">
      <span class="text-xs font-semibold uppercase tracking-wide text-[var(--color-text-secondary)]">
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

    <!-- FRIENDS LIST -->
    <div
      v-for="friend in friendsStore.friends"
      :key="friend.id"
      class="flex items-center gap-3 p-2 rounded-lg
             hover:bg-[var(--surface-panel-strong)]
             cursor-pointer"
      @click="$emit('openChat', friend)"
    >
      <div class="text-[var(--color-text-primary)]  h-9 w-9 rounded-full flex items-center justify-center font-bold bg-[var(--surface-panel-strong)]">
        {{ friend.name.charAt(0) }}
      </div>

      <div class="flex-1 min-w-0 text-[var(--color-text-primary)]">
        <p class="text-sm truncate">{{ friend.name }} {{ friend.surname }}</p>
      </div>
    </div>

  </div>
</template>
