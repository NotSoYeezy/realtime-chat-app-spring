import api from "@/api/axios.js";

class User {
  updateUser(payload) {
    return api.post('auth/update', payload);
  }

  deleteUser() {
    return api.post(`auth/delete`);
  }

  checkPassword(password) {
    return api.post('auth/checkPassword', {"password": password});
  }


  getUser(userId) {
    return api.get(`users/${userId}`);
  }

  getMyId() {
    return api.get('auth/me');
  }

  searchUsers(query) {
    return api.get('users/search', {
      params: {query}
    });
  }

}

export default new User();
