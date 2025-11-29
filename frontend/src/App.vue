<script setup>
import { onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

onMounted(() => {
  if (authStore.accessToken) {
    authStore.startRefreshTokenTimer();
  }
});
</script>

<template>
  <RouterView />
</template>

