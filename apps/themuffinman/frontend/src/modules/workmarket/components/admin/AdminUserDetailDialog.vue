<script setup lang="ts">
import {computed, ref, watch} from "vue"
import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import ProfileOpenQuestItem from "../../../../components/profile/ProfileOpenQuestItem.vue"
import UiDialog from "../../../../components/ui/UiDialog.vue"
import UiStatusBanner from "../../../../components/ui/UiStatusBanner.vue"
import {compressProfileAvatar} from "../../../../shared/imageCompression.ts"
import {PROFILE_IMAGE_PROCESSING_ERROR_MESSAGE} from "../../../../shared/clientMessages.ts"
import {formatQuestTermForDisplay} from "../../../../shared/questSchedule.ts"
import DetailDialogFrame from "../shared/DetailDialogFrame.vue"
import DetailUtilitySection from "../shared/DetailUtilitySection.vue"
import ProfileDescriptionEditor from "../shared/ProfileDescriptionEditor.vue"
import ProfileIdentityEditor from "../shared/ProfileIdentityEditor.vue"
import ProfileLocationEditor from "../shared/ProfileLocationEditor.vue"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {
  AdminUserDetail,
  AppUserRequest,
  CircleContact,
  CircleGroup,
  ExactLocationVisibilityScopeOption,
  LocationLookupCandidate,
  LocationLookupResponse,
  LocationModeOption
} from "../../api/workmarketApi.ts"

const props = defineProps<{
  open: boolean
  userId: number | null
}>()

const emit = defineEmits<{
  (event: "close"): void
  (event: "saved"): void
}>()

const detail = ref<AdminUserDetail | null>(null)
const isLoading = ref(false)
const isSaving = ref(false)
const error = ref("")
const bannerMessage = ref("")
const bannerTone = ref<"success" | "warning" | "error">("success")

const email = ref("")
const username = ref("")
const role = ref<"USER" | "ADMIN">("USER")
const password = ref("")
const profileDescription = ref("")
const profileAvatar = ref("")
const locationMode = ref<NonNullable<LocationModeOption["value"]>>("OFF")
const exactVisibilityScope = ref<NonNullable<ExactLocationVisibilityScopeOption["value"]>>("NOBODY")
const exactVisibleCircleIds = ref<number[]>([])
const exactVisibleUserIds = ref<number[]>([])
const locationProvider = ref("")
const locationProviderPlaceId = ref("")
const locationLabel = ref("")
const locationCountry = ref("")
const locationLocality = ref("")
const locationPostalCode = ref("")
const locationStreet = ref("")
const locationHouseNumber = ref("")
const locationLatitude = ref("")
const locationLongitude = ref("")
const locationResolvedAt = ref("")
const locationSearchQuery = ref("")
const locationSuggestions = ref<LocationLookupCandidate[]>([])
const lookupConfigured = ref(false)
const isLookupLoading = ref(false)
const locationError = ref("")
const originalSignature = ref("")

const modeOptions = computed(() => detail.value?.locationModes ?? [])
const visibilityOptions = computed(() => detail.value?.exactLocationVisibilityScopes ?? [])
const roleOptions = computed(() => detail.value?.appUserRoles ?? [])
const circleOptions = computed<CircleGroup[]>(() => detail.value?.circles ?? [])
const contactOptions = computed<CircleContact[]>(() => detail.value?.contacts ?? [])
const targetUser = computed(() => detail.value?.user ?? null)

const buildManualAddressQuery = () => [
  locationStreet.value,
  locationHouseNumber.value,
  locationPostalCode.value,
  locationLocality.value,
  locationCountry.value
]
  .map((value) => value.trim())
  .filter(Boolean)
  .join(", ")

const isLocationResolved = computed(() => Boolean(
  locationLatitude.value.trim()
  && locationLongitude.value.trim()
  && (locationStreet.value.trim() || locationLocality.value.trim() || locationLabel.value.trim())
))

const buildSignature = () => JSON.stringify({
  email: email.value.trim(),
  username: username.value.trim(),
  role: role.value,
  password: password.value,
  profileDescription: profileDescription.value,
  profileAvatar: profileAvatar.value,
  locationMode: locationMode.value,
  exactVisibilityScope: exactVisibilityScope.value,
  exactVisibleCircleIds: [...exactVisibleCircleIds.value].sort((a, b) => a - b),
  exactVisibleUserIds: [...exactVisibleUserIds.value].sort((a, b) => a - b),
  locationProvider: locationProvider.value,
  locationProviderPlaceId: locationProviderPlaceId.value,
  locationLabel: locationLabel.value,
  locationCountry: locationCountry.value,
  locationLocality: locationLocality.value,
  locationPostalCode: locationPostalCode.value,
  locationStreet: locationStreet.value,
  locationHouseNumber: locationHouseNumber.value,
  locationLatitude: locationLatitude.value,
  locationLongitude: locationLongitude.value,
  locationResolvedAt: locationResolvedAt.value
})

const hasUnsavedChanges = computed(() => buildSignature() !== originalSignature.value)

const syncDraft = () => {
  const user = detail.value?.user
  if (!user) {
    return
  }

  email.value = user.email ?? ""
  username.value = user.username ?? ""
  role.value = user.role === "ADMIN" ? "ADMIN" : "USER"
  password.value = ""
  profileDescription.value = user.profileDescription ?? ""
  profileAvatar.value = user.profileAvatarDataUrl ?? ""
  locationMode.value = user.locationSettings?.mode ?? "OFF"
  exactVisibilityScope.value = user.locationSettings?.exactVisibilityScope ?? "NOBODY"
  exactVisibleCircleIds.value = [...(user.locationSettings?.exactVisibleCircleIds ?? [])]
  exactVisibleUserIds.value = [...(user.locationSettings?.exactVisibleUserIds ?? [])]
  locationProvider.value = user.locationSettings?.provider ?? ""
  locationProviderPlaceId.value = user.locationSettings?.providerPlaceId ?? ""
  locationLabel.value = user.locationSettings?.label ?? ""
  locationCountry.value = user.locationSettings?.country ?? ""
  locationLocality.value = user.locationSettings?.locality ?? ""
  locationPostalCode.value = user.locationSettings?.postalCode ?? ""
  locationStreet.value = user.locationSettings?.street ?? ""
  locationHouseNumber.value = user.locationSettings?.houseNumber ?? ""
  locationLatitude.value = user.locationSettings?.latitude != null ? String(user.locationSettings.latitude) : ""
  locationLongitude.value = user.locationSettings?.longitude != null ? String(user.locationSettings.longitude) : ""
  locationResolvedAt.value = user.locationSettings?.resolvedAt ?? ""
  locationSearchQuery.value = locationLabel.value || buildManualAddressQuery()
  locationSuggestions.value = []
  locationError.value = ""
  originalSignature.value = buildSignature()
}

const loadDetail = async () => {
  if (!props.userId) {
    detail.value = null
    return
  }

  isLoading.value = true
  error.value = ""
  bannerMessage.value = ""
  try {
    detail.value = await workmarketApi.getAdminUserDetail(props.userId)
    syncDraft()
  } catch (requestError) {
    error.value = getApiErrorMessage(requestError, "Could not load user details.")
  } finally {
    isLoading.value = false
  }
}

const clearLocationProviderMetadata = () => {
  locationProvider.value = ""
  locationProviderPlaceId.value = ""
  locationResolvedAt.value = ""
}

const markAddressAsUnresolved = () => {
  clearLocationProviderMetadata()
  locationLatitude.value = ""
  locationLongitude.value = ""
  locationError.value = "Address changed. Resolve it again before saving."
}

const applyLocationCandidate = (candidate: LocationLookupCandidate) => {
  locationProvider.value = candidate.provider ?? ""
  locationProviderPlaceId.value = candidate.providerPlaceId ?? ""
  locationLabel.value = candidate.label ?? ""
  locationCountry.value = candidate.country ?? ""
  locationLocality.value = candidate.locality ?? ""
  locationPostalCode.value = candidate.postalCode ?? ""
  locationStreet.value = candidate.street ?? ""
  locationHouseNumber.value = candidate.houseNumber ?? ""
  locationLatitude.value = String(candidate.latitude)
  locationLongitude.value = String(candidate.longitude)
  locationResolvedAt.value = candidate.resolvedAt ?? ""
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
    locationError.value = "Enter at least city or address details."
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

const clearLocation = () => {
  clearLocationProviderMetadata()
  locationLabel.value = ""
  locationCountry.value = ""
  locationLocality.value = ""
  locationPostalCode.value = ""
  locationStreet.value = ""
  locationHouseNumber.value = ""
  locationLatitude.value = ""
  locationLongitude.value = ""
  locationSearchQuery.value = ""
  locationSuggestions.value = []
  locationError.value = ""
}

const toggleCircle = (circleId: number) => {
  exactVisibleCircleIds.value = exactVisibleCircleIds.value.includes(circleId)
    ? exactVisibleCircleIds.value.filter((id) => id !== circleId)
    : [...exactVisibleCircleIds.value, circleId]
}

const toggleUser = (userId: number) => {
  exactVisibleUserIds.value = exactVisibleUserIds.value.includes(userId)
    ? exactVisibleUserIds.value.filter((id) => id !== userId)
    : [...exactVisibleUserIds.value, userId]
}

const pickAvatar = async (file: File | null) => {
  if (!file) {
    return
  }

  try {
    profileAvatar.value = await compressProfileAvatar(file)
  } catch {
    bannerMessage.value = PROFILE_IMAGE_PROCESSING_ERROR_MESSAGE
    bannerTone.value = "warning"
  }
}

const discardChanges = () => {
  syncDraft()
}

const saveChanges = async () => {
  if (!props.userId) {
    return
  }

  if (locationMode.value !== "OFF" && (!locationLatitude.value.trim() || !locationLongitude.value.trim())) {
    bannerMessage.value = "Resolve the address before saving."
    bannerTone.value = "warning"
    return
  }

  isSaving.value = true
  bannerMessage.value = ""
  try {
    const request: AppUserRequest = {
      email: email.value.trim(),
      username: username.value.trim(),
      role: role.value,
      password: password.value || undefined,
      profileDescription: profileDescription.value,
      profileAvatarDataUrl: profileAvatar.value || null,
      locationSettings: {
        mode: locationMode.value,
        defaultRadiusKm: 10,
        exactVisibilityScope: exactVisibilityScope.value,
        exactVisibleCircleIds: exactVisibleCircleIds.value,
        exactVisibleUserIds: exactVisibleUserIds.value,
        provider: locationProvider.value || null,
        providerPlaceId: locationProviderPlaceId.value || null,
        label: locationLabel.value || null,
        countryCode: null,
        country: locationCountry.value || null,
        locality: locationLocality.value || null,
        postalCode: locationPostalCode.value || null,
        street: locationStreet.value || null,
        houseNumber: locationHouseNumber.value || null,
        latitude: locationLatitude.value.trim() ? Number(locationLatitude.value) : null,
        longitude: locationLongitude.value.trim() ? Number(locationLongitude.value) : null,
        resolvedAt: locationResolvedAt.value || null
      }
    }

    await workmarketApi.updateAppUser(props.userId, request)
    await loadDetail()
    bannerMessage.value = "User updated."
    bannerTone.value = "success"
    emit("saved")
  } catch (requestError) {
    bannerMessage.value = getApiErrorMessage(requestError, "Could not update user.")
    bannerTone.value = "warning"
  } finally {
    isSaving.value = false
  }
}

watch(() => [props.open, props.userId] as const, ([open]) => {
  if (!open) {
    detail.value = null
    error.value = ""
    return
  }

  void loadDetail()
}, {immediate: true})
</script>

<template>
  <UiDialog
    :open="open"
    title="Admin user"
    size="xl"
    @close="$emit('close')"
  >
    <div class="surface-stack">
      <UiStatusBanner :message="bannerMessage" :tone="bannerTone" />

      <div v-if="isLoading" class="empty-state">Loading user...</div>
      <div v-else-if="error" class="alert alert--error">{{ error }}</div>

      <template v-else-if="targetUser">
        <DetailDialogFrame>
          <template #main>
            <ProfileIdentityEditor
              :key="`admin-user:${targetUser.id}:${targetUser.email}:${targetUser.username}`"
              :email="email"
              :username="username"
              :avatar-data-url="profileAvatar"
              @update:email="email = $event"
              @update:username="username = $event"
              @pick-image="pickAvatar"
              @clear-image="profileAvatar = ''"
            />

            <ProfileDescriptionEditor
              :description="profileDescription"
              @update:description="profileDescription = $event"
            />

            <ProfileLocationEditor
              :mode="locationMode"
              :label="locationLabel"
              :country="locationCountry"
              :locality="locationLocality"
              :postal-code="locationPostalCode"
              :street="locationStreet"
              :house-number="locationHouseNumber"
              :has-coordinates="!!locationLatitude.trim() && !!locationLongitude.trim()"
              :is-resolved="isLocationResolved"
              :search-query="locationSearchQuery"
              :mode-options="modeOptions"
              :exact-visibility-scope="exactVisibilityScope"
              :exact-visibility-options="visibilityOptions"
              :circle-options="circleOptions"
              :contact-options="contactOptions"
              :selected-circle-ids="exactVisibleCircleIds"
              :selected-user-ids="exactVisibleUserIds"
              :suggestions="locationSuggestions"
              :lookup-configured="lookupConfigured"
              :is-lookup-loading="isLookupLoading"
              :is-using-current-location="false"
              :location-error="locationError"
              :allow-current-location="false"
              @update:mode="locationMode = $event"
              @update:exact-visibility-scope="exactVisibilityScope = $event"
              @update:country="locationCountry = $event; markAddressAsUnresolved()"
              @update:locality="locationLocality = $event; markAddressAsUnresolved()"
              @update:postal-code="locationPostalCode = $event; markAddressAsUnresolved()"
              @update:street="locationStreet = $event; markAddressAsUnresolved()"
              @update:house-number="locationHouseNumber = $event; markAddressAsUnresolved()"
              @update:search-query="locationSearchQuery = $event"
              @search="searchLocations()"
              @resolve-manual-address="resolveManualAddress"
              @clear-location="clearLocation"
              @pick-suggestion="applyLocationCandidate"
              @toggle-circle="toggleCircle"
              @toggle-user="toggleUser"
            />
          </template>

          <template #side>
            <DetailUtilitySection title="Account" tone="summary">
              <div class="ui-edit-form">
                <div class="field">
                  <span class="label">Role</span>
                  <select v-model="role" class="input">
                    <option v-for="option in roleOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
                  </select>
                </div>

                <div class="field">
                  <span class="label">Reset password</span>
                  <input v-model="password" class="input" type="password" placeholder="Leave blank to keep current password" />
                </div>
              </div>
            </DetailUtilitySection>

            <DetailUtilitySection title="Open jobs" tone="summary">
              <div v-if="targetUser.openQuests?.length" class="surface-stack">
                <ProfileOpenQuestItem
                  v-for="quest in targetUser.openQuests"
                  :key="quest.id"
                  :title="quest.title"
                  :meta="`$ ${quest.awardAmount} · ${formatQuestTermForDisplay(quest.scheduledAt, quest.endsAt, quest.termFixed)}`"
                  :status-class="quest.presentation.statusBadgeClass"
                  :status-label="quest.presentation.statusLabel"
                  :description="quest.description"
                  action-label="Open job"
                  :action-path="routeForNavigationTarget(quest.questNavigation)"
                />
              </div>
              <div v-else class="empty-state empty-state--compact">No open jobs.</div>
            </DetailUtilitySection>

            <DetailUtilitySection v-if="hasUnsavedChanges" title="Actions" tone="actions">
              <div class="ui-action-stack">
                <button class="button button--action" type="button" :disabled="isSaving" @click="saveChanges">
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
