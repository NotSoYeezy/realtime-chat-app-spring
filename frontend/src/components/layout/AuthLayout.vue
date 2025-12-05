<script setup>
import AppLogo from '@/components/ui/AppLogo.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
</script>

<template>
  <div
    class="min-h-screen w-full auth-background flex flex-col items-center justify-center p-4 relative"
  >

    <ThemeToggle class="!absolute !top-4 !right-4 !z-50" />
    <AppLogo class="h-12 w-auto mb-8" />
    <slot />

  </div>
</template>

