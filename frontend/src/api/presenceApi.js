import axios from './axios';

export const sendHeartbeat = async () => {
  return await axios.post('/chat/heartbeat');
};

export const getOnlineUsers = async () => {
  return await axios.get('/chat/users/online');
};
