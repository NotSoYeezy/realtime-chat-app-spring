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
  currentName: String,
  currentSurname: String,
  isConnected: Boolean,
  messageContent: String,
  onlineUsers: Object,
  myStatus: String,
  formatTime: Function,
})

const currentView = ref('chat')

const openSettings = () => {
  currentView.value = 'settings'
}
const closeSettings = () => {
  currentView.value = 'chat'
}

defineEmits(['sendMessage', 'typing', 'updateMessageContent', 'setStatus', 'logout'])
</script>

<template>
  <div class="flex h-screen bg-[var(--color-bg-body)] font-display overflow-hidden">
    <ChatSidebarLeft />

    <!-- CHAT AREA -->
    <div class="flex-1 flex flex-col min-w-0 bg-[var(--surface-panel)] relative">

      <ChatHeader
        :currentName="currentName"
        :currentSurname="currentSurname"
      />

      <ChatMessages
        :messages="messages"
        :typingUsers="typingUsers"
        :loading="loading"
        :currentUser="currentUser"
        :currentName="currentName"
        :currentSurname="currentSurname"
        :formatTime="formatTime"
      />

      <ChatTypingIndicator
        :onlineUsers="onlineUsers"
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
      :currentName="currentName"
      :currentSurname="currentSurname"
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
