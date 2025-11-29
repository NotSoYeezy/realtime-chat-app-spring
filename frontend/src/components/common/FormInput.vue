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
    <p class="text-slate-600 dark:text-slate-300 text-sm font-medium leading-normal pb-2">
      {{ label }}
    </p>
    <input
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      :type="type"
      :placeholder="placeholder"
      :required="required"
      class="flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-lg text-slate-800
      dark:text-white focus:outline-none focus:ring-2 focus:ring-primary focus:ring-opacity-50 border
      border-slate-300 dark:border-slate-600 bg-slate-200 dark:bg-slate-800 h-12
      placeholder:text-slate-400 dark:placeholder:text-slate-500 p-3 text-base font-normal
      leading-normal"
    />
  </label>
</template>

<style scoped>

</style>
