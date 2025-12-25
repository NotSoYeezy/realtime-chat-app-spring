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
  if (!friendsStore.outgoing.length) {
    friendsStore.fetchAll()
  }
})

const items = computed(() => friendsStore.outgoing ?? [])

const filteredItems = computed(() => {
  const q = props.query.trim().toLowerCase()
  if (!q) return items.value

  return items.value.filter(inv =>
    inv.toUser?.name?.toLowerCase().includes(q) ||
    inv.toUser?.surname?.toLowerCase().includes(q)
  )
})

const busyId = ref(null)
const localError = ref('')

const cancel = async (inv) => {
  localError.value = ''
  busyId.value = inv.friendshipId
  try {
    await friendsStore.cancelRequest(inv.friendshipId)
  } catch (e) {
    localError.value = 'Cancel failed.'
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
      Loading requests…
    </div>

    <!-- EMPTY -->
    <div
      v-else-if="items.length === 0"
      class="p-4 text-sm text-[var(--color-text-secondary)]"
    >
      No outgoing requests.
    </div>

    <!-- NO MATCH -->
    <div
      v-else-if="filteredItems.length === 0"
      class="p-4 text-sm text-[var(--color-text-secondary)]"
    >
      No requests match your search.
    </div>

    <!-- LIST -->
    <ul v-else class="divide-y divide-[var(--color-border)]">
      <li
        v-for="inv in filteredItems"
        :key="inv.id"
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
          {{ inv.toUser?.name?.charAt(0) ?? '?' }}
        </div>

        <!-- user -->
        <div class="min-w-0 flex-1">
          <p class="text-sm font-medium truncate">
            {{ inv.toUser?.name }} {{ inv.toUser?.surname }}
          </p>
        </div>

        <!-- action -->
        <button
          class="text-xs px-2 py-1 rounded-md
                 bg-[var(--color-danger-bg-soft)]
                 text-[var(--color-danger-text)]
                 hover:opacity-90
                 disabled:opacity-50"
          :disabled="busyId === inv.friendshipId"
          @click="cancel(inv)"
        >
          {{ busyId === inv.friendshipId ? 'Cancelling…' : 'Cancel' }}
        </button>
      </li>
    </ul>
  </div>
</template>
