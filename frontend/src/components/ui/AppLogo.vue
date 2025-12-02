<script setup>
defineProps({
  size: { type: String, default: 'md' }
})

const sizes = {
  sm: { icon: 'h-8 w-8', text: 'text-lg' },
  md: { icon: 'h-10 w-10', text: 'text-xl' },
  lg: { icon: 'h-12 w-12', text: 'text-2xl' }
}
</script>

<template>
  <div class="flex items-center gap-2">
    <div
      :class="[
        sizes[size].icon,
        'flex items-center justify-center rounded-lg',
        'bg-[var(--color-primary)]'
      ]"
    >
      <span class="material-symbols-outlined text-2xl text-[#ffffff]">
        connect_without_contact
      </span>
    </div>

    <span
      :class="[
        sizes[size].text,
        'font-bold text-[var(--color-text-primary)]'
      ]"
    >
      ConnectApp
    </span>
  </div>
</template>
