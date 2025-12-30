<script setup>
import { computed, ref, onMounted } from 'vue'
import { useFriendsStore } from '@/stores/friendsStore'
import DefaultUsersList from '@/components/user/DefaultUsersList.vue'

const props = defineProps({
  query: {
    type: String,
    default: ''
  }
})

const friendsStore = useFriendsStore()

onMounted(() => {
  if (!friendsStore.incoming.length) {
    friendsStore.fetchAll()
  }
})

const items = computed(() => friendsStore.incoming ?? [])

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
      :users="items.map(inv => inv.fromUser)"
      :loading="friendsStore.loading"
      :query="query"
      empty-text="You don’t have any incoming friend requests."
      @rowClick="() => {}"
    >
      <template #actions="{ user }">
        <!-- znajdź zaproszenie dla tego usera -->
        <template
          v-for="inv in items"
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
  </div>
</template>
