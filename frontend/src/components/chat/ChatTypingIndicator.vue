<script setup>
defineProps({
  users: Set,
  compact: { type: Boolean, default: false }
})
</script>

<template>
  <div v-if="users.size > 0"
       :class="['flex items-center gap-1 text-xs text-[var(--color-primary)]',
                compact ? '' : 'px-2 min-h-[1.5rem] italic']">

    <span class="flex space-x-0.5 mr-1">
      <span class="w-1 h-1 bg-[var(--color-primary)] rounded-full animate-bounce" style="animation-delay:0ms"></span>
      <span class="w-1 h-1 bg-[var(--color-primary)] rounded-full animate-bounce" style="animation-delay:150ms"></span>
      <span class="w-1 h-1 bg-[var(--color-primary)] rounded-full animate-bounce" style="animation-delay:300ms"></span>
    </span>

    <span class="font-medium">
      {{ Array.from(users).join(', ') }} typing&#8230;
    </span>

  </div>
</template>
