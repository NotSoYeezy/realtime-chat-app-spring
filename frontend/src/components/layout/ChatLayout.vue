<script setup>
import { ref, computed } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useFriendsStore } from '@/stores/friendsStore'

import ChatSidebarLeft from '@/components/chat/ChatSidebarLeft.vue'
import ImageViewerModal from "@/components/chat/ImageViewerModal.vue"
import ChatHeader from '@/components/chat/ChatHeader.vue'
import ChatMessages from '@/components/chat/ChatMessages.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import ChatSidebarRight from '@/components/chat/ChatSidebarRight.vue'
import ChatTypingIndicator from '@/components/chat/ChatTypingIndicator.vue'
import SettingsLayout from '@/components/settings/SettingsLayout.vue'
import FriendsView from "@/views/FriendsView.vue"
import GroupInfoModal from "@/components/chat/GroupInfoModal.vue"
import AddMemberModal from "@/components/chat/AddMemberModal.vue"

const props = defineProps({
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
  activeGroupId: [Number, String],
  replyingTo: Object
})

defineEmits(['sendMessage', 'typing', 'updateMessageContent',
  'setStatus', 'logout', "updateProfile", 'reply', 'cancelReply', 'uploadImage', 'uploadFile'])

const chatStore = useChatStore()
const friendsStore = useFriendsStore()
const currentView = ref('chat')

const showGroupSettings = ref(false)
const showAddMemberModal = ref(false)

const selectedImage = ref(null)
const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080'

const activeGroupMembers = computed(() => {
  return chatStore.activeGroup ? chatStore.activeGroup.members : []
})

const handleOpenGroupSettings = async () => {
  if (chatStore.activeGroupId) {
    await chatStore.refreshGroup(chatStore.activeGroupId)
    showGroupSettings.value = true
  }
}

const handleOpenAddMember = () => {
  showGroupSettings.value = false
  showAddMemberModal.value = true
}

const handleMembersAdded = async (userIds) => {
  await chatStore.addMembersToGroup(userIds)
  showAddMemberModal.value = false
  showGroupSettings.value = true
}

const handleViewImage = (relativePath) => {
  if (!relativePath) return
  if (!relativePath.startsWith('http')) {
    selectedImage.value = `${backendUrl}${relativePath}`
  } else {
    selectedImage.value = relativePath
  }
}

const handleCloseAddMember = () => {
  showAddMemberModal.value = false
  showGroupSettings.value = true
}

const openSettings = () => {
  currentView.value = 'settings'
}
const closeSettings = () => {
  currentView.value = 'chat'
}
const openFriendsTab = () => {
  currentView.value = 'friends'
}
</script>

<template>
  <div class="flex h-screen bg-[var(--color-bg-body)] font-display overflow-hidden">
    <ChatSidebarLeft />

    <div class="flex-1 flex flex-col min-w-0 bg-[var(--surface-panel)] relative">

      <template v-if="activeGroupId">
        <ChatHeader
          :activeGroup="chatStore.activeGroup"
          :currentUser="currentUser"
          @openInfo="handleOpenGroupSettings"
        />

        <ChatMessages
          :messages="messages"
          :typingUsers="typingUsers"
          :loading="loading"
          :currentUser="currentUser"
          :currentName="currentName"
          :currentSurname="currentSurname"
          :formatTime="formatTime"
          @reply="$emit('reply', $event)"
          @view-image="handleViewImage"
        />

        <ChatTypingIndicator
          :onlineUsers="onlineUsers"
          :users="typingUsers"
          :compact="false"
        />

        <ChatInput
          :messageContent="messageContent"
          :isConnected="isConnected"
          :replyingTo="replyingTo"
          @cancelReply="$emit('cancelReply')"
          @typing="$emit('typing')"
          @sendMessage="$emit('sendMessage')"
          @update:messageContent="$emit('updateMessageContent', $event)"
          @uploadImage="$emit('uploadImage', $event)"
          @uploadFile="$emit('uploadFile', $event)"
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
      :activeGroup="chatStore.activeGroup"
      :onlineUsers="onlineUsers"
      :groupMembers="activeGroupMembers"
      :currentUser="currentUser"
      :currentName="currentName"
      :currentSurname="currentSurname"
      :myStatus="myStatus"
      @setStatus="$emit('setStatus', $event)"
      @logout="$emit('logout')"
      @open-settings="openSettings"
      @openFriends="openFriendsTab"
    />

    <Teleport to="body">
      <GroupInfoModal
        v-if="showGroupSettings"
        :activeGroup="chatStore.activeGroup"
        :currentUser="currentUser"
        @close="showGroupSettings = false"
        @openAddMember="handleOpenAddMember"
      />

      <AddMemberModal
        v-if="showAddMemberModal"
        :friends="friendsStore.friends"
        :currentMembers="chatStore.activeGroup?.members || []"
        @close="handleCloseAddMember"
        @add="handleMembersAdded"
      />

      <ImageViewerModal
        v-if="selectedImage"
        :imageUrl="selectedImage"
        @close="selectedImage = null"
      />
    </Teleport>

    <SettingsLayout
      v-if="currentView === 'settings'"
      :currentUser="currentUser"
      @close="closeSettings"
      @logout="$emit('logout')"
    />

    <FriendsView
      v-if="currentView === 'friends'"
      :onlineUsers="onlineUsers"
      @close="currentView = 'chat'"
    />
  </div>
</template>
