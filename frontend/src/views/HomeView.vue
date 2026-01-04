<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';
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
const groupSubscriptions = new Map()
const replyingTo = ref(null)
let typingTimeout = null

const subscribeToGroup = (groupId) => {
  if (groupSubscriptions.has(groupId)) return
  if (!stompClient.value || !isConnected.value) return

  const sub = stompClient.value.subscribe(
    `/topic/group.${groupId}`,
    onGroupMessageReceived
  )
  groupSubscriptions.set(groupId, sub)
}

const unsubscribeFromGroup = (groupId) => {
  if (groupSubscriptions.has(groupId)) {
    groupSubscriptions.get(groupId).unsubscribe()
    groupSubscriptions.delete(groupId)
  }
}

const subscribeToAllGroups = () => {
  chatStore.groups.forEach(group => {
    subscribeToGroup(group.id)
  })
}

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

  stompClient.value = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),

    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },

    debug: function (str) {
      console.log(str);
    },

    reconnectDelay: 5000,

    heartbeatIncoming: 20000,
    heartbeatOutgoing: 20000,

    onConnect: (frame) => {
      isConnected.value = true
      console.log('Connected: ' + frame)

      stompClient.value.subscribe('/user/queue/messages', onMessageReceived)
      stompClient.value.subscribe('/user/queue/groups', onGroupNotificationReceived)
      stompClient.value.subscribe('/user/queue/friends-status', onFriendStatusReceived)
      stompClient.value.subscribe('/topic/public', onPublicMessageReceived)

      // stompClient.value.publish({
      //   destination: `/app/chat.addUser/${chatStore.activeGroupId}`,
      //   body: JSON.stringify({ type: 'JOIN', content: '' })
      // })

      subscribeToAllGroups()
    },

    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message'])
      console.error('Additional details: ' + frame.body)
    },

    onWebSocketClose: () => {
      console.log('Connection closed')
      isConnected.value = false
    }
  })

  stompClient.value.activate()
}

const onError = (error) => {
  isConnected.value = false
}

const handleReply = (message) => {
  replyingTo.value = message
}

const cancelReply = () => {
  replyingTo.value = null
}


const onGroupMessageReceived = (payload) => {
  const message = JSON.parse(payload.body)

  if (message.type === 'TYPING') {
    if (message.groupId === chatStore.activeGroupId && message.sender.username !== currentUser.value) {
      handleTypingNotification(message.sender)
    }
    return
  }

  if (message.type === 'READ_RECEIPT') {
    chatStore.updateMemberReadTime(message.groupId, message.member)
    return
  }

  chatStore.handleIncomingMessage(message)
}

const onGroupNotificationReceived = (payload) => {
  const data = JSON.parse(payload.body)

  if (data.eventType === 'REMOVE') {
      chatStore.removeGroup(data.groupId)
      unsubscribeFromGroup(data.groupId)
  } else {
    chatStore.addGroup(data)
    subscribeToGroup(data.id)
  }

}

const onMessageReceived = (payload) => {
  const message = JSON.parse(payload.body)

  if (message.type === 'TYPING') return

  chatStore.handleIncomingMessage(message)
}

const onFriendStatusReceived = (payload) => {
  const message = JSON.parse(payload.body)
  const s = message.sender
  
  if (s && s.username) {
     // If user goes offline, you might want to remove them or set status to OFFLINE
     // For now, we just update the map which drives the UI
     onlineUsers.value[s.username] = s
  }
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
      groupId: chatStore.activeGroupId,
      parentId: replyingTo.value ? replyingTo.value.id : null // <--- PRZEKAZANIE ID
    }

    stompClient.value.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify(chatMessage)
    })

    messageContent.value = ''
    replyingTo.value = null
  }
}

const handleTyping = () => {
  if (!stompClient.value || !isConnected.value || !chatStore.activeGroupId) return

  if (!typingTimeout) {
    stompClient.value.publish({
      destination: `/app/chat.typing/${chatStore.activeGroupId}`,
      body: JSON.stringify({})
    })

    typingTimeout = setTimeout(() => {
      typingTimeout = null
    }, 2000)
  }
}

const handleLogout = async () => {
  if (stompClient.value) {
    stompClient.value.deactivate()
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
  if (!stompClient.value || !stompClient.value.connected){
    return
  }
  myStatus.value = status
  stompClient.value.publish({
    destination: '/app/user/setStatus',
    body: JSON.stringify({
      status: status,
    })
  })
}

const updateProfile = () => {
  currentUser.value = getCurrentUserFromToken()
}



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
    stompClient.value.deactivate()
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
    :replyingTo="replyingTo"
    @sendMessage="sendMessage"
    @reply="handleReply"
    @cancelReply="cancelReply"
    @typing="handleTyping"
    @updateMessageContent="messageContent = $event"
    @setStatus="setStatus"
    @logout="handleLogout"
    @openFriends="fetchOnlineUsers" 
  />
</template>
