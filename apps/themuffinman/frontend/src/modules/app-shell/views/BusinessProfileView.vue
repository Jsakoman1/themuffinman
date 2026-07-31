<script setup lang="ts">
import {onMounted, ref} from "vue"
import type {BusinessBookingPolicyRequestDTO, BusinessGalleryImageRequestDTO, BusinessGalleryImageResponseDTO, BusinessProfileRequestDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import DetailUtilityRail from "../components/DetailUtilityRail.vue"
import {confirmAction} from "../composables/useActionDialog.ts"
import RichTextEditor from "../components/RichTextEditor.vue"
import RichTextPreview from "../components/RichTextPreview.vue"
import BusinessAvailabilityView from "./BusinessAvailabilityView.vue"
import BusinessSettingsSectionNav, {type BusinessSettingsSection} from "../components/BusinessSettingsSectionNav.vue"

const form = ref<BusinessProfileRequestDTO | null>(null)
const policy = ref<BusinessBookingPolicyRequestDTO | null>(null)
const isPolicySaving = ref(false)
// Profile editing and publication remain one owner responsibility surface.
const profiles = ref<Awaited<ReturnType<typeof userShellApi.getMyBusinessProfiles>>>([])
const selectedProfileId = ref<number | null>(null)
const isLoading = ref(true)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const gallery = ref<BusinessGalleryImageResponseDTO[]>([])
const galleryForm = ref<BusinessGalleryImageRequestDTO>({imageUrl: "", altText: "", sortOrder: 0, active: true})
const isGallerySaving = ref(false)
const galleryFile = ref<File | null>(null)
const section = ref<BusinessSettingsSection>("public")

const load = async () => {
  isLoading.value = true; error.value = ""
  try {
    const [value, galleryResponse, ownedProfiles, bookingPolicy] = await Promise.all([userShellApi.getBusinessProfile(), userShellApi.getBusinessGallery(), userShellApi.getMyBusinessProfiles(), userShellApi.getBusinessBookingPolicy()])
    profiles.value = ownedProfiles.length ? ownedProfiles : [value]
    selectedProfileId.value = value.id
    gallery.value = galleryResponse.items
    policy.value = bookingPolicy
    form.value = {businessName: value.businessName, slug: value.slug, headline: value.headline, description: value.description ?? "", contactEmail: value.contactEmail, contactPhone: value.contactPhone, websiteUrl: value.websiteUrl, timezone: value.timezone, bookingEnabled: value.bookingEnabled, publicAddressLabel: value.publicAddressLabel, latitude: value.latitude, longitude: value.longitude, contactWhatsapp: value.contactWhatsapp, heroImageUrl: value.heroImageUrl, active: value.active}
  } catch { error.value = "Could not load your business profile." }
  finally { isLoading.value = false }
}
const saveBookingRules = async () => {
  if (!form.value || !policy.value) return
  isSaving.value = true; isPolicySaving.value = true; error.value = ""; feedback.value = ""
  try {
    if (selectedProfileId.value) await userShellApi.updateBusinessProfileById(selectedProfileId.value, form.value)
    else await userShellApi.updateBusinessProfile(form.value)
    policy.value = await userShellApi.updateBusinessBookingPolicy(policy.value)
    feedback.value = "Booking rules updated."
  } catch (cause) { error.value = userShellApi.actionFailureMessage("Could not save booking rules.", cause) }
  finally { isSaving.value = false; isPolicySaving.value = false }
}

const toForm = (value: Awaited<ReturnType<typeof userShellApi.getBusinessProfile>>) => ({businessName: value.businessName, slug: value.slug, headline: value.headline, description: value.description ?? "", contactEmail: value.contactEmail, contactPhone: value.contactPhone, websiteUrl: value.websiteUrl, timezone: value.timezone, bookingEnabled: value.bookingEnabled, publicAddressLabel: value.publicAddressLabel, latitude: value.latitude, longitude: value.longitude, contactWhatsapp: value.contactWhatsapp, heroImageUrl: value.heroImageUrl, active: value.active})
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

const onGalleryFileChanged = (event: Event) => { galleryFile.value = (event.target as HTMLInputElement).files?.[0] || null }
const uploadGalleryImage = async () => {
  if (!galleryFile.value) return
  isGallerySaving.value = true; error.value = ""
  try {
    const uploaded = await userShellApi.uploadBusinessGalleryImage(galleryFile.value, galleryForm.value.altText, galleryForm.value.sortOrder)
    gallery.value = [...gallery.value, uploaded].sort((left, right) => left.sortOrder - right.sortOrder)
    galleryFile.value = null; galleryForm.value = {imageUrl: "", altText: "", sortOrder: gallery.value.length, active: true}; feedback.value = "Gallery image uploaded."
  } catch { error.value = "Could not upload this gallery image. Check the file type and try again." } finally { isGallerySaving.value = false }
}

const toggleGalleryImage = async (image: BusinessGalleryImageResponseDTO) => {
  isGallerySaving.value = true; error.value = ""
  try {
    const updated = await userShellApi.updateBusinessGalleryImage(image.id, {imageUrl: image.imageUrl, altText: image.altText, sortOrder: image.sortOrder, active: !image.active})
    gallery.value = gallery.value.map(item => item.id === updated.id ? updated : item)
  } catch { error.value = "Could not update this gallery image." } finally { isGallerySaving.value = false }
}

const moveGalleryImage = async (image: BusinessGalleryImageResponseDTO, direction: -1 | 1) => {
  const index = gallery.value.findIndex(item => item.id === image.id)
  const adjacent = gallery.value[index + direction]
  if (!adjacent) return
  isGallerySaving.value = true; error.value = ""
  try {
    const [updatedImage, updatedAdjacent] = await Promise.all([
      userShellApi.updateBusinessGalleryImage(image.id, {...image, sortOrder: adjacent.sortOrder}),
      userShellApi.updateBusinessGalleryImage(adjacent.id, {...adjacent, sortOrder: image.sortOrder})
    ])
    gallery.value = gallery.value.map(item => item.id === updatedImage.id ? updatedImage : item.id === updatedAdjacent.id ? updatedAdjacent : item).sort((left, right) => left.sortOrder - right.sortOrder)
  } catch { error.value = "Could not reorder this gallery image." } finally { isGallerySaving.value = false }
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
<style scoped>
.business-profile{border-top:1px solid var(--orientation-line)}

.business-profile { display:grid; gap:var(--space-3); max-width:none; }.business-profile__intro{margin:0;color:var(--text-muted);line-height:1.5}
.business-profile h1 { margin:0; color:var(--text); font-size:var(--text-size-page-title); letter-spacing:var(--tracking-tight); }
.business-profile__workspace { display:grid; grid-template-columns:minmax(0,1fr); gap:var(--space-3); align-items:start; }
.business-profile__main { display:grid; gap:var(--space-3); min-width:0; }
.business-profile__form { display:grid; gap:var(--space-3); }.business-profile__section-card{padding:var(--space-4);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.business-profile__section-heading{display:grid;gap:var(--space-1)}.business-profile__section-heading h2,.business-profile__section-heading p{margin:0}.business-profile__section-heading h2{font-size:var(--text-size-title)}.business-profile__section-heading p{color:var(--text-muted);line-height:1.5}
.business-profile__form input,.business-profile__form textarea,.business-profile__gallery-form input { width:100%; border:1px solid var(--control-border); border-radius:var(--radius-control); padding:var(--space-2); background:var(--control-bg); color:var(--control-ink); font:inherit; }
.business-profile__form textarea { min-height:7rem; resize:vertical; }
.business-profile__form input:focus-visible,.business-profile__form textarea:focus-visible,.business-profile__gallery-form input:focus-visible { border-color:var(--control-border-active); outline:2px solid var(--focus-ring); outline-offset:2px; }
.business-profile__grid { display:grid; grid-template-columns:1fr 1fr; gap:var(--space-3); }
.business-profile__toggle { display:flex; align-items:center; gap:var(--space-2); color:var(--text); font-size:var(--text-size-body); font-weight:var(--text-weight-semibold); }
.business-profile__policy { display:grid; gap:var(--space-3); margin:0; border:1px solid var(--border-subtle); border-radius:var(--radius-surface); padding:var(--space-3); background:var(--surface-raised); }
.business-profile__policy legend { padding:0 var(--space-1); color:var(--text); font-weight:var(--text-weight-semibold); }
.business-profile__advanced-policy{display:grid;gap:var(--space-2);color:var(--text-muted)}.business-profile__advanced-policy summary{cursor:pointer;color:var(--text);font-weight:var(--text-weight-semibold)}.business-profile__advanced-policy[open]{padding-top:var(--space-2);border-top:1px solid var(--border-subtle)}
.business-profile__toggle input { width:auto; }
.business-profile__gallery { display:grid; gap:var(--space-3); padding:var(--space-3); border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-base); }
.business-profile__gallery h2,.business-profile__gallery p { margin:0; }
.business-profile__gallery h2 { color:var(--text); font-size:var(--text-size-title); }
.business-profile__gallery header p { margin-top:var(--space-1); color:var(--text-muted); font-size:var(--text-size-meta); }
.business-profile__gallery-form { display:grid; grid-template-columns:minmax(0,2fr) minmax(0,1.3fr) minmax(5rem,.6fr) auto; gap:var(--space-2); align-items:end; }
.business-profile__gallery-upload { display:flex; gap:var(--space-2); align-items:end; padding-top:var(--space-2); border-top:1px solid var(--border-subtle); }
.business-profile__gallery-preview { width:3rem; height:3rem; object-fit:cover; border-radius:var(--radius-control); }
.business-profile__gallery-list { display:grid; gap:0; overflow:hidden; border:1px solid var(--border-subtle); border-radius:var(--radius-surface); }
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

<template>
  <section class="business-profile" data-mental-model="identity-content-gallery-inspector">
    <CollectionToolbar title="Business settings" :count="profiles.length" :busy="isLoading" />
    <p class="business-profile__intro">Choose one clear job at a time. Your public page, hours, booking rules, and technical details are kept separate.</p>
    <BusinessSettingsSectionNav :active="section" @select="section = $event" />
    <AppStatus v-if="isLoading" message="Loading your business profile." busy />
    <AppStatus v-else-if="error && !form" :message="error" tone="error" retry @retry="load" />
    <div class="business-profile__workspace">
    <div class="business-profile__main">
    <form v-if="form && section === 'public'" class="business-profile__form" @submit.prevent="save">
      <header class="business-profile__section-heading"><h2>What customers see</h2><p>Keep this page warm, accurate, and easy to recognise.</p></header>
      <AppFormField label="Business name" required><input v-model="form.businessName" required maxlength="160" aria-label="Business name"></AppFormField>
      <AppFormField label="Headline" optional><input v-model="form.headline" maxlength="200" aria-label="Headline"></AppFormField>
      <div class="business-profile__rich-description"><AppFormField label="Public description" optional hint="Format the welcome text customers see on your business page."><RichTextEditor v-model="form.description" label="Public business description" placeholder="Tell customers what makes this business useful…" /></AppFormField><aside class="business-profile__description-preview" aria-label="Public description preview"><p>Preview</p><RichTextPreview :content="form.description" /></aside></div>
      <div class="business-profile__grid"><AppFormField label="Contact email" optional><input v-model="form.contactEmail" type="email"></AppFormField><AppFormField label="Contact phone" optional><input v-model="form.contactPhone"></AppFormField></div>
      <div class="business-profile__grid"><AppFormField label="WhatsApp" optional><input v-model="form.contactWhatsapp"></AppFormField><AppFormField label="Website" optional><input v-model="form.websiteUrl" type="url"></AppFormField></div>
      <AppFormField label="Public address or service area" optional><input v-model="form.publicAddressLabel"></AppFormField>
      <AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="error" :message="error" tone="error" />
      <AppFormFooter><template #primary><AppButton tone="primary" type="submit" :loading="isSaving">Save public page</AppButton></template></AppFormFooter>
    </form>
    <section v-if="section === 'public'" class="business-profile__gallery">
      <header><h2>Gallery</h2><p>Show a small set of public images for your business.</p></header>
      <form class="business-profile__gallery-form" @submit.prevent="addGalleryImage">
        <AppFormField label="Image URL" required><input v-model="galleryForm.imageUrl" type="url" required maxlength="500"></AppFormField>
        <AppFormField label="Alt text" optional><input v-model="galleryForm.altText" maxlength="240"></AppFormField>
        <AppFormField label="Order"><input v-model.number="galleryForm.sortOrder" type="number" min="0"></AppFormField>
        <AppButton tone="primary" type="submit" :loading="isGallerySaving">Add image</AppButton>
      </form>
      <form class="business-profile__gallery-upload" @submit.prevent="uploadGalleryImage">
        <AppFormField label="Upload image" hint="Images only, up to 10 MB."><input type="file" accept="image/*" @change="onGalleryFileChanged"></AppFormField>
        <AppButton tone="primary" type="submit" :loading="isGallerySaving" :disabled="!galleryFile">Upload image</AppButton>
      </form>
      <div v-if="gallery.length" class="business-profile__gallery-list"><SurfaceRow v-for="(image, index) in gallery" :key="image.id" :row="{id: String(image.id), title: image.altText || 'Gallery image', description: image.active ? 'Published business image' : 'Hidden business image', thumbnailUrl: image.imageUrl, badge: image.active ? 'Published' : 'Hidden', meta: `Order ${image.sortOrder}`}" ><template #actions><AppButton :disabled="index === 0" :loading="isGallerySaving" aria-label="Move image up" @click="moveGalleryImage(image, -1)">↑</AppButton><AppButton :disabled="index === gallery.length - 1" :loading="isGallerySaving" aria-label="Move image down" @click="moveGalleryImage(image, 1)">↓</AppButton><AppButton :loading="isGallerySaving" @click="toggleGalleryImage(image)">{{ image.active ? "Hide" : "Publish" }}</AppButton><AppButton tone="danger" :loading="isGallerySaving" @click="removeGalleryImage(image)">Remove</AppButton></template></SurfaceRow></div>
      <AppStatus v-else message="No gallery images yet." />
    </section>
    <section v-if="form && section === 'hours'" class="business-profile__section-card"><header class="business-profile__section-heading"><h2>Working hours</h2><p>Set the normal times customers can choose. Special dates are handled separately.</p></header><BusinessAvailabilityView v-if="selectedProfileId" :business-id="selectedProfileId" /></section>
    <form v-if="form && section === 'booking'" class="business-profile__form business-profile__section-card" @submit.prevent="saveBookingRules"><header class="business-profile__section-heading"><h2>Booking rules</h2><p>Decide when customers can request a booking and whether you confirm it first. One save applies everything on this page.</p></header><label class="business-profile__toggle"><input v-model="form.bookingEnabled" type="checkbox"> <span>Accept booking requests</span></label><fieldset v-if="policy" class="business-profile__policy"><legend>Timing and decisions</legend><div class="business-profile__grid"><AppFormField label="Minimum notice (minutes)"><input v-model.number="policy.leadTimeMinutes" type="number" min="0"></AppFormField><AppFormField label="How far ahead can customers book? (days)"><input v-model.number="policy.maxAdvanceDays" type="number" min="1"></AppFormField></div><div class="business-profile__grid"><AppFormField label="Cancellation notice (minutes)"><input v-model.number="policy.customerCancellationWindowMinutes" type="number" min="0"></AppFormField><AppFormField label="Rescheduling notice (minutes)"><input v-model.number="policy.ownerRescheduleWindowMinutes" type="number" min="0"></AppFormField></div><label class="business-profile__toggle"><input v-model="policy.requiresOwnerConfirmationDefault" type="checkbox"> Confirm each request myself</label><label class="business-profile__toggle"><input v-model="policy.allowCustomerCancellation" type="checkbox"> Let customers cancel</label><details class="business-profile__advanced-policy"><summary>More booking controls</summary><label class="business-profile__toggle"><input v-model="policy.allowOwnerManualApproval" type="checkbox"> Allow manual approval</label><label class="business-profile__toggle"><input v-model="policy.allowOwnerManualRejection" type="checkbox"> Allow manual rejection</label><label class="business-profile__toggle"><input v-model="policy.allowWaitlist" type="checkbox"> Offer a waitlist</label></details><AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="error" :message="error" tone="error" /><AppButton type="submit" tone="primary" :loading="isSaving || isPolicySaving">Save booking rules</AppButton></fieldset></form>
    <form v-if="form && section === 'advanced'" class="business-profile__form business-profile__section-card" @submit.prevent="save"><header class="business-profile__section-heading"><h2>Advanced business details</h2><p>Only change these when you know why they are needed.</p></header><AppFormField label="Public page link" required hint="Lowercase words separated by hyphens."><input v-model="form.slug" required pattern="[a-z0-9]+(?:-[a-z0-9]+)*" maxlength="160"></AppFormField><AppFormField label="Time zone" required hint="Used to calculate local availability."><input v-model="form.timezone" required placeholder="Europe/Zurich" aria-label="Timezone"></AppFormField><AppFormField label="Hero image URL" optional><input v-model="form.heroImageUrl" type="url"></AppFormField><div class="business-profile__grid"><AppFormField label="Latitude" optional><input v-model.number="form.latitude" type="number" step="any"></AppFormField><AppFormField label="Longitude" optional><input v-model.number="form.longitude" type="number" step="any"></AppFormField></div><AppFormFooter><template #secondary><AppButton v-if="form.active" tone="danger" type="button" @click="archiveBusiness">Archive business</AppButton></template><template #primary><AppButton tone="primary" type="submit" :loading="isSaving">Save advanced details</AppButton></template></AppFormFooter></form>
    </div>
    <DetailUtilityRail v-if="form" class="business-profile__utility" title="Business context">
      <h2>{{ form.businessName || "Business identity" }}</h2>
      <p>Keep the public profile, booking switch, and gallery aligned with the backend-owned business context.</p><RichTextPreview v-if="form.description" :content="form.description" />
      <dl>
        <div><dt>Public slug</dt><dd>{{ form.slug || "Not set" }}</dd></div>
        <div><dt>Bookings</dt><dd>{{ form.bookingEnabled ? "Enabled" : "Disabled" }}</dd></div>
        <div><dt>Timezone</dt><dd>{{ form.timezone || "Not set" }}</dd></div>
        <div><dt>Gallery</dt><dd>{{ gallery.length }} images</dd></div>
      </dl>
      <p class="business-profile__utility-note">Save, archive, visibility, and booking permissions are validated by the server. This rail is context only.</p>
    </DetailUtilityRail>
    </div>
  </section>
</template>

