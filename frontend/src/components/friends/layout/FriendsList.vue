<script setup>
import { computed, onMounted } from 'vue'
import { useFriendsStore } from '@/stores/friendsStore'

const props = defineProps({
  query: {
    type: String,
    default: ''
  }
})

const friendsStore = useFriendsStore()

onMounted(() => {
  if (!friendsStore.friends.length) {
    friendsStore.fetchAll()
  }
})

const filteredFriends = computed(() => {
  const q = props.query.trim().toLowerCase()
  if (!q) return friendsStore.friends

  return friendsStore.friends.filter(f =>
    f.name?.toLowerCase().includes(q) ||
    f.surname?.toLowerCase().includes(q))
})

const removeFriend = (id) => friendsStore.removeFriend(id)
const blockUser = (id) => friendsStore.blockUser(id)
</script>

<template>
  <div class="flex-1 overflow-y-auto">

    <!-- LOADING -->
    <div
      v-if="friendsStore.loading"
      class="p-4 text-sm text-[var(--color-text-secondary)]"
    >
      Loading friends&#8230;
    </div>

    <div
      v-else-if="!friendsStore.friends.length"
      class="p-4 text-sm text-[var(--color-text-secondary)]"
    >
      You don’t have any friends yet.
    </div>

    <div
      v-else-if="filteredFriends.length === 0"
      class="p-4 text-sm text-[var(--color-text-secondary)]"
    >
      No friends match your search.
    </div>

    <ul v-else class="divide-y divide-[var(--color-border)]">
      <li
        v-for="friend in filteredFriends"
        :key="friend.id"
        class="flex items-center gap-3 p-3 hover:bg-[var(--surface-panel-strong)] transition"
      >
        <div
          class="h-10 w-10 rounded-full flex items-center justify-center
                 bg-[var(--surface-panel-strong)]
                 text-[var(--color-text-primary)]
                 font-bold"
        >
          {{ friend.name?.charAt(0) ?? '?' }}
        </div>

        <div class="min-w-0 flex-1">
          <p class="text-sm font-medium truncate">
            {{ friend.name }} {{ friend.surname }}
          </p>
        </div>

        <div class="flex gap-2 shrink-0">
          <button
            class="text-xs px-2 py-1 rounded-md
                   bg-[var(--color-danger-bg-soft)]
                   text-[var(--color-danger-text)]
                   hover:opacity-90"
            @click="removeFriend(friend.id)"
          >
            Remove
          </button>

          <button
            class="text-xs px-2 py-1 rounded-md
                   text-[var(--color-text-secondary)]
                   hover:text-[var(--color-text-primary)]"
            @click="blockUser(friend.id)"
          >
            Block
          </button>
        </div>
      </li>
    </ul>
  </div>
</template>
