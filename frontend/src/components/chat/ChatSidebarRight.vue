<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import DropdownLayout from '@/components/layout/DropdownLayout.vue'

defineProps({
  onlineUsers: Object,
  currentUser: String,
  currentName: String,
  currentSurname: String,
  myStatus: String,
})

const emit = defineEmits(['setStatus', 'logout', 'openSettings'])

const isDropdown = ref(false)
const dropdownRef = ref(null)

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

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <aside
    class="w-64 hidden lg:flex flex-col bg-[var(--surface-panel)] border-l border-[var(--color-border)]"
  >
    <div class="p-4 border-b border-[var(--color-border)]">

      <!-- HEADER -->
      <div class="flex items-center justify-between mb-3">
        <h3 class="font-bold text-[var(--color-text-primary)]">Friends</h3>

        <!-- ONLINE COUNTER -->
        <span class="text-xs w-fit px-2 py-0.5 rounded-full mr-auto ml-2 bg-[var(--status-online-bg)] text-[var(--status-online)] font-medium">
          {{ Object.keys(onlineUsers).length }}
        </span>

        <!-- CURRENT USER DROPDOWN -->
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
        </div>
      </div>

      <!-- STATUS SELECTOR -->
      <div class="grid grid-cols-3 rounded-md h-7 bg-[var(--surface-panel-strong)] p-[2px] relative">
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
          @click="$emit('setStatus', s)"
          class="z-10 flex items-center justify-center text-[10px] font-bold transition-colors duration-200 leading-none"
          :class="[
            s === myStatus ? 'text-[var(--color-primary)]' : 'text-[var(--color-text-secondary)]',
          ]"
        >
          {{ s }}
        </button>
      </div>
    </div>


    <!-- USERS LIST -->
    <div class="flex-1 p-2 space-y-1 overflow-y-auto">
      <div
        v-for="(info, username) in onlineUsers"
        :key="username"
        class="flex items-center gap-3 p-2 rounded-lg hover:bg-[var(--surface-panel-strong)]"
      >
        <!-- AVATAR -->
        <div class="relative">
          <div
            class="h-9 w-9 rounded-full flex items-center justify-center font-bold bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)]"
          >
            {{ info.name.charAt(0) }}
          </div>

          <span
            class="absolute bottom-0 right-0 h-3 w-3 rounded-full border-2 border-[var(--surface-panel)]"
            :class="{
              'bg-green-500': info.status === 'ONLINE',
              'bg-red-500': info.status === 'BUSY',
              'bg-yellow-500': info.status === 'AWAY',
            }"
          ></span>
        </div>

        <!-- USER INFO -->
        <div class="min-w-0 flex-1">
          <p class="text-sm font-medium text-[var(--color-text-primary)] truncate">
            {{ info.name }} {{ info.surname }}
          </p>
          <p class="text-[10px] text-[var(--color-text-secondary)]">
            {{ info.status }}
          </p>
        </div>

        <!-- CURRENT USER BADGE -->
        <span
          v-if="username === currentUser"
          class="text-[10px] px-1.5 py-0.5 rounded bg-[var(--color-primary-bg-soft)] text-[var(--color-primary)] font-bold"
        >
          YOU
        </span>
      </div>
    </div>

  </aside>
</template>
