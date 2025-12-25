<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import SockJS from 'sockjs-client/dist/sockjs'
import { Stomp } from '@stomp/stompjs'
import api from '@/api/axios.js'
import ChatLayout from '@/components/layout/ChatLayout.vue'

const router = useRouter()
const authStore = useAuthStore()
const chatStore = useChatStore()

const messageContent = ref('')
const stompClient = ref(null)
const isConnected = ref(false)
const currentName = ref('')
const currentSurname = ref('')
const currentUser = ref('')
const typingUsers = ref(new Set())
const onlineUsers = ref({})
const myStatus = ref('ONLINE')
const groupSubscription = ref(null)
let typingTimeout = null

const decodeToken = (token) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = window.atob(base64)
    return JSON.parse(jsonPayload)
  } catch (e) {
    return null
  }
}

const getUserIdFromToken = () => {
  const token = authStore.accessToken
  if (!token) return null
  const payload = decodeToken(token)
  if (!payload) return null
  return payload.userId
}

const loadUserProfile = async () => {
  const userId = getUserIdFromToken()
  try {
    const response = await api.get(`/users/${userId}`)
    return response.data
  } catch (e) {
    return null
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const connect = () => {
  const token = authStore.accessToken
  if (!token) return

  const socket = new SockJS('http://localhost:8080/ws')
  stompClient.value = Stomp.over(socket)

  stompClient.value.connect({ Authorization: `Bearer ${token}` }, onConnected, onError)
}

const onConnected = () => {
  isConnected.value = true

  stompClient.value.subscribe('/user/queue/messages', onMessageReceived)
  stompClient.value.subscribe('/topic/public', onPublicMessageReceived)

  stompClient.value.send(
    '/app/chat.addUser',
    {},
    JSON.stringify({
      type: 'JOIN',
      content: '',
    }),
  )

  if (chatStore.activeGroupId) {
    subscribeToActiveGroup(chatStore.activeGroupId)
  }
}

const onError = (error) => {
  isConnected.value = false
}

const subscribeToActiveGroup = (groupId) => {
  if (!stompClient.value || !isConnected.value) return

  if (groupSubscription.value) {
    groupSubscription.value.unsubscribe()
    groupSubscription.value = null
  }

  groupSubscription.value = stompClient.value.subscribe(
    `/topic/group.${groupId}`,
    onGroupMessageReceived
  )
}

const onGroupMessageReceived = (payload) => {
  const message = JSON.parse(payload.body)

  if (message.type === 'TYPING') {
    if (message.sender.username !== currentUser.value) {
      handleTypingNotification(message.sender)
    }
  }
}

const onMessageReceived = (payload) => {
  const message = JSON.parse(payload.body)

  if (message.type === 'TYPING') return

  chatStore.handleIncomingMessage(message)
}

const onPublicMessageReceived = (payload) => {
  const message = JSON.parse(payload.body)

  if (message.type === 'JOIN') {
    const s = message.sender
    if (s && s.username) {
      onlineUsers.value[s.username] = s
    }
    return
  }

  if (message.type === 'LEAVE') {
    const s = message.sender
    if (s && s.username) {
      delete onlineUsers.value[s.username]
    }
    return
  }
}

const handleTypingNotification = (sender) => {
  typingUsers.value.add(sender.username)

  setTimeout(() => {
    typingUsers.value.delete(sender.username)
  }, 3000)
}

const sendMessage = () => {
  if (messageContent.value.trim() && stompClient.value && isConnected.value && chatStore.activeGroupId) {
    const chatMessage = {
      content: messageContent.value,
      type: 'CHAT',
      groupId: chatStore.activeGroupId
    }

    stompClient.value.send('/app/chat.sendMessage', {}, JSON.stringify(chatMessage))
    messageContent.value = ''
  }
}

const handleTyping = () => {
  if (!stompClient.value || !isConnected.value || !chatStore.activeGroupId) return

  if (!typingTimeout) {
    stompClient.value.send(`/app/chat.typing/${chatStore.activeGroupId}`,
      {}, JSON.stringify({}))

    typingTimeout = setTimeout(() => {
      typingTimeout = null
    }, 2000)
  }
}

const handleLogout = async () => {
  if (stompClient.value) {
    stompClient.value.disconnect()
  }
  authStore.logout()
  router.push('/login')
}

const fetchOnlineUsers = async () => {
  try {
    const response = await api.get('/chat/users/online')
    onlineUsers.value = response.data
  } catch (error) {
    console.error(error)
  }
}

const setStatus = (status) => {
  myStatus.value = status
  stompClient.value.send(
    '/app/user/setStatus',
    {},
    JSON.stringify({
      status: status,
    }),
  )
}

const updateProfile = () => {
  currentUser.value = getCurrentUserFromToken()
}

watch(
  () => chatStore.activeGroupId,
  (newGroupId) => {
    if (newGroupId) {
      subscribeToActiveGroup(newGroupId)
    }
  }
)

onMounted(async () => {
  const profile = await loadUserProfile()
  if (profile) {
    currentName.value = profile.name
    currentSurname.value = profile.surname
    currentUser.value = profile.username
  }
  await fetchOnlineUsers()
  await chatStore.fetchGroups()
  connect()
})

onBeforeUnmount(() => {
  if (stompClient.value) {
    stompClient.value.disconnect()
  }
})
</script>

<template>
  <ChatLayout
    :activeGroupId="chatStore.activeGroupId"
    :messages="chatStore.activeMessages"
    :typingUsers="typingUsers"
    :loading="chatStore.loading"
    :currentUser="currentUser"
    :currentName="currentName"
    :currentSurname="currentSurname"
    :isConnected="isConnected"
    :messageContent="messageContent"
    :onlineUsers="onlineUsers"
    :myStatus="myStatus"
    :formatTime="formatTime"
    @sendMessage="sendMessage"
    @typing="handleTyping"
    @updateMessageContent="messageContent = $event"
    @setStatus="setStatus"
    @logout="handleLogout"
  />
</template>
