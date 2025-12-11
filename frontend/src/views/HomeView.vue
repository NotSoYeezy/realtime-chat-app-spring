<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import SockJS from 'sockjs-client/dist/sockjs'
import { Stomp } from '@stomp/stompjs'
import api from '@/api/axios.js'
import ChatLayout from '@/components/layout/ChatLayout.vue'

const router = useRouter()
const authStore = useAuthStore()

const messages = ref([])
const messageContent = ref('')
const stompClient = ref(null)
const isConnected = ref(false)
const currentName = ref('')
const currentSurname = ref('')
const currentUser = ref('')
const loading = ref(true)
const typingUsers = ref(new Set())
const onlineUsers = ref({})
const myStatus = ref('ONLINE')
let typingTimeout = null


const decodeToken = (token) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = window.atob(base64)

    return JSON.parse(jsonPayload)
  } catch (e) {
    console.error("Invalid token", e)
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
  const userId = getUserIdFromToken();

  try {
    const response = await api.get(`/users/${userId}`)
    console.log(response.data)
    return response.data
  } catch (e) {
    console.error("Failed to load user profile", e)
    return null
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const loadHistory = async () => {
  try {
    const response = await api.get('/chat/history')
    messages.value = response.data
  } catch (err) {
    console.error("Can't fetch history:", err)
  } finally {
    loading.value = false
  }
}

const connect = () => {
  const token = authStore.accessToken
  if (!token) return

  const socket = new SockJS('http://localhost:8080/ws')
  stompClient.value = Stomp.over(socket)

  // stompClient.value.debug = () => {}

  stompClient.value.connect({ Authorization: `Bearer ${token}` }, onConnected, onError)
}

const onConnected = () => {
  isConnected.value = true
  console.log('Connected to websocket')

  stompClient.value.subscribe('/topic/public', onMessageReceived)

  stompClient.value.send(
    '/app/chat.addUser',
    {},
    JSON.stringify({
      type: 'JOIN',
      content: '',
    }),
  )
}

const onError = (error) => {
  console.error('WebSocket error:', error)
  isConnected.value = false
}

const onMessageReceived = (payload) => {
  const message = JSON.parse(payload.body)

  if (message.type === 'TYPING') {
    handleTypingNotification(message.sender)
    return
  }

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

  // normal message
  messages.value.push(message)

  if (message.type === 'CHAT') {
    typingUsers.value.delete(message.sender?.username)
  }
}


const handleTypingNotification = (sender) => {
  if (sender.username === currentUser.value) return

  typingUsers.value.add(sender.username)

  setTimeout(() => {
    typingUsers.value.delete(sender.username)
  }, 3000)
}

const sendMessage = () => {
  if (messageContent.value.trim() && stompClient.value && isConnected.value) {
    const chatMessage = {
      content: messageContent.value,
      type: 'CHAT',
    }

    stompClient.value.send('/app/chat.sendMessage', {}, JSON.stringify(chatMessage))
    messageContent.value = ''
  }
}

const handleTyping = () => {
  if (!stompClient.value || !isConnected.value) return

  if (!typingTimeout) {
    stompClient.value.send(`/app/chat.typing`, {}, JSON.stringify({}))

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
    console.log("abc", response.data)
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

onMounted(async () => {
  const profile = await loadUserProfile()
  if (profile) {
    currentName.value = profile.name
    currentSurname.value = profile.surname
    currentUser.value = profile.username
  }
  await fetchOnlineUsers()
  await loadHistory()
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
    :messages="messages"
    :typingUsers="typingUsers"
    :loading="loading"
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
    @onMessageReceived="onMessageReceived"
  />
</template>
