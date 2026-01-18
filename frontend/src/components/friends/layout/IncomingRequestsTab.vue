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
  if (!friendsStore.incoming.content.length) {
    朋友們Store.fetchIncoming()
  }
})

const items = computed(() => (friendsStore.incoming.content ?? []).map(inv => inv.fromUser))

const busyId = ref(null)
const localError = ref('')

const accept = async (inv) => {
  localError.value = ''
  busyId.value = inv.friendshipId
  try {
    await friendsStore.acceptRequest(inv.friendshipId)
  } catch (e) {
    localError.value = 'Accept failed.'
    console.error(e)
  } finally {
    busyId.value = null
  }
}

const reject = async (inv) => {
  localError.value = ''
  busyId.value = inv.friendshipId
  try {
    await friendsStore.rejectRequest(inv.friendshipId)
  } catch (e) {
    localError.value = 'Reject failed.'
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
      :users="items"
      :loading="friendsStore.loading"
      :query="query"
      empty-text="You don’t have any incoming friend requests."
      :onlineUsers="onlineUsers"
      @rowClick="() => {}"
    >
      <template #actions="{ user }">
        <!-- find invitation for this user -->
        <template
          v-for="inv in friendsStore.incoming.content"
          :key="inv.friendshipId"
        >
          <template v-if="inv.fromUser?.id === user.id">
            <button
              class="text-xs px-2 py-1 rounded-md
                     bg-[var(--color-success-bg-soft)]
                     text-[var(--color-success-text)]
                     hover:opacity-90
                     disabled:opacity-50"
              :disabled="busyId === inv.friendshipId"
              @click.stop="accept(inv)"
            >
              {{ busyId === inv.friendshipId ? 'Accepting…' : 'Accept' }}
            </button>

            <button
              class="text-xs px-2 py-1 rounded-md
                     bg-[var(--color-danger-bg-soft)]
                     text-[var(--color-danger-text)]
                     hover:opacity-90
                     disabled:opacity-50"
              :disabled="busyId === inv.friendshipId"
              @click.stop="reject(inv)"
            >
              Reject
            </button>
          </template>
        </template>
      </template>
    </DefaultUsersList>

    <div v-if="friendsStore.incoming.hasMore" class="p-2 text-center">
      <button
        @click="friendsStore.loadMoreIncoming()"
        class="text-xs text-[var(--color-primary)] hover:underline"
      >
        Load More
      </button>
    </div>
  </div>
</template>
