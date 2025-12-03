<script setup>
import BaseButton from '@/components/ui/BaseButton.vue'

defineProps({
  messageContent: String,
  isConnected: Boolean,
})

const emit = defineEmits(['update:messageContent', 'sendMessage', 'typing'])
</script>

<template>
  <div class="p-4 border-t border-[var(--color-border)] bg-[var(--surface-panel)]">
    <div
      class="max-w-4xl mx-auto flex items-center gap-2 p-2 rounded-xl bg-[var(--surface-panel) border border-[var(--color-border)]]"
    >
      <input
        :value="messageContent"
        @input="
          emit('update:messageContent', $event.target.value); emit('typing')
        "
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
</template>
