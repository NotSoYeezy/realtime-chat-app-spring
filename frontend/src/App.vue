<script setup>
import {onMounted} from "vue";
import {applyTheme} from "@/utils/theme.js";

onMounted(() => {
  applyTheme();
});
</script>

<template>
  <RouterView/>
</template>
