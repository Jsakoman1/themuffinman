<script setup lang="ts">
import {computed} from "vue"
import InlineEditableField from "./InlineEditableField.vue"
import UiSearchableMultiSelect from "../../../../components/ui/UiSearchableMultiSelect.vue"
import type {
  CircleContact,
  CircleGroup,
  ExactLocationVisibilityScopeOption,
  LocationLookupCandidate,
  LocationModeOption
} from "../../api/workmarketApi.ts"

const props = defineProps<{
  mode: NonNullable<LocationModeOption["value"]>
  label: string
  country: string
  locality: string
  postalCode: string
  street: string
  houseNumber: string
  hasCoordinates: boolean
  isResolved: boolean
  searchQuery: string
  modeOptions: LocationModeOption[]
  exactVisibilityScope: NonNullable<ExactLocationVisibilityScopeOption["value"]>
  exactVisibilityOptions: ExactLocationVisibilityScopeOption[]
  circleOptions: CircleGroup[]
  contactOptions: CircleContact[]
  selectedCircleIds: number[]
  selectedUserIds: number[]
  suggestions: LocationLookupCandidate[]
  lookupConfigured: boolean
  isLookupLoading: boolean
  isUsingCurrentLocation: boolean
  locationError: string
  allowCurrentLocation?: boolean
}>()

const emit = defineEmits<{
  "update:mode": [value: NonNullable<LocationModeOption["value"]>]
  "update:exactVisibilityScope": [value: NonNullable<ExactLocationVisibilityScopeOption["value"]>]
  "update:country": [value: string]
  "update:locality": [value: string]
  "update:postalCode": [value: string]
  "update:street": [value: string]
  "update:houseNumber": [value: string]
  "update:searchQuery": [value: string]
  "search": []
  "resolveManualAddress": []
  "useCurrentLocation": []
  "clearLocation": []
  "pickSuggestion": [candidate: LocationLookupCandidate]
  "toggleCircle": [circleId: number]
  "toggleUser": [userId: number]
}>()

const handleModeChange = (event: Event) => {
  emit("update:mode", (event.target as HTMLSelectElement).value as NonNullable<LocationModeOption["value"]>)
}

const handleExactVisibilityChange = (event: Event) => {
  emit("update:exactVisibilityScope", (event.target as HTMLSelectElement).value as NonNullable<ExactLocationVisibilityScopeOption["value"]>)
}

const locationTitle = computed(() => {
  return [props.street, props.houseNumber].filter(Boolean).join(" ").trim()
    || props.label
    || props.locality
    || "No location selected"
})

const locationMeta = computed(() => {
  return [props.postalCode, props.locality, props.country].filter(Boolean).join(", ")
})

const locationResolutionLabel = computed(() => {
  if (!props.hasCoordinates) {
    return "No coordinates saved"
  }

  return props.isResolved ? "Resolved from provider" : "Approximate coordinates only"
})
</script>

<template>
  <div class="dashboard-profile-edit-side settings-location-editor">
    <InlineEditableField label="Location sharing" :editing="true" :editable="false">
      <template #editor>
        <select :value="mode" class="input" @change="handleModeChange">
          <option v-for="option in modeOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
        </select>
      </template>
    </InlineEditableField>

    <template v-if="mode !== 'OFF'">
      <div class="settings-location-editor__group">
        <div class="settings-location-editor__summary">
          <span class="label">Selected location</span>
          <div class="settings-location-editor__summary-card">
            <strong>{{ locationTitle }}</strong>
            <span v-if="locationMeta" class="settings-location-editor__summary-meta">{{ locationMeta }}</span>
            <span
              :class="[
                'settings-location-editor__summary-status',
                isResolved ? 'settings-location-editor__summary-status--resolved' : 'settings-location-editor__summary-status--pending'
              ]"
            >
              {{ locationResolutionLabel }}
            </span>
          </div>
        </div>
      </div>

      <div class="settings-location-editor__group">
        <div class="ui-edit-field ui-edit-field--message field">
          <span class="label">Find location</span>
          <div class="ui-inline-readonly-stack">
            <input
              :value="searchQuery"
              class="input"
              placeholder="Search address or place"
              @input="emit('update:searchQuery', ($event.target as HTMLInputElement).value)"
              @keydown.enter.prevent="emit('search')"
            />
            <div class="button-row">
              <button class="button button--secondary" type="button" :disabled="isLookupLoading || !searchQuery.trim()" @click="emit('search')">
                {{ isLookupLoading ? "Searching..." : "Search" }}
              </button>
              <button v-if="allowCurrentLocation !== false" class="button button--secondary" type="button" :disabled="isUsingCurrentLocation" @click="emit('useCurrentLocation')">
                {{ isUsingCurrentLocation ? "Locating..." : "Use current location" }}
              </button>
              <button class="button button--secondary" type="button" :disabled="!searchQuery.trim() && !hasCoordinates" @click="emit('clearLocation')">
                Clear
              </button>
            </div>
            <div v-if="locationError" class="alert alert--error">{{ locationError }}</div>
            <div v-else-if="!lookupConfigured" class="muted">Location search is not configured yet.</div>

            <div v-if="suggestions.length" class="location-result-list">
              <button
                v-for="candidate in suggestions"
                :key="`${candidate.latitude}:${candidate.longitude}:${candidate.label}`"
                class="location-result-button"
                type="button"
                @click="emit('pickSuggestion', candidate)"
              >
                <span class="location-result-button__title">{{ candidate.label }}</span>
                <span class="location-result-button__meta">{{ candidate.locality || candidate.country || "Location result" }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="settings-location-editor__group settings-location-editor__group--address">
        <span class="label">Structured address</span>
        <div class="settings-location-editor__address-grid">
          <input
            :value="street"
            class="input"
            placeholder="Street"
            @input="emit('update:street', ($event.target as HTMLInputElement).value)"
          />
          <input
            :value="houseNumber"
            class="input"
            placeholder="House number"
            @input="emit('update:houseNumber', ($event.target as HTMLInputElement).value)"
          />
          <input
            :value="postalCode"
            class="input"
            placeholder="Postal code"
            @input="emit('update:postalCode', ($event.target as HTMLInputElement).value)"
          />
          <input
            :value="locality"
            class="input"
            placeholder="City"
            @input="emit('update:locality', ($event.target as HTMLInputElement).value)"
          />
          <input
            :value="country"
            class="input settings-location-editor__country"
            placeholder="Country"
            @input="emit('update:country', ($event.target as HTMLInputElement).value)"
          />
        </div>
        <div class="button-row">
          <button class="button button--secondary" type="button" @click="emit('resolveManualAddress')">
            Resolve typed address
          </button>
        </div>
      </div>

      <template v-if="mode === 'EXACT'">
        <InlineEditableField label="Exact address visibility" :editing="true" :editable="false">
          <template #editor>
            <select :value="exactVisibilityScope" class="input" @change="handleExactVisibilityChange">
              <option v-for="option in exactVisibilityOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
            </select>
          </template>
        </InlineEditableField>

        <div v-if="exactVisibilityScope === 'CIRCLES'" class="ui-edit-field field">
          <span class="label">Selected circles</span>
          <UiSearchableMultiSelect
            :options="circleOptions.map((circle) => ({id: circle.id, label: circle.name}))"
            :selected-ids="selectedCircleIds"
            placeholder="Search circles"
            empty-label="No circles found."
            @toggle="emit('toggleCircle', $event)"
          />
        </div>

        <div v-if="exactVisibilityScope === 'USERS'" class="ui-edit-field field">
          <span class="label">Selected people</span>
          <UiSearchableMultiSelect
            :options="contactOptions.map((contact) => ({id: contact.userId, label: contact.username, meta: contact.circleSummaryLabel}))"
            :selected-ids="selectedUserIds"
            placeholder="Search people"
            empty-label="No people found."
            @toggle="emit('toggleUser', $event)"
          />
        </div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.settings-location-editor {
  gap: 14px;
}

.settings-location-editor__group {
  display: grid;
  gap: 8px;
}

.settings-location-editor__summary {
  display: grid;
  gap: 8px;
}

.settings-location-editor__summary-card {
  display: grid;
  gap: 2px;
  padding: 2px 0;
}

.settings-location-editor__summary-meta {
  color: var(--text-muted);
  font-size: 0.76rem;
  font-weight: 600;
}

.settings-location-editor__summary-status {
  font-size: 0.72rem;
  font-weight: 700;
}

.settings-location-editor__summary-status--resolved {
  color: #166534;
}

.settings-location-editor__summary-status--pending {
  color: #b45309;
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
