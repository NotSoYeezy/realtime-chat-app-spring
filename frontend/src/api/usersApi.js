import axios from '@/api/axios'

export default {
  search(query, page = 0, size = 20) {
    return axios.get('/api/users/search', {
      params: { query, page, size }
    })
  }
}
