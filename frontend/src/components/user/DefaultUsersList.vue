<script setup>
import { computed } from 'vue'
import UserItem from '@/components/user/UserItem.vue'

const props = defineProps({
  users: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  query: {
    type: String,
    default: ''
  },
  emptyText: {
    type: String,
    default: 'No users found.'
  },
  userTextColor: {
    type: String,
    default: 'var(--color-text-primary)'
  }
})

defineEmits(['rowClick'])

const filteredUsers = computed(() => {
  const q = props.query.trim().toLowerCase()
  if (!q) return props.users

  return props.users.filter(u =>
    u.name?.toLowerCase().includes(q) ||
    u.surname?.toLowerCase().includes(q)
  )
})
</script>

<template>
  <div class="flex-1 overflow-y-auto">

    <div
      v-if="loading"
      class="p-4 text-sm text-[var(--color-text-secondary)]"
    >
      Loading…
    </div>

    <div
      v-else-if="!users.length"
      class="p-4 text-sm text-[var(--color-text-secondary)]"
    >
      {{ emptyText }}
    </div>

    <div
      v-else-if="filteredUsers.length === 0"
      class="p-4 text-sm text-[var(--color-text-secondary)]"
    >
      No users match your search.
    </div>

    <ul v-else class="divide-y divide-[var(--color-border)]">
      <li
        v-for="user in filteredUsers"
        :key="user.id"
        class="p-3 hover:bg-[var(--surface-panel-strong)]
               transition cursor-pointer"
        @click="$emit('rowClick', user)"
      >
        <UserItem
          :user="user"
          :text-color="userTextColor"
        >
          <template #actions>
            <slot
              name="actions"
              :user="user"
            />
          </template>
        </UserItem>
      </li>
    </ul>
  </div>
</template>
