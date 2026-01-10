import api from "@/api/axios.js";

class MediaHandler {
  async postImage(file) {
    if (!file) throw new Error("No file provided");

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.post('/files/upload', formData);
      return response.data;

    } catch (error) {
      console.error("MediaHandler Error:", error);
      throw error;
    }
  }

  async postFile(file) {
    if (!file) throw new Error("No file provided");

    const formData = new FormData();
    formData.append('file', file);

    try{
      const response = await api.post('/files/upload', formData);
      return response.data;
    } catch (error) {
      console.error("MediaHandler Error:", error);
      throw error;
    }
  }
}

export default new MediaHandler();
