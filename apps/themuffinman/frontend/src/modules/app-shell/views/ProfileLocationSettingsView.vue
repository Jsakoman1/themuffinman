<script setup lang="ts">
import {onMounted, ref} from "vue"
import {RouterLink, useRoute} from "vue-router"
import {updateSessionUser} from "../../identity/auth.ts"
import type {AppUserResponseDTO, CircleContactDTO, CircleGroupResponseDTO, LocationLookupCandidateDTO, ProfileFieldVisibility, UserLocationMode, LocationResolutionStatus} from "../../../contracts/index.ts"
import {userShellApi, type AppearancePreference, type ProfileGalleryImage} from "../api/userShellApi.ts"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppButton from "../components/AppButton.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import InlineEditText from "../components/InlineEditText.vue"
import GuidedIntakePanel from "../components/GuidedIntakePanel.vue"

// Optional location panels fail independently so profile editing remains usable.
const user = ref<AppUserResponseDTO | null>(null)
const username = ref("")
const profileDescription = ref("")
const avatarDataUrl = ref<string | null>(null)
const profileDescriptionVisibility = ref<ProfileFieldVisibility>("PUBLIC")
const profileAvatarVisibility = ref<ProfileFieldVisibility>("PUBLIC")
const profileDescriptionVisibleCircleIds = ref<number[]>([])
const profileAvatarVisibleCircleIds = ref<number[]>([])
const circleGroups = ref<CircleGroupResponseDTO[]>([])
const circleConnections = ref<CircleContactDTO[]>([])
const selectedCircleIds = ref<number[]>([])
const selectedUserIds = ref<number[]>([])
const mode = ref<UserLocationMode>("OFF")
const exactVisibilityScope = ref<"NOBODY" | "EVERYONE" | "CIRCLES" | "USERS">("NOBODY")
const radius = ref(25)
const resolutionStatus = ref<LocationResolutionStatus>("OFF")
const providerConfigured = ref(true)
const providerUnavailable = ref(false)
const currentLocationOutcome = ref<"IDLE" | "BROWSER_UNAVAILABLE" | "PERMISSION_DENIED" | "PROVIDER_NOT_CONFIGURED" | "PROVIDER_UNAVAILABLE" | "NOT_RESOLVED">("IDLE")
const query = ref("")
const candidate = ref<LocationLookupCandidateDTO | null>(null)
const suggestions = ref<LocationLookupCandidateDTO[]>([])
const isLoading = ref(true)
const isSearching = ref(false)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const guidedProfileOpen = ref(false)
const acceptGuidedProfileDraft = (draft: Record<string, string>) => { profileDescription.value = draft.description ?? profileDescription.value; guidedProfileOpen.value = false; feedback.value = "Guided profile draft ready to review." }
const gallery = ref<ProfileGalleryImage[]>([])
const galleryUrl = ref("")
const galleryAltText = ref("")
const isGalleryActing = ref(false)
const route = useRoute()
const exactVisibilityInput = ref<HTMLSelectElement | null>(null)
const appearanceTheme = ref<AppearancePreference["theme"]>("SYSTEM")
const isAppearanceSaving = ref(false)

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
    const [currentUser, groups, connections, galleryResponse, appearance] = await Promise.all([
      userShellApi.getCurrentAppUser(),
      userShellApi.getCircleGroups(),
      userShellApi.getCircleConnections(),
      userShellApi.getMyProfileGallery(),
      userShellApi.getAppearancePreference()
    ])
    user.value = currentUser
    circleGroups.value = groups
    circleConnections.value = connections.items
    gallery.value = galleryResponse.items
    appearanceTheme.value = appearance.theme
    username.value = user.value.username
    profileDescription.value = user.value.profileDescription ?? ""
    avatarDataUrl.value = user.value.profileAvatarDataUrl ?? null
    profileDescriptionVisibility.value = user.value.profileDescriptionVisibility ?? "PUBLIC"
    profileAvatarVisibility.value = user.value.profileAvatarVisibility ?? "PUBLIC"
    profileDescriptionVisibleCircleIds.value = [...(user.value.profileDescriptionVisibleCircleIds ?? [])]
    profileAvatarVisibleCircleIds.value = [...(user.value.profileAvatarVisibleCircleIds ?? [])]
    const settings = user.value.locationSettings
    mode.value = settings.mode
    resolutionStatus.value = settings.resolutionStatus ?? (settings.hasCoordinates ? "RESOLVED" : settings.mode === "OFF" ? "OFF" : "NEEDS_RESOLUTION")
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

const updateAppearance = async () => {
  isAppearanceSaving.value = true
  try {
    const updated = await userShellApi.updateAppearancePreference(appearanceTheme.value)
    appearanceTheme.value = updated.theme
    window.dispatchEvent(new CustomEvent("app:appearance-changed", {detail: updated.theme}))
    feedback.value = "Appearance updated."
  } catch {
    error.value = "Could not update appearance."
  } finally {
    isAppearanceSaving.value = false
  }
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
  try {
    const response = await userShellApi.lookupLocations(query.value.trim())
    providerConfigured.value = response.configured
    providerUnavailable.value = response.resolutionStatus === "PROVIDER_UNAVAILABLE"
    suggestions.value = response.items
    if (!response.configured || providerUnavailable.value) error.value = "Location search is temporarily unavailable. Retry later or use another correction path."
  }
  catch { error.value = "Could not search this location." }
  finally { isSearching.value = false }
}

const useCurrentLocation = () => {
  if (!navigator.geolocation) { currentLocationOutcome.value = "BROWSER_UNAVAILABLE"; error.value = "Current location is not available in this browser."; return }
  error.value = ""
  currentLocationOutcome.value = "IDLE"
  navigator.geolocation.getCurrentPosition(async position => {
    try { applyCandidate(await userShellApi.reverseLookupLocation(position.coords.latitude, position.coords.longitude)); feedback.value = "Current location resolved. Review it before saving." }
    catch (cause: any) {
      const code = cause?.response?.data?.code
      currentLocationOutcome.value = code === "LOCATION_PROVIDER_NOT_CONFIGURED" ? "PROVIDER_NOT_CONFIGURED" : code === "LOCATION_PROVIDER_UNAVAILABLE" ? "PROVIDER_UNAVAILABLE" : "NOT_RESOLVED"
      error.value = code === "LOCATION_PROVIDER_UNAVAILABLE" ? "The location provider is temporarily unavailable. Retry or search for a place instead." : code === "LOCATION_PROVIDER_NOT_CONFIGURED" ? "Current location lookup is not configured. Search for a place instead." : "Could not resolve the current location. Search for a place instead."
    }
  }, (cause) => { currentLocationOutcome.value = cause.code === 1 ? "PERMISSION_DENIED" : "BROWSER_UNAVAILABLE"; error.value = cause.code === 1 ? "Location permission was denied. Search for a place instead." : "Current location is unavailable. Retry or search for a place instead." })
}

const clearCandidate = () => { candidate.value = null; query.value = ""; suggestions.value = []; resolutionStatus.value = mode.value === "OFF" ? "OFF" : "NEEDS_RESOLUTION" }

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
      profileDescriptionVisibility: profileDescriptionVisibility.value,
      profileAvatarVisibility: profileAvatarVisibility.value,
      profileDescriptionVisibleCircleIds: profileDescriptionVisibility.value === "CIRCLES" ? profileDescriptionVisibleCircleIds.value : [],
      profileAvatarVisibleCircleIds: profileAvatarVisibility.value === "CIRCLES" ? profileAvatarVisibleCircleIds.value : [],
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
    resolutionStatus.value = user.value.locationSettings.resolutionStatus
    feedback.value = "Profile and location settings saved."
  } catch { error.value = "Could not save location settings. Select a resolved place and try again." }
  finally { isSaving.value = false }
}

onMounted(async () => {
  await load()
  if (route.query.location === "current") useCurrentLocation()
  if (route.query.visibility === "1") window.setTimeout(() => exactVisibilityInput.value?.focus(), 0)
})
</script>

<template>
  <section class="location-settings">
    <header><p class="eyebrow">Profile settings</p><h1>Location</h1><p class="intro">Choose how your location supports nearby discovery. Exact address visibility remains governed by your existing privacy settings.</p><RouterLink class="settings-link" to="/profile/settings/notifications">Manage notification preferences</RouterLink></header>
    <CollectionToolbar title="Profile and location" :busy="isLoading"><template #actions><RouterLink to="/profile/settings/notifications">Notification preferences</RouterLink></template></CollectionToolbar>
    <div class="guided-profile-entry"><AppButton type="button" tone="secondary" @click="guidedProfileOpen = !guidedProfileOpen">{{ guidedProfileOpen ? "Close guided profile setup" : "Use guided profile setup" }}</AppButton><GuidedIntakePanel v-if="guidedProfileOpen" flow="identity.profile.update" title="Update your profile" description="Answer the meaningful profile questions first, then review the rest of your privacy settings." @completed="acceptGuidedProfileDraft" @cancel="guidedProfileOpen = false" /></div>
    <AppStatus v-if="error" :message="error" tone="error" />
    <AppStatus v-if="feedback" :message="feedback" tone="success" />
    <AppStatus v-if="isLoading" message="Loading profile and location settings." busy />
    <AppStatus v-if="!isLoading && resolutionStatus === 'NEEDS_RESOLUTION'" message="Location is enabled but still needs a resolved place. Choose a search result or use current location, then review it before saving." tone="warning" />
    <AppStatus v-if="!isLoading && !providerConfigured" message="The location provider is unavailable. Existing saved settings remain intact; retry search later or use a different correction path." tone="warning" />
    <AppStatus v-if="!isLoading && providerUnavailable" message="The location provider did not respond. Existing saved settings remain intact; retry search later or choose another correction path." tone="warning" />
    <div v-if="!isLoading" class="location-settings__workspace">
    <form @submit.prevent="save">
      <fieldset class="profile-fields"><legend>Profile</legend><div class="avatar-editor"><img v-if="avatarDataUrl" :src="avatarDataUrl" alt="Profile preview" class="avatar-editor__image"><span v-else class="avatar-editor__image avatar-editor__image--fallback">{{ (username[0] ?? "A").toUpperCase() }}</span><div><label>Profile picture<input type="file" accept="image/*" @change="selectAvatar"></label><AppButton v-if="avatarDataUrl" type="button" tone="danger" class="avatar-editor__remove" @click="removeAvatar">Remove picture</AppButton><small>Optional. Use a clear image under 2 MB.</small></div></div><AppFormField label="Username"><InlineEditText :model-value="username" label="username" @save="username = $event" /></AppFormField><AppFormField label="Description" :hint="`${profileDescription.length}/2000`"><textarea v-model="profileDescription" maxlength="2000" rows="4" placeholder="What should people know about you?"></textarea></AppFormField><div class="profile-visibility-grid"><AppFormField label="Profile picture visibility"><select v-model="profileAvatarVisibility" aria-label="Profile picture visibility"><option value="PUBLIC">Everyone</option><option value="PRIVATE">Only me</option><option value="CIRCLES">Selected circles</option></select></AppFormField><AppFormField label="Description visibility"><select v-model="profileDescriptionVisibility" aria-label="Description visibility"><option value="PUBLIC">Everyone</option><option value="PRIVATE">Only me</option><option value="CIRCLES">Selected circles</option></select></AppFormField></div><fieldset v-if="profileAvatarVisibility === 'CIRCLES'" class="visibility-options"><legend>Profile picture circles</legend><label v-for="group in circleGroups" :key="`avatar-${group.id}`" class="check-option"><input v-model="profileAvatarVisibleCircleIds" type="checkbox" :value="group.id">{{ group.name }}</label><small v-if="circleGroups.length === 0">Create a circle before sharing your profile picture with it.</small></fieldset><fieldset v-if="profileDescriptionVisibility === 'CIRCLES'" class="visibility-options"><legend>Description circles</legend><label v-for="group in circleGroups" :key="`description-${group.id}`" class="check-option"><input v-model="profileDescriptionVisibleCircleIds" type="checkbox" :value="group.id">{{ group.name }}</label><small v-if="circleGroups.length === 0">Create a circle before sharing your description with it.</small></fieldset><small class="profile-visibility-note">Visibility is consent-based and enforced by the backend. Selected-circle access applies only to members of the circles you choose.</small></fieldset>
      <fieldset class="profile-gallery"><legend>Photo gallery</legend><p class="profile-gallery__intro">Optional photos for your profile. These are separate from your profile picture.</p><div class="profile-gallery__form"><AppFormField label="Image URL"><input v-model="galleryUrl" type="url" placeholder="https://…" aria-label="Image URL"></AppFormField><AppFormField label="Alt text" optional><input v-model="galleryAltText" maxlength="240" placeholder="Describe the photo" aria-label="Alt text"></AppFormField><AppButton type="button" tone="primary" :loading="isGalleryActing" @click="addGalleryImage">Add photo</AppButton></div><div v-if="gallery.length" class="profile-gallery__grid"><article v-for="image in gallery" :key="image.id" class="profile-gallery__item"><img :src="image.imageUrl" :alt="image.altText || 'Profile gallery photo'"><AppButton type="button" tone="danger" :loading="isGalleryActing" @click="removeGalleryImage(image)">Remove</AppButton></article></div><AppStatus v-else message="No profile gallery photos yet." /></fieldset>
      <fieldset class="appearance-settings"><legend>Appearance</legend><p class="profile-gallery__intro">Choose the Web workspace theme. VisionForWeb follows this Web setting; the future standalone Vision console will have its own shell.</p><AppFormField label="Theme"><select v-model="appearanceTheme" aria-label="Theme"><option value="SYSTEM">System</option><option value="DARK">Dark</option><option value="LIGHT">Light</option></select></AppFormField><AppButton type="button" tone="primary" :loading="isAppearanceSaving" @click="updateAppearance">Save appearance</AppButton></fieldset>
      <fieldset><legend>Location</legend><AppFormField label="Location mode"><select v-model="mode" aria-label="Location mode"><option value="OFF">Off</option><option value="APPROXIMATE">Approximate</option><option value="EXACT">Exact</option></select></AppFormField>
      <AppFormField label="Default search radius (km)"><input v-model.number="radius" type="number" min="1" max="200"></AppFormField>
      <AppFormField v-if="mode === 'EXACT'" label="Exact address visibility" hint="Choose exactly who may see the resolved address."><select ref="exactVisibilityInput" v-model="exactVisibilityScope" aria-label="Exact address visibility"><option value="NOBODY">Nobody</option><option value="EVERYONE">Everyone</option><option value="CIRCLES">Selected circles</option><option value="USERS">Selected people</option></select></AppFormField>
      <fieldset v-if="mode === 'EXACT' && exactVisibilityScope === 'CIRCLES'" class="visibility-options"><legend>Visible circles</legend><label v-for="group in circleGroups" :key="group.id" class="check-option"><input v-model="selectedCircleIds" type="checkbox" :value="group.id">{{ group.name }}</label><small v-if="circleGroups.length === 0">Create or join a circle before sharing an exact address with circles.</small></fieldset>
      <fieldset v-if="mode === 'EXACT' && exactVisibilityScope === 'USERS'" class="visibility-options"><legend>Visible people</legend><label v-for="person in circleConnections" :key="person.userId" class="check-option"><input v-model="selectedUserIds" type="checkbox" :value="person.userId">{{ person.username }}</label><small v-if="circleConnections.length === 0">Connect with someone through Circles before sharing an exact address with them.</small></fieldset>
      <div class="location-search"><AppFormField label="Place or address"><input v-model="query" placeholder="Search for a place" @input="handleLocationQueryInput"></AppFormField><AppButton type="button" tone="secondary" @click="useCurrentLocation">Use current location</AppButton></div>
      <AppStatus v-if="isSearching" message="Searching for locations." busy />
      <ul v-if="suggestions.length" class="suggestions"><li v-for="item in suggestions" :key="`${item.providerPlaceId}-${item.label}`"><AppButton type="button" tone="quiet" @click="applyCandidate(item)">{{ item.label }}</AppButton></li></ul>
      <div v-if="candidate" class="selected"><strong>Resolved place</strong><span>{{ candidate.label }}</span><small>{{ candidate.locality || candidate.country || "Ready to save" }}</small><AppButton type="button" tone="quiet" @click="clearCandidate">Choose a different place</AppButton></div>
      </fieldset><AppFormFooter sticky><template #secondary><span>Location visibility is enforced by the backend.</span></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Saving…" : "Save profile and location" }}</AppButton></template></AppFormFooter>
    </form>
    <aside class="location-settings__utility" aria-label="Location privacy context">
      <p class="location-settings__utility-eyebrow">Privacy context</p>
      <h2>Location sharing</h2>
      <p>Choose a useful discovery signal while keeping exact address access explicit and backend-authoritative.</p>
      <dl>
        <div><dt>Mode</dt><dd>{{ mode === "EXACT" ? "Exact" : mode === "APPROXIMATE" ? "Approximate" : "Off" }}</dd></div>
        <div><dt>Exact scope</dt><dd>{{ exactVisibilityScope.toLowerCase() }}</dd></div>
      </dl>
      <p class="location-settings__utility-note">Saving validates consent and visibility rules on the server. This panel is a summary, not a second edit surface.</p>
    </aside>
    </div>
  </section>
</template>

<style scoped>
.location-settings{display:grid;gap:var(--space-3);max-width:60rem}.location-settings__workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(16rem,20rem);gap:var(--space-3);align-items:start}.location-settings__workspace form{min-width:0}.location-settings__utility{display:grid;gap:var(--space-2);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised);color:var(--text-muted)}.location-settings__utility h2{margin:0;color:var(--text);font-size:var(--text-size-title)}.location-settings__utility p{margin:0;line-height:1.5}.location-settings__utility-eyebrow{color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.location-settings__utility dl{display:grid;gap:var(--space-2);margin:var(--space-2) 0}.location-settings__utility dl div{display:flex;justify-content:space-between;gap:var(--space-2);border-top:1px solid var(--border-subtle);padding-top:var(--space-2)}.location-settings__utility dt{color:var(--text-soft);font-size:var(--text-size-meta)}.location-settings__utility dd{margin:0;color:var(--text);font-weight:var(--text-weight-semibold);text-transform:capitalize}.location-settings__utility-note{font-size:var(--text-size-meta);color:var(--text-soft)!important}.eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.intro{max-width:38rem;color:var(--text-muted)}form{display:grid;gap:var(--space-4);padding:var(--space-4);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}fieldset{display:grid;gap:var(--space-3);border:0;padding:0;margin:0}legend{color:var(--text);font-size:var(--text-size-title);font-weight:var(--text-weight-semibold);padding:0}label{display:grid;gap:var(--space-1);font-weight:var(--text-weight-semibold)}input,select,textarea{width:100%;box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}.avatar-editor{display:flex;align-items:center;gap:var(--space-3)}.avatar-editor__image{width:4.5rem;height:4.5rem;flex:0 0 4.5rem;border-radius:50%;object-fit:cover;border:1px solid var(--border-subtle)}.avatar-editor__image--fallback{display:grid;place-items:center;background:var(--surface-muted);font-size:1.4rem;font-weight:700}.avatar-editor__remove{justify-self:start;border:0;background:transparent;color:var(--danger);text-decoration:underline;cursor:pointer;font:inherit;font-size:var(--text-size-meta)}.profile-gallery{padding-top:var(--space-3);border-top:1px solid var(--border-subtle)}.profile-gallery__intro{margin:0;color:var(--text-muted);font-size:var(--text-size-meta)}.profile-gallery__form{display:grid;grid-template-columns:1fr 1fr auto;gap:var(--space-2);align-items:end}.profile-gallery__form button,.location-search button,.suggestions button{min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-3);background:var(--accent);color:var(--canvas);font:inherit;font-weight:var(--text-weight-semibold);cursor:pointer}.profile-gallery__grid{display:grid;grid-template-columns:repeat(3,1fr);gap:var(--space-2)}.profile-gallery__item{display:grid;gap:var(--space-1)}.profile-gallery__item img{width:100%;aspect-ratio:4/3;object-fit:cover;border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface-muted)}.profile-gallery__item button{justify-self:start;border:0;background:transparent;color:var(--danger);text-decoration:underline;cursor:pointer;font:inherit;font-size:var(--text-size-meta)}.check-option{display:flex;grid-template-columns:none;align-items:center;gap:var(--space-2);font-weight:var(--text-weight-medium)}.check-option input{width:auto}.visibility-options{padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}.location-search{display:grid;grid-template-columns:1fr auto;align-items:end;gap:var(--space-2)}.suggestions{display:grid;gap:var(--space-1);margin:0;padding:0;list-style:none}.suggestions button{width:100%;text-align:left;background:var(--surface-raised);color:var(--text)}.selected{display:grid;gap:var(--space-1);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}.selected small{color:var(--text-muted)}.app-form-footer__secondary{color:var(--text-soft);font-size:var(--text-size-meta)}@media(max-width:860px){.location-settings__workspace{grid-template-columns:1fr}.location-settings__utility{order:2}}@media(max-width:600px){.location-search,.profile-gallery__form{grid-template-columns:1fr}.location-search button{justify-self:start}.profile-gallery__grid{grid-template-columns:1fr 1fr}}
</style>
<style scoped>
.avatar-editor__remove { justify-self: start; }
.profile-gallery__form .app-button, .location-search .app-button { min-width: max-content; }
.profile-gallery__item .app-button { justify-self: start; }
.suggestions .app-button { width: 100%; justify-content: flex-start; background: var(--surface-raised); color: var(--text); }
.profile-visibility-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--space-2); }
.profile-visibility-note { color: var(--text-soft); font-size: var(--text-size-meta); }
@media (max-width: 600px) { .profile-visibility-grid { grid-template-columns: 1fr; } }
</style>
