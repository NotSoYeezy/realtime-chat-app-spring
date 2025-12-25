<script setup>
import { computed, ref, onMounted } from 'vue'
import { useFriendsStore } from '@/stores/friendsStore'

const props = defineProps({
  query: {
    type: String,
    default: ''
  }
})

const friendsStore = useFriendsStore()

onMounted(() => {
  if (!friendsStore.blocked.length) {
    friendsStore.fetchAll()
  }
})

const items = computed(() => friendsStore.blocked ?? [])

const filteredItems = computed(() => {
  const q = props.query.trim().toLowerCase()
  if (!q) return items.value

  return items.value.filter(user =>
    user.name?.toLowerCase().includes(q) ||
    user.surname?.toLowerCase().includes(q) ||
    user.username?.toLowerCase().includes(q)
  )
})

const busyId = ref(null)
const localError = ref('')

const unblock = async (user) => {
  localError.value = ''
  busyId.value = user.id
  try {
    await friendsStore.unblockUser(user.id)
  } catch (e) {
    localError.value = 'Unblock failed.'
    console.error(e)
  } finally {
    busyId.value = null
  }
}
</script>

<template>
  <div class="flex-1 overflow-y-auto">

    <!-- ERROR -->
    <div
      v-if="localError"
      class="p-2 text-xs text-[var(--color-danger-text)]"
    >
      {{ localError }}
    </div>

    <!-- LOADING -->
    <div
      v-if="friendsStore.loading"
      class="p-4 text-sm text-[var(--color-text-secondary)]"
    >
      Loading blocked users…
    </div>

    <!-- EMPTY -->
    <div
      v-else-if="items.length === 0"
      class="p-4 text-sm text-[var(--color-text-secondary)]"
    >
      You don’t have any blocked users.
    </div>

    <!-- NO MATCH -->
    <div
      v-else-if="filteredItems.length === 0"
      class="p-4 text-sm text-[var(--color-text-secondary)]"
    >
      No blocked users match your search.
    </div>

    <!-- LIST -->
    <ul v-else class="divide-y divide-[var(--color-border)]">
      <li
        v-for="user in filteredItems"
        :key="user.friendshipId"
        class="flex items-center gap-3 p-3
               hover:bg-[var(--surface-panel-strong)]
               transition"
      >
        <!-- avatar -->
        <div
          class="h-10 w-10 rounded-full flex items-center justify-center
                 bg-[var(--surface-panel-strong)]
                 text-[var(--color-text-primary)]
                 font-bold"
        >
          {{ user.name?.charAt(0) ?? '?' }}
        </div>

        <!-- user -->
        <div class="min-w-0 flex-1">
          <p class="text-sm font-medium truncate">
            {{ user.name }} {{ user.surname }}
          </p>
        </div>

        <!-- UNBLOCK -->
        <button
          class="text-xs px-2 py-1 rounded-md
                 bg-[var(--color-warning-bg-soft)]
                 text-[var(--color-warning-text)]
                 font-semibold hover:opacity-90
                 disabled:opacity-50"
          :disabled="busyId === user.id"
          @click="unblock(user)"
        >
          {{ busyId === user.id ? 'Unblocking…' : 'Unblock' }}
        </button>
      </li>
    </ul>
  </div>
</template>
