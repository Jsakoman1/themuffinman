<script setup lang="ts">
import {computed, ref, watch} from "vue"
import {currentUser} from "../../../../auth.ts"
import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {useDebouncedWatch} from "../../../../composables/useDebouncedWatch.ts"
import {updateSessionUser} from "../../../../services/sessionService.ts"
import type {
  AppUser,
  CircleContact,
  CircleGroup,
  ExactLocationVisibilityScopeOption,
  LocationLookupCandidate,
  LocationLookupResponse,
  LocationModeOption
} from "../../../workmarket/api/workmarketApi.ts"
import {workmarketApi} from "../../../workmarket/api/workmarketApi.ts"
import DetailDialogFrame from "../../../workmarket/components/shared/DetailDialogFrame.vue"
import DetailUtilitySection from "../../../workmarket/components/shared/DetailUtilitySection.vue"
import ProfileIdentityEditor from "../../../workmarket/components/shared/ProfileIdentityEditor.vue"
import {useUserProfileView} from "../../composables/useUserProfileView.ts"
import UiDialog from "../../../../components/ui/UiDialog.vue"
import UiStatusBanner from "../../../../components/ui/UiStatusBanner.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import UiSearchableMultiSelect from "../../../../components/ui/UiSearchableMultiSelect.vue"

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  (event: "close"): void
}>()

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
const editingSelectedLocation = ref(false)
const locationEditMode = ref<"search" | "current" | "manual">("search")

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
  userId: () => currentUser.value?.id ?? null,
  enabled: () => props.open,
  loadErrorMessage: "Could not load settings."
})

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
  editingSelectedLocation.value = false
  locationEditMode.value = "search"
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
  editingSelectedLocation.value = false
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
    const response = await workmarketApi.lookupLocation({query: sourceQuery})
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

useDebouncedWatch(locationSearchQuery, () => {
  if (!props.open || profileLocationMode.value === "OFF") {
    return
  }

  void searchLocations()
}, 350)

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
      const candidate = await workmarketApi.reverseLookupLocation({latitude, longitude})
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

const discardChanges = () => {
  syncDraft()
}

const openLocationEditor = (mode: "search" | "current" | "manual" = "search") => {
  editingSelectedLocation.value = true
  locationEditMode.value = mode
  locationSuggestions.value = []
  locationError.value = ""
  if (mode === "search") {
    locationSearchQuery.value = profileLocationLabel.value || buildManualAddressQuery()
  }
}

const closeLocationEditor = () => {
  editingSelectedLocation.value = false
  locationSuggestions.value = []
  locationError.value = ""
}

const saveProfile = async () => {
  if (!currentUser.value || !isOwnProfile.value) {
    return
  }

  if (profileLocationMode.value !== "OFF" && (!profileLocationLatitude.value.trim() || !profileLocationLongitude.value.trim())) {
    showMessage("Resolve the address first by selecting a search result or using your current location before saving.", "warning")
    return
  }

  isSaving.value = true
  try {
    const response = await workmarketApi.updateCurrentAppUser({
      email: profileEmail.value.trim(),
      username: profileUsername.value.trim(),
      profileDescription: profile.value?.profileDescription ?? null,
      profileAvatarDataUrl: profile.value?.profileAvatarDataUrl ?? null,
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
  } finally {
    isSaving.value = false
  }
}

watch(() => props.open, (open) => {
  if (!open) {
    clearProfile()
    return
  }

  void fetchProfile()
  void workmarketApi.getAppUserOptions()
    .then((options) => {
      locationModeOptions.value = options.locationModes
      exactVisibilityScopeOptions.value = options.exactLocationVisibilityScopes
    })
    .catch(() => {
      locationModeOptions.value = []
      exactVisibilityScopeOptions.value = []
    })

  void workmarketApi.getCircleGroups()
    .then((circles) => {
      myCircleOptions.value = circles
    })
    .catch(() => {
      myCircleOptions.value = []
    })

  void workmarketApi.getCircleConnectionsPage({page: 0, size: 200})
    .then((response) => {
      myContactOptions.value = response.items
    })
    .catch(() => {
      myContactOptions.value = []
    })
}, {immediate: true})

watch(() => [profile.value?.id, isOwnProfile.value] as const, () => {
  syncDraft()
}, {immediate: true})
</script>

<template>
  <UiDialog
    :open="open"
    title="Settings"
    size="xl"
    @close="$emit('close')"
  >
    <div class="surface-stack">
      <UiStatusBanner :message="bannerMessage" :tone="bannerTone" />

      <div v-if="isLoading" class="empty-state">Loading settings...</div>
      <div v-else-if="error" class="alert alert--error">{{ error }}</div>
      <div v-else-if="!profile || !isOwnProfile" class="empty-state">Settings are only available for your own account.</div>

      <template v-else>
        <DetailDialogFrame>
          <template #main>
            <ProfileIdentityEditor
              :key="`settings-identity:${originalProfileEmail}:${originalProfileUsername}`"
              :email="profileEmail"
              :username="profileUsername"
              :show-avatar="false"
              @update:email="profileEmail = $event"
              @update:username="profileUsername = $event"
            />

            <UiSurfaceSection
              compact
              eyebrow="Location settings"
            >
              <div class="settings-location-groups">
                <div class="settings-location-group settings-location-group--option">
                  <div class="settings-location-group__header">
                    <span class="settings-location-group__title">Location sharing</span>
                    <span class="settings-location-group__value">{{ selectedLocationModeLabel }}</span>
                  </div>
                  <select :value="profileLocationMode" class="input" @change="profileLocationMode = ($event.target as HTMLSelectElement).value as NonNullable<LocationModeOption['value']>">
                    <option v-for="option in locationModeOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
                  </select>
                </div>

                <div class="settings-location-group settings-location-group--option">
                  <div class="settings-location-group__header">
                    <span class="settings-location-group__title">Location visibility</span>
                    <span class="settings-location-group__value">{{ selectedVisibilityScopeLabel }}</span>
                  </div>
                  <select
                    :value="profileExactVisibilityScope"
                    class="input"
                    :disabled="profileLocationMode !== 'EXACT'"
                    @change="profileExactVisibilityScope = ($event.target as HTMLSelectElement).value as NonNullable<ExactLocationVisibilityScopeOption['value']>"
                  >
                    <option v-for="option in exactVisibilityScopeOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
                  </select>

                  <div v-if="profileLocationMode !== 'EXACT'" class="settings-location-group__note">
                    Exact visibility matters only when you share your exact location.
                  </div>

                  <div v-else-if="profileExactVisibilityScope === 'CIRCLES'" class="field">
                    <span class="label">Selected circles</span>
                    <UiSearchableMultiSelect
                      :options="myCircleOptions.map((circle) => ({id: circle.id, label: circle.name}))"
                      :selected-ids="profileExactVisibleCircleIds"
                      placeholder="Search circles"
                      empty-label="No circles found."
                      @toggle="toggleExactVisibleCircle($event)"
                    />
                  </div>

                  <div v-else-if="profileExactVisibilityScope === 'USERS'" class="field">
                    <span class="label">Selected people</span>
                    <UiSearchableMultiSelect
                      :options="myContactOptions.map((contact) => ({id: contact.userId, label: contact.username, meta: contact.circleSummaryLabel}))"
                      :selected-ids="profileExactVisibleUserIds"
                      placeholder="Search people"
                      empty-label="No people found."
                      @toggle="toggleExactVisibleUser($event)"
                    />
                  </div>
                </div>

              </div>
            </UiSurfaceSection>
          </template>

          <template #side>
            <DetailUtilitySection title="Address">
              <div class="settings-location-card">
                <div class="settings-location-card__summary">
                  <strong>{{ selectedLocationTitle }}</strong>
                  <span v-if="selectedLocationMeta" class="settings-location-card__meta">{{ selectedLocationMeta }}</span>
                  <span
                    :class="['settings-location-card__status', isLocationResolved ? 'settings-location-card__status--resolved' : 'settings-location-card__status--pending']"
                  >
                    {{ selectedLocationStatus }}
                  </span>
                </div>

                <button
                  v-if="!editingSelectedLocation"
                  class="button button--icon button--secondary button--icon-compact"
                  type="button"
                  aria-label="Edit selected location"
                  @click="openLocationEditor('search')"
                >
                  ✎
                </button>
              </div>

              <div v-if="editingSelectedLocation" class="settings-location-editor">
                <div class="ui-choice-chips">
                  <button
                    :class="['ui-choice-chip', {'ui-choice-chip--active': locationEditMode === 'search'}]"
                    type="button"
                    @click="locationEditMode = 'search'"
                  >
                    Search
                  </button>
                  <button
                    :class="['ui-choice-chip', {'ui-choice-chip--active': locationEditMode === 'current'}]"
                    type="button"
                    @click="locationEditMode = 'current'"
                  >
                    Current location
                  </button>
                  <button
                    :class="['ui-choice-chip', {'ui-choice-chip--active': locationEditMode === 'manual'}]"
                    type="button"
                    @click="locationEditMode = 'manual'"
                  >
                    Manual address
                  </button>
                </div>

                <div v-if="locationEditMode === 'search'" class="surface-stack">
                  <input
                    :value="locationSearchQuery"
                    class="input"
                    placeholder="Search address or place"
                    @input="locationSearchQuery = ($event.target as HTMLInputElement).value"
                    @keydown.enter.prevent="searchLocations()"
                  />
                  <div class="button-row">
                    <button class="button button--secondary" type="button" :disabled="isLookupLoading || !locationSearchQuery.trim()" @click="searchLocations()">
                      {{ isLookupLoading ? "Searching..." : "Search" }}
                    </button>
                    <button class="button button--secondary" type="button" @click="closeLocationEditor">
                      Cancel
                    </button>
                  </div>
                  <div v-if="locationSuggestions.length" class="location-result-list">
                    <button
                      v-for="candidate in locationSuggestions"
                      :key="`${candidate.latitude}:${candidate.longitude}:${candidate.label}`"
                      class="location-result-button"
                      type="button"
                      @click="applyLocationCandidate(candidate)"
                    >
                      <span class="location-result-button__title">{{ candidate.label }}</span>
                      <span class="location-result-button__meta">{{ candidate.locality || candidate.country || "Location result" }}</span>
                    </button>
                  </div>
                </div>

                <div v-else-if="locationEditMode === 'current'" class="surface-stack">
                  <p class="muted">
                    Use your device location and let the app try to resolve the nearest address automatically.
                  </p>
                  <div class="button-row">
                    <button class="button button--secondary" type="button" :disabled="isUsingCurrentLocation" @click="useCurrentLocation">
                      {{ isUsingCurrentLocation ? "Locating..." : "Use current location" }}
                    </button>
                    <button class="button button--secondary" type="button" @click="closeLocationEditor">
                      Cancel
                    </button>
                  </div>
                </div>

                <div v-else class="surface-stack">
                  <div class="settings-location-editor__address-grid">
                    <input :value="profileLocationStreet" class="input" placeholder="Street" @input="updateLocationStreet(($event.target as HTMLInputElement).value)" />
                    <input :value="profileLocationHouseNumber" class="input" placeholder="House number" @input="updateLocationHouseNumber(($event.target as HTMLInputElement).value)" />
                    <input :value="profileLocationPostalCode" class="input" placeholder="Postal code" @input="updateLocationPostalCode(($event.target as HTMLInputElement).value)" />
                    <input :value="profileLocationLocality" class="input" placeholder="City" @input="updateLocationLocality(($event.target as HTMLInputElement).value)" />
                    <input :value="profileLocationCountry" class="input settings-location-editor__country" placeholder="Country" @input="updateLocationCountry(($event.target as HTMLInputElement).value)" />
                  </div>
                  <div class="button-row">
                    <button class="button button--secondary" type="button" @click="resolveManualAddress">
                      Resolve typed address
                    </button>
                    <button class="button button--secondary" type="button" @click="closeLocationEditor">
                      Cancel
                    </button>
                  </div>
                </div>

                <div v-if="locationError" class="alert alert--error">{{ locationError }}</div>
                <div v-else-if="!lookupConfigured && locationEditMode === 'search'" class="muted">Location search is not configured yet.</div>
                <div class="button-row">
                  <button class="button button--secondary" type="button" :disabled="!profileLocationLatitude.trim() && !profileLocationLongitude.trim()" @click="clearSelectedLocation">
                    Clear location
                  </button>
                </div>
              </div>
            </DetailUtilitySection>

            <DetailUtilitySection v-if="hasUnsavedChanges" title="Actions" tone="actions">
              <div class="ui-action-stack">
                <button class="button button--action" type="button" :disabled="isSaving" @click="saveProfile">
                  Save changes
                </button>
                <button class="button button--secondary" type="button" :disabled="isSaving" @click="discardChanges">
                  Discard
                </button>
              </div>
            </DetailUtilitySection>
          </template>
        </DetailDialogFrame>
      </template>
    </div>
  </UiDialog>
</template>

<style scoped>
.settings-location-groups {
  display: grid;
  gap: 14px;
}

.settings-location-group {
  display: grid;
  gap: 8px;
}

.settings-location-group--option {
  gap: 10px;
}

.settings-location-group__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.settings-location-group__title {
  font-size: 0.84rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.settings-location-group__value {
  color: var(--brand-700);
  font-size: 0.76rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  white-space: nowrap;
}

.settings-location-group__note {
  color: var(--text-muted);
  font-size: 0.74rem;
  font-weight: 600;
  line-height: 1.45;
}

.settings-location-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: start;
}

.settings-location-card__summary {
  display: grid;
  gap: 4px;
}

.settings-location-card__meta {
  color: var(--text-muted);
  font-size: 0.76rem;
  font-weight: 600;
}

.settings-location-card__status {
  font-size: 0.72rem;
  font-weight: 700;
}

.settings-location-card__status--resolved {
  color: #166534;
}

.settings-location-card__status--pending {
  color: #b45309;
}

.settings-location-editor {
  display: grid;
  gap: 12px;
  margin-top: 10px;
}

.settings-location-editor__address-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(0, 0.8fr);
  gap: 8px;
}

.settings-location-editor__country {
  grid-column: 1 / -1;
}

.location-result-list {
  display: grid;
  gap: 6px;
}

.location-result-button {
  display: grid;
  gap: 2px;
  width: 100%;
  padding: 8px 0;
  border: 0;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.location-result-button:last-child {
  border-bottom: 0;
}

.location-result-button__title {
  font-size: 0.84rem;
  font-weight: 700;
  line-height: 1.35;
}

.location-result-button__meta {
  color: var(--text-muted);
  font-size: 0.74rem;
  font-weight: 600;
}
</style>
