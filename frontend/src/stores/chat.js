import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/axios'

export const useChatStore = defineStore('chat', () => {
  const groups = ref([])
  const activeGroupId = ref(null)
  const loading = ref(false)

  const activeGroup = computed(() => {
    return groups.value.find(g => g.id === activeGroupId.value) || null
  })

  const activeMessages = computed(() => {
    return activeGroup.value ? activeGroup.value.messages : []
  })

  const sortedGroups = computed(() => {
    return [...groups.value].sort((a, b) => {
      const timeA = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0
      const timeB = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0

      return timeB - timeA
    })
  })

  const fetchGroups = async () => {
    loading.value = true
    try {
      const response = await api.get('/groups')

      groups.value = response.data.map(g => ({
        ...g,
        messages: g.messages || [],
        unreadCount: g.unreadCount || 0
      }))

      if (groups.value.length > 0 && !activeGroupId.value) {
        await setActiveGroup(groups.value[0].id)
      }
    } catch (err) {
      console.error('Failed to fetch groups', err)
    } finally {
      loading.value = false
    }
  }

  const fetchHistory = async (groupId) => {
    try {
      const response = await api.get(`/chat/history/${groupId}`)

      const index = groups.value.findIndex(g => g.id === groupId)

      if (index !== -1) {
        groups.value[index].messages = response.data
        console.log(`History loaded for group ${groupId}:`, response.data)
      }
    } catch (err) {
      console.error('Failed to fetch history:', err)
    }
  }

  const setActiveGroup = async (groupId) => {
    activeGroupId.value = groupId

    const group = groups.value.find(g => g.id === groupId)
    if (group) {
      group.unreadCount = 0

      await fetchHistory(groupId)
    }
  }

  const handleIncomingMessage = (message) => {
    const groupId = message.groupId
    const index = groups.value.findIndex(g => g.id === groupId)

    if (index !== -1) {
      groups.value[index].messages.push(message)
      groups.value[index].lastMessage = message.content
      groups.value[index].lastMessageTime = message.timestamp

      if (groupId !== activeGroupId.value) {
        groups.value[index].unreadCount++
      }
    } else {
      fetchGroups()
    }
  }

  const addGroup = (newGroup) => {
    groups.value.push({
      ...newGroup,
      messages: [],
      unreadCount: 0
    })
    setActiveGroup(newGroup.id)
  }

  return {
    groups,
    sortedGroups,
    activeGroupId,
    activeGroup,
    activeMessages,
    loading,
    fetchGroups,
    setActiveGroup,
    handleIncomingMessage,
    addGroup
  }
})
