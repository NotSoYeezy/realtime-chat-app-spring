import api from "@/api/axios.js";

const checkStatus = {
  async isBusy() {
    return api.get('google/calendar/check-status');
  }
};

export default checkStatus;
