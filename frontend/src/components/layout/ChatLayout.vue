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
  activeGroupId: [Number, String] // Nowy prop
})

defineEmits(['sendMessage', 'typing', 'updateMessageContent', 'setStatus', 'logout', "updateProfile"])

const currentView = ref('chat')

const openSettings = () => {
  currentView.value = 'settings'
}
const closeSettings = () => {
  currentView.value = 'chat'
}
const updateProfile = () => {
  emit('updateProfile')
}

</script>

<template>
  <div class="flex h-screen bg-[var(--color-bg-body)] font-display overflow-hidden">
    <ChatSidebarLeft />

    <div class="flex-1 flex flex-col min-w-0 bg-[var(--surface-panel)] relative">

      <template v-if="activeGroupId">
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
      </template>

      <div v-else class="flex-1 flex flex-col items-center justify-center text-[var(--color-text-secondary)]">
        <div class="h-24 w-24 bg-[var(--surface-panel-strong)] rounded-full flex items-center justify-center mb-6">
          <span class="material-symbols-outlined text-5xl opacity-50">forum</span>
        </div>
        <h2 class="text-2xl font-bold text-[var(--color-text-primary)] mb-2">Welcome to ConnectApp</h2>
        <p class="max-w-md text-center">
          Select a channel from the sidebar or create a new group to start messaging.
        </p>
      </div>

    </div>

    <ChatSidebarRight
      :onlineUsers="onlineUsers"
      :currentUser="currentUser"
      :currentName="currentName"
      :currentSurname="currentSurname"
      :myStatus="myStatus"
      @setStatus="$emit('setStatus', $event)"
      @logout="$emit('logout')"
      @open-settings="openSettings"
    />

    <SettingsLayout v-if="currentView === 'settings'"
                    :currentUser="currentUser"
                    @close="closeSettings"
                    @logout="$emit('logout')"
    />
  </div>
</template>
