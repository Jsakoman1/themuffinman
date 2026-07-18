<script setup lang="ts">
import {onMounted, ref} from "vue"
import {RouterLink} from "vue-router"
import {updateSessionUser} from "../../identity/auth.ts"
import type {AppUserResponseDTO, CircleContactDTO, CircleGroupResponseDTO, LocationLookupCandidateDTO, UserLocationMode} from "../../../contracts/index.ts"
import {userShellApi, type ProfileGalleryImage} from "../api/userShellApi.ts"
import InlineEditText from "../components/InlineEditText.vue"

const user = ref<AppUserResponseDTO | null>(null)
const username = ref("")
const profileDescription = ref("")
const avatarDataUrl = ref<string | null>(null)
const circleGroups = ref<CircleGroupResponseDTO[]>([])
const circleConnections = ref<CircleContactDTO[]>([])
const selectedCircleIds = ref<number[]>([])
const selectedUserIds = ref<number[]>([])
const mode = ref<UserLocationMode>("OFF")
const exactVisibilityScope = ref<"NOBODY" | "EVERYONE" | "CIRCLES" | "USERS">("NOBODY")
const radius = ref(25)
const query = ref("")
const candidate = ref<LocationLookupCandidateDTO | null>(null)
const suggestions = ref<LocationLookupCandidateDTO[]>([])
const isLoading = ref(true)
const isSearching = ref(false)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const gallery = ref<ProfileGalleryImage[]>([])
const galleryUrl = ref("")
const galleryAltText = ref("")
const isGalleryActing = ref(false)

const applyCandidate = (value: LocationLookupCandidateDTO) => {
  candidate.value = value
  query.value = value.label
  suggestions.value = []
  error.value = ""
}

const handleLocationQueryInput = () => {
  if (candidate.value && query.value.trim() !== candidate.value.label) candidate.value = null
  void search()
}

const load = async () => {
  isLoading.value = true
  try {
    const [currentUser, groups, connections, galleryResponse] = await Promise.all([
      userShellApi.getCurrentAppUser(),
      userShellApi.getCircleGroups(),
      userShellApi.getCircleConnections(),
      userShellApi.getMyProfileGallery()
    ])
    user.value = currentUser
    circleGroups.value = groups
    circleConnections.value = connections.items
    gallery.value = galleryResponse.items
    username.value = user.value.username
    profileDescription.value = user.value.profileDescription ?? ""
    avatarDataUrl.value = user.value.profileAvatarDataUrl ?? null
    const settings = user.value.locationSettings
    mode.value = settings.mode
    exactVisibilityScope.value = settings.exactVisibilityScope
    selectedCircleIds.value = [...(settings.exactVisibleCircleIds ?? [])]
    selectedUserIds.value = [...(settings.exactVisibleUserIds ?? [])]
    radius.value = settings.defaultRadiusKm
    if (settings.label && settings.latitude !== null && settings.longitude !== null) {
      candidate.value = {
        provider: settings.provider ?? "profile",
        providerPlaceId: settings.providerPlaceId,
        label: settings.label,
        countryCode: settings.countryCode,
        country: settings.country,
        locality: settings.locality,
        postalCode: settings.postalCode,
        street: settings.street,
        houseNumber: settings.houseNumber,
        latitude: settings.latitude,
        longitude: settings.longitude,
        resolvedAt: settings.resolvedAt
      }
      query.value = settings.label
    }
  } catch { error.value = "Could not load location settings." }
  finally { isLoading.value = false }
}

const addGalleryImage = async () => {
  if (!galleryUrl.value.trim()) { error.value = "Add an image URL before saving a gallery image."; return }
  isGalleryActing.value = true
  try {
    gallery.value.push(await userShellApi.createProfileGalleryImage({imageUrl: galleryUrl.value.trim(), altText: galleryAltText.value.trim() || null, sortOrder: gallery.value.length}))
    galleryUrl.value = ""
    galleryAltText.value = ""
    feedback.value = "Gallery image added."
  } catch { error.value = "Could not add this gallery image." }
  finally { isGalleryActing.value = false }
}

const removeGalleryImage = async (image: ProfileGalleryImage) => {
  isGalleryActing.value = true
  try {
    await userShellApi.deleteProfileGalleryImage(image.id)
    gallery.value = gallery.value.filter(item => item.id !== image.id)
    feedback.value = "Gallery image removed."
  } catch { error.value = "Could not remove this gallery image." }
  finally { isGalleryActing.value = false }
}

const selectAvatar = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!file.type.startsWith("image/")) { error.value = "Choose an image file for your profile picture."; return }
  if (file.size > 2_000_000) { error.value = "Profile pictures must be smaller than 2 MB."; return }
  const reader = new FileReader()
  reader.onload = () => { avatarDataUrl.value = typeof reader.result === "string" ? reader.result : null; error.value = "" }
  reader.onerror = () => { error.value = "Could not read this image. Try another file." }
  reader.readAsDataURL(file)
}

const removeAvatar = () => { avatarDataUrl.value = null }

const search = async () => {
  if (query.value.trim().length < 3) { suggestions.value = []; return }
  isSearching.value = true
  error.value = ""
  try { suggestions.value = (await userShellApi.lookupLocations(query.value.trim())).items }
  catch { error.value = "Could not search this location." }
  finally { isSearching.value = false }
}

const useCurrentLocation = () => {
  if (!navigator.geolocation) { error.value = "Current location is not available in this browser."; return }
  error.value = ""
  navigator.geolocation.getCurrentPosition(async position => {
    try { applyCandidate(await userShellApi.reverseLookupLocation(position.coords.latitude, position.coords.longitude)); feedback.value = "Current location resolved. Review it before saving." }
    catch { error.value = "Could not resolve the current location." }
  }, () => { error.value = "Location permission was denied or unavailable. Search for a place instead." })
}

const save = async () => {
  if (!user.value) return
  if (mode.value !== "OFF" && !candidate.value) { error.value = "Select a valid location before enabling location."; return }
  if (mode.value === "EXACT" && exactVisibilityScope.value === "CIRCLES" && selectedCircleIds.value.length === 0) { error.value = "Select at least one circle for exact address visibility."; return }
  if (mode.value === "EXACT" && exactVisibilityScope.value === "USERS" && selectedUserIds.value.length === 0) { error.value = "Select at least one person for exact address visibility."; return }
  isSaving.value = true
  error.value = ""
  try {
    const settings = candidate.value
    user.value = await userShellApi.updateCurrentAppUser({
      email: user.value.email,
      username: username.value,
      profileDescription: profileDescription.value,
      profileAvatarDataUrl: avatarDataUrl.value,
      locationSettings: {
        mode: mode.value,
        defaultRadiusKm: radius.value,
        exactVisibilityScope: exactVisibilityScope.value,
        exactVisibleCircleIds: mode.value === "EXACT" && exactVisibilityScope.value === "CIRCLES" ? selectedCircleIds.value : [],
        exactVisibleUserIds: mode.value === "EXACT" && exactVisibilityScope.value === "USERS" ? selectedUserIds.value : [],
        provider: mode.value === "OFF" ? null : settings?.provider ?? null,
        providerPlaceId: mode.value === "OFF" ? null : settings?.providerPlaceId ?? null,
        label: mode.value === "OFF" ? null : settings?.label ?? null,
        countryCode: mode.value === "OFF" ? null : settings?.countryCode ?? null,
        country: mode.value === "OFF" ? null : settings?.country ?? null,
        locality: mode.value === "OFF" ? null : settings?.locality ?? null,
        postalCode: mode.value === "OFF" ? null : settings?.postalCode ?? null,
        street: mode.value === "OFF" ? null : settings?.street ?? null,
        houseNumber: mode.value === "OFF" ? null : settings?.houseNumber ?? null,
        latitude: mode.value === "OFF" ? null : settings?.latitude ?? null,
        longitude: mode.value === "OFF" ? null : settings?.longitude ?? null,
        resolvedAt: mode.value === "OFF" ? null : settings?.resolvedAt ?? null
      }
    })
    updateSessionUser({profileAvatarDataUrl: user.value.profileAvatarDataUrl, username: user.value.username, profileDescription: user.value.profileDescription})
    feedback.value = "Profile and location settings saved."
  } catch { error.value = "Could not save location settings. Select a resolved place and try again." }
  finally { isSaving.value = false }
}

onMounted(() => void load())
</script>

<template>
  <section class="location-settings">
    <header><p class="eyebrow">Profile settings</p><h1>Location</h1><p class="intro">Choose how your location supports nearby discovery. Exact address visibility remains governed by your existing privacy settings.</p><RouterLink class="settings-link" to="/profile/settings/notifications">Manage notification preferences</RouterLink></header>
    <p v-if="error" class="status status--error" role="alert">{{ error }}</p>
    <p v-if="feedback" class="status" role="status">{{ feedback }}</p>
    <form v-if="!isLoading" @submit.prevent="save">
      <fieldset class="profile-fields"><legend>Profile</legend><div class="avatar-editor"><img v-if="avatarDataUrl" :src="avatarDataUrl" alt="Profile preview" class="avatar-editor__image"><span v-else class="avatar-editor__image avatar-editor__image--fallback">{{ (username[0] ?? "A").toUpperCase() }}</span><div><label>Profile picture<input type="file" accept="image/*" @change="selectAvatar"></label><button v-if="avatarDataUrl" type="button" class="avatar-editor__remove" @click="removeAvatar">Remove picture</button><small>Optional. Use a clear image under 2 MB.</small></div></div><label>Username<InlineEditText :model-value="username" label="username" @save="username = $event" /></label><label>Description<textarea v-model="profileDescription" maxlength="2000" rows="4" placeholder="What should people know about you?"></textarea><small>{{ profileDescription.length }}/2000</small></label></fieldset>
      <fieldset class="profile-gallery"><legend>Photo gallery</legend><p class="profile-gallery__intro">Optional photos for your profile. These are separate from your profile picture.</p><div class="profile-gallery__form"><label>Image URL<input v-model="galleryUrl" type="url" placeholder="https://…"></label><label>Alt text<input v-model="galleryAltText" maxlength="240" placeholder="Describe the photo"></label><button type="button" :disabled="isGalleryActing" @click="addGalleryImage">Add photo</button></div><div v-if="gallery.length" class="profile-gallery__grid"><article v-for="image in gallery" :key="image.id" class="profile-gallery__item"><img :src="image.imageUrl" :alt="image.altText || 'Profile gallery photo'"><button type="button" :disabled="isGalleryActing" @click="removeGalleryImage(image)">Remove</button></article></div><p v-else class="profile-gallery__empty">No profile gallery photos yet.</p></fieldset>
      <fieldset><legend>Location</legend><label>Location mode<select v-model="mode"><option value="OFF">Off</option><option value="APPROXIMATE">Approximate</option><option value="EXACT">Exact</option></select></label>
      <label>Default search radius (km)<input v-model.number="radius" type="number" min="1" max="200"></label>
      <label v-if="mode === 'EXACT'">Exact address visibility<select v-model="exactVisibilityScope"><option value="NOBODY">Nobody</option><option value="EVERYONE">Everyone</option><option value="CIRCLES">Selected circles</option><option value="USERS">Selected people</option></select><small>Choose exactly who may see the resolved address.</small></label>
      <fieldset v-if="mode === 'EXACT' && exactVisibilityScope === 'CIRCLES'" class="visibility-options"><legend>Visible circles</legend><label v-for="group in circleGroups" :key="group.id" class="check-option"><input v-model="selectedCircleIds" type="checkbox" :value="group.id">{{ group.name }}</label><small v-if="circleGroups.length === 0">Create or join a circle before sharing an exact address with circles.</small></fieldset>
      <fieldset v-if="mode === 'EXACT' && exactVisibilityScope === 'USERS'" class="visibility-options"><legend>Visible people</legend><label v-for="person in circleConnections" :key="person.userId" class="check-option"><input v-model="selectedUserIds" type="checkbox" :value="person.userId">{{ person.username }}</label><small v-if="circleConnections.length === 0">Connect with someone through Circles before sharing an exact address with them.</small></fieldset>
      <div class="location-search"><label>Place or address<input v-model="query" placeholder="Search for a place" @input="handleLocationQueryInput"></label><button type="button" @click="useCurrentLocation">Use current location</button></div>
      <div v-if="isSearching" class="status">Searching…</div>
      <ul v-if="suggestions.length" class="suggestions"><li v-for="item in suggestions" :key="`${item.providerPlaceId}-${item.label}`"><button type="button" @click="applyCandidate(item)">{{ item.label }}</button></li></ul>
      <div v-if="candidate" class="selected"><strong>Resolved place</strong><span>{{ candidate.label }}</span><small>{{ candidate.locality || candidate.country || "Ready to save" }}</small></div>
      </fieldset><button class="save" type="submit" :disabled="isSaving">{{ isSaving ? "Saving…" : "Save profile and location" }}</button>
    </form>
  </section>
</template>

<style scoped>
.location-settings{display:grid;gap:1rem;max-width:44rem}.eyebrow{margin:0 0 .3rem;color:var(--text-muted);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.intro{max-width:38rem;color:var(--text-muted)}form{display:grid;gap:.9rem;padding:1rem;border:1px solid var(--border-subtle);border-radius:1rem;background:var(--surface)}fieldset{display:grid;gap:.9rem;border:0;padding:0;margin:0}legend{font-weight:700;padding:0}label{display:grid;gap:.35rem;font-weight:600}input,select,textarea{width:100%;box-sizing:border-box;border:1px solid var(--border-subtle);border-radius:.65rem;padding:.65rem .75rem;background:var(--bg-raised);font:inherit}.avatar-editor{display:flex;align-items:center;gap:.8rem}.avatar-editor__image{width:4.5rem;height:4.5rem;flex:0 0 4.5rem;border-radius:50%;object-fit:cover;border:1px solid var(--border-subtle)}.avatar-editor__image--fallback{display:grid;place-items:center;background:var(--surface-muted);font-size:1.4rem;font-weight:700}.avatar-editor__remove{justify-self:start;border:0;background:transparent;color:var(--danger);text-decoration:underline;cursor:pointer;font:inherit;font-size:.8rem}.profile-gallery{padding-top:.9rem;border:1px solid var(--border-subtle)}.profile-gallery__intro,.profile-gallery__empty{margin:0;color:var(--text-muted);font-size:.82rem}.profile-gallery__form{display:grid;grid-template-columns:1fr 1fr auto;gap:.55rem;align-items:end}.profile-gallery__form button{border:1px solid var(--border-subtle);border-radius:999px;padding:.65rem .85rem;background:var(--accent);color:var(--text);font:inherit;cursor:pointer}.profile-gallery__grid{display:grid;grid-template-columns:repeat(3,1fr);gap:.7rem}.profile-gallery__item{display:grid;gap:.4rem}.profile-gallery__item img{width:100%;aspect-ratio:4/3;object-fit:cover;border-radius:.7rem;background:rgba(23,34,26,.06)}.profile-gallery__item button{justify-self:start;border:0;background:transparent;color:var(--danger);text-decoration:underline;cursor:pointer;font:inherit;font-size:.78rem}.check-option{display:flex;grid-template-columns:none;align-items:center;gap:.5rem;font-weight:500}.check-option input{width:auto}.visibility-options{padding:.7rem;border-radius:.7rem;background:var(--surface-muted)}.location-search{display:grid;grid-template-columns:1fr auto;align-items:end;gap:.6rem}.location-search button,.save,.suggestions button{border:1px solid var(--border-subtle);border-radius:999px;padding:.65rem .85rem;background:var(--accent);color:var(--text);font:inherit;cursor:pointer}.suggestions{display:grid;gap:.35rem;margin:0;padding:0;list-style:none}.suggestions button{width:100%;text-align:left;background:var(--surface-muted);color:var(--text)}.selected{display:grid;gap:.2rem;padding:.75rem;border-radius:.7rem;background:var(--surface-muted)}.selected small{color:var(--text-muted)}.status{margin:0;color:var(--text-muted)}.status--error{color:var(--danger)}.save{justify-self:start}.save:disabled{opacity:.55;cursor:wait}@media(max-width:600px){.location-search,.profile-gallery__form{grid-template-columns:1fr}.location-search button{justify-self:start}.profile-gallery__grid{grid-template-columns:1fr 1fr}}
</style>
