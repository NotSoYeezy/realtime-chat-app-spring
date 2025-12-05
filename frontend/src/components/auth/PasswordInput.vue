<script setup>
import { ref } from 'vue'

defineProps({
  modelValue: String,
  label: String,
  placeholder: String,
  required: { type: Boolean, default: false }
})

defineEmits(['update:modelValue'])

// MISSING PART — added now:
const showPassword = ref(false)

function toggleVisibility() {
  showPassword.value = !showPassword.value
}
</script>

<template>
  <label class="flex flex-col w-full">

    <!-- LABEL -->
    <p class="text-sm font-medium leading-normal pb-2 text-[var(--color-text-secondary)]">
      {{ label }}
    </p>

    <div class="relative flex w-full items-stretch">

      <!-- INPUT -->
      <input
        :value="modelValue"
        @input="$emit('update:modelValue', $event.target.value)"
        :type="showPassword ? 'text' : 'password'"
        :placeholder="placeholder"
        :required="required"
        class="flex w-full rounded-lg bg-[var(--color-bg-input)]
               text-[var(--color-text-primary)]
               placeholder:text-[var(--color-text-secondary)]
               border border-[var(--color-border)]
               focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]
               h-12 p-3 pr-10 text-base"
      />

      <!-- ICON -->
      <button
        type="button"
        @click="toggleVisibility"
        class="absolute inset-y-0 right-0 flex items-center pr-3
               text-[var(--color-text-secondary)]
               hover:text-[var(--color-primary)]
               cursor-pointer"
      >
        <span class="material-symbols-outlined text-xl">
          {{ showPassword ? 'visibility_off' : 'visibility' }}
        </span>
      </button>

    </div>
  </label>
</template>

<style scoped>
/* hide default eye */
input[type="password"]::-ms-reveal,
input[type="password"]::-ms-clear {
  display: none !important;
}
</style>
