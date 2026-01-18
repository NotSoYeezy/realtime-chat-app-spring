import { defineStore } from 'pinia'
import axios from '@/api/axios'
import { useChatStore } from '@/stores/chat'

export const useFriendsStore = defineStore('friends', {
  state: () => ({
    friends: { content: [], page: 0, hasMore: true },
    incoming: { content: [], page: 0, hasMore: true },
    outgoing: { content: [], page: 0, hasMore: true },
    blocked: { content: [], page: 0, hasMore: true },
    loading: false
  }),

  actions: {
    async fetchAll() {
      this.loading = true
      try {
        await Promise.all([
          this.fetchFriends(),
          this.fetchIncoming(),
          this.fetchOutgoing(),
          this.fetchBlocked()
        ])
      } finally {
        this.loading = false
      }
    },

    async fetchFriends(page = 0) {
      if (page === 0) this.friends = { content: [], page: 0, hasMore: true }
      try {
        const res = await axios.get('/friends', { params: { page, size: 20 } })
        const newContent = res.data.content
        if (page === 0) {
          this.friends.content = newContent
        } else {
          this.friends.content.push(...newContent)
        }
        this.friends.page = page
        this.friends.hasMore = !res.data.last
      } catch (e) {
        console.error('Fetch friends failed', e)
      }
    },

    async fetchIncoming(page = 0) {
      if (page === 0) this.incoming = { content: [], page: 0, hasMore: true }
      try {
        const res = await axios.get('/friends/requests/incoming', { params: { page, size: 20 } })
        const newContent = res.data.content
        if (page === 0) {
          this.incoming.content = newContent
        } else {
          this.incoming.content.push(...newContent)
        }
        this.incoming.page = page
        this.incoming.hasMore = !res.data.last
      } catch (e) {
        console.error('Fetch incoming failed', e)
      }
    },

    async fetchOutgoing(page = 0) {
      if (page === 0) this.outgoing = { content: [], page: 0, hasMore: true }
      try {
        const res = await axios.get('/friends/requests/outgoing', { params: { page, size: 20 } })
        const newContent = res.data.content
        if (page === 0) {
          this.outgoing.content = newContent
        } else {
          this.outgoing.content.push(...newContent)
        }
        this.outgoing.page = page
        this.outgoing.hasMore = !res.data.last
      } catch (e) {
        console.error('Fetch outgoing failed', e)
      }
    },

    async fetchBlocked(page = 0) {
      if (page === 0) this.blocked = { content: [], page: 0, hasMore: true }
      try {
        const res = await axios.get('/friends/blocked', { params: { page, size: 20 } })
        const newContent = res.data.content
        if (page === 0) {
          this.blocked.content = newContent
        } else {
          this.blocked.content.push(...newContent)
        }
        this.blocked.page = page
        this.blocked.hasMore = !res.data.last
      } catch (e) {
        console.error('Fetch blocked failed', e)
      }
    },

    async loadMoreFriends() {
      if (this.friends.hasMore) {
        await this.fetchFriends(this.friends.page + 1)
      }
    },

    async loadMoreIncoming() {
      if (this.incoming.hasMore) {
        await this.fetchIncoming(this.incoming.page + 1)
      }
    },

    async loadMoreOutgoing() {
      if (this.outgoing.hasMore) {
        await this.fetchOutgoing(this.outgoing.page + 1)
      }
    },

    async loadMoreBlocked() {
      if (this.blocked.hasMore) {
        await this.fetchBlocked(this.blocked.page + 1)
      }
    },

    async removeFriend(id) {
      await axios.delete(`/friends/${id}`)
      await this.fetchAll()
    },

    async blockUser(id) {
      await axios.post('/friends/blocks', { blockedUserId: id })
      await this.fetchAll()
    },

    async acceptRequest(id) {
      await axios.patch(`/friends/requests/${id}/accept`)
      await this.fetchAll()

      const chatStore = useChatStore()
      await chatStore.fetchGroups()
    },

    async rejectRequest(id) {
      await axios.patch(`/friends/requests/${id}/reject`)
      await this.fetchAll()
    },

    async sendRequest(receiverId) {
      await axios.post('/friends/requests', { receiverId })
      await this.fetchAll()
    },

    async cancelRequest(id) {
      await axios.delete(`/friends/requests/${id}`)
      await this.fetchAll()
    },

    async unblockUser(id) {
      await axios.delete(`/friends/blocks/${id}`)
      await this.fetchAll()
    }
  }
})
