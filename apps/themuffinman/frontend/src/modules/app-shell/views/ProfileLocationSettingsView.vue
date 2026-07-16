<script setup lang="ts">
import {onMounted, ref} from "vue"
import type {AppUserResponseDTO, LocationLookupCandidateDTO, UserLocationMode} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"

const user = ref<AppUserResponseDTO | null>(null)
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

const applyCandidate = (value: LocationLookupCandidateDTO) => {
  candidate.value = value
  query.value = value.label
  suggestions.value = []
  error.value = ""
}

const load = async () => {
  isLoading.value = true
  try {
    user.value = await userShellApi.getCurrentAppUser()
    const settings = user.value.locationSettings
    mode.value = settings.mode
    exactVisibilityScope.value = settings.exactVisibilityScope
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
  isSaving.value = true
  error.value = ""
  try {
    const settings = candidate.value
    user.value = await userShellApi.updateCurrentAppUser({
      email: user.value.email,
      username: user.value.username,
      profileDescription: user.value.profileDescription,
      profileAvatarDataUrl: user.value.profileAvatarDataUrl,
      locationSettings: {
        mode: mode.value,
        defaultRadiusKm: radius.value,
        exactVisibilityScope: exactVisibilityScope.value,
        exactVisibleCircleIds: user.value.locationSettings.exactVisibleCircleIds,
        exactVisibleUserIds: user.value.locationSettings.exactVisibleUserIds,
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
    feedback.value = "Location settings saved."
  } catch { error.value = "Could not save location settings. Select a resolved place and try again." }
  finally { isSaving.value = false }
}

onMounted(() => void load())
</script>

<template>
  <section class="location-settings">
    <header><p class="eyebrow">Profile settings</p><h1>Location</h1><p class="intro">Choose how your location supports nearby discovery. Exact address visibility remains governed by your existing privacy settings.</p></header>
    <p v-if="error" class="status status--error" role="alert">{{ error }}</p>
    <p v-if="feedback" class="status" role="status">{{ feedback }}</p>
    <form v-if="!isLoading" @submit.prevent="save">
      <label>Location mode<select v-model="mode"><option value="OFF">Off</option><option value="APPROXIMATE">Approximate</option><option value="EXACT">Exact</option></select></label>
      <label>Default search radius (km)<input v-model.number="radius" type="number" min="1" max="200"></label>
      <label v-if="mode === 'EXACT'">Exact address visibility<select v-model="exactVisibilityScope"><option value="NOBODY">Nobody</option><option value="EVERYONE">Everyone</option><option value="CIRCLES">Selected circles</option><option value="USERS">Selected people</option></select><small>Backend validation still requires valid selected circles or people for scoped sharing.</small></label>
      <div class="location-search"><label>Place or address<input v-model="query" placeholder="Search for a place" @input="void search"></label><button type="button" @click="useCurrentLocation">Use current location</button></div>
      <div v-if="isSearching" class="status">Searching…</div>
      <ul v-if="suggestions.length" class="suggestions"><li v-for="item in suggestions" :key="`${item.providerPlaceId}-${item.label}`"><button type="button" @click="applyCandidate(item)">{{ item.label }}</button></li></ul>
      <div v-if="candidate" class="selected"><strong>Resolved place</strong><span>{{ candidate.label }}</span><small>{{ candidate.locality || candidate.country || "Ready to save" }}</small></div>
      <button class="save" type="submit" :disabled="isSaving">{{ isSaving ? "Saving…" : "Save location settings" }}</button>
    </form>
  </section>
</template>

<style scoped>
.location-settings{display:grid;gap:1rem;max-width:44rem}.eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.intro{max-width:38rem;color:rgba(23,34,26,.65)}form{display:grid;gap:.9rem;padding:1rem;border:1px solid rgba(23,34,26,.08);border-radius:1rem;background:rgba(255,255,255,.62)}label{display:grid;gap:.35rem;font-weight:600}input,select{width:100%;box-sizing:border-box;border:1px solid rgba(23,34,26,.14);border-radius:.65rem;padding:.65rem .75rem;background:white;font:inherit}.location-search{display:grid;grid-template-columns:1fr auto;align-items:end;gap:.6rem}.location-search button,.save,.suggestions button{border:1px solid rgba(23,34,26,.14);border-radius:999px;padding:.65rem .85rem;background:#17221a;color:#f8f8f4;font:inherit;cursor:pointer}.suggestions{display:grid;gap:.35rem;margin:0;padding:0;list-style:none}.suggestions button{width:100%;text-align:left;background:rgba(214,228,218,.7);color:#17221a}.selected{display:grid;gap:.2rem;padding:.75rem;border-radius:.7rem;background:rgba(214,228,218,.55)}.selected small{color:rgba(23,34,26,.6)}.status{margin:0;color:rgba(23,34,26,.65)}.status--error{color:#8d2f25}.save{justify-self:start}.save:disabled{opacity:.55;cursor:wait}@media(max-width:600px){.location-search{grid-template-columns:1fr}.location-search button{justify-self:start}}
</style>
