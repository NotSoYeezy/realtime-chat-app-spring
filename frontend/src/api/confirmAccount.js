import api from "@/api/axios.js";

class ConfirmAccount {
  confirm(token) {
    return api.get(`auth/confirm/${token}`);
  }

  resend(email) {
    return api.post('auth/resend-confirmation', { email });
  }
}

export default new ConfirmAccount();
