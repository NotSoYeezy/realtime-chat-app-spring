<script setup>
import { computed, ref, onMounted } from 'vue'
import { useFriendsStore } from '@/stores/friendsStore'
import DefaultUsersList from '@/components/user/DefaultUsersList.vue'

defineProps({
  query: {
    type: String,
    default: ''
  },
  onlineUsers: Object
})

const friendsStore = useFriendsStore()

onMounted(() => {
  if (!friendsStore.outgoing.content.length) {
    friendsStore.fetchOutgoing()
  }
})

const items = computed(() => friendsStore.outgoing.content ?? [])

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

    <DefaultUsersList
      :users="items.map(inv => inv.toUser)"
      :loading="friendsStore.loading"
      :query="query"
      empty-text="No outgoing requests."
      :onlineUsers="onlineUsers"
      @rowClick="() => {}"
    >
      <template #actions="{ user }">
        <!-- user -> invitation -->
        <template
          v-for="inv in friendsStore.outgoing.content"
          :key="inv.friendshipId"
        >
          <template v-if="inv.toUser?.id === user.id">
            <button
              class="text-xs px-2 py-1 rounded-md
                     bg-[var(--color-danger-bg-soft)]
                     text-[var(--color-danger-text)]
                     hover:opacity-90
                     disabled:opacity-50"
              :disabled="busyId === inv.friendshipId"
              @click.stop="cancel(inv)"
            >
              {{ busyId === inv.friendshipId ? 'Cancelling…' : 'Cancel' }}
            </button>
          </template>
        </template>
      </template>
    </DefaultUsersList>

    <div v-if="friendsStore.outgoing.hasMore" class="p-2 text-center">
      <button
        @click="friendsStore.loadMoreOutgoing()"
        class="text-xs text-[var(--color-primary)] hover:underline"
      >
        Load More
      </button>
    </div>
  </div>
</template>
