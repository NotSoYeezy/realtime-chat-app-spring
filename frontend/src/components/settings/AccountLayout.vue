<script setup>
import { ref, onMounted } from "vue";
import getUser from "@/api/getUser.js";

const user = ref(null);
const loading = ref(true);
const error = ref(null);

const fetchUser = async () => {
  loading.value = true;
  error.value = null;

  try {
    const idResponse = await getUser.getMyId();
    const myId = idResponse.data;

    console.log("My User ID is:", myId);

    if (!myId) {
      throw new Error("Backend returned no ID");
    }
    const profileResponse = await getUser.getUser(myId);
    user.value = profileResponse.data;

  } catch (err) {
    console.error("Failed to load profile:", err);
    error.value = "Could not load user data.";
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchUser();
});

defineEmits(['updateProfile'])
</script>

<template>
  <div class="space-y-6 animate-in fade-in duration-300">
    <div>
      <h3 class="text-lg font-semibold mb-1">Account Information</h3>
      <p class="text-sm text-[var(--color-text-secondary)] mb-4">
        Manage your personal details and preferences.
      </p>

      <div v-if="loading" class="p-5 text-sm text-[var(--color-text-secondary)]">
        Loading profile data...
      </div>

      <div v-else-if="error" class="p-5 text-red-500 text-sm">
        {{ error }}
      </div>

      <div v-else-if="user" class="p-5 bg-[var(--surface-panel-strong)] rounded-lg border border-[var(--color-border)] space-y-4">
        <div>
          <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] font-bold mb-1">Email</label>
          <div class="text-lg font-medium">{{ user.email }}</div>
        </div>

        <div>
          <label class="block text-xs uppercase tracking-wider text-[var(--color-text-secondary)] font-bold mb-1">Username</label>
          <div class="text-lg font-medium">{{ user.username }}</div>
        </div>

        <div class="pt-2">
          <button @click="$emit('updateProfile')" class="px-4 py-2 bg-[var(--color-primary)]/10 text-[var(--color-primary)] rounded-md text-sm font-medium hover:bg-[var(--color-primary)]/20 transition-colors">
            Update Profile
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
