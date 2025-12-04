<script setup>

defineProps({
  onlineUsers: Object,
  currentUser: String,
  myStatus: String,
})

defineEmits(['setStatus'])
</script>

<template>
  <aside
    class="w-64 hidden lg:flex flex-col bg-[var(--surface-panel)] border-l border-[var(--color-border)]"
  >
    <div class="p-4 border-b border-[var(--color-border)]">
      <!-- HEADER -->
      <div class="flex items-center justify-between mb-3">
        <h3 class="font-bold text-[var(--color-text-primary)]">Community</h3>

        <span class="text-xs px-2 py-0.5 rounded-full bg-green-100 text-green-700 font-medium">
          {{ Object.keys(onlineUsers).length }}
        </span>
      </div>

      <!-- STATUS SELECTOR -->
      <div class="grid grid-cols-3 rounded-md h-7 bg-[var(--surface-panel)] p-[2px] relative">
        <!-- SLIDER -->
        <div
          class="absolute top-[2px] bottom-[2px] left-[2px] rounded-md bg-[var(--surface-panel)] shadow transition-all duration-300 ease-in-out"
          :class="{
            'translate-x-0': myStatus === 'ONLINE',
            'translate-x-full': myStatus === 'BUSY',
            'translate-x-[200%]': myStatus === 'AWAY',
          }"
          style="width: calc((100% - 4px) / 3)"
        ></div>

        <!-- BUTTONS -->
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

      <!-- USERS LIST -->
      <div class="flex-1 p-2 space-y-1 overflow-y-auto">
        <div
          v-for="(status, name) in onlineUsers"
          :key="name"
          class="flex items-center gap-3 p-2 rounded-lg hover:bg-[var(--surface-panel-strong)]"
        >
          <!-- AVATAR -->
          <div class="relative">
            <div
              class="h-9 w-9 rounded-full flex items-center justify-center font-bold bg-[var(--surface-panel-strong)] text-[var(--color-text-primary)]"
            >
              {{ name.charAt(0) }}
            </div>

            <span
              class="absolute bottom-0 right-0 h-3 w-3 rounded-full border-2 border-[var(--surface-panel)]"
              :class="{
                'bg-green-500': status === 'ONLINE',
                'bg-red-500': status === 'BUSY',
                'bg-yellow-500': status === 'AWAY',
              }"
            ></span>
          </div>

          <!-- USER INFO -->
          <div class="min-w-0 flex-1">
            <p class="text-sm font-medium text-[var(--color-text-primary)] truncate">{{ name }}</p>
            <p class="text-[10px] text-[var(--color-text-secondary)]">{{ status }}</p>
          </div>

          <!-- CURRENT USER BADGE -->
          <span
            v-if="name === currentUser"
            class="text-[10px] px-1.5 py-0.5 rounded bg-[var(--color-primary-bg-soft)] text-[var(--color-primary)] font-bold"
          >
            YOU
          </span>
        </div>
      </div>
    </div>
  </aside>
</template>
