<script setup>
import { ref, watch, nextTick, onMounted, computed } from 'vue'
import { useChatStore } from '@/stores/chat'
import ChatMessageBubble from '@/components/chat/ChatMessageBubble.vue'

const props = defineProps({
  messages: Array,
  currentUser: String,
  currentName: String,
  currentSurname: String,
  typingUsers: Set,
  loading: Boolean,
  formatTime: Function
})

const emit = defineEmits(['reply', 'view-image'])

const chatStore = useChatStore()
const scrollerRef = ref(null)
const isLoadingMore = ref(false)

const scrollToBottom = async () => {
  await nextTick()
  if (scrollerRef.value) {
    scrollerRef.value.scrollToBottom()
  }
}

const lastMyMessageIndex = computed(() => {
  if (!props.messages) return -1
  for (let i = props.messages.length - 1; i >= 0; i--) {
    const msg = props.messages[i]
    if (msg.sender?.username === props.currentUser && msg.type === 'CHAT') {
      return i
    }
  }
  return -1
})

const lastMessageId = computed(() => {

  if (!props.messages || props.messages.length === 0) return null

  return props.messages[props.messages.length - 1].id

})

watch(() => chatStore.activeGroupId, () => {
  scrollToBottom()
})

watch(lastMessageId, (newId, oldId) => {

  if (newId && newId !== oldId) {
    scrollToBottom()
  }
})

const onScroll = async (event) => {
  const target = event.target

  if (target.scrollTop < 100 && !isLoadingMore.value && chatStore.activeGroup?.hasMore) {

    isLoadingMore.value = true

    const oldScrollHeight = target.scrollHeight

    const oldScrollTop = target.scrollTop

    await chatStore.loadMoreMessages()

    await nextTick()

    const newScrollHeight = target.scrollHeight

    target.scrollTop = newScrollHeight - oldScrollHeight + oldScrollTop

    isLoadingMore.value = false

  }

}

watch(() => props.loading, (isLoading) => {

  if (!isLoading) {
    scrollToBottom()
  }

})

onMounted(() => {
  scrollToBottom()
})

</script>

<template>

  <div class="flex-1 overflow-hidden bg-[var(--surface-panel)] relative">
    <div v-if="loading" class="absolute inset-0 flex flex-col items-center justify-center text-[var(--color-text-secondary)] z-10 bg-[var(--surface-panel)]">
      <span class="material-symbols-outlined animate-spin text-3xl mb-2">sync</span>
      <p>Loading messages&#8230;</p>
    </div>
    <div v-else-if="!messages || messages.length === 0" class="absolute inset-0 flex flex-col items-center justify-center text-[var(--color-text-secondary)] opacity-50 z-10">
      <span class="material-symbols-outlined text-4xl mb-2">chat_bubble_outline</span>
      <p>No messages here yet.</p>
    </div>
    <DynamicScroller
      v-if="messages && messages.length > 0"
      ref="scrollerRef"
      :items="messages"
      :min-item-size="60"
      key-field="id"
      class="h-full px-4 pt-4"
      id="messages-scroll"
      @scroll="onScroll"
    >
      <template v-slot="{ item, index, active }">
        <DynamicScrollerItem
          :item="item"
          :active="active"
          :size-dependencies="[
            item.content,
            item.type,
            item.contentType
          ]"
          :data-index="index"
          class="pb-4"
        >
          <ChatMessageBubble
            :message="item"
            :isMine="item.sender?.username === currentUser"
            :groupMembers="chatStore.activeGroup?.members"
            :isLastSentByMe="index === lastMyMessageIndex"
            :themeColor="chatStore.activeGroup?.colorTheme"
            @reply="$emit('reply', $event)"
            @view-image="$emit('view-image', $event)"
          />
        </DynamicScrollerItem>
      </template>
    </DynamicScroller>

  </div>
</template>
