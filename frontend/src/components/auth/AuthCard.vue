<script setup>
defineProps({
  title: String,
  subtitle: String
})
</script>

<template>
  <div class="relative flex min-h-screen w-full flex-col items-center justify-center bg-background-light dark:bg-background-dark p-4 sm:p-6 lg:p-8 font-display">
    <div class="w-full max-w-md rounded-xl bg-slate-100 dark:bg-[#1F2937] p-8 md:p-10 shadow-lg">
      <div class="flex flex-col items-center">
        <div class="mb-6">
          <slot name="logo" />
        </div>

        <h1 class="text-slate-800 dark:text-white tracking-tight text-2xl sm:text-3xl font-bold leading-tight text-center pb-2">
          {{ title }}
        </h1>
        <p v-if="subtitle" class="text-center text-slate-600 dark:text-slate-400">
          {{ subtitle }}
        </p>
      </div>

      <div class="mt-8">
        <slot />
      </div>

      <div v-if="$slots.footer" class="mt-6">
        <slot name="footer" />
      </div>
    </div>
  </div>
</template>
