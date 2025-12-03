<script setup>
import { ref, watch, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import '@/assets/main.css'


const router = useRouter()
const authStore = useAuthStore()
const isDarkMode = ref(false);
const currentUser = ref(' ');
const currentLanguage = ref('Polski');


const loadThemePreference = () => {
  // Check localStorage first, otherwise respect the initial class on the <html> tag
  const savedPreference = localStorage.getItem('theme');
  if (savedPreference) {
    return savedPreference === 'dark';
  }
  return document.documentElement.classList.contains('dark');
};


const getCurrentUserFromToken = () => {
  const token = authStore.accessToken
  if (!token) return 'Anonymous'

  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))

    const payload = JSON.parse(jsonPayload)
    return payload.sub
  } catch (e) {
    console.error("Error decoding token", e)
    return 'Anonymous'
  }
}

const updateTheme = (isDark) => {
  if (isDark) {
    document.documentElement.classList.add('dark');
    document.documentElement.classList.remove('light');
    localStorage.setItem('theme', 'dark');
  } else {
    document.documentElement.classList.add('light');
    document.documentElement.classList.remove('dark');
    localStorage.setItem('theme', 'light');
  }
};

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value;
};

onMounted(async() => {
  isDarkMode.value = loadThemePreference();
  updateTheme(isDarkMode.value);
  currentUser.value = setCurrentUser();
});

watch(isDarkMode, updateTheme);

const saveChanges = () => {
  console.log('Zapisano zmiany!');
  console.log('Motyw:', isDarkMode.value ? 'Ciemny' : 'Jasny');
  console.log('Język:', currentLanguage.value);
};
</script>

<template>
  <div class="relative flex h-auto min-h-screen w-full flex-col font-display overflow-x-hidden">
    <div class="layout-container flex h-full grow">
      <div class="flex flex-1 justify-center p-4 sm:p-6 lg:p-8">
        <div class="layout-content-container grid w-full max-w-6xl grid-cols-1 gap-8 lg:grid-cols-[280px_1fr]">

          <aside class="flex h-full flex-col">
            <div class="flex flex-col gap-4">
              <div class="flex items-center gap-3">
                <div
                  class="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-10"
                  data-alt="User avatar"
                ></div>
                <div class="flex flex-col">
                  <h1 class="text-base font-medium leading-normal dark:text-gray-200">{{ currentUser }}</h1>
                  <p class="text-sm font-normal leading-normal dark:text-gray-400">{{ currentUser }}</p>
                </div>
              </div>

              <div class="flex flex-col gap-1 mt-4">
                <a class="flex items-center gap-3 px-3 py-2 rounded-lg bg-primary/20 dark:bg-primary/30" href="#">
                  <span class="material-symbols-outlined dark:text-gray-200 text-primary text-2xl">settings</span>
                  <p class="text-sm font-medium leading-normal dark:text-gray-200 text-primary">Ogólne</p>
                </a>
                <a class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-black/5 dark:hover:bg-white/5" href="#">
                  <span class="material-symbols-outlined dark:text-gray-200 text-2xl">person</span>
                  <p class="text-sm font-medium leading-normal dark:text-gray-200">Konto</p>
                </a>
                <a class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-black/5 dark:hover:bg-white/5" href="#">
                  <span class="material-symbols-outlined dark:text-gray-200 text-2xl">notifications</span>
                  <p class="text-sm font-medium leading-normal dark:text-gray-200">Powiadomienia</p>
                </a>
                <a class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-black/5 dark:hover:bg-white/5" href="#">
                  <span class="material-symbols-outlined dark:text-gray-200 text-2xl">lock</span>
                  <p class="text-sm font-medium leading-normal dark:text-gray-200">Prywatność</p>
                </a>
                <a class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-black/5 dark:hover:bg-white/5" href="#">
                  <span class="material-symbols-outlined dark:text-gray-200 text-2xl">help</span>
                  <p class="text-sm font-medium leading-normal dark:text-gray-200">Pomoc i Informacje</p>
                </a>
              </div>
            </div>
          </aside>

          <main class="flex w-full flex-col gap-6">

            <div class="flex min-w-72 flex-col gap-2">
              <p class="text-3xl font-black leading-tight tracking-[-0.03em] dark:text-gray-200">Ogólne</p>
              <p class="text-base font-normal leading-normal dark:text-gray-400">Dostosuj język, motyw i zachowanie aplikacji.</p>
            </div>

            <div class="flex flex-col gap-2 rounded-xl dark:text-gray-200 border border-border-light dark:border-border-dark bg-card-light dark:bg-card-dark divide-y divide-border-light dark:divide-border-dark">

              <div class="flex items-center gap-4 px-4 min-h-16 justify-between">
                <div class="flex items-center gap-4">
                  <div class="flex items-center justify-center rounded-lg bg-primary/20 dark:bg-primary/30 shrink-0 size-10">
                    <span class="material-symbols-outlined text-primary text-2xl dark:text-gray-200">language</span>
                  </div>
                  <p class="text-base font-normal leading-normal flex-1 truncate dark:text-gray-200">Język</p>
                </div>
                <div class="shrink-0">
                  <button class="text-base font-medium leading-normal dark:text-gray-400">
                    {{ currentLanguage }}
                  </button>
                </div>
              </div>

              <div class="flex items-center gap-4 px-4 min-h-[72px] py-2 justify-between">
                <div class="flex items-center gap-4">
                  <div class="flex items-center justify-center rounded-lg bg-primary/20 dark:bg-primary/30 shrink-0 size-12">
                    <span class="material-symbols-outlined text-primary text-2xl dark:text-gray-200">dark_mode</span>
                  </div>
                  <div class="flex flex-col justify-center">
                    <p class="text-base font-medium leading-normal line-clamp-1 dark:text-gray-200">Motyw</p>
                    <p class="text-sm font-normal leading-normal line-clamp-2 dark:text-gray-400">Wybierz jasny, ciemny lub systemowy motyw.</p>
                  </div>
                </div>

                <div class="shrink-0">
                  <label
                    class="relative flex h-[31px] w-[51px] cursor-pointer items-center rounded-full border-none p-0.5 transition-colors duration-300"
                    :class="[isDarkMode ? 'justify-end bg-primary' : 'justify-start bg-gray-200 dark:bg-slate-700']"
                    @click="toggleTheme"
                  >
                    <div class="h-full w-[27px] rounded-full bg-white transition-transform duration-300"
                         style="box-shadow: rgba(0, 0, 0, 0.15) 0px 3px 8px, rgba(0, 0, 0, 0.06) 0px 3px 1px;">
                    </div>
                    <input class="invisible absolute" type="checkbox" :checked="isDarkMode"/>
                  </label>
                </div>
              </div>
            </div>

            <div class="flex justify-end mt-4">
              <button
                @click="saveChanges"
                class="flex min-w-[120px] max-w-[480px] cursor-pointer items-center justify-center overflow-hidden rounded-lg h-10 px-6 bg-primary text-white text-sm font-bold leading-normal tracking-[0.015em] hover:bg-primary/90 transition-colors"
              >
                <span class="truncate">Zapisz zmiany</span>
              </button>
            </div>

          </main>
        </div>
      </div>
    </div>
  </div>
</template>


