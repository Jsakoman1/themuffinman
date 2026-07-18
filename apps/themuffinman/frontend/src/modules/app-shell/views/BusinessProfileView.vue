<script setup lang="ts">
import {onMounted, ref} from "vue"
import type {BusinessGalleryImageRequestDTO, BusinessGalleryImageResponseDTO, BusinessProfileRequestDTO} from "../../../contracts/index.ts"
import {setActiveBusinessProfileId, userShellApi} from "../api/userShellApi.ts"
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
  const name = window.prompt("Business name")?.trim()
  if (!name) return
  try {
    const created = await userShellApi.createBusinessProfile({businessName: name, slug: "", headline: "", description: "", contactEmail: "", contactPhone: "", websiteUrl: "", timezone: "Europe/Zurich", bookingEnabled: false, publicAddressLabel: "", latitude: undefined as unknown as number, longitude: undefined as unknown as number, contactWhatsapp: "", heroImageUrl: "", active: true})
    profiles.value = await userShellApi.getMyBusinessProfiles()
    selectedProfileId.value = created.id
    setActiveBusinessProfileId(created.id)
    form.value = toForm(created)
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
    <div class="business-profile__switcher"><label v-if="profiles.length">Business<select v-model="selectedProfileId" @change="onProfileSelectionChanged"><option v-for="profile in profiles" :key="profile.id" :value="profile.id">{{ profile.businessName }}</option></select></label><button v-if="profiles.length" type="button" @click="selectProfile">Switch business</button><button type="button" @click="createBusiness">Create business</button></div>
    <div v-if="isLoading" class="business-profile__status" role="status">Loading.</div>
    <div v-else-if="error && !form" class="business-profile__status business-profile__status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div>
    <form v-else-if="form" class="business-profile__form" @submit.prevent="save">
      <label>Business name<input v-model="form.businessName" required maxlength="160"></label>
      <label>Headline<input v-model="form.headline" maxlength="200"></label>
      <label>Description<textarea v-model="form.description" maxlength="4000"></textarea></label>
      <div class="business-profile__grid"><label>Contact email<input v-model="form.contactEmail" type="email"></label><label>Contact phone<input v-model="form.contactPhone"></label></div>
      <label>Public address label<input v-model="form.publicAddressLabel"></label>
      <label>Timezone<input v-model="form.timezone" required placeholder="Europe/Zurich"></label>
      <label class="business-profile__toggle"><input v-model="form.bookingEnabled" type="checkbox"> Accept bookings</label>
      <p v-if="feedback" class="business-profile__feedback" role="status">{{ feedback }}</p><p v-if="error" class="business-profile__status business-profile__status--error" role="alert">{{ error }}</p>
      <div class="business-profile__actions"><button class="business-profile__save" type="submit" :disabled="isSaving">{{ isSaving ? "Saving" : "Save profile" }}</button><button v-if="form.active" class="business-profile__archive" type="button" @click="archiveBusiness">Archive business</button></div>
    </form>
    <section class="business-profile__gallery">
      <header><h2>Gallery</h2><p>Show a small set of public images for your business.</p></header>
      <form class="business-profile__gallery-form" @submit.prevent="addGalleryImage">
        <label>Image URL<input v-model="galleryForm.imageUrl" type="url" required maxlength="500"></label>
        <label>Alt text<input v-model="galleryForm.altText" maxlength="240"></label>
        <label>Order<input v-model.number="galleryForm.sortOrder" type="number" min="0"></label>
        <button type="submit" :disabled="isGallerySaving">{{ isGallerySaving ? "Saving" : "Add image" }}</button>
      </form>
      <div v-if="gallery.length" class="business-profile__gallery-list">
        <article v-for="image in gallery" :key="image.id" class="business-profile__gallery-item">
          <img :src="image.imageUrl" :alt="image.altText || 'Business gallery image'">
          <div><span>{{ image.active ? "Published" : "Hidden" }} · order {{ image.sortOrder }}</span><button type="button" :disabled="isGallerySaving" @click="toggleGalleryImage(image)">{{ image.active ? "Hide" : "Publish" }}</button><button type="button" :disabled="isGallerySaving" @click="removeGalleryImage(image)">Remove</button></div>
        </article>
      </div>
      <p v-else class="business-profile__status">No gallery images yet.</p>
    </section>
  </section>
</template>

<style scoped>
.business-profile{display:grid;gap:1rem;max-width:44rem}.business-profile__eyebrow{margin:0 0 .3rem;color:var(--text-muted);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.business-profile__intro{margin:.45rem 0 0;color:var(--text-muted)}.business-profile__form{display:grid;gap:.8rem}.business-profile__form label{display:grid;gap:.35rem;font-size:.83rem;font-weight:650;color:var(--text-muted)}.business-profile__form input,.business-profile__form textarea{width:100%;border:1px solid var(--border-subtle);border-radius:.65rem;padding:.65rem;background:var(--surface);font:inherit;font-weight:400}.business-profile__form textarea{min-height:7rem;resize:vertical}.business-profile__grid{display:grid;grid-template-columns:1fr 1fr;gap:.8rem}.business-profile__toggle{display:flex!important;align-items:center;gap:.5rem!important}.business-profile__toggle input{width:auto}.business-profile__save{justify-self:start;border:0;border-radius:999px;padding:.65rem 1rem;background:var(--accent);color:var(--text);font-weight:650;cursor:pointer}.business-profile__save:disabled{opacity:.55;cursor:wait}.business-profile__feedback{margin:0;color:var(--success)}.business-profile__status{padding:.7rem 0;color:var(--text-muted)}.business-profile__status--error{color:var(--danger)}.business-profile__status button{margin-left:.5rem;border:0;background:none;color:inherit;text-decoration:underline;cursor:pointer}@media(max-width:620px){.business-profile__grid{grid-template-columns:1fr}}
.business-profile__gallery{display:grid;gap:.8rem;padding-top:1rem;border:1px solid var(--border-subtle)}.business-profile__gallery h2{margin:0}.business-profile__gallery header p{margin:.25rem 0 0;color:var(--text-muted);font-size:.85rem}.business-profile__gallery-form{display:grid;grid-template-columns:2fr 1.3fr .6fr auto;gap:.5rem;align-items:end}.business-profile__gallery-form label{display:grid;gap:.3rem;font-size:.78rem;font-weight:650}.business-profile__gallery-form input{border:1px solid var(--border-subtle);border-radius:.6rem;padding:.55rem;font:inherit}.business-profile__gallery-form button,.business-profile__gallery-item button{border:1px solid var(--border-subtle);border-radius:999px;padding:.5rem .7rem;background:var(--accent);color:var(--text);font:inherit;cursor:pointer}.business-profile__gallery-form button:disabled,.business-profile__gallery-item button:disabled{opacity:.5;cursor:wait}.business-profile__gallery-list{display:grid;grid-template-columns:repeat(3,1fr);gap:.7rem}.business-profile__gallery-item{display:grid;gap:.4rem}.business-profile__gallery-item img{width:100%;aspect-ratio:4/3;object-fit:cover;border-radius:.7rem;background:rgba(23,34,26,.06)}.business-profile__gallery-item div{display:flex;gap:.3rem;align-items:center;flex-wrap:wrap}.business-profile__gallery-item span{flex:1;color:var(--text-muted);font-size:.75rem}.business-profile__gallery-item button{padding:.35rem .55rem;background:transparent;color:var(--text);font-size:.75rem}@media(max-width:700px){.business-profile__gallery-form{grid-template-columns:1fr 1fr}.business-profile__gallery-form label:first-child{grid-column:1/-1}.business-profile__gallery-list{grid-template-columns:1fr 1fr}}
.business-profile__switcher{display:flex;align-items:end;gap:.6rem}.business-profile__switcher label{display:grid;gap:.3rem;font-size:.8rem;font-weight:650;flex:1}.business-profile__switcher select{border:1px solid var(--border-subtle);border-radius:.65rem;padding:.6rem;background:var(--bg-raised);font:inherit}.business-profile__switcher button{border:1px solid var(--border-subtle);border-radius:999px;padding:.6rem .8rem;background:var(--accent);color:var(--text);font:inherit;cursor:pointer}
.business-profile__actions{display:flex;gap:.55rem;align-items:center}.business-profile__archive{border:1px solid rgba(141,47,37,.3);border-radius:999px;padding:.6rem .8rem;background:transparent;color:var(--danger);font:inherit;cursor:pointer}
</style>
