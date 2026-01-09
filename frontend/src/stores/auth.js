import { defineStore } from 'pinia';
import axios from '@/api/axios';
import { jwtDecode } from "jwt-decode";
import router from "@/router/index.js";
import { sendHeartbeat } from '@/api/presenceApi';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem('access_token') || null,
    refreshToken: localStorage.getItem('refresh_token') || null,
    refreshTimeout: null,
    heartbeatInterval: null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.accessToken,
  },

  actions: {
    async login(credentials) {
      try {
        const response = await axios.post('/auth/login', credentials);

        const accessToken = response.data.token
        const refreshToken = response.data.refreshToken

        this.setTokens(accessToken, refreshToken);
      } catch (e) {
      }
    },

    setTokens(accessToken, refreshToken) {
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;

      localStorage.setItem('access_token', accessToken);
      localStorage.setItem('refresh_token', refreshToken);

      this.startRefreshTokenTimer();
      this.startHeartbeat();
    },


    async refreshAccessToken() {
      if (!this.refreshToken) {
        this.logout();
        return;
      }

      try {
        const response = await axios.post('/auth/refresh', {
          refreshToken: this.refreshToken
        });

        const newAccessToken = response.data.token || response.data.access_token;
        const newRefreshToken = response.data.refreshToken;

        if (newRefreshToken) {
          this.setTokens(newAccessToken, newRefreshToken);
        } else {
          this.accessToken = newAccessToken;
          localStorage.setItem('access_token', newAccessToken);
          this.startRefreshTokenTimer();
        }

        return this.accessToken;
      } catch (error) {
        console.error("Refresh failed", error);
        this.logout();
        throw error;
      }
    },

    startRefreshTokenTimer() {
      if (!this.accessToken) return;

      try {
        const jwt = jwtDecode(this.accessToken);
        const expires = new Date(jwt.exp * 1000);
        const currentTime = Date.now();

        const buffer = 10 * 1000;

        const timeout = expires.getTime() - currentTime - buffer;

        console.log(`Token wygasa: ${expires.toLocaleTimeString()}`);
        console.log(`Za ile wywołać refresh (ms): ${timeout}`);

        if (this.refreshTimeout) clearTimeout(this.refreshTimeout);

        if (timeout > 0) {
          console.log(`Auto-refresh ustawiony za ${Math.round(timeout / 1000)}s`);
          this.refreshTimeout = setTimeout(() => {
            this.refreshAccessToken();
          }, timeout);
        } else {
          console.warn("Token kończy się lub wygasł. Próba natychmiastowego odświeżenia...");

          this.refreshAccessToken();
        }
      } catch (e) {
        console.error("Błąd timera:", e);
        this.logout();
      }
    },

    startHeartbeat() {
      if (this.heartbeatInterval) return;

      sendHeartbeat().catch(console.error);

      this.heartbeatInterval = setInterval(async () => {
        try {
          await sendHeartbeat();
        } catch (e) {
          console.error("Heartbeat failed", e);
        }
      }, 30000);
    },

    stopHeartbeat() {
      if (this.heartbeatInterval) {
        clearInterval(this.heartbeatInterval);
        this.heartbeatInterval = null;
      }
    },

    logout() {
      this.accessToken = null;
      this.refreshToken = null;
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');

      if (this.refreshTimeout) {
        clearTimeout(this.refreshTimeout);
        this.refreshTimeout = null;
      }
      this.stopHeartbeat();
      router.push('/login');
    }
  }
});
