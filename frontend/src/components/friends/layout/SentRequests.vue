<script setup>
import { onMounted } from 'vue'
import { useFriendsStore } from '@/stores/friendsStore'

import UserSearch from "@/components/friends/UserSearch.vue";
import OutgoingList from '@/components/friends/layout/OutgoingList.vue'

const friendsStore = useFriendsStore()

onMounted(() => {
  if (!friendsStore.outgoing.length) {
    friendsStore.fetchAll()
  }
})
</script>

<template>
  <div class="flex flex-col h-full">

    <!-- ADD FRIEND (SEARCH) -->
    <!-- TODO: make search filter blocked users, show friends as 'added' and show incoming requests as 'accept/reject' -->
    <UserSearch placeholder="Search users to send friend requests" />

    <!-- OUTGOING REQUESTS LIST -->
    <OutgoingList class="flex-1 overflow-y-auto" />

  </div>
</template>
