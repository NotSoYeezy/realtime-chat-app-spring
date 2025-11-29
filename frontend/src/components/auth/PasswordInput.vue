<script setup>
import { ref } from 'vue'

const props = defineProps({
  modelValue: String,
  label: String,
  placeholder: String,
  required: { type: Boolean, default: false }
})

defineEmits(['update:modelValue'])

const showPassword = ref(false)

const toggleVisibility = () => {
  showPassword.value = !showPassword.value
}
</script>

<template>
  <label class="flex flex-col w-full">
    <p class="text-slate-600 dark:text-slate-300 text-sm font-medium leading-normal pb-2">
      {{ label }}
    </p>
    <div class="relative flex w-full flex-1 items-stretch">
      <input
        :value="modelValue"
        @input="$emit('update:modelValue', $event.target.value)"
        :type="showPassword ? 'text' : 'password'"
        :placeholder="placeholder"
        :required="required"
        class="flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg
        text-slate-800 dark:text-white focus:outline-none focus:ring-2
        focus:ring-primary focus:ring-opacity-50 border border-slate-300
        dark:border-slate-600 bg-slate-200 dark:bg-slate-800 h-12
        placeholder:text-slate-400 dark:placeholder:text-slate-500 p-3 pr-10
        text-base font-normal leading-normal"
      />
      <div
        @click="toggleVisibility"
        class="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 dark:text-slate-500 cursor-pointer"
      >
        <span class="material-symbols-outlined text-xl">
          {{ showPassword ? 'visibility_off' : 'visibility' }}
        </span>
      </div>
    </div>
  </label>
</template>
