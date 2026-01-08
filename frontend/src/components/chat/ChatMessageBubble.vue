<script setup>
import { computed } from 'vue'

const props = defineProps({
  message: Object,
  isMine: Boolean,
  groupMembers: Array,
  isLastSentByMe: Boolean
})

defineEmits(['reply'])

const readBy = computed(() => {
  if (!props.groupMembers) return []

  const msgTime = new Date(props.message.timestamp).getTime()

  return props.groupMembers.filter((member) => {
    if (member.username === props.message.sender.username) return false
    if (!member.lastReadTime) return false
    const readTime = new Date(member.lastReadTime).getTime()
    return readTime >= msgTime
  })
})

</script>

<template>
  <div
    :class="['flex w-full group mb-1 z-10 relative', isMine ? 'justify-end' : 'justify-start']"
  >
    <div
      v-if="message.type !== 'CHAT'"
      class="w-full text-center opacity-60 text-xs my-2 text-[var(--color-text-secondary)]"
    >
      {{ message.content }}
    </div>

    <div
      v-else
      class="flex gap-2"
      :class="[
          isMine ? 'flex-row-reverse' : 'flex-row',
          'items-end'
      ]"
    >
      <div class="flex flex-col max-w-[80%] md:max-w-[75%]">

        <span
          v-if="!isMine"
          class="text-xs mb-1 font-medium text-[var(--color-text-secondary)] ml-1"
        >
          {{ message.sender.name }}
        </span>

        <div
          :class="[
            'p-3 rounded-2xl shadow-sm break-words border relative transition-all',
            isMine
              ? 'bg-[var(--color-primary)] text-white border-transparent rounded-br-none'
              : 'bg-[var(--surface-panel)] text-[var(--color-text-primary)] border-[var(--color-border)] rounded-bl-none',
          ]"
        >
          <div
            v-if="message.parent"
            class="mb-2 p-2 rounded-lg text-xs border-l-4 opacity-90 flex flex-col select-none"
            :class="
              isMine
                ? 'bg-white/20 border-white/50'
                : 'bg-[var(--surface-panel-strong)] border-[var(--color-primary)]'
            "
          >
            <span class="font-bold opacity-80 mb-0.5" v-if="message.parent.sender">
              {{ message.parent.sender.name }}
            </span>
            <span class="line-clamp-2 italic">{{ message.parent.content }}</span>
          </div>

          <template v-if="message.contentType === 'LINK'">
            <a
              :href="message.content"
              target="_blank"
              rel="noopener noreferrer"
              class="underline break-words hover:opacity-80"
              :class="isMine ? 'text-inherit' : 'text-blue-600 dark:text-blue-400'"
              @click.stop
            >
              {{ message.content }}
            </a>
          </template>
          <template v-else>
            {{ message.content }}
          </template>
        </div>

        <span
          :class="[
            'text-[10px] mt-1 opacity-70 text-[var(--color-text-secondary)]',
            isMine ? 'text-right' : 'text-left',
          ]"
        >
          {{ new Date(message.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) }}
        </span>

        <div
          v-if="isMine && readBy.length > 0"
          class="flex justify-end mt-1 gap-1 transition-opacity duration-300"
          :class="isLastSentByMe ? 'opacity-100' : 'opacity-0 group-hover:opacity-100'"
        >
          <div
            v-for="member in readBy"
            :key="member.id"
            class="w-4 h-4 rounded-full bg-gray-200 text-[var(--color-text-secondary)] text-[8px] flex items-center justify-center border border-white font-bold overflow-hidden"
            :title="'Read by ' + member.name"
          >
            {{ member.name.charAt(0) }}
          </div>
        </div>
      </div>

      <button
        @click="$emit('reply', message)"
        class="mb-6 opacity-0 group-hover:opacity-100 transition-all duration-200 p-2 text-[var(--color-text-secondary)] hover:text-[var(--color-primary)] shrink-0 flex items-center justify-center rounded-full hover:bg-[var(--surface-panel-strong)]"
        title="Reply"
      >
        <span class="material-symbols-outlined text-[20px]">reply</span>
      </button>
    </div>
  </div>
</template>
