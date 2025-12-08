import api from "@/api/axios.js";

class CheckPassword {
  checkPassword(password) {
    return api.post('auth/checkPassword', {"password": password});
  }
}

export default new CheckPassword();
