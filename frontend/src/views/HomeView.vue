<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import SockJS from 'sockjs-client/dist/sockjs'
import { Stomp } from '@stomp/stompjs'
import api from '@/api/axios.js'

const router = useRouter()
const authStore = useAuthStore()

const messages = ref([])
const messageContent = ref('')
const stompClient = ref(null)
const isConnected = ref(false)
const currentUser = ref('')
const messagesContainer = ref(null)
const loading = ref(true)
const typingUsers = ref(new Set())
const onlineUsers = ref({})
const myStatus = ref("ONLINE")
let typingTimeout = null

const getCurrentUserFromToken = () => {
  const token = authStore.accessToken
  if (!token) return 'Anonymous'

  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))

    const payload = JSON.parse(jsonPayload)
    return payload.sub
  } catch (e) {
    console.error("Error decoding token", e)
    return 'Anonymous'
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const loadHistory = async () => {
  try {
    const response = await api.get('/chat/history')
    messages.value = response.data
    scrollToBottom()
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

  stompClient.value.connect(
    { 'Authorization': `Bearer ${token}` },
    onConnected,
    onError
  )
}

const onConnected = () => {
  isConnected.value = true
  console.log("Connected to websocket")

  stompClient.value.subscribe('/topic/public', onMessageReceived)

  stompClient.value.send("/app/chat.addUser", {}, JSON.stringify({
    type: 'JOIN',
    content: ''
  }))
}

const onError = (error) => {
  console.error('WebSocket error:', error)
  isConnected.value = false
}

const onMessageReceived = (payload) => {
  const message = JSON.parse(payload.body)
  if (message.type === 'TYPING') {
    handleTypingNotification(message.sender)
  } else if (message.type === 'JOIN') {
    onlineUsers.value[message.sender] = message.userStatus  || 'ONLINE'
  } else if (message.type === 'LEAVE') {
    delete onlineUsers.value[message.sender]
  }

  else {
    messages.value.push(message)
    scrollToBottom()

    if (message.type === 'CHAT') {
      typingUsers.value.delete(message.sender)
    }
  }
}

const handleTypingNotification = (sender) => {
  if (sender === currentUser.value) return;

  typingUsers.value.add(sender);

  setTimeout(() => {
    typingUsers.value.delete(sender);
  }, 3000);
}

const sendMessage = () => {
  if (messageContent.value.trim() && stompClient.value && isConnected.value) {
    const chatMessage = {
      content: messageContent.value,
      type: 'CHAT'
    }

    stompClient.value.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage))
    messageContent.value = ''
  }
}

const handleTyping = () => {
  if (!stompClient.value || !isConnected.value) return;

  if (!typingTimeout) {
    stompClient.value.send(
      `/app/chat.typing`,
      {},
      JSON.stringify({})
    );

    typingTimeout = setTimeout(() => {
      typingTimeout = null;
    }, 2000);
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
  stompClient.value.send("/app/user/setStatus", {}, JSON.stringify({
    status: status
  }))
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
  <div class="flex h-screen bg-background-light dark:bg-background-dark font-display overflow-hidden">

    <aside class="w-64 bg-slate-100 dark:bg-slate-900 border-r border-slate-200 dark:border-slate-700 hidden md:flex flex-col">
      <div class="p-4 border-b border-slate-200 dark:border-slate-700">
        <h2 class="font-bold text-slate-700 dark:text-slate-200 text-lg">Kanały</h2>
      </div>
    </aside>

    <main class="flex-1 flex flex-col min-w-0 bg-white dark:bg-slate-800 relative">

      <header class="bg-white dark:bg-slate-800 shadow-sm p-4 flex justify-between items-center z-10 border-b border-slate-200 dark:border-slate-700">
        <div class="flex items-center gap-3">
          <div class="h-10 w-10 bg-primary/10 text-primary rounded-lg flex items-center justify-center">
            <span class="material-symbols-outlined">tag</span>
          </div>
          <div>
            <h1 class="font-bold text-slate-800 dark:text-white text-lg">
            </h1>
            <p class="text-xs text-slate-500 dark:text-slate-400">
              Zalogowany jako: <span class="font-medium text-primary">{{ currentUser }}</span>
            </p>
          </div>
        </div>

        <button
          @click="handleLogout"
          class="text-sm text-red-600 hover:text-red-700 font-medium px-4 py-2 border border-red-200 rounded-lg hover:bg-red-50 transition-colors"
        >
          Wyloguj
        </button>
      </header>

      <div
        ref="messagesContainer"
        class="flex-1 overflow-y-auto p-4 space-y-4 scroll-smooth bg-slate-50 dark:bg-[#0f172a]"
      >
        <div v-if="loading" class="text-center text-slate-500 mt-10 flex flex-col items-center">
          <span class="material-symbols-outlined animate-spin text-3xl mb-2">sync</span>
          Ładowanie historii...
        </div>

        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['flex w-full', msg.sender === currentUser ? 'justify-end' : 'justify-start']"
        >
          <div v-if="msg.type === 'JOIN' || msg.type === 'LEAVE'" class="w-full text-center my-2 opacity-50">
            <span class="text-xs bg-slate-200 dark:bg-slate-700 px-2 py-1 rounded text-slate-600 dark:text-slate-300">
              {{ msg.sender }} {{ msg.type === 'JOIN' ? 'dołączył' : 'wyszedł' }}
            </span>
          </div>

          <div v-else class="flex flex-col max-w-[75%] md:max-w-[60%]">
            <span
              v-if="msg.sender !== currentUser"
              class="text-xs text-slate-500 ml-1 mb-1 font-medium"
            >
              {{ msg.sender }}
            </span>

            <div
              :class="[
                'p-3 rounded-2xl shadow-sm break-words',
                msg.sender === currentUser
                  ? 'bg-primary text-white rounded-br-none'
                  : 'bg-white dark:bg-slate-700 text-slate-800 dark:text-slate-200 rounded-bl-none border border-slate-200 dark:border-slate-600'
              ]"
            >
              {{ msg.content }}
            </div>

            <span
              :class="['text-[10px] mt-1 opacity-70', msg.sender === currentUser ? 'text-right' : 'text-left']"
            >
              {{ formatTime(msg.timestamp) }}
            </span>
          </div>
        </div>
      </div>

      <div class="bg-white dark:bg-slate-800 p-4 border-t border-slate-200 dark:border-slate-700">
        <div class="max-w-4xl mx-auto flex flex-col gap-1">

          <div class="px-2 text-xs text-slate-500 dark:text-slate-400 italic flex items-center min-h-[1.5rem]">
            <transition
              enter-active-class="transition ease-out duration-200"
              enter-from-class="opacity-0 translate-y-1"
              enter-to-class="opacity-100 translate-y-0"
              leave-active-class="transition ease-in duration-150"
              leave-from-class="opacity-100 translate-y-0"
              leave-to-class="opacity-0 translate-y-1"
            >
              <span v-if="typingUsers.size > 0" class="flex items-center gap-1">
                <span class="flex space-x-0.5 mr-1">
                    <span class="w-1 h-1 bg-primary rounded-full animate-bounce" style="animation-delay: 0ms"></span>
                    <span class="w-1 h-1 bg-primary rounded-full animate-bounce" style="animation-delay: 150ms"></span>
                    <span class="w-1 h-1 bg-primary rounded-full animate-bounce" style="animation-delay: 300ms"></span>
                </span>
                <span class="font-medium text-primary">{{ Array.from(typingUsers).join(', ') }}</span> pisze...
              </span>
            </transition>
          </div>

          <div class="flex items-center gap-2 bg-slate-100 dark:bg-slate-900 p-2 rounded-xl border border-slate-200 dark:border-slate-700 focus-within:ring-2 focus-within:ring-primary/20 transition-all">
            <input
              v-model="messageContent"
              @keyup.enter="sendMessage"
              @input="handleTyping"
              type="text"
              placeholder="Napisz wiadomość..."
              class="flex-1 bg-transparent border-none focus:ring-0 text-slate-800 dark:text-white placeholder-slate-400 px-2"
            />

            <button
              @click="sendMessage"
              :disabled="!messageContent.trim() || !isConnected"
              class="bg-primary hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed text-white p-2 rounded-lg transition-colors flex items-center justify-center shadow-sm"
            >
              <span class="material-symbols-outlined text-[20px]">send</span>
            </button>
          </div>

          <div v-if="!isConnected" class="text-center text-xs text-red-500 mt-1">
            Brak połączenia z serwerem!
          </div>
        </div>
      </div>
    </main>

    <aside class="w-64 bg-white dark:bg-slate-900 border-l border-slate-200 dark:border-slate-700 hidden lg:flex flex-col">

      <div class="p-4 border-b border-slate-200 dark:border-slate-700">
        <div class="flex items-center justify-between mb-3">
          <h3 class="font-bold text-slate-800 dark:text-white">Online</h3>
          <span class="bg-green-100 text-green-700 text-xs px-2 py-0.5 rounded-full font-medium">
            {{ Object.keys(onlineUsers).length }}
          </span>
        </div>

        <div class="grid grid-cols-3 gap-1 bg-slate-100 dark:bg-slate-800 p-1 rounded-lg">
          <button
            @click="setStatus('ONLINE')"
            :class="['py-1 rounded text-[10px] font-bold transition-all', myStatus === 'ONLINE' ? 'bg-white dark:bg-slate-700 shadow text-green-600' : 'text-slate-500']"
          >
            ON
          </button>
          <button
            @click="setStatus('BUSY')"
            :class="['py-1 rounded text-[10px] font-bold transition-all', myStatus === 'BUSY' ? 'bg-white dark:bg-slate-700 shadow text-red-600' : 'text-slate-500']"
          >
            BUSY
          </button>
          <button
            @click="setStatus('AWAY')"
            :class="['py-1 rounded text-[10px] font-bold transition-all', myStatus === 'AWAY' ? 'bg-white dark:bg-slate-700 shadow text-yellow-600' : 'text-slate-500']"
          >
            AWAY
          </button>
        </div>
      </div>

      <div class="p-2 overflow-y-auto flex-1 space-y-1">
        <div
          v-for="(status, username) in onlineUsers"
          :key="username"
          class="flex items-center gap-3 p-2 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors group cursor-default"
        >
          <div class="relative">
            <div class="h-9 w-9 rounded-full bg-slate-200 dark:bg-slate-700 flex items-center justify-center font-bold text-sm text-slate-600 dark:text-slate-300 group-hover:bg-slate-300 transition-colors">
              {{ username.charAt(0).toUpperCase() }}
            </div>

            <span
              :class="[
                'absolute bottom-0 right-0 h-3 w-3 rounded-full border-2 border-white dark:border-slate-900',
                status === 'ONLINE' ? 'bg-green-500' : '',
                status === 'BUSY' ? 'bg-red-500' : '',
                status === 'AWAY' ? 'bg-yellow-500' : '',
              ]"
            ></span>
          </div>

          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-200 truncate">
              {{ username }}
            </p>
            <p class="text-[10px] text-slate-400 truncate">
              {{ status }}
            </p>
          </div>

          <span v-if="username === currentUser" class="text-[10px] font-bold text-primary bg-primary/10 px-1.5 py-0.5 rounded">
            TY
          </span>
        </div>
      </div>
    </aside>

  </div>
</template>
