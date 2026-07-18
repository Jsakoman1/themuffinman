<script setup lang="ts">
import {onMounted, ref} from "vue"
import type {BusinessGalleryImageRequestDTO, BusinessGalleryImageResponseDTO, BusinessProfileRequestDTO} from "../../../contracts/index.ts"
import {setActiveBusinessProfileId, userShellApi} from "../api/userShellApi.ts"
import AppButton from "../components/AppButton.vue"
import AppDialog from "../components/AppDialog.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import {confirmAction} from "../composables/useActionDialog.ts"

const form = ref<BusinessProfileRequestDTO | null>(null)
const profiles = ref<Awaited<ReturnType<typeof userShellApi.getMyBusinessProfiles>>>([])
const selectedProfileId = ref<number | null>(null)
const isLoading = ref(true)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const gallery = ref<BusinessGalleryImageResponseDTO[]>([])
const galleryForm = ref<BusinessGalleryImageRequestDTO>({imageUrl: "", altText: "", sortOrder: 0, active: true})
const isGallerySaving = ref(false)
const isCreateOpen = ref(false)
const newBusinessName = ref("")

const load = async () => {
  isLoading.value = true; error.value = ""
  try {
    const [value, galleryResponse, ownedProfiles] = await Promise.all([userShellApi.getBusinessProfile(), userShellApi.getBusinessGallery(), userShellApi.getMyBusinessProfiles()])
    profiles.value = ownedProfiles.length ? ownedProfiles : [value]
    selectedProfileId.value = value.id
    gallery.value = galleryResponse.items
    form.value = {businessName: value.businessName, slug: value.slug, headline: value.headline, description: value.description, contactEmail: value.contactEmail, contactPhone: value.contactPhone, websiteUrl: value.websiteUrl, timezone: value.timezone, bookingEnabled: value.bookingEnabled, publicAddressLabel: value.publicAddressLabel, latitude: value.latitude, longitude: value.longitude, contactWhatsapp: value.contactWhatsapp, heroImageUrl: value.heroImageUrl, active: value.active}
  } catch { error.value = "Could not load your business profile." }
  finally { isLoading.value = false }
}

const toForm = (value: Awaited<ReturnType<typeof userShellApi.getBusinessProfile>>) => ({businessName: value.businessName, slug: value.slug, headline: value.headline, description: value.description, contactEmail: value.contactEmail, contactPhone: value.contactPhone, websiteUrl: value.websiteUrl, timezone: value.timezone, bookingEnabled: value.bookingEnabled, publicAddressLabel: value.publicAddressLabel, latitude: value.latitude, longitude: value.longitude, contactWhatsapp: value.contactWhatsapp, heroImageUrl: value.heroImageUrl, active: value.active})
const selectProfile = async () => {
  if (!selectedProfileId.value) return
  try { setActiveBusinessProfileId(selectedProfileId.value); form.value = toForm(await userShellApi.getBusinessProfileById(selectedProfileId.value)); feedback.value = "Business selected." }
  catch { error.value = "Could not switch business." }
}
const onProfileSelectionChanged = (event: Event) => { const value = Number((event.target as HTMLSelectElement).value); if (Number.isFinite(value)) selectedProfileId.value = value }
const createBusiness = async () => {
  const name = newBusinessName.value.trim()
  if (!name) return
  try {
    const created = await userShellApi.createBusinessProfile({businessName: name, slug: "", headline: "", description: "", contactEmail: "", contactPhone: "", websiteUrl: "", timezone: "Europe/Zurich", bookingEnabled: false, publicAddressLabel: "", latitude: undefined as unknown as number, longitude: undefined as unknown as number, contactWhatsapp: "", heroImageUrl: "", active: true})
    profiles.value = await userShellApi.getMyBusinessProfiles()
    selectedProfileId.value = created.id
    setActiveBusinessProfileId(created.id)
    form.value = toForm(created)
    newBusinessName.value = ""
    isCreateOpen.value = false
    feedback.value = "Business created."
  } catch { error.value = "Could not create this business." }
}

const addGalleryImage = async () => {
  if (!galleryForm.value.imageUrl.trim()) return
  isGallerySaving.value = true; error.value = ""
  try {
    const created = await userShellApi.createBusinessGalleryImage({...galleryForm.value, imageUrl: galleryForm.value.imageUrl.trim()})
    gallery.value = [...gallery.value, created].sort((left, right) => left.sortOrder - right.sortOrder)
    galleryForm.value = {imageUrl: "", altText: "", sortOrder: gallery.value.length, active: true}
    feedback.value = "Gallery image added."
  } catch { error.value = "Could not add this gallery image." } finally { isGallerySaving.value = false }
}

const toggleGalleryImage = async (image: BusinessGalleryImageResponseDTO) => {
  isGallerySaving.value = true; error.value = ""
  try {
    const updated = await userShellApi.updateBusinessGalleryImage(image.id, {imageUrl: image.imageUrl, altText: image.altText, sortOrder: image.sortOrder, active: !image.active})
    gallery.value = gallery.value.map(item => item.id === updated.id ? updated : item)
  } catch { error.value = "Could not update this gallery image." } finally { isGallerySaving.value = false }
}

const removeGalleryImage = async (image: BusinessGalleryImageResponseDTO) => {
  if (!await confirmAction("Remove this gallery image?", "Remove gallery image")) return
  isGallerySaving.value = true; error.value = ""
  try { await userShellApi.deleteBusinessGalleryImage(image.id); gallery.value = gallery.value.filter(item => item.id !== image.id); feedback.value = "Gallery image removed." }
  catch { error.value = "Could not remove this gallery image." } finally { isGallerySaving.value = false }
}

const save = async () => {
  if (!form.value) return
  isSaving.value = true; error.value = ""; feedback.value = ""
  try { if (selectedProfileId.value) await userShellApi.updateBusinessProfileById(selectedProfileId.value, form.value); else await userShellApi.updateBusinessProfile(form.value); feedback.value = "Profile updated." }
  catch { error.value = "Could not update your business profile." }
  finally { isSaving.value = false }
}

const archiveBusiness = async () => {
  if (!selectedProfileId.value || !await confirmAction("Archive this business? It will no longer appear publicly.", "Archive business")) return
  try {
    const archived = await userShellApi.archiveBusinessProfile(selectedProfileId.value)
    profiles.value = profiles.value.map(profile => profile.id === archived.id ? archived : profile)
    form.value = toForm(archived)
    feedback.value = "Business archived."
  } catch { error.value = "Could not archive this business." }
}

onMounted(() => void load())
</script>

<template>
  <section class="business-profile">
    <header><p class="business-profile__eyebrow">Business / Profile</p><h1>Profile</h1><p class="business-profile__intro">Keep the public identity and booking switch in one place.</p></header>
    <CollectionToolbar title="Business identity" :count="profiles.length" :busy="isLoading"><template #filters><label v-if="profiles.length" class="business-profile__selector">Business<select v-model="selectedProfileId" @change="onProfileSelectionChanged"><option v-for="profile in profiles" :key="profile.id" :value="profile.id">{{ profile.businessName }}</option></select></label><AppButton v-if="profiles.length" type="button" @click="selectProfile">Switch</AppButton></template><template #actions><AppButton tone="primary" type="button" @click="isCreateOpen = true">Create business</AppButton></template></CollectionToolbar>
    <AppStatus v-if="isLoading" message="Loading." />
    <AppStatus v-else-if="error && !form" :message="error" tone="error" retry @retry="load" />
    <div class="business-profile__workspace">
    <div class="business-profile__main">
    <form v-if="form" class="business-profile__form" @submit.prevent="save">
      <AppFormField label="Business name" required><input v-model="form.businessName" required maxlength="160" aria-label="Business name"></AppFormField>
      <AppFormField label="Headline" optional><input v-model="form.headline" maxlength="200" aria-label="Headline"></AppFormField>
      <AppFormField label="Description" optional hint="This appears in the public business profile."><textarea v-model="form.description" maxlength="4000" aria-label="Description"></textarea></AppFormField>
      <div class="business-profile__grid"><AppFormField label="Contact email" optional><input v-model="form.contactEmail" type="email"></AppFormField><AppFormField label="Contact phone" optional><input v-model="form.contactPhone"></AppFormField></div>
      <AppFormField label="Public address label" optional><input v-model="form.publicAddressLabel"></AppFormField>
      <AppFormField label="Timezone" required hint="Used by availability and booking schedules."><input v-model="form.timezone" required placeholder="Europe/Zurich" aria-label="Timezone"></AppFormField>
      <label class="business-profile__toggle"><input v-model="form.bookingEnabled" type="checkbox"> <span>Accept bookings</span></label>
      <AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="error" :message="error" tone="error" />
      <AppFormFooter><template #secondary><AppButton v-if="form.active" tone="danger" type="button" @click="archiveBusiness">Archive business</AppButton></template><template #primary><AppButton tone="primary" type="submit" :loading="isSaving">Save profile</AppButton></template></AppFormFooter>
    </form>
    <section class="business-profile__gallery">
      <header><h2>Gallery</h2><p>Show a small set of public images for your business.</p></header>
      <form class="business-profile__gallery-form" @submit.prevent="addGalleryImage">
        <AppFormField label="Image URL" required><input v-model="galleryForm.imageUrl" type="url" required maxlength="500"></AppFormField>
        <AppFormField label="Alt text" optional><input v-model="galleryForm.altText" maxlength="240"></AppFormField>
        <AppFormField label="Order"><input v-model.number="galleryForm.sortOrder" type="number" min="0"></AppFormField>
        <AppButton tone="primary" type="submit" :loading="isGallerySaving">Add image</AppButton>
      </form>
      <div v-if="gallery.length" class="business-profile__gallery-list"><SurfaceRow v-for="image in gallery" :key="image.id" :row="{id: String(image.id), title: image.altText || 'Gallery image', description: image.imageUrl, badge: image.active ? 'Published' : 'Hidden', meta: `Order ${image.sortOrder}`}" ><template #actions><AppButton :loading="isGallerySaving" @click="toggleGalleryImage(image)">{{ image.active ? "Hide" : "Publish" }}</AppButton><AppButton tone="danger" :loading="isGallerySaving" @click="removeGalleryImage(image)">Remove</AppButton></template></SurfaceRow></div>
      <AppStatus v-else message="No gallery images yet." />
    </section>
    </div>
    <aside v-if="form" class="business-profile__utility" aria-label="Business profile context">
      <p class="business-profile__eyebrow">Workspace context</p>
      <h2>{{ form.businessName || "Business identity" }}</h2>
      <p>Keep the public profile, booking switch, and gallery aligned with the backend-owned business context.</p>
      <dl>
        <div><dt>Public slug</dt><dd>{{ form.slug || "Not set" }}</dd></div>
        <div><dt>Bookings</dt><dd>{{ form.bookingEnabled ? "Enabled" : "Disabled" }}</dd></div>
        <div><dt>Timezone</dt><dd>{{ form.timezone || "Not set" }}</dd></div>
        <div><dt>Gallery</dt><dd>{{ gallery.length }} images</dd></div>
      </dl>
      <p class="business-profile__utility-note">Save, archive, visibility, and booking permissions are validated by the server. This rail is context only.</p>
    </aside>
    </div>
    <AppDialog :open="isCreateOpen" title="Create business" layout="workspace" @close="isCreateOpen = false"><form class="business-profile__create-form" @submit.prevent="createBusiness"><AppFormField label="Business name" required><input v-model="newBusinessName" required maxlength="160" autofocus></AppFormField><AppFormFooter><template #secondary><AppButton type="button" @click="isCreateOpen = false">Cancel</AppButton></template><template #primary><AppButton tone="primary" type="submit">Create business</AppButton></template></AppFormFooter></form><template #utility><p>Creating a business creates the backend-owned profile context used by bookings, offerings, availability, and public discovery.</p></template></AppDialog>
  </section>
</template>

<style scoped>
.business-profile { display:grid; gap:var(--space-3); max-width:none; }
.business-profile__eyebrow { margin:0 0 var(--space-1); color:var(--text-soft); font-size:var(--text-size-label); font-weight:var(--text-weight-semibold); letter-spacing:var(--tracking-label); text-transform:uppercase; }
.business-profile h1 { margin:0; color:var(--text); font-size:var(--text-size-page-title); letter-spacing:var(--tracking-tight); }
.business-profile__intro { margin:var(--space-1) 0 0; color:var(--text-muted); }
.business-profile__workspace { display:grid; grid-template-columns:minmax(0,1fr) minmax(16rem,22rem); gap:var(--space-3); align-items:start; }
.business-profile__main { display:grid; gap:var(--space-3); min-width:0; }
.business-profile__form { display:grid; gap:var(--space-3); }
.business-profile__form input,.business-profile__form textarea,.business-profile__gallery-form input,.business-profile__selector select { width:100%; border:1px solid var(--control-border); border-radius:var(--radius-control); padding:var(--space-2); background:var(--control-bg); color:var(--control-ink); font:inherit; }
.business-profile__form textarea { min-height:7rem; resize:vertical; }
.business-profile__form input:focus-visible,.business-profile__form textarea:focus-visible,.business-profile__gallery-form input:focus-visible,.business-profile__selector select:focus-visible { border-color:var(--control-border-active); outline:2px solid var(--focus-ring); outline-offset:2px; }
.business-profile__grid { display:grid; grid-template-columns:1fr 1fr; gap:var(--space-3); }
.business-profile__toggle { display:flex; align-items:center; gap:var(--space-2); color:var(--text); font-size:var(--text-size-body); font-weight:var(--text-weight-semibold); }
.business-profile__toggle input { width:auto; }
.business-profile__gallery { display:grid; gap:var(--space-3); padding:var(--space-3); border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-base); }
.business-profile__gallery h2,.business-profile__gallery p { margin:0; }
.business-profile__gallery h2 { color:var(--text); font-size:var(--text-size-title); }
.business-profile__gallery header p { margin-top:var(--space-1); color:var(--text-muted); font-size:var(--text-size-meta); }
.business-profile__gallery-form { display:grid; grid-template-columns:minmax(0,2fr) minmax(0,1.3fr) minmax(5rem,.6fr) auto; gap:var(--space-2); align-items:end; }
.business-profile__gallery-list { display:grid; gap:0; overflow:hidden; border:1px solid var(--border-subtle); border-radius:var(--radius-surface); }
.business-profile__selector { display:flex; align-items:center; gap:var(--space-1); color:var(--text-muted); font-size:var(--text-size-meta); font-weight:var(--text-weight-semibold); }
.business-profile__selector select { min-width:12rem; width:auto; }
.business-profile__create-form { display:grid; gap:var(--space-3); }
.business-profile__utility { display:grid; gap:var(--space-2); padding:var(--space-3); border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-raised); color:var(--text-muted); }
.business-profile__utility h2,.business-profile__utility p { margin:0; }
.business-profile__utility h2 { color:var(--text); font-size:var(--text-size-title); }
.business-profile__utility dl { display:grid; gap:var(--space-2); margin:var(--space-2) 0; }
.business-profile__utility dl div { display:flex; justify-content:space-between; gap:var(--space-2); border-top:1px solid var(--border-subtle); padding-top:var(--space-2); }
.business-profile__utility dt { color:var(--text-soft); font-size:var(--text-size-meta); }
.business-profile__utility dd { margin:0; color:var(--text); font-size:var(--text-size-meta); font-weight:var(--text-weight-semibold); }
.business-profile__utility-note { color:var(--text-soft); font-size:var(--text-size-meta); line-height:1.45; }
.business-profile :deep(.app-dialog__utility) { padding:var(--space-3); color:var(--text-muted); font-size:var(--text-size-body); line-height:1.5; }
@media(max-width:860px) { .business-profile__workspace { grid-template-columns:1fr; } .business-profile__utility { order:2; } }
@media(max-width:700px) { .business-profile__grid,.business-profile__gallery-form { grid-template-columns:1fr; } .business-profile__selector select { min-width:0; } }
</style>
