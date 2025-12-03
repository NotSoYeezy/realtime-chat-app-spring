<script setup>
import ChatSidebarLeft from '@/components/chat/ChatSidebarLeft.vue'
import ChatHeader from '@/components/chat/ChatHeader.vue'
import ChatMessages from '@/components/chat/ChatMessages.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import ChatSidebarRight from '@/components/chat/ChatSidebarRight.vue'
import ChatTypingIndicator from '@/components/chat/ChatTypingIndicator.vue'

defineProps({
  messages: Array,
  typingUsers: Object,
  loading: Boolean,
  currentUser: String,
  isConnected: Boolean,
  messageContent: String,
  onlineUsers: Object,
  myStatus: String,
  formatTime: Function
})

defineEmits(['sendMessage', 'typing', 'updateMessageContent', 'setStatus', 'logout'])
</script>

<template>
  <div class="flex h-screen bg-[var(--color-bg-body)] font-display overflow-hidden">

    <!-- LEFT SIDEBAR -->
    <ChatSidebarLeft />

    <!-- CHAT AREA -->
    <div class="flex-1 flex flex-col min-w-0 bg-[var(--surface-panel)] relative">

      <ChatHeader
        :currentUser="currentUser"
        @logout="$emit('logout')"
      />

      <ChatMessages
        :messages="messages"
        :typingUsers="typingUsers"
        :loading="loading"
        :currentUser="currentUser"
        :formatTime="formatTime"
      />

      <ChatTypingIndicator
        :users="typingUsers"
        :compact="false"
      />

      <ChatInput
        :messageContent="messageContent"
        :isConnected="isConnected"
        @typing="$emit('typing')"
        @sendMessage="$emit('sendMessage')"
        @update:messageContent="$emit('updateMessageContent', $event)"
      />
    </div>

    <!-- RIGHT SIDEBAR -->
    <ChatSidebarRight
      :onlineUsers="onlineUsers"
      :currentUser="currentUser"
      :myStatus="myStatus"
      @setStatus="$emit('setStatus', $event)"
    />
  </div>
</template>
