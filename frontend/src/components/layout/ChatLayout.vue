<script setup>
import { ref } from 'vue'
import ChatSidebarLeft from '@/components/chat/ChatSidebarLeft.vue'
import ChatHeader from '@/components/chat/ChatHeader.vue'
import ChatMessages from '@/components/chat/ChatMessages.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import ChatSidebarRight from '@/components/chat/ChatSidebarRight.vue'
import ChatTypingIndicator from '@/components/chat/ChatTypingIndicator.vue'
import SettingsLayout from '@/components/settings/SettingsLayout.vue'

defineProps({
  messages: Array,
  typingUsers: Object,
  loading: Boolean,
  currentUser: String,
  isConnected: Boolean,
  messageContent: String,
  onlineUsers: Object,
  myStatus: String,
  formatTime: Function,
  currentRoom: String,
})

const currentView = ref('chat')

const openSettings = () => {
  currentView.value = 'settings'
}
const closeSettings = () => {
  currentView.value = 'chat'
}

defineEmits(['sendMessage', 'typing', 'updateMessageContent', 'setStatus', 'logout',
                        'changeRoom'])
</script>

<template>
  <div class="flex h-screen bg-[var(--color-bg-body)] font-display overflow-hidden">
    <ChatSidebarLeft
      @changeRoom="$emit('changeRoom', $event)"
      :currentRoom="currentRoom"
    />

    <!-- CHAT AREA -->
    <div class="flex-1 flex flex-col min-w-0 bg-[var(--surface-panel)] relative">

      <ChatHeader
        :currentUser="currentUser"
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
      @logout="$emit('logout')"
      @open-settings = "openSettings"
    />

    <SettingsLayout v-if="currentView === 'settings'"
                    :currentUser = currentUser
                    @close="closeSettings"
    />
  </div>
</template>
