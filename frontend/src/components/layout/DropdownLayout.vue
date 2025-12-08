<script setup>

const emit = defineEmits(['logout', 'closeDropdown'])

const handleSettings = () => {
  emit('closeDropdown')
}

const handleLogout = () => {
  emit('logout')
  emit('closeDropdown')
}

</script>


<template>
  <div class="py-1">
    <button
      class="w-full text-left px-4 py-2 text-sm text-[var(--color-text-primary)] hover:bg-[var(--surface-panel-strong)] transition-colors duration-150 rounded-none flex items-center gap-2"
      @click="handleSettings"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
        <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
      </svg>
      Settings
    </button>
    <div class="h-px mx-2 my-1 bg-[var(--color-border)]"></div>
    <button
      class="w-full text-left px-4 py-2 text-sm font-semibold
             text-red-500 hover:bg-red-50 hover:text-red-600
             transition-colors duration-150 flex items-center gap-2"
      @click="handleLogout"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
      </svg>
      Log out
    </button>
  </div>
</template>
