<script setup>
import { onMounted, ref } from 'vue'
import { useFriendsStore } from '@/stores/friendsStore'
import DefaultUsersList from '@/components/user/DefaultUsersList.vue'

defineProps({
  query: String,
  onlineUsers: Object
})

const friendsStore = useFriendsStore()

const localError = ref('')

/**
 * Busy state per action
 * remove: userId | null
 * block:  userId | null
 */
const busy = ref({
  remove: null,
  block: null
})

onMounted(() => {
  if (!friendsStore.friends.length) {
    friendsStore.fetchAll()
  }
})

const openChat = (user) => {
  console.log('open chat', user)
}

/* ================= ACTIONS ================= */

const removeFriend = async (user) => {
  localError.value = ''
  busy.value.remove = user.id
  try {
    await friendsStore.removeFriend(user.id)
  } catch (e) {
    localError.value =
      e?.response?.data?.message ?? 'Failed to remove friend.'
    console.error(e)
  } finally {
    busy.value.remove = null
  }
}

const blockUser = async (user) => {
  localError.value = ''
  busy.value.block = user.id
  try {
    await friendsStore.blockUser(user.id)
  } catch (e) {
    localError.value =
      e?.response?.data?.message ?? 'Failed to block user.'
    console.error(e)
  } finally {
    busy.value.block = null
  }
}
</script>

<template>
  <!-- ERROR MESSAGE -->
  <div
    v-if="localError"
    class="p-2 text-xs text-[var(--color-danger-text)]"
  >
    {{ localError }}
  </div>

  <DefaultUsersList
    :users="friendsStore.friends"
    :loading="friendsStore.loading"
    :query="query"
    :onlineUsers="onlineUsers"
    empty-text="You don’t have any friends yet."
    @rowClick="openChat"
  >
    <template #actions="{ user }">
      <!-- REMOVE -->
      <button
        class="text-xs px-2 py-1 rounded-md
               bg-[var(--color-danger-bg-soft)]
               text-[var(--color-danger-text)]
               hover:opacity-90
               disabled:opacity-50"
        :disabled="busy.remove === user.id || busy.block === user.id"
        @click.stop="removeFriend(user)"
      >
        {{ busy.remove === user.id ? 'Removing…' : 'Remove' }}
      </button>

      <!-- BLOCK -->
      <button
        class="text-xs px-2 py-1 rounded-md
               text-[var(--color-text-secondary)]
               hover:text-[var(--color-text-primary)]
               disabled:opacity-50"
        :disabled="busy.block === user.id || busy.remove === user.id"
        @click.stop="blockUser(user)"
      >
        {{ busy.block === user.id ? 'Blocking…' : 'Block' }}
      </button>
    </template>
  </DefaultUsersList>
</template>
