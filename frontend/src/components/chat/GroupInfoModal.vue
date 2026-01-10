<script setup>
import { ref, computed, watch } from 'vue'
import { useChatStore } from '@/stores/chat'
import api from '@/api/axios'
import ConfirmModal from '@/components/ui/ConfirmModal.vue'
import ImageViewerModal from "@/components/chat/ImageViewerModal.vue";
import ColorThemeApi from "@/api/colorThemeApi.js";

const props = defineProps({
  activeGroup: { type: Object, required: true },
  currentUser: String
})

const emit = defineEmits(['close', 'openAddMember'])
const chatStore = useChatStore()

const isEditingName = ref(false)
const newGroupName = ref(props.activeGroup.name)
const isRenaming = ref(false)
const fileInput = ref(null)
const isUploadingImage = ref(false)
const showConfirm = ref(false)
const confirmConfig = ref({ title: '', message: '', danger: false, action: null })
const confirmLoading = ref(false)
const showMedia = ref(false)
const activeTab = ref('images')
const mediaImages = ref([])
const mediaFiles = ref([])
const loadingMedia = ref(false)
const showImageViewer = ref(false)
const selectedImage = ref('')

const themeColors = [
  '#3B82F6',
  '#8B5CF6',
  '#10B981',
  '#F59E0B',
  '#EF4444',
  '#EC4899',
  '#6366F1',
  '#14B8A6',
]

const isGroupChat = computed(() => props.activeGroup?.groupType === 'GROUP')
const amIAdmin = computed(() => {
  if (!props.activeGroup || !props.activeGroup.admins) return false
  return props.activeGroup.admins.some(admin => admin.username === props.currentUser)
})

const isUserAdmin = (member) => {
  if (!props.activeGroup.admins) return false
  return props.activeGroup.admins.some(admin => admin.id === member.id)
}

const getFullUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http') || path.startsWith('data:')) return path
  const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  const cleanPath = path.startsWith('/') ? path : `/${path}`
  return `${baseUrl}${cleanPath}`
}

const getFileName = (path) => {
  if (!path) return 'File'
  try {
    const filenameWithUuid = path.split('/').pop()
    const parts = filenameWithUuid.split('_')
    if (parts.length < 2) return filenameWithUuid
    return parts.slice(1).join('_')
  } catch (e) {
    return 'Attachment'
  }
}

const handleUpdateTheme = async (color) => {
  if (chatStore.activeGroup) {
    chatStore.activeGroup.colorTheme = color
  }

  try {
    await ColorThemeApi.setColorTheme(props.activeGroup.id, color)

    await chatStore.refreshGroup(props.activeGroup.id)
  } catch (e) {
    console.error("Failed to update theme:", e)
  }
}

const openImageViewer = (imgUrl) => {
  selectedImage.value = getFullUrl(imgUrl)
  showImageViewer.value = true
}

const triggerFileInput = () => {
  if (!isGroupChat.value || !amIAdmin.value) return
  fileInput.value.click()
}

const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  isUploadingImage.value = true
  try {
    const formData = new FormData()
    formData.append('data', new Blob([JSON.stringify({})], { type: 'application/json' }))
    formData.append('image', file)
    await api.patch(`/groups/${props.activeGroup.id}`, formData, { headers: { 'Content-Type': undefined } })
    await chatStore.refreshGroup(props.activeGroup.id)
  } catch (e) {
    console.error("Failed to upload group image:", e)
  } finally {
    isUploadingImage.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

const fetchMedia = async (type) => {
  loadingMedia.value = true
  try {
    const endpoint = type === 'images'
      ? `/messages/group/${props.activeGroup.id}/images`
      : `/messages/group/${props.activeGroup.id}/files`

    const response = await api.get(endpoint)

    if (type === 'images') {
      mediaImages.value = response.data || []
    } else {
      mediaFiles.value = response.data || []
    }
  } catch (e) {
    console.error(`Failed to fetch group ${type}`, e)
    if (type === 'images') mediaImages.value = []
    else mediaFiles.value = []
  } finally {
    loadingMedia.value = false
  }
}

watch(activeTab, (newTab) => {
  if (showMedia.value) fetchMedia(newTab)
})

const handleShowMedia = () => {
  showMedia.value = true
  activeTab.value = 'images'
  fetchMedia('images')
}

const handlePromoteAdmin = async (member) => {
  try { await chatStore.addAdmin(member.id) } catch (e) { console.error(e) }
}
const handleDemoteAdmin = async (member) => {
  try { await chatStore.removeAdmin(member.id) } catch (e) { console.error(e) }
}

const toggleMute = async () => {
  if (props.activeGroup.muted) {
    await chatStore.unmuteGroup(props.activeGroup.id)
  } else {
    await chatStore.muteGroup(props.activeGroup.id)
  }
}

const openConfirm = (title, message, danger, actionCallback) => {
  confirmConfig.value = { title, message, danger, action: actionCallback }
  showConfirm.value = true
}

const handleConfirmAction = async () => {
  if (!confirmConfig.value.action) return
  confirmLoading.value = true
  try {
    await confirmConfig.value.action()
  } catch (e) {
    console.error(e)
  } finally {
    confirmLoading.value = false
    showConfirm.value = false
  }
}

const handleUpdateName = async () => {
  if (!newGroupName.value.trim() || newGroupName.value === props.activeGroup.name) {
    isEditingName.value = false
    return
  }
  isRenaming.value = true
  try {
    const formData = new FormData()
    formData.append('data', new Blob([JSON.stringify({ name: newGroupName.value })], { type: 'application/json' }))
    await api.patch(`/groups/${props.activeGroup.id}`, formData, { headers: { 'Content-Type': undefined } })
    await chatStore.refreshGroup(props.activeGroup.id)
    isEditingName.value = false
  } catch (e) {
    console.error("Failed to rename group:", e)
  } finally {
    isRenaming.value = false
  }
}

const removeMember = (userId) => {
  openConfirm(
    'Remove Member?',
    'Are you sure you want to remove this person from the group?',
    true,
    async () => await chatStore.removeMembersFromGroup([userId])
  )
}

const leaveGroup = () => {
  openConfirm(
    'Leave Group?',
    'Are you sure you want to leave this conversation? You will need to be re-added to join again.',
    true,
    async () => {
      const groupId = props.activeGroup.id
      const myId = props.activeGroup.members.find(m => m.username === props.currentUser)?.id
      if (myId) {
        await chatStore.removeMembersFromGroup([myId])
        emit('close')
        chatStore.removeGroup(groupId)
      }
    }
  )
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="$emit('close')"></div>

    <div
      v-if="activeGroup"
      class="relative w-full max-w-md bg-[var(--surface-panel)] rounded-xl shadow-2xl border border-[var(--color-border)] overflow-hidden flex flex-col max-h-[85vh] animate-in zoom-in-95 duration-200"
    >
      <div class="p-4 border-b border-[var(--color-border)] flex justify-between items-center">
        <div class="flex items-center gap-2 text-[var(--color-text-primary)]">
          <button v-if="showMedia" @click="showMedia = false" class="mr-1 hover:bg-[var(--surface-panel-strong)] rounded-full p-1">
            <span class="material-symbols-outlined">arrow_back</span>
          </button>
          <span class="material-symbols-outlined text-[24px]">{{ showMedia ? 'perm_media' : 'settings' }}</span>
          <h3 class="font-bold text-lg">{{ showMedia ? "Shared Media" : (activeGroup.groupType == "PRIVATE" ? "Chat Settings" : "Group Settings") }}</h3>
        </div>
        <button @click="$emit('close')" class="text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)]">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>

      <div class="overflow-y-auto p-6 space-y-6">

        <div v-if="!showMedia" class="space-y-6">

          <div class="text-center space-y-3 flex flex-col items-center">
            <input type="file" ref="fileInput" accept="image/*" class="hidden" @change="handleFileChange" />
            <div class="h-20 w-20 relative group/avatar rounded-full overflow-hidden border border-[var(--color-border)]" :class="(isGroupChat && amIAdmin) ? 'cursor-pointer hover:opacity-90' : ''" @click="triggerFileInput">
              <img v-if="activeGroup?.imageUrl" :src="getFullUrl(activeGroup.imageUrl)" class="h-full w-full object-cover" />
              <div v-else class="h-full w-full flex items-center justify-center font-bold text-[var(--color-primary)] bg-[var(--color-primary-bg-soft)]">
                <span class="material-symbols-outlined text-4xl">{{ isGroupChat ? 'groups' : 'person' }}</span>
              </div>
              <div v-if="isGroupChat && amIAdmin" class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover/avatar:opacity-100 transition-opacity">
                <span v-if="!isUploadingImage" class="material-symbols-outlined text-white">photo_camera</span>
                <span v-else class="material-symbols-outlined text-white animate-spin">sync</span>
              </div>
            </div>

            <div v-if="!isEditingName" class="flex items-center justify-center gap-2 group/name">
              <h2 class="text-xl font-bold text-[var(--color-text-primary)] break-all">{{ activeGroup.name }}</h2>
              <button v-if="isGroupChat && amIAdmin" @click="isEditingName = true; newGroupName = activeGroup.name" class="opacity-0 group-hover/name:opacity-100 text-[var(--color-text-secondary)] hover:text-[var(--color-primary)] transition-opacity p-1">
                <span class="material-symbols-outlined text-lg">edit</span>
              </button>
            </div>
            <div v-else class="flex flex-col gap-2 items-center w-full">
              <input v-model="newGroupName" class="w-full text-center px-3 py-2 bg-[var(--surface-panel-strong)] border border-[var(--color-border)] rounded-lg font-bold" @keyup.enter="handleUpdateName" autoFocus />
              <div class="flex gap-2 text-sm">
                <button @click="handleUpdateName" :disabled="isRenaming" class="text-[var(--color-primary)] font-semibold hover:underline">{{ isRenaming ? 'Saving...' : 'Save' }}</button>
                <span class="text-[var(--color-border)]">|</span>
                <button @click="isEditingName = false" class="text-[var(--color-text-secondary)]">Cancel</button>
              </div>
            </div>
          </div>

          <div class="flex justify-center gap-3">
            <button @click="toggleMute" class="flex items-center gap-2 px-4 py-2 rounded-full border transition-colors text-sm font-medium" :class="activeGroup.muted ? 'bg-[var(--color-primary-bg-soft)] text-[var(--color-primary)] border-[var(--color-primary)]' : 'bg-[var(--surface-panel-strong)] text-[var(--color-text-secondary)] border-transparent hover:text-[var(--color-text-primary)]'">
              <span class="material-symbols-outlined text-[18px]">{{ activeGroup.muted ? 'notifications_off' : 'notifications_active' }}</span>
              {{ activeGroup.muted ? 'Muted' : 'Mute' }}
            </button>
            <button @click="handleShowMedia" class="flex items-center gap-2 px-4 py-2 rounded-full border border-transparent bg-[var(--surface-panel-strong)] text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] transition-colors text-sm font-medium">
              <span class="material-symbols-outlined text-[18px]">perm_media</span>
              Media & Files
            </button>
          </div>

          <div class="space-y-3">
            <label class="text-xs font-bold uppercase text-[var(--color-text-secondary)] tracking-wider">Chat Theme</label>
            <div class="bg-[var(--surface-panel-strong)]/50 p-4 rounded-xl border border-[var(--color-border)]">
              <div class="flex flex-wrap gap-3 justify-center">
                <button
                  v-for="color in themeColors"
                  :key="color"
                  @click="handleUpdateTheme(color)"
                  class="w-8 h-8 rounded-full transition-all duration-200 hover:scale-110 focus:outline-none ring-2 ring-offset-2 ring-offset-[var(--surface-panel)]"
                  :class="activeGroup.colorTheme === color ? 'scale-110 shadow-lg' : 'ring-transparent hover:ring-black/10'"
                  :style="{
                    backgroundColor: color,
                    '--tw-ring-color': activeGroup.colorTheme === color ? color : 'transparent'
                  }"
                  :title="color"
                ></button>

              </div>
            </div>
          </div>

          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <label class="text-xs font-bold uppercase text-[var(--color-text-secondary)] tracking-wider">Members ({{ activeGroup.members?.length || 0 }})</label>
              <button v-if="isGroupChat && amIAdmin" @click="$emit('openAddMember')" class="text-xs font-semibold text-[var(--color-primary)] hover:underline flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px]">add</span> Add People
              </button>
            </div>
            <div class="bg-[var(--surface-panel-strong)]/50 rounded-xl border border-[var(--color-border)] divide-y divide-[var(--color-border)] overflow-hidden">
              <div v-for="member in activeGroup.members" :key="member.id" class="flex items-center gap-3 p-3 hover:bg-[var(--surface-panel-strong)] transition-colors group">
                <div class="h-9 w-9 shrink-0 rounded-full bg-[var(--surface-panel)] flex items-center justify-center text-sm font-bold border border-[var(--color-border)] relative">
                  {{ member.name?.charAt(0) }}
                  <div v-if="isUserAdmin(member)" class="absolute -top-1 -right-1 text-yellow-500 bg-[var(--surface-panel)] rounded-full">
                    <span class="material-symbols-outlined text-[12px] font-bold">crown</span>
                  </div>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-[var(--color-text-primary)] truncate">{{ member.name }} {{ member.surname }}</p>
                  <p class="text-xs text-[var(--color-text-secondary)] truncate">@{{ member.username }}</p>
                </div>
                <div v-if="isGroupChat && amIAdmin && member.username !== currentUser" class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button v-if="!isUserAdmin(member)" @click="handlePromoteAdmin(member)" class="p-1.5 rounded-md hover:text-[var(--color-primary)]">
                    <span class="material-symbols-outlined text-[18px]">security</span>
                  </button>
                  <button v-else @click="handleDemoteAdmin(member)" class="p-1.5 rounded-md hover:text-orange-600">
                    <span class="material-symbols-outlined text-[18px]">person_remove</span>
                  </button>
                  <button @click="removeMember(member.id)" class="p-1.5 rounded-md hover:text-red-600">
                    <span class="material-symbols-outlined text-[18px]">remove_circle</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="isGroupChat" class="pt-2">
            <button @click="leaveGroup" class="w-full flex items-center justify-center gap-2 py-3 rounded-lg text-red-500 hover:bg-red-50 font-medium transition-colors border border-transparent hover:border-red-100">
              <span class="material-symbols-outlined">logout</span> Leave Group
            </button>
          </div>
        </div>

        <div v-else class="space-y-4 animate-in slide-in-from-right-4 duration-200">
          <div class="flex border-b border-[var(--color-border)] mb-4">
            <button @click="activeTab = 'images'" class="flex-1 pb-3 text-sm font-medium transition-colors relative" :class="activeTab === 'images' ? 'text-[var(--color-primary)]' : 'text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)]'">
              Photos
              <div v-if="activeTab === 'images'" class="absolute bottom-0 left-0 right-0 h-0.5 bg-[var(--color-primary)]"></div>
            </button>
            <button @click="activeTab = 'files'" class="flex-1 pb-3 text-sm font-medium transition-colors relative" :class="activeTab === 'files' ? 'text-[var(--color-primary)]' : 'text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)]'">
              Files
              <div v-if="activeTab === 'files'" class="absolute bottom-0 left-0 right-0 h-0.5 bg-[var(--color-primary)]"></div>
            </button>
          </div>

          <div v-if="loadingMedia" class="flex justify-center py-12">
            <span class="material-symbols-outlined animate-spin text-[var(--color-text-secondary)] text-3xl">sync</span>
          </div>

          <div v-else-if="activeTab === 'images'">
            <div v-if="mediaImages.length === 0" class="text-center py-12 text-[var(--color-text-secondary)]">
              <span class="material-symbols-outlined text-4xl opacity-50 mb-2">image_not_supported</span>
              <p>No photos shared yet.</p>
            </div>
            <div v-else class="grid grid-cols-3 gap-1">
              <div v-for="(img, index) in mediaImages" :key="index" class="aspect-square relative cursor-pointer hover:opacity-90 overflow-hidden bg-black/10 rounded-md" @click="openImageViewer(img.url || img)">
                <img :src="getFullUrl(img.url || img)" class="w-full h-full object-cover" loading="lazy" />
              </div>
            </div>
          </div>

          <div v-else-if="activeTab === 'files'">
            <div v-if="mediaFiles.length === 0" class="text-center py-12 text-[var(--color-text-secondary)]">
              <span class="material-symbols-outlined text-4xl opacity-50 mb-2">folder_off</span>
              <p>No files shared yet.</p>
            </div>
            <div v-else class="space-y-2">
              <a v-for="(fileUrl, index) in mediaFiles" :key="index" :href="getFullUrl(fileUrl)" target="_blank" download class="flex items-center gap-3 p-3 rounded-lg bg-[var(--surface-panel-strong)] hover:bg-[var(--color-bg-input)] transition-colors group">
                <div class="h-10 w-10 rounded-lg bg-[var(--color-primary-bg-soft)] text-[var(--color-primary)] flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined">description</span>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-[var(--color-text-primary)] truncate">{{ getFileName(fileUrl) }}</p>
                  <p class="text-xs text-[var(--color-text-secondary)]">Document</p>
                </div>
                <span class="material-symbols-outlined text-[var(--color-text-secondary)] group-hover:text-[var(--color-primary)]">download</span>
              </a>
            </div>
          </div>
        </div>

      </div>
    </div>

    <Teleport to="body">
      <ConfirmModal v-if="showConfirm" :title="confirmConfig.title" :message="confirmConfig.message" :danger="confirmConfig.danger" :loading="confirmLoading" @close="showConfirm = false" @confirm="handleConfirmAction" />
      <ImageViewerModal v-if="showImageViewer" :image-url="selectedImage" @close="showImageViewer = false" />
    </Teleport>
  </div>
</template>
