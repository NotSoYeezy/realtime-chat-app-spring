<template>
  <div class="flex-1 overflow-y-auto p-3">
    <FriendList v-if="tab === 'Friends'" />
    <FriendRequests v-if="tab === 'Requests'" />
    <BlockedUsers v-if="tab === 'Blocked'" />
  </div>
</template>
