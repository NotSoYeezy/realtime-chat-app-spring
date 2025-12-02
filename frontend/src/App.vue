<script setup>
import { onMounted, onBeforeUnmount } from 'vue';
import { applyTheme } from '@/utils/theme';

onMounted(() => {
  applyTheme(); // inicjalizacja motywu
  const mq = window.matchMedia('(prefers-color-scheme: dark)');
  mq.addEventListener('change', applyTheme);
});

onBeforeUnmount(() => {
  const mq = window.matchMedia('(prefers-color-scheme: dark)');
  mq.removeEventListener('change', applyTheme);
});
</script>

<template>
  <RouterView />
</template>
