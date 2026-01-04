<script setup>
import { onMounted } from "vue";
import { applyTheme } from "@/utils/theme.js";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();

onMounted(() => {
  applyTheme();
  if (authStore.isAuthenticated) {
    authStore.startHeartbeat();
  }
});
</script>

<template>
  <RouterView/>
</template>
