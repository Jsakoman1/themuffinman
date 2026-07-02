<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, ref, watch} from "vue"
import {useRouter} from "vue-router"
import {currentUser} from "../../../auth.ts"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {PROFILE_IMAGE_PROCESSING_ERROR_MESSAGE} from "../../../shared/clientMessages.ts"
import {compressProfileAvatar} from "../../../shared/imageCompression.ts"
import {updateSessionUser} from "../../../services/sessionService.ts"
import {useDebouncedWatch} from "../../../composables/useDebouncedWatch.ts"
import type {
  AppUser,
  CircleContact,
  CircleGroup,
  ExactLocationVisibilityScopeOption,
  LocationLookupCandidate,
  LocationLookupResponse,
  LocationModeOption
} from "../../../contracts/index.ts"
import {visionApi} from "../api/visionApi.ts"
import {useUserProfileView} from "../composables/useUserProfileView.ts"
import {getProfileInitials} from "../../../shared/profileFormatting.ts"
import VisionDetailSurface from "../components/VisionDetailSurface.vue"

const router = useRouter()
const profileImageInputRef = ref<HTMLInputElement | null>(null)
const profileEmailDraft = ref("")
const profileUsernameDraft = ref("")
const profileDescriptionDraft = ref("")
const profileAvatarDraft = ref("")
const profileLocationMode = ref<NonNullable<NonNullable<AppUser["locationSettings"]>["mode"]>>("OFF")
const profileExactVisibilityScope = ref<NonNullable<NonNullable<AppUser["locationSettings"]>["exactVisibilityScope"]>>("NOBODY")
const profileExactVisibleCircleIds = ref<number[]>([])
const profileExactVisibleUserIds = ref<number[]>([])
const profileLocationProvider = ref("")
const profileLocationProviderPlaceId = ref("")
const profileLocationLabel = ref("")
const profileLocationCountry = ref("")
const profileLocationLocality = ref("")
const profileLocationPostalCode = ref("")
const profileLocationStreet = ref("")
const profileLocationHouseNumber = ref("")
const profileLocationLatitude = ref("")
const profileLocationLongitude = ref("")
const profileLocationResolvedAt = ref("")
const locationModeOptions = ref<LocationModeOption[]>([])
const exactVisibilityScopeOptions = ref<ExactLocationVisibilityScopeOption[]>([])
const myCircleOptions = ref<CircleGroup[]>([])
const myContactOptions = ref<CircleContact[]>([])
const locationSearchQuery = ref("")
const locationSuggestions = ref<LocationLookupCandidate[]>([])
const lookupConfigured = ref(false)
const isLookupLoading = ref(false)
const isUsingCurrentLocation = ref(false)
const locationError = ref("")
const originalProfileEmail = ref("")
const originalProfileUsername = ref("")
const originalProfileDescription = ref("")
const originalProfileAvatar = ref("")
const originalLocationSignature = ref("")
const locationSectionRef = ref<HTMLElement | null>(null)
const locationAutoFocused = ref(false)

const activeUserId = computed(() => currentUser.value?.id ?? null)

const {
  profile,
  isLoading,
  isSaving,
  error,
  isOwnProfile,
  bannerMessage,
  bannerTone,
  showMessage,
  clearProfile,
  fetchProfile
} = useUserProfileView({
  userId: () => activeUserId.value,
  enabled: () => true,
  loadErrorMessage: "Could not load profile setup."
})

const loadSetupOptions = async () => {
  try {
    const options = await visionApi.getAppUserOptions()
    locationModeOptions.value = options.locationModes
    exactVisibilityScopeOptions.value = options.exactLocationVisibilityScopes
  } catch {
    locationModeOptions.value = []
    exactVisibilityScopeOptions.value = []
  }

  try {
    myCircleOptions.value = await visionApi.getCircleGroups()
  } catch {
    myCircleOptions.value = []
  }

  try {
    const response = await visionApi.getCircleConnectionsPage({page: 0, size: 200})
    myContactOptions.value = response.items
  } catch {
    myContactOptions.value = []
  }
}

const syncDraft = () => {
  if (!profile.value || !isOwnProfile.value) {
    return
  }

  profileEmailDraft.value = profile.value.email ?? currentUser.value?.email ?? ""
  profileUsernameDraft.value = profile.value.username ?? currentUser.value?.username ?? ""
  profileDescriptionDraft.value = profile.value.profileDescription ?? ""
  profileAvatarDraft.value = profile.value.profileAvatarDataUrl ?? ""
  profileLocationMode.value = profile.value.locationSettings?.mode ?? "OFF"
  profileExactVisibilityScope.value = profile.value.locationSettings?.exactVisibilityScope ?? "NOBODY"
  profileExactVisibleCircleIds.value = [...(profile.value.locationSettings?.exactVisibleCircleIds ?? [])]
  profileExactVisibleUserIds.value = [...(profile.value.locationSettings?.exactVisibleUserIds ?? [])]
  profileLocationProvider.value = profile.value.locationSettings?.provider ?? ""
  profileLocationProviderPlaceId.value = profile.value.locationSettings?.providerPlaceId ?? ""
  profileLocationLabel.value = profile.value.locationSettings?.label ?? ""
  profileLocationCountry.value = profile.value.locationSettings?.country ?? ""
  profileLocationLocality.value = profile.value.locationSettings?.locality ?? ""
  profileLocationPostalCode.value = profile.value.locationSettings?.postalCode ?? ""
  profileLocationStreet.value = profile.value.locationSettings?.street ?? ""
  profileLocationHouseNumber.value = profile.value.locationSettings?.houseNumber ?? ""
  profileLocationLatitude.value = profile.value.locationSettings?.latitude != null ? String(profile.value.locationSettings.latitude) : ""
  profileLocationLongitude.value = profile.value.locationSettings?.longitude != null ? String(profile.value.locationSettings.longitude) : ""
  profileLocationResolvedAt.value = profile.value.locationSettings?.resolvedAt ?? ""
  locationSearchQuery.value = profileLocationLabel.value || buildManualAddressQuery()
  locationSuggestions.value = []
  locationError.value = ""
  originalProfileEmail.value = profileEmailDraft.value
  originalProfileUsername.value = profileUsernameDraft.value
  originalProfileDescription.value = profileDescriptionDraft.value
  originalProfileAvatar.value = profileAvatarDraft.value
  originalLocationSignature.value = buildLocationSignature()
}

const hasUnsavedChanges = computed(() =>
  profileEmailDraft.value !== originalProfileEmail.value
  || profileUsernameDraft.value !== originalProfileUsername.value
  || profileDescriptionDraft.value !== originalProfileDescription.value
  || profileAvatarDraft.value !== originalProfileAvatar.value
  || buildLocationSignature() !== originalLocationSignature.value
)

const hasSavedLocation = computed(() =>
  profileLocationMode.value !== "OFF"
  && (
    profileLocationLatitude.value.trim().length > 0
    || profileLocationLongitude.value.trim().length > 0
    || profileLocationLabel.value.trim().length > 0
  )
)

const buildLocationSignature = () => JSON.stringify({
  mode: profileLocationMode.value,
  exactVisibilityScope: profileExactVisibilityScope.value,
  exactVisibleCircleIds: [...profileExactVisibleCircleIds.value].sort((left, right) => left - right),
  exactVisibleUserIds: [...profileExactVisibleUserIds.value].sort((left, right) => left - right),
  provider: profileLocationProvider.value,
  providerPlaceId: profileLocationProviderPlaceId.value,
  label: profileLocationLabel.value,
  country: profileLocationCountry.value,
  locality: profileLocationLocality.value,
  postalCode: profileLocationPostalCode.value,
  street: profileLocationStreet.value,
  houseNumber: profileLocationHouseNumber.value,
  latitude: profileLocationLatitude.value,
  longitude: profileLocationLongitude.value,
  resolvedAt: profileLocationResolvedAt.value
})

const buildManualAddressQuery = () => {
  return [
    profileLocationStreet.value,
    profileLocationHouseNumber.value,
    profileLocationPostalCode.value,
    profileLocationLocality.value,
    profileLocationCountry.value
  ]
    .map((value) => value.trim())
    .filter(Boolean)
    .join(", ")
}

const clearLocationProviderMetadata = () => {
  profileLocationProvider.value = ""
  profileLocationProviderPlaceId.value = ""
  profileLocationResolvedAt.value = ""
}

const markAddressAsUnresolved = () => {
  clearLocationProviderMetadata()
  profileLocationLatitude.value = ""
  profileLocationLongitude.value = ""
  locationError.value = "Address changed. Click Resolve typed address to refresh it."
}

const clearSelectedLocation = () => {
  clearLocationProviderMetadata()
  profileLocationLabel.value = ""
  profileLocationCountry.value = ""
  profileLocationLocality.value = ""
  profileLocationPostalCode.value = ""
  profileLocationStreet.value = ""
  profileLocationHouseNumber.value = ""
  profileLocationLatitude.value = ""
  profileLocationLongitude.value = ""
  locationSearchQuery.value = ""
  locationSuggestions.value = []
  locationError.value = ""
}

const applyLocationCandidate = (candidate: LocationLookupCandidate) => {
  profileLocationProvider.value = candidate.provider ?? ""
  profileLocationProviderPlaceId.value = candidate.providerPlaceId ?? ""
  profileLocationLabel.value = candidate.label ?? ""
  profileLocationCountry.value = candidate.country ?? ""
  profileLocationLocality.value = candidate.locality ?? ""
  profileLocationPostalCode.value = candidate.postalCode ?? ""
  profileLocationStreet.value = candidate.street ?? ""
  profileLocationHouseNumber.value = candidate.houseNumber ?? ""
  profileLocationLatitude.value = String(candidate.latitude)
  profileLocationLongitude.value = String(candidate.longitude)
  profileLocationResolvedAt.value = candidate.resolvedAt ?? ""
  locationSearchQuery.value = candidate.label ?? buildManualAddressQuery()
  locationSuggestions.value = []
  locationError.value = ""
}

const searchLocations = async (queryOverride?: string): Promise<LocationLookupResponse | null> => {
  const sourceQuery = (queryOverride ?? locationSearchQuery.value).trim().replaceAll(/\s+/g, " ")
  if (sourceQuery.length < 3) {
    locationSuggestions.value = []
    locationError.value = ""
    return null
  }

  isLookupLoading.value = true
  locationError.value = ""

  try {
    const response = await visionApi.lookupLocation({query: sourceQuery})
    lookupConfigured.value = response.configured
    locationSuggestions.value = response.items
    if (!response.configured) {
      locationError.value = "Location provider is not configured."
    }
    return response
  } catch (requestError) {
    locationSuggestions.value = []
    locationError.value = getApiErrorMessage(requestError, "Could not search locations.")
    return null
  } finally {
    isLookupLoading.value = false
  }
}

const resolveManualAddress = async () => {
  const manualQuery = buildManualAddressQuery()
  if (manualQuery.length < 3) {
    locationError.value = "Enter at least city or address details before resolving."
    return
  }

  locationSearchQuery.value = manualQuery
  const response = await searchLocations(manualQuery)
  if (!response || !response.items.length) {
    locationError.value = locationError.value || "Could not resolve that address."
    return
  }

  applyLocationCandidate(response.items[0])
}

const useCurrentLocation = async () => {
  if (!navigator.geolocation) {
    locationError.value = "Browser geolocation is not available."
    return
  }

  isUsingCurrentLocation.value = true
  locationError.value = ""

  try {
    const position = await new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 30000
      })
    })

    const latitude = position.coords.latitude
    const longitude = position.coords.longitude
    profileLocationLatitude.value = String(latitude)
    profileLocationLongitude.value = String(longitude)

    try {
      const candidate = await visionApi.reverseLookupLocation({latitude, longitude})
      lookupConfigured.value = true
      applyLocationCandidate(candidate)
    } catch (requestError) {
      locationError.value = getApiErrorMessage(requestError, "Could not resolve your current location.")
      if (!profileLocationLabel.value.trim()) {
        profileLocationLabel.value = "Current location"
      }
    }
  } catch {
    locationError.value = "Could not access your current location."
  } finally {
    isUsingCurrentLocation.value = false
  }
}

const updateLocationCountry = (value: string) => {
  profileLocationCountry.value = value
  markAddressAsUnresolved()
}

const updateLocationLocality = (value: string) => {
  profileLocationLocality.value = value
  markAddressAsUnresolved()
}

const updateLocationPostalCode = (value: string) => {
  profileLocationPostalCode.value = value
  markAddressAsUnresolved()
}

const updateLocationStreet = (value: string) => {
  profileLocationStreet.value = value
  markAddressAsUnresolved()
}

const updateLocationHouseNumber = (value: string) => {
  profileLocationHouseNumber.value = value
  markAddressAsUnresolved()
}

const toggleExactVisibleCircle = (circleId: number) => {
  profileExactVisibleCircleIds.value = profileExactVisibleCircleIds.value.includes(circleId)
    ? profileExactVisibleCircleIds.value.filter((id) => id !== circleId)
    : [...profileExactVisibleCircleIds.value, circleId]
}

const toggleExactVisibleUser = (userId: number) => {
  profileExactVisibleUserIds.value = profileExactVisibleUserIds.value.includes(userId)
    ? profileExactVisibleUserIds.value.filter((id) => id !== userId)
    : [...profileExactVisibleUserIds.value, userId]
}

const openProfileImagePicker = () => {
  profileImageInputRef.value?.click()
}

const profileAvatarStyle = (size: number) => ({
  "--profile-avatar-size": `${size}px`
})

const updateProfileAvatarFromFile = async (file: File | null) => {
  if (!file) {
    profileAvatarDraft.value = ""
    return
  }

  try {
    profileAvatarDraft.value = await compressProfileAvatar(file)
  } catch {
    showMessage(PROFILE_IMAGE_PROCESSING_ERROR_MESSAGE, "warning")
  }
}

const clearProfileAvatar = () => {
  profileAvatarDraft.value = ""
}

const saveProfile = async () => {
  if (!currentUser.value || !profile.value || !isOwnProfile.value) {
    return
  }

  try {
    const response = await visionApi.updateCurrentAppUser({
      email: profileEmailDraft.value.trim(),
      username: profileUsernameDraft.value.trim(),
      profileDescription: profileDescriptionDraft.value.trim() || null,
      profileAvatarDataUrl: profileAvatarDraft.value || null,
      locationSettings: {
        mode: profileLocationMode.value,
        defaultRadiusKm: profile.value.locationSettings?.defaultRadiusKm ?? 10,
        exactVisibilityScope: profileExactVisibilityScope.value,
        exactVisibleCircleIds: profileExactVisibleCircleIds.value,
        exactVisibleUserIds: profileExactVisibleUserIds.value,
        provider: profileLocationProvider.value || null,
        providerPlaceId: profileLocationProviderPlaceId.value || null,
        label: profileLocationLabel.value || null,
        countryCode: null,
        country: profileLocationCountry.value || null,
        locality: profileLocationLocality.value || null,
        postalCode: profileLocationPostalCode.value || null,
        street: profileLocationStreet.value || null,
        houseNumber: profileLocationHouseNumber.value || null,
        latitude: profileLocationLatitude.value.trim() ? Number(profileLocationLatitude.value) : null,
        longitude: profileLocationLongitude.value.trim() ? Number(profileLocationLongitude.value) : null,
        resolvedAt: profileLocationResolvedAt.value || null
      }
    })

    updateSessionUser({
      email: response.email,
      username: response.username,
      profileDescription: response.profileDescription,
      profileAvatarDataUrl: response.profileAvatarDataUrl,
      createdAt: response.createdAt ?? currentUser.value.createdAt,
      role: response.role ?? currentUser.value.role
    })

    showMessage("Profile saved.", "success")
    await fetchProfile()
    await router.push(`/vision/users/${response.id}`)
  } catch (requestError) {
    showMessage(getApiErrorMessage(requestError, "Could not save profile."), "warning")
  }
}

const skipForNow = async () => {
  await router.push("/vision")
}

watch(() => activeUserId.value, () => {
  void fetchProfile()
  void loadSetupOptions()
}, {immediate: true})

watch(() => [profile.value?.id, isOwnProfile.value] as const, () => {
  syncDraft()
}, {immediate: true})

watch(() => [isLoading.value, profile.value?.id, hasSavedLocation.value] as const, async () => {
  if (isLoading.value || hasSavedLocation.value || locationAutoFocused.value) {
    return
  }

  await nextTick()
  locationSectionRef.value?.scrollIntoView({behavior: "smooth", block: "start"})
  locationAutoFocused.value = true
}, {immediate: true})

useDebouncedWatch(locationSearchQuery, () => {
  if (profileLocationMode.value === "OFF") {
    return
  }

  void searchLocations()
}, 350)

onBeforeUnmount(() => {
  clearProfile()
})
</script>

<template>
  <VisionDetailSurface
    eyebrow="Vision route"
    title="Complete your profile"
    subtitle="Set up the identity Vision will use when you talk, post, and join circles."
    @close="skipForNow"
  >
    <div class="vision-terminal-feed">
      <p v-if="bannerMessage" :class="['vision-terminal-feed__line', `vision-terminal-feed__line--${bannerTone}`]">
        {{ bannerMessage }}
      </p>
      <p class="vision-terminal-feed__line">> onboarding</p>
      <p v-if="isLoading" class="vision-terminal-feed__line vision-terminal-feed__line--soft">Loading profile setup...</p>
      <p v-else-if="error" class="vision-terminal-feed__line vision-terminal-feed__line--error">{{ error }}</p>
      <p v-else-if="!profile || !isOwnProfile" class="vision-terminal-feed__line vision-terminal-feed__line--soft">
        Profile setup is only available for your own account.
      </p>

      <template v-else>
        <section class="vision-terminal-feed__block">
          <p class="vision-terminal-feed__block-title">identity</p>
          <div class="vision-terminal-feed__avatar-row">
            <span class="profile-avatar" :style="profileAvatarStyle(88)">
              <img
                v-if="profileAvatarDraft"
                class="profile-avatar__image"
                :src="profileAvatarDraft"
                :alt="`${profileUsernameDraft || 'User'} avatar`"
              />
              <span v-else class="profile-avatar__fallback">{{ getProfileInitials(profileUsernameDraft) }}</span>
            </span>
            <div class="vision-terminal-feed__action-row">
              <button class="vision-terminal-feed__link-button" type="button" @click="openProfileImagePicker">
                {{ profileAvatarDraft ? "Change picture" : "Add picture" }}
              </button>
              <button v-if="profileAvatarDraft" class="vision-terminal-feed__link-button" type="button" @click="clearProfileAvatar">
                Remove
              </button>
            </div>
          </div>

          <label class="vision-terminal-feed__field">
            <span>Email</span>
            <input v-model="profileEmailDraft" class="input" type="email" placeholder="name@example.com" />
          </label>

          <label class="vision-terminal-feed__field">
            <span>Username</span>
            <input v-model="profileUsernameDraft" class="input" placeholder="Your display name" />
          </label>

          <label class="vision-terminal-feed__field">
            <span>About you</span>
            <textarea v-model="profileDescriptionDraft" class="input vision-terminal-feed__textarea" rows="5" placeholder="Say a little about what you do."></textarea>
          </label>
        </section>

        <section ref="locationSectionRef" class="vision-terminal-feed__block">
          <p class="vision-terminal-feed__block-title">{{ hasSavedLocation ? "location" : "location setup" }}</p>
          <label class="vision-terminal-feed__field">
            <span>Mode</span>
            <select v-model="profileLocationMode" class="input">
              <option v-for="option in locationModeOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
            </select>
          </label>

          <label class="vision-terminal-feed__field">
            <span>Search location</span>
            <input v-model="locationSearchQuery" class="input" placeholder="Search address or place" />
          </label>

          <div class="vision-terminal-feed__action-row">
            <button class="vision-terminal-feed__link-button" type="button" :disabled="isLookupLoading || !locationSearchQuery.trim()" @click="searchLocations()">
              {{ isLookupLoading ? "Searching..." : "Search" }}
            </button>
            <button class="vision-terminal-feed__link-button" type="button" :disabled="isUsingCurrentLocation" @click="useCurrentLocation">
              {{ isUsingCurrentLocation ? "Locating..." : "Use current location" }}
            </button>
            <button class="vision-terminal-feed__link-button" type="button" :disabled="!locationSearchQuery.trim()" @click="clearSelectedLocation">
              Clear
            </button>
          </div>

          <p v-if="locationError" class="vision-terminal-feed__line vision-terminal-feed__line--error">{{ locationError }}</p>
          <p v-else-if="!lookupConfigured" class="vision-terminal-feed__line vision-terminal-feed__line--soft">Location search is not configured yet.</p>

          <div v-if="locationSuggestions.length" class="vision-terminal-feed__stack">
            <button
              v-for="candidate in locationSuggestions"
              :key="`${candidate.latitude}:${candidate.longitude}:${candidate.label}`"
              type="button"
              class="vision-terminal-feed__list-button"
              @click="applyLocationCandidate(candidate)"
            >
              <span>{{ candidate.label }}</span>
              <span>{{ candidate.locality || candidate.country || "Location result" }}</span>
            </button>
          </div>

          <label class="vision-terminal-feed__field">
            <span>Street</span>
            <input :value="profileLocationStreet" class="input" placeholder="Street" @input="updateLocationStreet(($event.target as HTMLInputElement).value)" />
          </label>
          <label class="vision-terminal-feed__field">
            <span>House number</span>
            <input :value="profileLocationHouseNumber" class="input" placeholder="House number" @input="updateLocationHouseNumber(($event.target as HTMLInputElement).value)" />
          </label>
          <label class="vision-terminal-feed__field">
            <span>Postal code</span>
            <input :value="profileLocationPostalCode" class="input" placeholder="Postal code" @input="updateLocationPostalCode(($event.target as HTMLInputElement).value)" />
          </label>
          <label class="vision-terminal-feed__field">
            <span>City</span>
            <input :value="profileLocationLocality" class="input" placeholder="City" @input="updateLocationLocality(($event.target as HTMLInputElement).value)" />
          </label>
          <label class="vision-terminal-feed__field">
            <span>Country</span>
            <input :value="profileLocationCountry" class="input" placeholder="Country" @input="updateLocationCountry(($event.target as HTMLInputElement).value)" />
          </label>

          <button class="vision-terminal-feed__link-button" type="button" @click="resolveManualAddress">
            Resolve typed address
          </button>

          <template v-if="profileLocationMode === 'EXACT'">
            <label class="vision-terminal-feed__field">
              <span>Exact address visibility</span>
              <select v-model="profileExactVisibilityScope" class="input">
                <option v-for="option in exactVisibilityScopeOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
              </select>
            </label>

            <div v-if="profileExactVisibilityScope === 'CIRCLES'" class="vision-terminal-feed__stack">
              <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">Selected circles</p>
              <button
                v-for="circle in myCircleOptions"
                :key="circle.id"
                type="button"
                class="vision-terminal-feed__list-button"
                :class="{ 'vision-terminal-feed__list-button--active': profileExactVisibleCircleIds.includes(circle.id) }"
                @click="toggleExactVisibleCircle(circle.id)"
              >
                {{ circle.name }}
              </button>
            </div>

            <div v-if="profileExactVisibilityScope === 'USERS'" class="vision-terminal-feed__stack">
              <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">Selected people</p>
              <button
                v-for="contact in myContactOptions"
                :key="contact.userId"
                type="button"
                class="vision-terminal-feed__list-button"
                :class="{ 'vision-terminal-feed__list-button--active': profileExactVisibleUserIds.includes(contact.userId) }"
                @click="toggleExactVisibleUser(contact.userId)"
              >
                {{ contact.username }} <span class="vision-terminal-feed__line--soft">({{ contact.circleSummaryLabel || "No circle label" }})</span>
              </button>
            </div>
          </template>
        </section>

        <section class="vision-terminal-feed__block">
          <p class="vision-terminal-feed__block-title">summary</p>
          <p class="vision-terminal-feed__line">Username: {{ profileUsernameDraft }}</p>
          <p class="vision-terminal-feed__line">Location mode: {{ profileLocationMode }}</p>
          <p class="vision-terminal-feed__line">Location visibility: {{ profileExactVisibilityScope }}</p>
          <p class="vision-terminal-feed__line">Selected location: {{ profileLocationLabel || "No location selected" }}</p>
        </section>

        <section class="vision-terminal-feed__block">
          <p class="vision-terminal-feed__block-title">actions</p>
          <div class="vision-terminal-feed__action-row">
            <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || !hasUnsavedChanges" @click="saveProfile">
              Save profile
            </button>
            <button class="vision-terminal-feed__link-button" type="button" @click="skipForNow">
              Skip for now
            </button>
          </div>
        </section>
      </template>

      <input
        ref="profileImageInputRef"
        class="visually-hidden"
        type="file"
        accept="image/*"
        @change="updateProfileAvatarFromFile(($event.target as HTMLInputElement).files?.[0] ?? null); ($event.target as HTMLInputElement).value = ''"
      />
    </div>
  </VisionDetailSurface>
</template>
