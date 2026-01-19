<script setup>
import { ref, watch, computed, onBeforeUnmount } from 'vue'
import { useFriendsStore } from '@/stores/friendsStore.js'
import DefaultUsersList from '@/components/user/DefaultUsersList.vue'
import User from "@/api/User.js";

defineProps({
  onlineUsers: Object,
  placeholder: String
})

const friendsStore = useFriendsStore()

const query = ref('')
const results = ref([])
const loading = ref(false)
const sendingId = ref(null)
const error = ref('')
const open = ref(false)

let timer = null
onBeforeUnmount(() => clearTimeout(timer))

const trimmed = computed(() => query.value.trim())
const canSearch = computed(() => trimmed.value.length >= 2)

const doSearch = async () => {
  error.value = ''

  if (!canSearch.value) {
    results.value = []
    open.value = false
    return
  }

  loading.value = true
  open.value = true

  try {
    const res = await UserService.searchUsers(trimmed.value)
    if (res.data && Array.isArray(res.data.content)) {
      results.value = res.data.content
    } else {
      results.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (e) {
    error.value = e?.response?.data?.message ?? 'Search failed'
    results.value = []
  } finally {
    loading.value = false
  }
}

watch(trimmed, () => {
  clearTimeout(timer)
  timer = setTimeout(doSearch, 300)
})

const addFriend = async (user) => {
  error.value = ''
  sendingId.value = user.id

  try {
    await friendsStore.sendRequest(user.id)
    results.value = results.value.filter(u => u.id !== user.id)
  } catch (e) {
    error.value = e?.response?.data?.message ?? 'Send request failed'
  } finally {
    sendingId.value = null
  }
}

const clear = () => {
  query.value = ''
  results.value = []
  open.value = false
  error.value = ''
}

const onFocus = () => {
  if (results.value.length) open.value = true
}

const onBlur = () => {
  // mały delay, żeby klik w listę zadziałał
  setTimeout(() => (open.value = false), 150)
}
</script>

<template>
  <div class="p-3 border-b border-[var(--color-border)] relative">
    <div class="relative">
      <input
        v-model="query"
        @focus="onFocus"
        @blur="onBlur"
        @keydown.enter.prevent="doSearch"
        placeholder="Search users..."
        class="w-full h-9 pl-3 pr-9 rounded-md
               bg-[var(--surface-panel-strong)]
               text-[var(--color-text-primary)]
               text-sm outline-none
               border border-transparent
               focus:border-[var(--color-border)]"
      />

      <button
        v-if="query.length"
        type="button"
        @click="clear"
        class="absolute right-2 top-1/2 -translate-y-1/2
               text-[var(--color-text-secondary)]
               hover:text-[var(--color-text-primary)]
               text-xs"
        aria-label="Clear"
      >
        ✕
      </button>
    </div>

    <div class="mt-2 text-[10px] text-[var(--color-text-secondary)] flex justify-between">
      <span v-if="!canSearch && query.length">Type at least 2 characters</span>
      <span v-if="loading">Searching…</span>
    </div>

    <!-- DROPDOWN -->
    <div
      v-if="open"
      class="absolute left-3 right-3 mt-1
             rounded-lg border border-[var(--color-border)]
             bg-[var(--surface-panel)]
             shadow-xl z-20"
    >
      <div
        v-if="error"
        class="p-2 text-xs text-[var(--color-danger-text)]"
      >
        {{ error }}
      </div>

      <DefaultUsersList
        v-else
        :users="results"
        :loading="loading"
        empty-text="No users found."
        :query="''"
        user-text-color="var(--color-text-primary)"
        :onlineUsers="onlineUsers"
        @rowClick="() => {}"
      >
        <template #actions="{ user }">
          <button
            class="text-xs px-2 py-1 rounded-md
                   bg-[var(--color-primary-bg-soft)]
                   text-[var(--color-primary)]
                   font-semibold
                   hover:opacity-90
                   disabled:opacity-60"
            :disabled="sendingId === user.id"
            @click.stop="addFriend(user)"
          >
            {{ sendingId === user.id ? 'Sending…' : 'Add' }}
          </button>
        </template>
      </DefaultUsersList>
    </div>
  </div>
</template>
