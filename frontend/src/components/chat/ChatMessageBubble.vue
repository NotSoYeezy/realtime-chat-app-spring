<script setup>
defineProps({
  message: Object,
  isMine: Boolean,
})
</script>

<template>
  <div :class="['flex w-full', isMine ? 'justify-end' : 'justify-start']">
    <!-- JOIN/LEAVE -->
    <div v-if="message.type !== 'MESSAGE'" class="w-full text-center opacity-60">
      <span
        class="text-xs px-2 py-1 rounded bg-[var(--surface-panel-strong)] text-[var(--color-text-secondary)]"
      >
        {{ message.sender }} {{ message.type === 'JOIN' ? 'dołączył' : 'wyszedł' }}
      </span>
    </div>

    <div v-else class="flex flex-col max-w-[75%] md:max-w-[60%]">
      <span v-if="!isMine" class="text-xs mb-1 font-medium text-[var(--color-text-secondary)] ml-1">
        {{ message.sender }}
      </span>

      <div
        :class="[
          'p-3 rounded-2xl shadow-sm break-words border',
          isMine
            ? 'bg-[var(--color-primary)] text-white border-transparent rounded-br-none'
            : 'bg-[var(--surface-panel)] text-[var(--color-text-primary)] border-[var(--color-border)] rounded-bl-none',
        ]"
      >
        {{ message.content }}
      </div>

      <span :class="['text-[10px] mt-1 opacity-70', isMine ? 'text-right' : 'text-left']">
        {{ new Date(message.timestamp).toLocaleTimeString() }}
      </span>
    </div>
  </div>
</template>
