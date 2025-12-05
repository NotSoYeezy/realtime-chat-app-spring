<script setup>
defineProps({
  type: { type: String, default: 'button' },
  variant: { type: String, default: 'primary' },
  fullWidth: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
})
</script>

<template>
  <button
    :type="type"
    :disabled="disabled"
    :class="[
      'flex items-center justify-center rounded-lg h-12 px-4 py-2 text-base font-semibold transition-all duration-200 ease-in-out focus:outline-none focus:ring-2',
      fullWidth ? 'w-full' : '',
      variant === 'primary' &&
        'bg-[var(--color-primary)] text-[var(--color-text-on-primary)] ' +
          'hover:brightness-[1.12] hover:shadow-[0_0_12px_var(--color-primary-glow)] ' +
          'focus:ring-[var(--color-primary)] focus:ring-offset-2 ' +
          'ring-offset-[var(--surface-panel)]',
      disabled && 'opacity-50 cursor-not-allowed hover:none',
    ]"
  >
    <slot />
  </button>
</template>
