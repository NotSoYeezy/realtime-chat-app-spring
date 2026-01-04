<script setup>
import BaseButton from '@/components/ui/BaseButton.vue'

defineProps({
  messageContent: String,
  isConnected: Boolean,
  replyingTo: Object
})

const emit = defineEmits(['update:messageContent', 'sendMessage', 'typing', 'cancelReply'])
</script>

<template>
  <div class="border-t border-[var(--color-border)] bg-[var(--surface-panel)] flex flex-col">

    <div v-if="replyingTo" class="px-4 pt-3 flex items-center justify-between animate-in slide-in-from-bottom-2 duration-200">
      <div class="flex items-center gap-2 overflow-hidden">
        <span class="material-symbols-outlined text-[var(--color-primary)]">reply</span>
        <div class="flex flex-col text-sm border-l-2 border-[var(--color-primary)] pl-2 ml-1">
          <span class="font-bold text-[var(--color-primary)]">Replying to {{ replyingTo.sender?.name }}</span>
          <span class="text-[var(--color-text-secondary)] truncate max-w-xs md:max-w-md text-xs">{{ replyingTo.content }}</span>
        </div>
      </div>
      <button @click="emit('cancelReply')" class="text-[var(--color-text-secondary)] hover:text-[var(--color-danger)] p-1">
        <span class="material-symbols-outlined text-[20px]">close</span>
      </button>
    </div>

    <div class="p-4">
      <div class="max-w-4xl mx-auto flex items-center gap-2 p-2 rounded-xl bg-[var(--surface-panel)">
        <input
          :value="messageContent"
          @input="emit('update:messageContent', $event.target.value); emit('typing')"
          @keyup.enter="emit('sendMessage')"
          placeholder="Type a message&#8230;"
          class="flex-1 min-w-0 w-full h-12 p-3 text-base font-normal leading-normal bg-[var(--color-bg-input)] text-[var(--color-text-primary)] placeholder:text-[var(--color-text-secondary)] border border-[var(--color-border)] rounded-lg focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] focus:ring-opacity-50"
        />

        <BaseButton
          @click="emit('sendMessage')"
          :disabled="!messageContent?.trim() || !isConnected"
          class="p-2 rounded-lg bg-[var(--color-primary)] text-white disabled:opacity-50"
        >
          <span class="material-symbols-outlined text-[20px]">send</span>
        </BaseButton>
      </div>
    </div>
  </div>
</template>
