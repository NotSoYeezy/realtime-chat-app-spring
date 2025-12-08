<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import SockJS from 'sockjs-client/dist/sockjs'
import { Stomp } from '@stomp/stompjs'
import api from '@/api/axios.js'
import ChatLayout from '@/components/layout/ChatLayout.vue'

const currentRoom = ref('general')
let roomSubscription = null
const router = useRouter()
const authStore = useAuthStore()

const messages = ref([])
const messageContent = ref('')
const stompClient = ref(null)
const isConnected = ref(false)
const currentUser = ref('')
const loading = ref(true)
const typingUsers = ref(new Set())
const onlineUsers = ref({})
const myStatus = ref('ONLINE')
let typingTimeout = null

const getCurrentUserFromToken = () => {
  const token = authStore.accessToken
  if (!token) return 'Anonymous'

  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split('')
        .map(function (c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        })
        .join(''),
    )

    const payload = JSON.parse(jsonPayload)
    return payload.sub
  } catch (e) {
    console.error('Error decoding token', e)
    return 'Anonymous'
  }
}


const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const loadHistory = async () => {
  loading.value = true
  messages.value = []
  try {
    const response = await api.get(`/chat/history/${currentRoom.value}`)
    messages.value = response.data
  } catch (err) {
    console.error("Can't fetch history:", err)
  } finally {
    loading.value = false
  }
}

const subscribeToRoom = () => {
  if (!stompClient.value || !isConnected.value) return

  if (roomSubscription) {
    roomSubscription.unsubscribe()
  }

  roomSubscription = stompClient.value.subscribe(
    `/topic/${currentRoom.value}`,
    onMessageReceived
  )
}

const switchRoom = async (roomId) => {
  if (currentRoom.value === roomId) return
  currentRoom.value = roomId
  typingUsers.value.clear()
  await loadHistory()
  subscribeToRoom()
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

  subscribeToRoom()
  stompClient.value.subscribe('/topic/public', onGlobalMessageReceived)

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
  } else {
    messages.value.push(message)
    if (message.type === 'CHAT') {
      typingUsers.value.delete(message.sender)
    }
  }
}

const onGlobalMessageReceived = (payload) => {
  const message = JSON.parse(payload.body)
  if (message.type === 'JOIN') {
    onlineUsers.value[message.sender] = message.userStatus || 'ONLINE'
  } else if (message.type === 'LEAVE') {
    delete onlineUsers.value[message.sender]
  } else if (message.userStatus) {
    onlineUsers.value[message.sender] = message.userStatus
  }
}

const handleTypingNotification = (sender) => {
  if (sender === currentUser.value) return

  typingUsers.value.add(sender)

  setTimeout(() => {
    typingUsers.value.delete(sender)
  }, 3000)
}

const sendMessage = () => {
  if (messageContent.value.trim() && stompClient.value && isConnected.value) {
    const chatMessage = {
      content: messageContent.value,
      type: 'CHAT',
      roomId: currentRoom.value,
    }

    stompClient.value.send(
      `/app/chat.sendMessage/${currentRoom.value}`,
      {},
      JSON.stringify(chatMessage)
    )
    messageContent.value = ''
  }
}

const handleTyping = () => {
  if (!stompClient.value || !isConnected.value) return

  if (!typingTimeout) {
    stompClient.value.send(`/app/chat.typing/${currentRoom.value}`,
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

onMounted(async () => {
  currentUser.value = getCurrentUserFromToken()
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
    :currentRoom="currentRoom"
    @changeRoom="switchRoom"
  />
</template>
