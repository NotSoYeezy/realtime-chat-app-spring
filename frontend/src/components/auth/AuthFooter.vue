<script setup>
defineProps({
  text: { type: String, required: true },
  linkText: { type: String, required: true },
  to: { type: String, required: true }
})
</script>

<template>
  <p class="text-sm text-[var(--color-text-secondary)] text-center">
    {{ text }}
    <router-link
      :to="to"
      class="font-medium text-[var(--color-primary)] hover:underline ml-1"
    >
      {{ linkText }}
    </router-link>
  </p>
</template>
