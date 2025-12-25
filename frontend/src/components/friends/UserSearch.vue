<script setup>
import { ref, watch, computed, onBeforeUnmount } from 'vue'
import UserService from '@/api/getUser'
import { useFriendsStore } from '@/stores/friendsStore'

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
    results.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    // ważne: pokaż błąd, bo inaczej wygląda jakby "Add nie działało"
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
    await friendsStore.sendRequest(user.id) // <-- to wrzuci do outgoing + fetchAll()
    // UX: usuń z wyników po wysłaniu
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
</script>

<template>
  <div class="p-3 border-b border-[var(--color-border)]">
    <!-- SEARCH INPUT -->
    <div class="relative">
      <input
        v-model="query"
        @focus="onFocus"
        @keydown.enter.prevent="doSearch"
        placeholder="Search users..."
        class="w-full h-9 pl-3 pr-9 rounded-md bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)] text-sm outline-none border border-transparent focus:border-[var(--color-border)]"
      />

      <button
        v-if="query.length"
        type="button"
        @click="clear"
        class="absolute right-2 top-1/2 -translate-y-1/2 text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] text-xs"
        aria-label="Clear"
      >
        ✕
      </button>
    </div>

    <!-- HINTS -->
    <div class="mt-2 text-[10px] text-[var(--color-text-secondary)] flex items-center justify-between">
      <span v-if="!canSearch && query.length">Type at least 2 characters</span>
      <span v-if="loading">Searching&#8230;</span>
    </div>

    <!-- DROPDOWN RESULTS -->
    <div
      v-if="open"
      class="mt-1 rounded-lg border border-[var(--color-border)] bg-[var(--surface-panel)] overflow-hidden"
    >
      <div v-if="error" class="p-2 text-xs text-[var(--color-danger-text)]">
        {{ error }}
      </div>

      <div
        v-else-if="!loading && canSearch && results.length === 0"
        class="p-2 text-xs text-[var(--color-text-secondary)]"
      >
        No users found.
      </div>

      <div v-else class="max-h-64 overflow-y-auto">
        <div
          v-for="user in results"
          :key="user.id"
          class="flex items-center gap-3 p-2 hover:bg-[var(--surface-panel-strong)]"
        >
          <div class="h-8 w-8 rounded-full flex items-center justify-center font-bold bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)]">
            {{ user.name?.charAt(0) ?? '?' }}
          </div>

          <div class="min-w-0 flex-1">
            <p class="text-sm text-[var(--color-text-primary)] truncate">
              {{ user.name }} {{ user.surname }}
            </p>
            <p class="text-[10px] text-[var(--color-text-secondary)] truncate">
              @{{ user.username }}
            </p>
          </div>

          <button
            type="button"
            class="text-xs px-2 py-1 rounded-md bg-[var(--color-primary-bg-soft)] text-[var(--color-primary)] font-semibold hover:opacity-90 disabled:opacity-60"
            :disabled="sendingId === user.id"
            @click.prevent.stop="addFriend(user)"
          >
            <span v-if="sendingId === user.id">Sending...</span>
            <span v-else>Add</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
