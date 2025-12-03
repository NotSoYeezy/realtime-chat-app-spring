<script setup>
defineProps({
  title: String,
  subtitle: String,
})
</script>

<template>
  <div
    class="w-full max-w-md rounded-xl p-8 md:p-10 shadow-lg bg-[var(--surface-panel)] text-[var(--color-text-primary)]"
  >
    <!-- HEADER -->
    <div class="text-center">
      <h1 class="text-[var(--color-text-primary)] text-3xl font-bold tracking-tight pb-2">
        {{ title }}
      </h1>

      <p v-if="subtitle" class="text-[var(--color-text-secondary)]">
        {{ subtitle }}
      </p>
    </div>

    <!-- FORM CONTENT -->
    <div class="mt-8">
      <slot />
    </div>

    <!-- FOOTER -->
    <div v-if="$slots.footer" class="mt-6 text-center">
      <slot name="footer" />
    </div>
  </div>
</template>
