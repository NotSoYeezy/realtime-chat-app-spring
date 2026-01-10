<script setup>
import { onMounted, onUnmounted } from 'vue'

const props = defineProps({
  imageUrl: String
})

const emit = defineEmits(['close'])

const handleKeydown = (e) => {
  if (e.key === 'Escape') emit('close')
}

onMounted(() => window.addEventListener('keydown', handleKeydown))
onUnmounted(() => window.removeEventListener('keydown', handleKeydown))
</script>

<template>
  <div
    class="fixed inset-0 z-[100] flex items-center justify-center bg-black/90 backdrop-blur-sm animate-in fade-in duration-200"
    @click.self="$emit('close')"
  >
    <button
      class="absolute top-4 right-4 p-2 text-white/70 hover:text-white bg-black/50 rounded-full hover:bg-white/20 transition-all"
      @click="$emit('close')"
    >
      <span class="material-symbols-outlined text-3xl">close</span>
    </button>

    <img
      :src="imageUrl"
      class="max-w-[90vw] max-h-[90vh] object-contain rounded shadow-2xl animate-in zoom-in-95 duration-300 select-none"
      alt="Full size preview"
    />
  </div>
</template>
