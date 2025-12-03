<script setup>
defineEmits(['logout'])
</script>

<template>
  <Button
    @click="$emit('logout')"
    class="relative overflow-hidden text-sm font-semibold px-4 py-2 rounded-lg text-[var(--color-danger)] border border-[var(--color-danger-border)] bg-[var(--color-danger-bg)] transition-all duration-300 hover:text-white hover:border-[var(--color-danger)] hover:shadow-[0_0_12px_rgba(255,80,80,0.45)]"
  >
    <span
      class="absolute inset-0 opacity-0 hover:opacity-100 transition-opacity duration-300 bg-gradient-to-br from-[rgba(255,80,80,0.25)] to-[rgba(255,120,120,0.35)]"
    ></span>

    <span class="relative z-10 flex items-center gap-2">
      <span class="material-symbols-outlined text-[16px]">logout</span>
      Log out
    </span>
  </Button>
</template>
