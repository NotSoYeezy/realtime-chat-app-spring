import api from "@/api/axios.js";

class ColorTheme {
  setColorTheme(groupId, color) {
    return api.post(`/groups/${groupId}/setColorTheme`, {color: color});
  }

  getColorTheme(groupId) {
    return api.get(`/groups/${groupId}/getColorTheme`);
  }
}

export default new ColorTheme();
