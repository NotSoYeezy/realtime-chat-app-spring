import axios from '@/api/axios'

export default {
  search(query) {
    return axios.get('/api/users/search', {
      params: { query }
    })
  }
}
