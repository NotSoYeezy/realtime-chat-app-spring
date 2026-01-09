<script setup>
import {createSSRApp, onMounted, onUnmounted, ref, watch} from 'vue'
import DropdownLayout from '@/components/layout/DropdownLayout.vue'
import GoogleHandler from "@/api/googleHandler.js";
import FriendListLayout from "@/components/layout/FriendListLayout.vue"
import {useFriendsStore} from "@/stores/friendsStore.js"

defineProps({
  onlineUsers: Object,
  currentUser: String,
  currentName: String,
  currentSurname: String,
  myStatus: String,
})

const friendsStore = useFriendsStore()

const emit = defineEmits(['setStatus', 'logout', 'openSettings', 'openFriends'])

const isDropdown = ref(false)
const dropdownRef = ref(null)
let pollingInterval = null

const toggleDropdown = () => {
  isDropdown.value = !isDropdown.value
}

const handleSettings = () => {
  isDropdown.value = false
  emit('openSettings')
}

const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isDropdown.value = false
  }
}

const handleStatusClick = (status) => {
  localStorage.setItem('my_status_preference', status);
  emit('setStatus', status);
}

const checkStatus = async () => {
  const isGoogleOn = localStorage.getItem('google_calendar') === 'true'
  const isSyncOn = localStorage.getItem('google_calendar_sync_status') === 'true'
  if (localStorage.getItem('user_status') === 'AWAY') {
    emit('setStatus', 'AWAY');
    return
  }
  if (isGoogleOn && isSyncOn) {
    try {
        const response = await GoogleHandler.isBusy()
        if (response.data === true) {
          emit('setStatus', 'BUSY')
        } else {
          emit('setStatus', 'ONLINE')
        }
    } catch (error) {
      if(error.response.status !== 406) {
        console.error(error)
      }
      else {
        localStorage.setItem('google_calendar_sync_status', 'false');
        console.error(error)
      }
    }
  }
}


onMounted(() => {
  checkStatus()
  document.addEventListener('click', handleClickOutside)
  friendsStore.fetchAll()
  pollingInterval = setInterval(() => {
    checkStatus();
  }, 60000);
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  if (pollingInterval) {
    clearInterval(pollingInterval)
  }
})
</script>

<template>
  <aside
    class="w-64 hidden lg:flex flex-col bg-[var(--surface-panel)] border-l border-[var(--color-border)]"
  >
    <div class="p-4 border-b border-[var(--color-border)]">

      <div class="flex items-center justify-between mb-3">
        <div class="min-w-0 flex-1">
          <p class="text-sm font-medium text-[var(--color-text-primary)] truncate">
            {{ currentName }} {{ currentSurname }}
          </p>
          <p class="text-[10px] text-[var(--color-text-secondary)]">
            @{{ currentUser }}
          </p>
        </div>

        <div class="relative" ref="dropdownRef">
          <button
            class="h-9 w-9 rounded-full flex items-center justify-center font-bold bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)]"
            @click="toggleDropdown()"
          >
            {{ currentName.charAt(0) }}
          </button>

          <DropdownLayout
            v-if="isDropdown"
            class="absolute top-full right-0 mt-2 z-20 w-48 bg-[var(--surface-panel)] rounded-lg shadow-2xl border border-[var(--color-border)] overflow-hidden"
            @logout="$emit('logout')"
            @close-dropdown="handleSettings"
          />

          <span
            class="absolute bottom-0 right-0 h-3 w-3 rounded-full border-2 border-[var(--surface-panel)]"
            :class="{
              'bg-green-500': myStatus === 'ONLINE',
              'bg-red-500': myStatus === 'BUSY',
              'bg-yellow-500': myStatus === 'AWAY',
            }"
          ></span>
        </div>
      </div>

      <div
        class="grid grid-cols-3 rounded-md h-7 bg-[var(--surface-panel-strong)] p-[2px] relative">
        <div
          class="absolute top-[2px] bottom-[2px] left-[2px] rounded-md bg-[var(--surface-panel)] shadow transition-all duration-300 ease-in-out"
          :class="{
            'translate-x-0': myStatus === 'ONLINE',
            'translate-x-full': myStatus === 'BUSY',
            'translate-x-[200%]': myStatus === 'AWAY',
          }"
          style="width: calc((100% - 4px) / 3)"
        ></div>

        <button
          v-for="s in ['ONLINE', 'BUSY', 'AWAY']"
          :key="s"
          @click="handleStatusClick(s)"
          class="z-10 flex items-center justify-center text-[10px] font-bold transition-colors duration-200"
          :class="[ s === myStatus ? 'text-[var(--color-primary)]' : 'text-[var(--color-text-secondary)]' ]"
        >
          {{ s }}
        </button>
      </div>
    </div>

    <FriendListLayout
      :onlineUsers="onlineUsers"
      @openChat="user => console.log('open chat', user)"
      @removeFriend="id => friendsStore.removeFriend(id)"
      @blockFriend="id => friendsStore.blockUser(id)"
      @acceptRequest="id => friendsStore.acceptRequest(id)"
      @rejectRequest="id => friendsStore.rejectRequest(id)"
      @click="$emit('openFriends')"
    />

  </aside>
</template>
