<script setup>
defineProps({
  type: { type: String, default: 'button' },
  variant: { type: String, default: 'primary' },
  fullWidth: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})
</script>

<template>
  <button
    :type="type"
    :disabled="disabled"
    :class="[
      'flex items-center justify-center rounded-lg h-12 px-4 py-2 text-base font-semibold transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-offset-2',
      fullWidth ? 'w-full' : '',
      variant === 'primary' ? 'bg-primary text-white hover:bg-primary/90 focus:ring-primary focus:ring-offset-slate-100 dark:focus:ring-offset-slate-900' : ''
    ]"
  >
    <slot />
  </button>
</template>
