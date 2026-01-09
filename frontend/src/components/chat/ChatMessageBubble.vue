<script setup>
import { computed } from 'vue'

const props = defineProps({
  message: Object,
  isMine: Boolean,
  groupMembers: Array,
  isLastSentByMe: Boolean,
  themeColor: { type: String, default: null }
})

defineEmits(['reply', 'view-image'])

const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080'

const getFullUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${backendUrl}${path}`
}

const getFileName = (path) => {
  if (!path) return 'File'
  try {
    const filenameWithUuid = path.split('/').pop()

    const parts = filenameWithUuid.split('_')
    if (parts.length < 2) return filenameWithUuid

    return parts.slice(1).join('_')
  } catch (e) {
    return 'Attachment'
  }
}

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

const themeStyle = computed(() => {
  return props.themeColor ? { '--color-primary': props.themeColor } : {}
})
</script>

<template>
  <div
    :class="['flex w-full group mb-1 z-10 relative', isMine ? 'justify-end' : 'justify-start']"
    :style="themeStyle"
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
      :class="[ isMine ? 'flex-row-reverse' : 'flex-row', 'items-end' ]"
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

            <template v-if="message.parent.contentType === 'IMAGE'">
              <div class="flex items-center gap-2 mt-1">
                <img
                  :src="getFullUrl(message.parent.content)"
                  class="w-8 h-8 rounded object-cover bg-black/10 border border-white/20"
                  alt="Reply image"
                />
                <span class="italic opacity-70">Photo</span>
              </div>
            </template>

            <template v-else-if="message.parent.contentType === 'FILE'">
              <div class="flex items-center gap-2 mt-1">
                <span class="material-symbols-outlined text-[18px]">description</span>
                <span class="italic opacity-70 truncate max-w-[150px]">
                  {{ getFileName(message.parent.content) }}
                </span>
              </div>
            </template>

            <template v-else>
              <span class="line-clamp-2 italic">{{ message.parent.content }}</span>
            </template>
          </div>

          <template v-if="message.contentType === 'IMAGE'">
            <div class="relative group cursor-pointer">
              <img
                :src="getFullUrl(message.content)"
                alt="Image attachment"
                class="max-w-full rounded-lg object-cover bg-black/10"
                style="max-height: 300px; min-width: 100px;"
                @click.stop="$emit('view-image', message.content)"
              />
            </div>
          </template>

          <template v-else-if="message.contentType === 'FILE'">
            <div class="flex items-center gap-3 pr-2">
              <div
                class="p-2.5 rounded-xl shrink-0 flex items-center justify-center"
                :class="isMine ? 'bg-white/20 text-white' : 'bg-[var(--surface-panel-strong)] text-[var(--color-primary)]'"
              >
                <span class="material-symbols-outlined text-2xl">description</span>
              </div>

              <div class="flex flex-col min-w-0">
                <span class="text-sm font-bold truncate max-w-[200px]" :title="getFileName(message.content)">
                  {{ getFileName(message.content) }}
                </span>

                <a
                  :href="getFullUrl(message.content)"
                  download
                  target="_blank"
                  class="text-xs hover:underline flex items-center gap-1 mt-0.5 transition-opacity"
                  :class="isMine ? 'text-white/80 hover:text-white' : 'text-[var(--color-primary)] opacity-80 hover:opacity-100'"
                  @click.stop
                >
                  <span class="material-symbols-outlined text-[14px]">download</span>
                  Download
                </a>
              </div>
            </div>
          </template>

          <template v-else-if="message.contentType === 'LINK'">
            <a
              :href="message.content"
              target="_blank"
              rel="noopener noreferrer"
              class="underline break-words hover:opacity-80 block"
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
