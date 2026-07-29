<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {useRouter} from "vue-router"
import type {BusinessProfileRequestDTO, BusinessProfileResponseDTO} from "../../../contracts/index.ts"
import {setActiveBusinessProfileId, userShellApi} from "../api/userShellApi.ts"
import AppButton from "../components/AppButton.vue"
import AppDialog from "../components/AppDialog.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import ObjectPreviewPanel from "../components/ObjectPreviewPanel.vue"

const router = useRouter()
const businesses = ref<BusinessProfileResponseDTO[]>([])
const selectedBusinessId = ref<number | null>(null)
const isLoading = ref(true)
const error = ref("")
const createOpen = ref(false)
const isCreating = ref(false)
const createError = ref("")
const newBusiness = ref<Pick<BusinessProfileRequestDTO, "businessName" | "headline" | "publicAddressLabel" | "timezone">>({businessName: "", headline: "", publicAddressLabel: "", timezone: "Europe/Zurich"})
const selectedBusiness = computed(() => businesses.value.find((business) => business.id === selectedBusinessId.value) ?? null)
const openBusiness = () => {
  if (selectedBusiness.value) {
    void router.push({path: "/business/profile", query: {businessId: String(selectedBusiness.value.id)}})
  }
}
const load = async () => {
  isLoading.value = true
  error.value = ""
  try { businesses.value = await userShellApi.getMyBusinessProfiles() }
  catch { error.value = "Could not load your businesses." }
  finally { isLoading.value = false }
}
const createBusiness = async () => {
  const businessName = newBusiness.value.businessName.trim()
  if (!businessName) return
  isCreating.value = true
  createError.value = ""
  try {
    const created = await userShellApi.createBusinessProfile({
      businessName,
      slug: "",
      headline: newBusiness.value.headline.trim(),
      description: "",
      contactEmail: "",
      contactPhone: "",
      websiteUrl: "",
      timezone: newBusiness.value.timezone,
      bookingEnabled: false,
      publicAddressLabel: newBusiness.value.publicAddressLabel.trim(),
      latitude: null,
      longitude: null,
      contactWhatsapp: "",
      heroImageUrl: "",
      active: true
    })
    setActiveBusinessProfileId(created.id)
    await router.push({path: "/business/profile", query: {businessId: String(created.id)}})
  } catch {
    createError.value = "Could not create this business. Check the details and try again."
  } finally {
    isCreating.value = false
  }
}
onMounted(() => void load())
</script>

<template>
  <section class="business-overview" aria-label="My businesses">
    <CollectionToolbar title="My businesses" :count="businesses.length" :busy="isLoading"><template #actions><AppButton type="button" tone="primary" @click="createOpen = true">Add business</AppButton></template></CollectionToolbar>
    <AppStatus v-if="isLoading" message="Loading your businesses." busy />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" />
    <AppStatus v-else-if="!businesses.length" message="You do not have a business yet. Create your first one to get started." />
    <section v-else class="business-overview__workspace" :class="{'business-overview__workspace--preview': selectedBusiness}" aria-label="Owned businesses">
      <div class="business-overview__list">
        <SurfaceRow v-for="business in businesses" :key="business.id" :row="{id: String(business.id), title: business.businessName, description: business.headline || 'Business workspace', badge: business.active ? 'Active' : 'Archived', to: {path: '/business/profile', query: {businessId: String(business.id)}}}" primary-action="preview" :selected="selectedBusinessId === business.id" @preview="selectedBusinessId = business.id" />
      </div>
      <ObjectPreviewPanel v-if="selectedBusiness" :open="true" :title="selectedBusiness.businessName" subtitle="Business workspace" @close="selectedBusinessId = null" @open-detail="openBusiness">
        <p>{{ selectedBusiness.headline || "Open this business workspace to manage its profile, services, bookings, and calendar." }}</p>
        <dl>
          <div><dt>Status</dt><dd>{{ selectedBusiness.active ? "Active" : "Archived" }}</dd></div>
          <div><dt>Workspace</dt><dd>Business profile</dd></div>
        </dl>
      </ObjectPreviewPanel>
    </section>
    <AppDialog :open="createOpen" title="Create your business" layout="workspace" @close="createOpen = false; createError = ''">
      <form class="business-overview__create-form" @submit.prevent="createBusiness">
        <header><p class="business-overview__eyebrow">Start here</p><h2>Tell customers the essentials</h2><p>You can add photos, contact details, services, and working hours next.</p></header>
        <AppFormField label="Business name" required><input v-model="newBusiness.businessName" required maxlength="160" autofocus placeholder="e.g. Ana Hair Studio"></AppFormField>
        <AppFormField label="What do you offer?" required hint="A short sentence customers will see first."><input v-model="newBusiness.headline" required maxlength="200" placeholder="Haircuts and colour in central Zurich"></AppFormField>
        <AppFormField label="City or service area" required><input v-model="newBusiness.publicAddressLabel" required maxlength="240" placeholder="Zurich"></AppFormField>
        <AppFormField label="Your local time zone" required hint="We use this to show appointments at the right local time."><input v-model="newBusiness.timezone" required placeholder="Europe/Zurich"></AppFormField>
        <AppStatus v-if="createError" :message="createError" tone="error" />
        <AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="createOpen = false">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isCreating">Create business</AppButton></template></AppFormFooter>
      </form>
      <template #utility><p>Your public link, booking rules, and advanced scheduling options are created automatically. We will guide you through the next useful setup step.</p></template>
    </AppDialog>
  </section>
</template>

<style scoped>
.business-overview{display:grid;gap:var(--space-4);min-width:0}.business-overview__workspace{display:grid;grid-template-columns:minmax(0,1fr);overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.business-overview__workspace--preview{grid-template-columns:minmax(0,1fr) minmax(18rem,24rem)}.business-overview__list{display:grid}.business-overview__list :deep(.surface-row:last-child){border-bottom:0}.business-overview__create-form{display:grid;gap:var(--space-3)}.business-overview__create-form header{display:grid;gap:var(--space-1)}.business-overview__create-form h2,.business-overview__create-form p{margin:0}.business-overview__create-form header>p:last-child{color:var(--text-muted)}.business-overview__eyebrow{color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.business-overview__create-form input{width:100%;box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}
@media(max-width:980px){.business-overview__workspace--preview{grid-template-columns:minmax(0,1fr)}}
</style>
