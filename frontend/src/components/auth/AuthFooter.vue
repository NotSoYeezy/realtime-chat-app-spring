<script setup>
defineProps({
  text: { type: String },
  linkText: { type: String, required: true },
  to: { type: String, required: true },
  hasPasswordReset: { type: Boolean }
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
  <p v-if="hasPasswordReset" class="text-sm text-[var(--color-text-secondary)] text-center">
    Forgot password?
    <router-link
      to="/auth/password-reset/request"
      class="font-medium text-[var(--color-primary)] hover:underline ml-1"
    >
      Change password
    </router-link>
  </p>
</template>
