<script setup>
defineProps({
  modelValue: String,
  label: String,
  type: { type: String, default: 'text' },
  placeholder: String,
  required: { type: Boolean, default: false }
})

defineEmits(['update:modelValue'])
</script>

<template>
  <label class="flex flex-col w-full">

    <!-- LABEL -->
    <p class="text-sm font-medium leading-normal pb-2 text-[var(--color-text-secondary)]">
      {{ label }}
    </p>

    <!-- INPUT -->
    <input
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      :type="type"
      :placeholder="placeholder"
      :required="required"
      class="flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg
             h-12 p-3 text-base font-normal leading-normal
             bg-[var(--color-bg-input)]
             text-[var(--color-text-primary)]
             placeholder:text-[var(--color-text-secondary)]
             border border-[var(--color-border)]
             focus:outline-none
             focus:ring-2 focus:ring-[var(--color-primary)] focus:ring-opacity-50"
    />
  </label>
</template>
