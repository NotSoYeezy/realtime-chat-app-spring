<script setup>
import SettingsTab from '@/components/settings/SettingsTab.vue'
import SettingsOption from '@/components/settings/SettingsOption.vue'
import ThemeToggle from '@/components/ui/ThemeToggle.vue'
</script>

<template>
  <SettingsTab
    title="Display Options"
    description="Customize how the application looks on your device."
  >
    <SettingsOption label="Dark Mode" description="Switch between light and dark themes">
      <ThemeToggle />
    </SettingsOption>
  </SettingsTab>
</template>
