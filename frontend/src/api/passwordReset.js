import api from '@/api/axios'

export default {
  confirm(token, password) {
    return api.post('/auth/password-reset/confirm', {
      token,
      password
    })
  },

  request(email) {
    return api.post('/auth/password-reset/request', { email })
  }
}
