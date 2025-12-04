<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  messages: Array,
  currentUser: String,
  typingUsers: Set,
  loading: Boolean,
  formatTime: Function
})

const containerRef = ref(null)

watch(() => props.messages, async () => {
  await nextTick()
  if (containerRef.value) {
    containerRef.value.scrollTop = containerRef.value.scrollHeight
  }
}, { deep: true })
</script>

<template>
  <div id="messages-scroll" ref="containerRef" class="flex-1 overflow-y-auto p-4 space-y-4 bg-[var(--surface-panel)]">

    <div v-if="loading" class="text-center text-[var(--color-text-secondary)]">
      <span class="material-symbols-outlined animate-spin text-3xl mb-2">sync</span>
      Loading messages&#8230;
    </div>

    <template v-else>
      <div
        v-for="(msg, i) in messages"
        :key="i"
        class="flex w-full"
        :class="msg.sender === currentUser ? 'justify-end' : 'justify-start'"
      >

        <div v-if="msg.type === 'JOIN' || msg.type === 'LEAVE'" class="w-full text-center opacity-60">
          <span class="text-xs px-2 py-1 rounded bg-[var(--surface-panel-strong)] text-[var(--color-text-secondary)]">
            {{ msg.sender }} {{ msg.type === 'JOIN' ? 'joined' : 'left' }}
          </span>
        </div>

        <div v-else class="flex flex-col max-w-[75%] md:max-w-[60%]">

          <span
            v-if="msg.sender !== currentUser"
            class="text-xs mb-1 text-[var(--color-text-secondary)] ml-1"
          >
            {{ msg.sender }}
          </span>

          <div
            :class="[
              'p-3 rounded-2xl shadow-sm border break-words',
              msg.sender === currentUser
                ? 'bg-[var(--color-primary)] text-white border-transparent rounded-br-none'
                : 'bg-[var(--surface-panel)] border-[var(--color-border)] rounded-bl-none text-[var(--color-text-primary)]'
            ]">
            {{ msg.content }}
          </div>

          <span class="text-[var(--color-text-secondary)] text-[10px] mt-1 opacity-70" :class="msg.sender === currentUser ? 'text-right' : 'text-left'">
            {{ formatTime(msg.timestamp) }}
          </span>

        </div>

      </div>
    </template>

  </div>
</template>

<style scoped>

</style>
