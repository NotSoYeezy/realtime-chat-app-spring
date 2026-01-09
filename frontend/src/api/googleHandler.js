import api from "@/api/axios.js";

class GoogleHandler {
  async isBusy() {
    return api.get('google/calendar/check-status');
  }

  async isCalendarConnected() {
    return api.get('google/calendar/check-calendar');
  }
}

export default new GoogleHandler();
