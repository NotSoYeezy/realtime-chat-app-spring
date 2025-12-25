import api from "@/api/axios.js";

class UserService {
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

export default new UserService();
