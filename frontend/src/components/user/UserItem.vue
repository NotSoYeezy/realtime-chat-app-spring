<script setup>
defineProps({
  user: {
    type: Object,
    required: true
  },
  size: {
    type: String,
    default: 'md' // md | sm
  },
  textColor: {
    type: String,
    default: 'var(--color-text-primary)'
  },
  onlineStatus: {
    type: String,
    default: 'OFFLINE'
  }
})
</script>

<template>
  <div class="flex items-center gap-3 min-w-0 w-full">
    <!-- LEFT: avatar + name -->
    <div class="flex items-center gap-3 min-w-0 flex-1">
      <!-- AVATAR -->
      <div class="relative">
        <div
          class="rounded-full flex items-center justify-center font-bold
                 bg-[var(--surface-panel-strong)]"
          :class="{
            'h-9 w-9 text-sm': size === 'md',
            'h-7 w-7 text-xs': size === 'sm'
          }"
          :style="{ color: textColor }"
        >
          {{ user.name?.charAt(0) ?? '?' }}
        </div>
        <div
          v-if="onlineStatus !== 'OFFLINE'"
          class="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full border border-[var(--surface-panel)]"
          :class="{
            'bg-green-500': onlineStatus === 'ONLINE',
            'bg-yellow-500': onlineStatus === 'AWAY',
            'bg-red-500': onlineStatus === 'BUSY'
          }"
        ></div>
      </div>

      <!-- NAME -->
      <div class="min-w-0">
        <p
          class="text-sm font-medium truncate"
          :style="{ color: textColor }"
        >
          {{ user.name }} {{ user.surname }}
        </p>

        <!-- OPTIONAL SUBTITLE -->
        <slot name="subtitle" />
      </div>
    </div>

    <!-- RIGHT: ACTIONS -->
    <div
      v-if="$slots.actions"
      class="flex items-center gap-2 shrink-0"
    >
      <slot name="actions" />
    </div>
  </div>
</template>
