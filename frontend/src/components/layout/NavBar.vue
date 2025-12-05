<script setup>
import Logo from '@/components/ui/AppLogo.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
</script>

<template>
  <header
    class="w-full border-b bg-[var(--surface-panel)] border-[var(--color-border)]"
  >
    <nav class="max-w-7xl mx-auto h-16 flex items-center justify-between px-4 sm:px-6 lg:px-8">

      <!-- LEFT: LOGO -->
      <Logo />

      <!-- RIGHT: ACTIONS -->
      <div class="flex items-center gap-4">
        <ThemeToggle />
        <!-- <UserMenu /> -->
      </div>

    </nav>
  </header>
</template>
