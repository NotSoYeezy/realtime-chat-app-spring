<script setup>
import { onMounted } from 'vue'
import { useFriendsStore } from '@/stores/friendsStore'

import UserSearch from "@/components/user/UserSearch.vue";
import OutgoingList from '@/components/friends/layout/RequestsList.vue'

const friendsStore = useFriendsStore()

onMounted(() => {
  if (!friendsStore.outgoing.length) {
    friendsStore.fetchAll()
  }
})
</script>

<template>
  <div class="flex flex-col h-full">

    <!-- TODO: make search filter blocked users, show friends as 'added' and show incoming requests as 'accept/reject' -->
    <UserSearch placeholder="Search users to send friend requests" />

    <OutgoingList class="flex-1 overflow-y-auto" />

  </div>
</template>
