<script setup lang="ts">
import {computed, onBeforeUnmount, ref, watch} from "vue"
import {useRouter} from "vue-router"
import {currentUser} from "../../../auth.ts"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {updateSessionUser} from "../../../services/sessionService.ts"
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
import {useDebouncedWatch} from "../../../composables/useDebouncedWatch.ts"
import {useUserProfileView} from "../composables/useUserProfileView.ts"
import ProfileIdentityEditor from "../components/shared/ProfileIdentityEditor.vue"
import ProfileLocationEditor from "../components/shared/ProfileLocationEditor.vue"
import UiInfoGrid from "../../../components/ui/UiInfoGrid.vue"
import UiStatCard from "../../../components/ui/UiStatCard.vue"
import UiStatusBanner from "../../../components/ui/UiStatusBanner.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import VisionDetailSurface from "../components/VisionDetailSurface.vue"

const router = useRouter()

const profileEmail = ref("")
const profileUsername = ref("")
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
const originalProfileEmail = ref("")
const originalProfileUsername = ref("")
const originalLocationSignature = ref("")
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
  loadErrorMessage: "Could not load settings."
})

const loadSettingsOptions = async () => {
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

const isLocationResolved = computed(() => Boolean(
  profileLocationLatitude.value.trim()
  && profileLocationLongitude.value.trim()
  && (profileLocationStreet.value.trim() || profileLocationLocality.value.trim() || profileLocationLabel.value.trim())
))

const selectedLocationTitle = computed(() =>
  [profileLocationStreet.value.trim(), profileLocationHouseNumber.value.trim()].filter(Boolean).join(" ").trim()
  || profileLocationLabel.value.trim()
  || profileLocationLocality.value.trim()
  || "No location selected"
)

const selectedLocationMeta = computed(() =>
  [profileLocationPostalCode.value.trim(), profileLocationLocality.value.trim(), profileLocationCountry.value.trim()]
    .filter(Boolean)
    .join(", ")
)

const selectedLocationStatus = computed(() => {
  if (!profileLocationLatitude.value.trim() || !profileLocationLongitude.value.trim()) {
    return "No coordinates saved"
  }

  return isLocationResolved.value ? "Resolved from provider" : "Approximate coordinates only"
})

const selectedLocationModeLabel = computed(() =>
  locationModeOptions.value.find((option) => option.value === profileLocationMode.value)?.label ?? "Hidden"
)

const selectedVisibilityScopeLabel = computed(() =>
  exactVisibilityScopeOptions.value.find((option) => option.value === profileExactVisibilityScope.value)?.label ?? "Hidden"
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

const syncDraft = () => {
  if (!profile.value || !isOwnProfile.value) {
    return
  }

  profileEmail.value = profile.value.email ?? currentUser.value?.email ?? ""
  profileUsername.value = profile.value.username ?? currentUser.value?.username ?? ""
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
  originalProfileEmail.value = profileEmail.value
  originalProfileUsername.value = profileUsername.value
  originalLocationSignature.value = buildLocationSignature()
}

const hasUnsavedChanges = computed(() =>
  profileEmail.value.trim() !== originalProfileEmail.value.trim()
  || profileUsername.value.trim() !== originalProfileUsername.value.trim()
  || buildLocationSignature() !== originalLocationSignature.value
)

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

const saveProfile = async () => {
  if (!currentUser.value || !isOwnProfile.value || !profile.value) {
    return
  }

  if (profileLocationMode.value !== "OFF" && (!profileLocationLatitude.value.trim() || !profileLocationLongitude.value.trim())) {
    showMessage("Resolve the address first by selecting a search result or using your current location before saving.", "warning")
    return
  }

  try {
    const response = await visionApi.updateCurrentAppUser({
      email: profileEmail.value.trim(),
      username: profileUsername.value.trim(),
      profileDescription: profile.value.profileDescription ?? null,
      profileAvatarDataUrl: profile.value.profileAvatarDataUrl ?? null,
      locationSettings: {
        mode: profileLocationMode.value,
        defaultRadiusKm: 10,
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

    showMessage("Settings updated.", "success")
    await fetchProfile()
  } catch (requestError) {
    showMessage(getApiErrorMessage(requestError, "Could not update settings."), "warning")
  }
}

const discardChanges = () => {
  syncDraft()
}

const closeSettings = async () => {
  if (window.history.length > 1) {
    await router.back()
    return
  }

  if (currentUser.value?.id) {
    await router.push(`/vision/users/${currentUser.value.id}`)
    return
  }

  await router.push("/vision")
}

useDebouncedWatch(locationSearchQuery, () => {
  if (profileLocationMode.value === "OFF") {
    return
  }

  void searchLocations()
}, 350)

watch(() => activeUserId.value, () => {
  void fetchProfile()
  void loadSettingsOptions()
}, {immediate: true})

watch(() => [profile.value?.id, isOwnProfile.value] as const, () => {
  syncDraft()
}, {immediate: true})

onBeforeUnmount(() => {
  clearProfile()
})
</script>

<template>
  <VisionDetailSurface
    title="Settings"
    @close="closeSettings"
  >
    <UiStatusBanner :message="bannerMessage" :tone="bannerTone" />

    <div v-if="isLoading" class="empty-state">
      Loading settings...
    </div>
    <div v-else-if="error" class="alert alert--error">
      {{ error }}
    </div>
    <div v-else-if="!profile || !isOwnProfile" class="empty-state">
      Settings are only available for your own account.
    </div>
    <div v-else class="vision-settings-view">
      <div class="vision-settings-view__main">
        <UiSurfaceSection compact eyebrow="Identity">
          <ProfileIdentityEditor
            :email="profileEmail"
            :username="profileUsername"
            :show-avatar="false"
            @update:email="profileEmail = $event"
            @update:username="profileUsername = $event"
          />
        </UiSurfaceSection>

        <UiSurfaceSection compact eyebrow="Location settings">
          <ProfileLocationEditor
            :mode="profileLocationMode"
            :label="profileLocationLabel"
            :country="profileLocationCountry"
            :locality="profileLocationLocality"
            :postal-code="profileLocationPostalCode"
            :street="profileLocationStreet"
            :house-number="profileLocationHouseNumber"
            :has-coordinates="!!profileLocationLatitude.trim() && !!profileLocationLongitude.trim()"
            :is-resolved="isLocationResolved"
            :search-query="locationSearchQuery"
            :mode-options="locationModeOptions"
            :exact-visibility-scope="profileExactVisibilityScope"
            :exact-visibility-options="exactVisibilityScopeOptions"
            :circle-options="myCircleOptions"
            :contact-options="myContactOptions"
            :selected-circle-ids="profileExactVisibleCircleIds"
            :selected-user-ids="profileExactVisibleUserIds"
            :suggestions="locationSuggestions"
            :lookup-configured="lookupConfigured"
            :is-lookup-loading="isLookupLoading"
            :is-using-current-location="isUsingCurrentLocation"
            :location-error="locationError"
            @update:mode="profileLocationMode = $event"
            @update:exactVisibilityScope="profileExactVisibilityScope = $event"
            @update:country="updateLocationCountry"
            @update:locality="updateLocationLocality"
            @update:postalCode="updateLocationPostalCode"
            @update:street="updateLocationStreet"
            @update:houseNumber="updateLocationHouseNumber"
            @update:searchQuery="locationSearchQuery = $event"
            @search="searchLocations"
            @resolveManualAddress="resolveManualAddress"
            @useCurrentLocation="useCurrentLocation"
            @clearLocation="clearSelectedLocation"
            @pickSuggestion="applyLocationCandidate"
            @toggleCircle="toggleExactVisibleCircle"
            @toggleUser="toggleExactVisibleUser"
          />
        </UiSurfaceSection>
      </div>

      <aside class="vision-settings-view__side">
        <UiSurfaceSection compact eyebrow="Summary">
          <UiInfoGrid :columns="1">
            <UiStatCard label="Username" :value="profileUsername" />
            <UiStatCard label="Location mode" :value="selectedLocationModeLabel" />
            <UiStatCard label="Location visibility" :value="selectedVisibilityScopeLabel" />
            <UiStatCard label="Selected location" :value="selectedLocationTitle" :hint="selectedLocationMeta || selectedLocationStatus" />
          </UiInfoGrid>
        </UiSurfaceSection>

        <UiSurfaceSection v-if="hasUnsavedChanges" compact eyebrow="Actions">
          <div class="button-row">
            <button class="button button--action" type="button" :disabled="isSaving" @click="saveProfile">
              Save changes
            </button>
            <button class="button button--secondary" type="button" :disabled="isSaving" @click="discardChanges">
              Discard
            </button>
          </div>
        </UiSurfaceSection>
      </aside>
    </div>
  </VisionDetailSurface>
</template>

<style scoped>
.vision-settings-view {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(18rem, 24rem);
  gap: 1.25rem;
}

.vision-settings-view__main,
.vision-settings-view__side {
  display: grid;
  gap: 1rem;
}

@media (max-width: 920px) {
  .vision-settings-view {
    grid-template-columns: 1fr;
  }
}
</style>
