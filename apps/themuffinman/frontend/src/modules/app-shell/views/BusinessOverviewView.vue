<script setup lang="ts">
import {onMounted, ref} from "vue"
import {RouterLink} from "vue-router"
import type {BusinessProfileResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppButton from "../components/AppButton.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"

const businesses = ref<BusinessProfileResponseDTO[]>([])
const isLoading = ref(true)
const error = ref("")
const load = async () => {
  isLoading.value = true
  error.value = ""
  try { businesses.value = await userShellApi.getMyBusinessProfiles() }
  catch { error.value = "Could not load your businesses." }
  finally { isLoading.value = false }
}
onMounted(() => void load())
</script>

<template>
  <section class="business-overview" aria-label="My businesses">
    <CollectionToolbar title="My businesses" :count="businesses.length" :busy="isLoading"><template #actions><AppButton type="button" tone="primary" @click="$router.push({path: '/business/profile'})">Add business</AppButton></template></CollectionToolbar>
    <AppStatus v-if="isLoading" message="Loading your businesses." busy />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" />
    <AppStatus v-else-if="!businesses.length" message="You do not have a business yet. Create your first one to get started." />
    <section v-else class="business-overview__list" aria-label="Owned businesses">
      <SurfaceRow v-for="business in businesses" :key="business.id" :row="{id: String(business.id), title: business.businessName, description: business.headline || 'Business workspace', badge: business.active ? 'Active' : 'Archived', to: {path: '/business/profile', query: {businessId: String(business.id)}}}" />
    </section>
    <RouterLink v-if="businesses.length" class="business-overview__manage" to="/business/profile">Open current business workspace</RouterLink>
  </section>
</template>

<style scoped>
.business-overview{display:grid;gap:var(--space-4);min-width:0}.business-overview__list{display:grid;overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.business-overview__list :deep(.surface-row:last-child){border-bottom:0}.business-overview__manage{color:var(--text-muted);font-size:var(--text-size-meta)}
</style>
