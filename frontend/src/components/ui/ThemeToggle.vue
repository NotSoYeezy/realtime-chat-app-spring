<script setup>
import {ref, onMounted} from "vue";
import {toggleTheme, applyTheme} from "@/utils/theme.js";

const isDark = ref(false);

onMounted(() => {
  applyTheme();
  isDark.value = document.documentElement.classList.contains('dark');
});

function handleToggle() {
  toggleTheme();
  isDark.value = document.documentElement.classList.contains('dark');
}
</script>

<template>
  <button
    @click="handleToggle"
    class="relative inline-flex h-7 w-12 items-center rounded-full transition-colors duration-300 focus:outline-none
           bg-[var(--color-border)]"
    :class="{
      'bg-[var(--color-primary)]': isDark
    }"
  >
    <!-- knob -->
    <span
      class="absolute h-5 w-5 rounded-full bg-[var(--surface-panel)] shadow transform transition-all duration-300"
      :class="{
        'translate-x-6': isDark,
        'translate-x-1': !isDark
      }"
    />
    <!-- icons -->
    <span
      class="absolute left-1 text-xs pointer-events-none text-[var(--color-text-secondary)]"
      v-if="!isDark"
    >
    </span>
    <span
      class="absolute right-1 text-xs pointer-events-none text-[var(--color-text-on-primary)]"
      v-else
    >
    </span>
  </button>
</template>
