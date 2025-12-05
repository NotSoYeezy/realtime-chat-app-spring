import api from "@/api/axios.js";

class UserService {
  getUser(userId) {
    return api.get(`users/${userId}`);
  }
  getMyId() {
    return api.get('auth/me');
  }
}

export default new UserService();
