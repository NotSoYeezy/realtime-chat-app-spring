import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/axios'
import { CHAT_PAGE_SIZE } from '@/utils/constants'

export const useChatStore = defineStore('chat', () => {
  const groups = ref([])
  const activeGroupId = ref(null)
  const loading = ref(false)
  let readReceiptTimeout = null

  const activeGroup = computed(() => {
    return groups.value.find(g => g.id === activeGroupId.value) || null
  })

  const activeMessages = computed(() => {
    return activeGroup.value ? activeGroup.value.messages : []
  })

  const sortedGroups = computed(() => {
    return [...groups.value].sort((a, b) => {
      const timeA = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : (a.createdAt ? new Date(a.createdAt).getTime() : 0)
      const timeB = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : (b.createdAt ? new Date(b.createdAt).getTime() : 0)

      return timeB - timeA
    })
  })

  const sendReadReceiptDebounced = (groupId) => {
    if (readReceiptTimeout) {
      clearTimeout(readReceiptTimeout)
    }

    readReceiptTimeout = setTimeout(() => {
      api.post(`/groups/${groupId}/read`).catch(console.error)
      readReceiptTimeout = null
    }, 2000)
  }

  const fetchGroups = async () => {
    loading.value = true
    try {
      const response = await api.get('/groups')

      groups.value = response.data.map(g => ({
        ...g,
        messages: g.messages || [],
        unreadCount: g.unreadCount || 0,
        page: 0,
        hasMore: true
      }))

      if (groups.value.length > 0 && !activeGroupId.value) {
        const lastGroupId = localStorage.getItem('lastActiveGroupId')

        const groupToRestore = groups.value.find(g => g.id == lastGroupId)

        if (groupToRestore) {
          await setActiveGroup(groupToRestore.id)
        }
      }
    } catch (err) {
      console.error('Failed to fetch groups', err)
    } finally {
      loading.value = false
    }
  }

  const updateMemberReadTime = (groupId, updatedMember) => {
    const group = groups.value.find(g => g.id === groupId)

    if (group && group.members) {
      const index = group.members.findIndex(m => m.id === updatedMember.id)

      if (index !== -1) {
        console.log('Updating read time for:', updatedMember.username)

        group.members[index] = {
          ...group.members[index],
          lastReadTime: updatedMember.lastReadTime
        }
      }
    }
  }

  const fetchHistory = async (groupId, page = 0) => {
    try {
      const response = await api.get(`/chat/history/${groupId}`, {
        params: { 
          page, 
          size: CHAT_PAGE_SIZE 
        }
      })

      const index = groups.value.findIndex(g => g.id === groupId)

      if (index !== -1) {
        const newMessages = response.data
        const group = groups.value[index]

        if (page === 0) {
           group.messages = newMessages
           group.page = 0
           group.hasMore = newMessages.length === CHAT_PAGE_SIZE
        } else {
           // Prepend messages
           // We need to ensure no duplicates if any overlap occurs (though slice shouldn't overlap if stable)
           // But safe to filter just in case or just prepend
           const existingIds = new Set(group.messages.map(m => m.id))
           const uniqueNew = newMessages.filter(m => !existingIds.has(m.id))

           group.messages = [...uniqueNew, ...group.messages]
           group.page = page
           group.hasMore = newMessages.length === CHAT_PAGE_SIZE
        }
      }
    } catch (err) {
      console.error('Failed to fetch history:', err)
    }
  }

  const loadMoreMessages = async () => {
    if (!activeGroup.value || !activeGroup.value.hasMore) return

    try {
       const nextPage = (activeGroup.value.page || 0) + 1
       await fetchHistory(activeGroup.value.id, nextPage)
    } catch (e) {
        console.error("Load more failed", e)
    }
  }

  const refreshGroup = async (groupId) => {
    try {
      const response = await api.get(`/groups/${groupId}`)
      const updatedData = response.data

      const index = groups.value.findIndex(g => g.id === groupId)

      if (index !== -1) {
        groups.value[index] = {
          ...groups.value[index],
          ...updatedData,
          messages: groups.value[index].messages,
          unreadCount: groups.value[index].unreadCount
        }
      }
    } catch (err) {
      console.error('Failed to refresh group info', err)
    }
  }

  const setActiveGroup = async (groupId) => {
    if (activeGroupId.value === groupId) {
      return;
    }

    localStorage.setItem('lastActiveGroupId', groupId)

    if (readReceiptTimeout) {
      clearTimeout(readReceiptTimeout)
      readReceiptTimeout = null
    }

    activeGroupId.value = groupId
    const group = groups.value.find(g => g.id === groupId)

    if (group) {
      if (group.unreadCount > 0) {
        try {
          await api.post(`/groups/${groupId}/read`)
          group.unreadCount = 0
        } catch(e) { console.error(e) }
      }

      await fetchHistory(groupId)
    }
  }

  const handleIncomingMessage = (message) => {
    const groupId = message.groupId
    const index = groups.value.findIndex(g => g.id == groupId)

    if (index !== -1) {
      if (message.type === 'MEMBER_ADDED') {
        const currentMembers = groups.value[index].members || []
        // Avoid duplicates
        const newMembers = message.members.filter(nm =>
          !currentMembers.some(cm => cm.id === nm.id)
        )
        groups.value[index].members = [...currentMembers, ...newMembers]
        return
      }

      if (message.type === 'MEMBER_REMOVED') {
        const removedIds = message.userIds || []
        groups.value[index].members = groups.value[index].members.filter(
          m => !removedIds.includes(m.id)
        )
        return
      }

      if (message.type === 'GROUP_UPDATED') {
        if (message.name) groups.value[index].name = message.name
        if (message.imageUrl) groups.value[index].imageUrl = message.imageUrl
        return
      }

      groups.value[index].messages.push(message)

      if (message.type === 'CHAT') {
        groups.value[index].lastMessage = message.content
        groups.value[index].lastMessageTime = message.timestamp
      }

      if (message.type === 'SYSTEM') {
        // We handle updates via events now, but keep this as fallback/log
      }

      if (activeGroupId.value == groupId) {
        sendReadReceiptDebounced(groupId)
      } else {
        if (message.type === 'CHAT') {
          groups.value[index].unreadCount++
        }
      }

    } else {
      fetchGroups()
    }
  }

  const addGroup = (newGroup) => {
    const exists = groups.value.some(g => g.id === newGroup.id)

    if (!exists) {
      groups.value.push({
        ...newGroup,
        messages: [],
        unreadCount: 0,
        page: 0,
        hasMore: true
      })
    }
  }

  const addMembersToGroup = async (userIds) => {
    if (!activeGroupId.value) return
    try {
      await api.post(`/groups/${activeGroupId.value}/members`, { userIds })
      await refreshGroup(activeGroupId.value)
    } catch (err) {
      console.error("Failed to add members", err)
      throw err
    }
  }

  const removeMembersFromGroup = async (userIds) => {
    if (!activeGroupId.value) return
    try {
      await api.delete(`/groups/${activeGroupId.value}/members`, {
        data: { userIds }
      })

      try {
        await refreshGroup(activeGroupId.value)
      } catch (refreshError) {
        if (refreshError.response && (refreshError.response.status === 403 || refreshError.response.status === 404)) {
          removeGroup(activeGroupId.value)
        }
      }

    } catch (err) {
      console.error("Failed to remove members", err)
      throw err
    }
  }

  const addAdmin = async (userId) => {
    if (!activeGroupId.value) return
    try {
      await api.post(`/groups/${activeGroupId.value}/admin`, { userId })
      await refreshGroup(activeGroupId.value)
    } catch (err) {
      console.error("Failed to add admin", err)
      throw err
    }
  }

  const removeAdmin = async (userId) => {
    if (!activeGroupId.value) return
    try {
      await api.delete(`/groups/${activeGroupId.value}/admin`, {
        data: { userId }
      })
      await refreshGroup(activeGroupId.value)
    } catch (err) {
      console.error("Failed to remove admin", err)
      throw err
    }
  }

  const muteGroup = async (groupId) => {
    try {
      await api.post(`/groups/${groupId}/mute`)
      const group = groups.value.find(g => g.id === groupId)
      if (group) group.muted = true
    } catch (e) {
      console.error(e)
    }
  }

  const unmuteGroup = async (groupId) => {
    try {
      await api.delete(`/groups/${groupId}/mute`)
      const group = groups.value.find(g => g.id === groupId)
      if (group) group.muted = false
    } catch (e) {
      console.error(e)
    }
  }

  const removeGroup = (groupId) => {
    groups.value = groups.value.filter(g => g.id !== groupId)

    if (activeGroupId.value === groupId) {
      activeGroupId.value = null
    }
  }

  return {
    groups,
    sortedGroups,
    activeGroupId,
    activeGroup,
    activeMessages,
    loading,
    fetchGroups,
    refreshGroup,
    setActiveGroup,
    handleIncomingMessage,
    addGroup,
    addMembersToGroup,
    removeMembersFromGroup,
    addAdmin,
    removeAdmin,
    muteGroup,
    unmuteGroup,
    removeGroup,
    updateMemberReadTime,
    loadMoreMessages
  }
})
