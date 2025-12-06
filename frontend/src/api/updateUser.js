import api from "@/api/axios.js";

class UpdateUser {
  updateUser(payload) {

    return api.post('auth/update', payload);
  }
}

export default new UpdateUser();
