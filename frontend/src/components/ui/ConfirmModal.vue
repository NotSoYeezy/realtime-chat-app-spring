<script setup>
import BaseButton from '@/components/ui/BaseButton.vue'

defineProps({
  title: { type: String, default: 'Are you sure?' },
  message: { type: String, default: 'This action cannot be undone.' },
  confirmText: { type: String, default: 'Confirm' },
  cancelText: { type: String, default: 'Cancel' },
  danger: { type: Boolean, default: false },
  loading: { type: Boolean, default: false }
})

defineEmits(['close', 'confirm'])
</script>

<template>
  <div class="fixed inset-0 z-[100] flex items-center justify-center p-4">
    <div
      class="absolute inset-0 bg-black/50 backdrop-blur-sm transition-opacity"
      @click="$emit('close')"
    ></div>

    <div class="relative w-full max-w-sm bg-[var(--surface-panel)] rounded-xl shadow-2xl border border-[var(--color-border)] overflow-hidden animate-in zoom-in-95 duration-200">

      <div class="p-6 text-center">
        <div
          class="mx-auto flex h-12 w-12 items-center justify-center rounded-full mb-4"
          :class="danger ? 'bg-red-100 text-red-600' : 'bg-[var(--color-primary-bg-soft)] text-[var(--color-primary)]'"
        >
          <span class="material-symbols-outlined text-2xl">
            {{ danger ? 'warning' : 'help' }}
          </span>
        </div>

        <h3 class="text-lg font-bold text-[var(--color-text-primary)] mb-2">
          {{ title }}
        </h3>

        <p class="text-sm text-[var(--color-text-secondary)] leading-relaxed">
          {{ message }}
        </p>
      </div>

      <div class="p-4 bg-[var(--surface-panel-strong)]/50 border-t border-[var(--color-border)] flex gap-3">
        <button
          @click="$emit('close')"
          class="flex-1 px-4 py-2 text-sm font-medium rounded-lg border border-[var(--color-border)] bg-[var(--surface-panel)] text-[var(--color-text-primary)] hover:bg-[var(--surface-hover)] transition-colors"
          :disabled="loading"
        >
          {{ cancelText }}
        </button>

        <button
          @click="$emit('confirm')"
          class="flex-1 px-4 py-2 text-sm font-bold rounded-lg text-white shadow-sm transition-all hover:brightness-110 disabled:opacity-50 disabled:cursor-not-allowed"
          :class="danger ? 'bg-red-500 hover:bg-red-600' : 'bg-[var(--color-primary)]'"
          :disabled="loading"
        >
          {{ loading ? 'Processing...' : confirmText }}
        </button>
      </div>

    </div>
  </div>
</template>
