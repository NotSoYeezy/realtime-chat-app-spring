<script setup>
import { ref, computed } from 'vue'
import AccountLayout from "@/components/settings/layout/AccountLayout.vue";
import GeneralLayout from "@/components/settings/layout/GeneralLayout.vue";

defineEmits(['close', 'logout']);
defineProps({
  currentUser: String
})

const activeTab = ref('general')

const tabs = [
  { id: 'general', label: 'General', component: GeneralLayout },
  { id: 'account', label: 'Account', component: AccountLayout }
]
const currentComponent = computed(() => {
  const tab = tabs.find(t => t.id === activeTab.value)
  return tab ? tab.component : GeneralLayout
})
</script>

<template>
  <div
    class="fixed inset-0 z-50 flex items-center justify-center
         bg-black/10 backdrop-blur-sm"
    @click.self="$emit('close')"
  >
    <div
      class="bg-[var(--surface-panel)] rounded-xl shadow-2xl w-full max-w-4xl h-3/4
             flex flex-col text-[var(--color-text-primary)] overflow-hidden transition-all"
    >
      <header class="p-4 border-b border-[var(--color-border)] flex items-center justify-between shrink-0">
        <h2 class="text-xl font-bold">Settings</h2>
        <button
          @click="$emit('close')"
          class="text-[var(--color-text-secondary)] hover:text-red-500 transition-colors"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </header>
      <div class="flex-1 flex overflow-hidden">
        <aside class="w-56 bg-[var(--surface-panel-strong)]/20 border-r border-[var(--color-border)] flex flex-col p-3 gap-1">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="activeTab = tab.id"
            :class="[
              'px-4 py-2.5 rounded-lg text-sm font-medium text-left transition-all',
              activeTab === tab.id
                ? 'bg-[var(--color-primary)] text-white shadow-md'
                : 'text-[var(--color-text-secondary)] hover:bg-[var(--surface-panel-strong)] hover:text-[var(--color-text-primary)]'
            ]"
          >
            {{ tab.label }}
          </button>
        </aside>
        <main class="flex-1 p-8 overflow-y-auto">
          <component
            :is="currentComponent"
            :currentUser="currentUser"
            @logout="$emit('logout')"
          />
        </main>
      </div>
    </div>
  </div>
</template>
