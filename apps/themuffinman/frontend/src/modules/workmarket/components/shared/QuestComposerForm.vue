<script setup lang="ts">
import {computed, reactive} from "vue"
import RichTextEditor from "../../../../components/editor/AsyncRichTextEditor.vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import UiChoiceChips from "../../../../components/ui/UiChoiceChips.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import DetailDialogFrame from "./DetailDialogFrame.vue"
import {workmarketApi, type CircleGroup, type LocationLookupCandidate, type Quest, type QuestAudienceOption, type QuestLocationVisibilityOption} from "../../api/workmarketApi.ts"
import type {QuestStatus} from "../../domain/workmarketDomain.ts"
import {formatInstantForDisplay} from "../../../../shared/questSchedule.ts"
import InlineEditableField from "./InlineEditableField.vue"
import {useDebouncedWatch} from "../../../../composables/useDebouncedWatch.ts"

const props = withDefaults(defineProps<{
  formId?: string
  title: string
  description: string
  awardAmount: string
  termMode: "flexible" | "start-only" | "start-end"
  scheduledAt: string
  endsAt: string
  audience: "EVERYONE" | "CIRCLES"
  audienceOptions: QuestAudienceOption[]
  circles: CircleGroup[]
  selectedCircleIds: number[]
  locationSource: NonNullable<Quest["locationSource"]>
  locationCountry: string
  locationLocality: string
  locationPostalCode: string
  locationStreet: string
  locationHouseNumber: string
  locationVisibility: NonNullable<Quest["locationVisibility"]>
  locationVisibilityOptions: QuestLocationVisibilityOption[]
  images: string[]
  inlineEditable?: boolean
  submitVisible?: boolean
  submitLabel?: string
  submitDisabled?: boolean
  showCancel?: boolean
  cancelLabel?: string
  showCreator?: boolean
  creatorId?: string
  creatorOptions?: Array<{id: number; username: string; email: string}>
  showStatus?: boolean
  status?: QuestStatus
  statusOptions?: Array<{value: QuestStatus; label: string}>
}>(), {
  formId: "quest-composer-form",
  inlineEditable: false,
  submitVisible: true,
  submitLabel: "Save",
  submitDisabled: false,
  showCancel: false,
  cancelLabel: "Cancel",
  showCreator: false,
  creatorId: "",
  creatorOptions: () => [],
  showStatus: false,
  status: undefined,
  statusOptions: () => [],
})

const emit = defineEmits<{
  "update:title": [value: string]
  "update:description": [value: string]
  "update:awardAmount": [value: string]
  "update:termMode": [value: "flexible" | "start-only" | "start-end"]
  "update:scheduledAt": [value: string]
  "update:endsAt": [value: string]
  "update:audience": [value: "EVERYONE" | "CIRCLES"]
  "toggle:circle": [circleId: number]
  "update:locationSource": [value: NonNullable<Quest["locationSource"]>]
  "update:locationCountry": [value: string]
  "update:locationLocality": [value: string]
  "update:locationPostalCode": [value: string]
  "update:locationStreet": [value: string]
  "update:locationHouseNumber": [value: string]
  "update:locationVisibility": [value: NonNullable<Quest["locationVisibility"]>]
  "change:images": [payload: Event]
  "remove:image": [index: number]
  "update:creatorId": [value: string]
  "update:status": [value: QuestStatus]
  "save": []
  "cancel": []
}>()

const visibilityOptions = computed(() => {
  const everyoneOption = props.audienceOptions.find((option) => option.value === "EVERYONE")
  const circlesOption = props.audienceOptions.find((option) => option.value === "CIRCLES")

  return [everyoneOption, circlesOption]
    .filter((option): option is NonNullable<typeof option> => Boolean(option))
    .map((option) => ({value: option.value, label: option.label}))
})

const circlesRequiredMissing = computed(() =>
  props.audience === "CIRCLES" && props.selectedCircleIds.length === 0
)

const isSubmitDisabled = computed(() => props.submitDisabled || circlesRequiredMissing.value)

const minimumScheduleValue = computed(() => new Date().toISOString().slice(0, 16))

const editing = reactive({
  title: false,
  description: false,
  amount: false,
  term: false,
  start: false,
  end: false,
  audience: false,
  circles: false,
  photos: false,
  locationSource: false,
  creator: false,
  status: false,
  locationVisibility: false
})

const resetEditing = () => {
  editing.title = false
  editing.description = false
  editing.amount = false
  editing.term = false
  editing.start = false
  editing.end = false
  editing.audience = false
  editing.circles = false
  editing.photos = false
  editing.locationSource = false
  editing.creator = false
  editing.status = false
  editing.locationVisibility = false
}

const termModeLabel = computed(() => ({
  flexible: "Flexible",
  "start-only": "Start",
  "start-end": "Start + end"
})[props.termMode])

const audienceLabel = computed(() =>
  props.audienceOptions.find((option) => option.value === props.audience)?.label ?? props.audience
)

const locationVisibilityLabel = computed(() =>
  props.locationVisibilityOptions.find((option) => option.value === props.locationVisibility)?.label ?? props.locationVisibility
)

const locationSourceLabel = computed(() => (
  props.locationSource === "CUSTOM" ? "Custom quest location" : "Use my profile location"
))

const customLocationPending = computed(() => (
  props.locationSource === "CUSTOM"
  && Boolean(
    props.locationStreet.trim()
    || props.locationHouseNumber.trim()
    || props.locationPostalCode.trim()
    || props.locationLocality.trim()
    || props.locationCountry.trim()
  )
))

const customLocationQuery = computed(() => [
  props.locationStreet,
  props.locationHouseNumber,
  props.locationPostalCode,
  props.locationLocality,
  props.locationCountry
]
  .map((value) => value.trim())
  .filter(Boolean)
  .join(", "))

const customLocationSuggestions = reactive<{
  items: LocationLookupCandidate[]
  loading: boolean
  error: string
}>({
  items: [],
  loading: false,
  error: ""
})

const selectedCircleNames = computed(() => props.circles
  .filter((circle) => props.selectedCircleIds.includes(circle.id))
  .map((circle) => circle.name)
)

const creatorLabel = computed(() => {
  const selectedCreator = props.creatorOptions.find((user) => String(user.id) === props.creatorId)
  return selectedCreator ? `${selectedCreator.username} (${selectedCreator.email})` : "Not set"
})

const statusLabel = computed(() =>
  props.statusOptions.find((option) => option.value === props.status)?.label ?? props.status ?? "Not set"
)

const handleLocationVisibilityChange = (event: Event) => {
  emit("update:locationVisibility", (event.target as HTMLSelectElement).value as NonNullable<Quest["locationVisibility"]>)
}

const handleLocationSourceChange = (event: Event) => {
  emit("update:locationSource", (event.target as HTMLSelectElement).value as NonNullable<Quest["locationSource"]>)
}

const applyCustomLocationSuggestion = (candidate: LocationLookupCandidate) => {
  emit("update:locationStreet", candidate.street ?? "")
  emit("update:locationHouseNumber", candidate.houseNumber ?? "")
  emit("update:locationPostalCode", candidate.postalCode ?? "")
  emit("update:locationLocality", candidate.locality ?? "")
  emit("update:locationCountry", candidate.country ?? "")
  customLocationSuggestions.items = []
  customLocationSuggestions.error = ""
}

const searchCustomLocationSuggestions = async () => {
  if (props.locationSource !== "CUSTOM" || customLocationQuery.value.length < 3) {
    customLocationSuggestions.items = []
    customLocationSuggestions.error = ""
    return
  }

  customLocationSuggestions.loading = true
  customLocationSuggestions.error = ""
  try {
    const response = await workmarketApi.lookupLocation({query: customLocationQuery.value})
    customLocationSuggestions.items = response.items
    if (!response.configured) {
      customLocationSuggestions.error = "Location search is not configured yet."
    }
  } catch {
    customLocationSuggestions.items = []
    customLocationSuggestions.error = "Could not load address suggestions."
  } finally {
    customLocationSuggestions.loading = false
  }
}

useDebouncedWatch(customLocationQuery, () => {
  void searchCustomLocationSuggestions()
}, 350)
</script>

<template>
  <form
    :id="formId"
    :class="['form-stack', {'quest-composer-form--inline': inlineEditable}]"
    @submit.prevent="emit('save')"
  >
    <DetailDialogFrame>
      <template #main>
        <article
          :class="[
            'dashboard-work-panel surface-form-shell dashboard-work-panel--dialog',
            {'quest-composer-form__main--inline': inlineEditable}
          ]"
        >
          <div class="form-stack">
            <InlineEditableField
              v-if="inlineEditable"
              label="Title"
              :editing="editing.title"
              field-class="ui-edit-field--title"
              @toggle="editing.title = !editing.title"
            >
              <template #editor>
                <input
                  :value="title"
                  class="input"
                  maxlength="120"
                  @input="emit('update:title', ($event.target as HTMLInputElement).value)"
                />
              </template>
              <template #display>
                <h2 class="surface-hero__title quest-composer-form__title-display">{{ title || "Untitled" }}</h2>
              </template>
            </InlineEditableField>

            <UiFieldGroup v-else label="Title">
              <input
                :value="title"
                class="input"
                maxlength="120"
                @input="emit('update:title', ($event.target as HTMLInputElement).value)"
              />
            </UiFieldGroup>

            <InlineEditableField
              v-if="inlineEditable"
              label="Description"
              :editing="editing.description"
              field-class="ui-edit-field--description"
              @toggle="editing.description = !editing.description"
            >
              <template #editor>
                <RichTextEditor
                  :model-value="description"
                  placeholder=""
                  toolbar-label="Description tools"
                  @update:model-value="emit('update:description', $event)"
                />
              </template>
              <template #display>
                <ProfileBio
                  class="ui-content-prose ui-content-prose--flat ui-copy-prose ui-copy-prose--hero"
                  :text="description"
                  placeholder="No description."
                />
              </template>
            </InlineEditableField>

            <UiFieldGroup v-else label="Description" tag="div">
              <RichTextEditor
                :model-value="description"
                placeholder=""
                toolbar-label="Description tools"
                @update:model-value="emit('update:description', $event)"
              />
            </UiFieldGroup>

            <InlineEditableField
              v-if="inlineEditable"
              label="Photos"
              :editing="editing.photos"
              field-class="ui-edit-field--gallery"
              @toggle="editing.photos = !editing.photos"
            >
              <template #editor>
                <div class="ui-inline-readonly-stack">
                  <label class="ui-upload-trigger">
                    <input class="visually-hidden" type="file" accept="image/*" multiple @change="emit('change:images', $event)" />
                    <span class="ui-upload-trigger__label">Add photos</span>
                  </label>

                  <div v-if="images.length" class="quest-gallery">
                    <div
                      v-for="(image, index) in images"
                      :key="`${index}-${image.slice(0, 20)}`"
                      class="ui-media-tile ui-media-tile--create"
                    >
                      <img class="quest-gallery__image" :src="image" alt="Quest image preview" />
                      <button class="quest-gallery__remove quest-gallery__remove--overlay" type="button" @click="emit('remove:image', index)">
                        Remove
                      </button>
                    </div>
                  </div>
                </div>
              </template>
              <template #display>
                <div v-if="images.length" class="quest-gallery">
                  <div
                    v-for="(image, index) in images"
                    :key="`${index}-${image.slice(0, 20)}`"
                    class="ui-media-tile ui-media-tile--create"
                  >
                    <img class="quest-gallery__image" :src="image" alt="Quest image preview" />
                  </div>
                </div>
                <div v-else class="ui-inline-readonly-text">No photos.</div>
              </template>
            </InlineEditableField>

            <UiFieldGroup v-else label="Photos">
              <div class="ui-inline-readonly-stack">
                <label class="ui-upload-trigger">
                  <input class="visually-hidden" type="file" accept="image/*" multiple @change="emit('change:images', $event)" />
                  <span class="ui-upload-trigger__label">Add photos</span>
                </label>

                <div v-if="images.length" class="quest-gallery">
                  <div
                    v-for="(image, index) in images"
                    :key="`${index}-${image.slice(0, 20)}`"
                    class="ui-media-tile ui-media-tile--create"
                  >
                    <img class="quest-gallery__image" :src="image" alt="Quest image preview" />
                    <button class="quest-gallery__remove quest-gallery__remove--overlay" type="button" @click="emit('remove:image', index)">
                      Remove
                    </button>
                  </div>
                </div>
              </div>
            </UiFieldGroup>
          </div>
        </article>

        <slot name="main-after" />
      </template>

      <template #side>
        <div class="surface-stack">
            <InlineEditableField
              v-if="inlineEditable"
              label="Award amount"
              :editing="editing.amount"
              field-class="ui-edit-field--amount"
            @toggle="editing.amount = !editing.amount"
          >
            <template #editor>
              <div class="ui-amount-input">
                <span class="ui-amount-input__symbol" aria-hidden="true">$</span>
                <input
                  :value="awardAmount"
                  class="input ui-amount-input__input"
                  inputmode="decimal"
                  placeholder="50"
                  @input="emit('update:awardAmount', ($event.target as HTMLInputElement).value)"
                />
              </div>
              </template>
              <template #display>
              <div class="ui-inline-readonly-text">{{ awardAmount?.trim() ? `$ ${awardAmount.trim()}` : "Not set" }}</div>
              </template>
            </InlineEditableField>

          <UiFieldGroup v-else label="Award amount">
            <div class="ui-amount-input">
              <span class="ui-amount-input__symbol" aria-hidden="true">$</span>
              <input
                :value="awardAmount"
                class="input ui-amount-input__input"
                inputmode="decimal"
                placeholder="50"
                @input="emit('update:awardAmount', ($event.target as HTMLInputElement).value)"
              />
            </div>
          </UiFieldGroup>

          <InlineEditableField v-if="inlineEditable" label="Term" :editing="editing.term" @toggle="editing.term = !editing.term">
            <template #editor>
              <UiChoiceChips
                :model-value="termMode"
                :options="[
                  { value: 'flexible', label: 'Flexible' },
                  { value: 'start-only', label: 'Start' },
                  { value: 'start-end', label: 'Start + end' }
                ]"
                @update:model-value="emit('update:termMode', $event as 'flexible' | 'start-only' | 'start-end')"
              />
            </template>
            <template #display>
              <div class="ui-inline-readonly-text">{{ termModeLabel }}</div>
            </template>
          </InlineEditableField>

          <UiFieldGroup v-else label="Term">
            <UiChoiceChips
              :model-value="termMode"
              :options="[
                { value: 'flexible', label: 'Flexible' },
                { value: 'start-only', label: 'Start' },
                { value: 'start-end', label: 'Start + end' }
              ]"
              @update:model-value="emit('update:termMode', $event as 'flexible' | 'start-only' | 'start-end')"
            />
          </UiFieldGroup>

          <InlineEditableField v-if="inlineEditable && termMode !== 'flexible'" label="Start" :editing="editing.start" @toggle="editing.start = !editing.start">
            <template #editor>
              <input
                :value="scheduledAt"
                :min="minimumScheduleValue"
                class="input"
                type="datetime-local"
                @input="emit('update:scheduledAt', ($event.target as HTMLInputElement).value)"
              />
            </template>
            <template #display>
              <div class="ui-inline-readonly-text">{{ formatInstantForDisplay(scheduledAt) }}</div>
            </template>
          </InlineEditableField>

          <UiFieldGroup v-else-if="termMode !== 'flexible'" label="Start">
            <input
              :value="scheduledAt"
              :min="minimumScheduleValue"
              class="input"
              type="datetime-local"
              @input="emit('update:scheduledAt', ($event.target as HTMLInputElement).value)"
            />
          </UiFieldGroup>

          <InlineEditableField v-if="inlineEditable && termMode === 'start-end'" label="End" :editing="editing.end" @toggle="editing.end = !editing.end">
            <template #editor>
              <input
                :value="endsAt"
                :min="scheduledAt || minimumScheduleValue"
                class="input"
                type="datetime-local"
                @input="emit('update:endsAt', ($event.target as HTMLInputElement).value)"
              />
            </template>
            <template #display>
              <div class="ui-inline-readonly-text">{{ formatInstantForDisplay(endsAt) }}</div>
            </template>
          </InlineEditableField>

          <UiFieldGroup v-else-if="termMode === 'start-end'" label="End">
            <input
              :value="endsAt"
              :min="scheduledAt || minimumScheduleValue"
              class="input"
              type="datetime-local"
              @input="emit('update:endsAt', ($event.target as HTMLInputElement).value)"
            />
          </UiFieldGroup>

          <InlineEditableField v-if="inlineEditable" label="Visibility" :editing="editing.audience" @toggle="editing.audience = !editing.audience">
            <template #editor>
              <div class="dashboard-create-job__visibility">
                <UiChoiceChips
                  :model-value="audience"
                  :options="visibilityOptions"
                  @update:model-value="emit('update:audience', $event as 'EVERYONE' | 'CIRCLES')"
                />
              </div>
            </template>
            <template #display>
              <div class="ui-inline-readonly-text">{{ audienceLabel }}</div>
            </template>
          </InlineEditableField>

          <UiFieldGroup v-else label="Visibility">
            <div class="dashboard-create-job__visibility">
              <UiChoiceChips
                :model-value="audience"
                :options="visibilityOptions"
                @update:model-value="emit('update:audience', $event as 'EVERYONE' | 'CIRCLES')"
              />
            </div>
          </UiFieldGroup>

          <InlineEditableField
            v-if="inlineEditable"
            label="Location source"
            :editing="editing.locationSource"
            @toggle="editing.locationSource = !editing.locationSource"
          >
            <template #editor>
              <select :value="locationSource" class="input" @change="handleLocationSourceChange">
                <option value="PROFILE">Use my profile location</option>
                <option value="CUSTOM">Set different location</option>
              </select>
            </template>
            <template #display>
              <div class="ui-inline-readonly-text">{{ locationSourceLabel }}</div>
            </template>
          </InlineEditableField>

          <UiFieldGroup v-else label="Location source">
            <select :value="locationSource" class="input" @change="handleLocationSourceChange">
              <option value="PROFILE">Use my profile location</option>
              <option value="CUSTOM">Set different location</option>
            </select>
          </UiFieldGroup>

          <template v-if="locationSource === 'CUSTOM'">
            <div v-if="customLocationPending" class="quest-composer-form__location-note">
              Custom address will be resolved on save.
            </div>

            <InlineEditableField v-if="inlineEditable" label="Street" :editing="editing.locationSource" @toggle="editing.locationSource = true">
              <template #editor>
                <input :value="locationStreet" class="input" @input="emit('update:locationStreet', ($event.target as HTMLInputElement).value)" />
              </template>
              <template #display>
                <div class="ui-inline-readonly-text">{{ locationStreet || "Not set" }}</div>
              </template>
            </InlineEditableField>

            <UiFieldGroup v-else label="Street">
              <input :value="locationStreet" class="input" @input="emit('update:locationStreet', ($event.target as HTMLInputElement).value)" />
            </UiFieldGroup>

            <InlineEditableField v-if="inlineEditable" label="House number" :editing="editing.locationSource" @toggle="editing.locationSource = true">
              <template #editor>
                <input :value="locationHouseNumber" class="input" @input="emit('update:locationHouseNumber', ($event.target as HTMLInputElement).value)" />
              </template>
              <template #display>
                <div class="ui-inline-readonly-text">{{ locationHouseNumber || "Not set" }}</div>
              </template>
            </InlineEditableField>

            <UiFieldGroup v-else label="House number">
              <input :value="locationHouseNumber" class="input" @input="emit('update:locationHouseNumber', ($event.target as HTMLInputElement).value)" />
            </UiFieldGroup>

            <InlineEditableField v-if="inlineEditable" label="Postal code" :editing="editing.locationSource" @toggle="editing.locationSource = true">
              <template #editor>
                <input :value="locationPostalCode" class="input" @input="emit('update:locationPostalCode', ($event.target as HTMLInputElement).value)" />
              </template>
              <template #display>
                <div class="ui-inline-readonly-text">{{ locationPostalCode || "Not set" }}</div>
              </template>
            </InlineEditableField>

            <UiFieldGroup v-else label="Postal code">
              <input :value="locationPostalCode" class="input" @input="emit('update:locationPostalCode', ($event.target as HTMLInputElement).value)" />
            </UiFieldGroup>

            <InlineEditableField v-if="inlineEditable" label="City" :editing="editing.locationSource" @toggle="editing.locationSource = true">
              <template #editor>
                <input :value="locationLocality" class="input" @input="emit('update:locationLocality', ($event.target as HTMLInputElement).value)" />
              </template>
              <template #display>
                <div class="ui-inline-readonly-text">{{ locationLocality || "Not set" }}</div>
              </template>
            </InlineEditableField>

            <UiFieldGroup v-else label="City">
              <input :value="locationLocality" class="input" @input="emit('update:locationLocality', ($event.target as HTMLInputElement).value)" />
            </UiFieldGroup>

            <InlineEditableField v-if="inlineEditable" label="Country" :editing="editing.locationSource" @toggle="editing.locationSource = true">
              <template #editor>
                <input :value="locationCountry" class="input" @input="emit('update:locationCountry', ($event.target as HTMLInputElement).value)" />
              </template>
              <template #display>
                <div class="ui-inline-readonly-text">{{ locationCountry || "Not set" }}</div>
              </template>
            </InlineEditableField>

            <UiFieldGroup v-else label="Country">
              <input :value="locationCountry" class="input" @input="emit('update:locationCountry', ($event.target as HTMLInputElement).value)" />
            </UiFieldGroup>

            <div v-if="customLocationSuggestions.loading" class="muted">
              Searching address suggestions...
            </div>

            <div v-else-if="customLocationSuggestions.error" class="muted">
              {{ customLocationSuggestions.error }}
            </div>

            <div v-else-if="customLocationSuggestions.items.length" class="quest-composer-form__location-suggestions">
              <button
                v-for="candidate in customLocationSuggestions.items"
                :key="`${candidate.latitude}:${candidate.longitude}:${candidate.label}`"
                class="quest-composer-form__location-suggestion"
                type="button"
                @click="applyCustomLocationSuggestion(candidate)"
              >
                <strong>{{ candidate.label }}</strong>
                <small>{{ candidate.locality || candidate.country || "Address suggestion" }}</small>
              </button>
            </div>
          </template>

          <InlineEditableField
            v-if="inlineEditable"
            label="Quest location"
            :editing="editing.locationVisibility"
            @toggle="editing.locationVisibility = !editing.locationVisibility"
          >
            <template #editor>
              <select :value="locationVisibility" class="input" @change="handleLocationVisibilityChange">
                <option v-for="option in locationVisibilityOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </template>
            <template #display>
              <div class="ui-inline-readonly-text">{{ locationVisibilityLabel }}</div>
            </template>
          </InlineEditableField>

          <UiFieldGroup v-else label="Quest location">
            <select :value="locationVisibility" class="input" @change="handleLocationVisibilityChange">
              <option v-for="option in locationVisibilityOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </UiFieldGroup>

          <InlineEditableField
            v-if="inlineEditable && audience === 'CIRCLES'"
            label="Visible to circles"
            :editing="editing.circles"
            field-class="ui-edit-field--circles"
            @toggle="editing.circles = !editing.circles"
          >
            <template #editor>
              <div class="dashboard-create-job__circles">
                <details v-if="circles.length" class="compact-disclosure dashboard-create-job__circle-picker" open>
                  <summary>
                    {{ selectedCircleIds.length ? `${selectedCircleIds.length} selected` : "Choose circles" }}
                  </summary>

                  <div class="dashboard-create-job__circle-checklist">
                    <label
                      v-for="circle in circles"
                      :key="circle.id"
                      class="checkbox-field dashboard-create-job__circle-option"
                    >
                      <input
                        :checked="selectedCircleIds.includes(circle.id)"
                        type="checkbox"
                        @change="emit('toggle:circle', circle.id)"
                      />
                      <span>{{ circle.name }}</span>
                    </label>
                  </div>
                </details>

                <div v-if="circles.length" class="dashboard-create-job__circle-meta">
                  {{ selectedCircleIds.length }} selected
                </div>

                <div v-else class="muted">No circles yet.</div>
              </div>
            </template>
            <template #display>
              <div class="ui-inline-readonly-stack">
                <div v-if="selectedCircleNames.length" class="ui-chip-group ui-chip-group--stack">
                  <span v-for="circleName in selectedCircleNames" :key="circleName" class="ui-chip ui-chip--wide ui-chip--active">
                    {{ circleName }}
                  </span>
                </div>
                <div v-else class="ui-inline-readonly-text">No circles selected.</div>
              </div>
            </template>
          </InlineEditableField>

          <UiFieldGroup v-else-if="audience === 'CIRCLES'" label="Visible to circles">
            <div class="dashboard-create-job__circles">
              <details v-if="circles.length" class="compact-disclosure dashboard-create-job__circle-picker">
                <summary>
                  {{ selectedCircleIds.length ? `${selectedCircleIds.length} selected` : "Choose circles" }}
                </summary>

                <div class="dashboard-create-job__circle-checklist">
                  <label
                    v-for="circle in circles"
                    :key="circle.id"
                    class="checkbox-field dashboard-create-job__circle-option"
                  >
                    <input
                      :checked="selectedCircleIds.includes(circle.id)"
                      type="checkbox"
                      @change="emit('toggle:circle', circle.id)"
                    />
                    <span>{{ circle.name }}</span>
                  </label>
                </div>
              </details>

              <div v-if="circles.length" class="dashboard-create-job__circle-meta">
                {{ selectedCircleIds.length }} selected
              </div>

              <div v-else class="muted">No circles yet.</div>
            </div>
          </UiFieldGroup>

          <InlineEditableField v-if="inlineEditable && showCreator" label="Creator" :editing="editing.creator" @toggle="editing.creator = !editing.creator">
            <template #editor>
              <select
                :value="creatorId"
                class="input"
                @change="emit('update:creatorId', ($event.target as HTMLSelectElement).value)"
              >
                <option v-for="user in creatorOptions" :key="user.id" :value="String(user.id)">
                  {{ user.username }} ({{ user.email }})
                </option>
              </select>
            </template>
            <template #display>
              <div class="ui-inline-readonly-text">{{ creatorLabel }}</div>
            </template>
          </InlineEditableField>

          <UiFieldGroup v-else-if="showCreator" label="Creator">
            <select
              :value="creatorId"
              class="input"
              @change="emit('update:creatorId', ($event.target as HTMLSelectElement).value)"
            >
              <option v-for="user in creatorOptions" :key="user.id" :value="String(user.id)">
                {{ user.username }} ({{ user.email }})
              </option>
            </select>
          </UiFieldGroup>

          <InlineEditableField v-if="inlineEditable && showStatus" label="Status" :editing="editing.status" @toggle="editing.status = !editing.status">
            <template #editor>
              <select
                :value="status"
                class="input"
                @change="emit('update:status', ($event.target as HTMLSelectElement).value as QuestStatus)"
              >
                <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </template>
            <template #display>
              <div class="ui-inline-readonly-text">{{ statusLabel }}</div>
            </template>
          </InlineEditableField>

          <UiFieldGroup v-else-if="showStatus" label="Status">
            <select
              :value="status"
              class="input"
              @change="emit('update:status', ($event.target as HTMLSelectElement).value as QuestStatus)"
            >
              <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </UiFieldGroup>

          <button
            v-if="submitVisible"
            class="button button--action button--flat-primary"
            type="button"
            :disabled="isSubmitDisabled"
            @click="emit('save')"
          >
            {{ submitLabel }}
          </button>

          <button v-if="showCancel && submitVisible" class="button button--ghost" type="button" @click="resetEditing(); emit('cancel')">
            {{ cancelLabel }}
          </button>

          <slot name="side-after" />
        </div>
      </template>
    </DetailDialogFrame>
  </form>
</template>

<style scoped>
.quest-composer-form__location-note {
  color: #b45309;
  font-size: 0.74rem;
  font-weight: 700;
}

.quest-composer-form__location-suggestions {
  display: grid;
  gap: 6px;
}

.quest-composer-form__location-suggestion {
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

.quest-composer-form__location-suggestion small {
  color: var(--text-muted);
  font-size: 0.72rem;
  font-weight: 600;
}
</style>
