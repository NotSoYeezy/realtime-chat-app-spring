<template>
  <section class="mb-10">
    <h2 class="text-lg font-semibold mb-1">{{ title }}</h2>
    <p v-if="description" class="text-sm mb-4 text-[var(--color-text-secondary)]">
      {{ description }}
    </p>

    <div class="flex flex-col gap-3">
      <slot />
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: String,
  description: String
})
</script>
