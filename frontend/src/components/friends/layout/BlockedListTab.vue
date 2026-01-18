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
  if (!friendsStore.blocked.content.length) {
    friendsStore.fetchBlocked()
  }
})

const items = computed(() => friendsStore.blocked.content ?? [])

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

    <DefaultUsersList
      :users="items"
      :loading="friendsStore.loading"
      :query="query"
      empty-text="You don’t have any blocked users."
      user-text-color="var(--color-text-primary)"
      @rowClick="() => {}"
    >
      <template #actions="{ user }">
        <button
          class="text-xs px-2 py-1 rounded-md
                 bg-[var(--color-warning-bg-soft)]
                 text-[var(--color-warning-text)]
                 font-semibold
                 hover:opacity-90
                 disabled:opacity-50"
          :disabled="busyId === user.id"
          @click.stop="unblock(user)"
        >
          {{ busyId === user.id ? 'Unblocking…' : 'Unblock' }}
        </button>
      </template>
    </DefaultUsersList>

    <div v-if="friendsStore.blocked.hasMore" class="p-2 text-center">
      <button
        @click="friendsStore.loadMoreBlocked()"
        class="text-xs text-[var(--color-primary)] hover:underline"
      >
        Load More
      </button>
    </div>
  </div>
</template>
