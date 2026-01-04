import { defineStore } from 'pinia'
import axios from '@/api/axios'
import { useChatStore } from '@/stores/chat'

export const useFriendsStore = defineStore('friends', {
  state: () => ({
    friends: [],
    incoming: [],
    outgoing: [],
    blocked: [],
    loading: false
  }),

  actions: {
    async fetchAll() {
      this.loading = true
      try {
        const [friends, incoming, outgoing, blocked] = await Promise.all([
          axios.get('/friends'),
          axios.get('/friends/requests/incoming'),
          axios.get('/friends/requests/outgoing'),
          axios.get('/friends/blocked')
        ])

        this.friends = friends.data
        this.incoming = incoming.data
        this.outgoing = outgoing.data
        this.blocked = blocked.data
      } finally {
        this.loading = false
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
