<script setup lang="ts">
import {computed, reactive} from "vue"
import {visionApi, type CircleGroup, type LocationLookupCandidate, type Quest, type QuestAudienceOption, type QuestLocationVisibilityOption} from "../../api/visionApi.ts"
import type {QuestStatus} from "../../domain/visionDomain.ts"
import {useDebouncedWatch} from "../../../../composables/useDebouncedWatch.ts"
import {formatQuestReward} from "../../shared/pricing.ts"

const props = withDefaults(defineProps<{
  formId?: string
  title: string
  description: string
  awardAmount: string
  assigneeTarget: string
  showApprovedApplicants: boolean
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
  "update:assigneeTarget": [value: string]
  "update:showApprovedApplicants": [value: boolean]
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

const circlesRequiredMissing = computed(() => props.audience === "CIRCLES" && props.selectedCircleIds.length === 0)
const isSubmitDisabled = computed(() => props.submitDisabled || circlesRequiredMissing.value)
const minimumScheduleValue = computed(() => new Date().toISOString().slice(0, 16))

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
  .map((circle) => circle.name))

const statusLabel = computed(() =>
  props.statusOptions.find((option) => option.value === props.status)?.label ?? props.status ?? "Not set"
)

const locationVisibilityLabel = computed(() =>
  props.locationVisibilityOptions.find((option) => option.value === props.locationVisibility)?.label ?? props.locationVisibility
)

const searchCustomLocationSuggestions = async () => {
  if (props.locationSource !== "CUSTOM" || customLocationQuery.value.length < 3) {
    customLocationSuggestions.items = []
    customLocationSuggestions.error = ""
    return
  }

  customLocationSuggestions.loading = true
  customLocationSuggestions.error = ""
  try {
    const response = await visionApi.lookupLocation({query: customLocationQuery.value})
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

const applyCustomLocationSuggestion = (candidate: LocationLookupCandidate) => {
  emit("update:locationStreet", candidate.street ?? "")
  emit("update:locationHouseNumber", candidate.houseNumber ?? "")
  emit("update:locationPostalCode", candidate.postalCode ?? "")
  emit("update:locationLocality", candidate.locality ?? "")
  emit("update:locationCountry", candidate.country ?? "")
  customLocationSuggestions.items = []
  customLocationSuggestions.error = ""
}

useDebouncedWatch(customLocationQuery, () => {
  void searchCustomLocationSuggestions()
}, 350)
</script>

<template>
  <form :id="formId" class="vision-terminal-feed" @submit.prevent="emit('save')">
    <section class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">core</p>
      <label class="vision-terminal-feed__field">
        <span>Title</span>
        <input :value="title" class="input" maxlength="120" @input="emit('update:title', ($event.target as HTMLInputElement).value)" />
      </label>
      <label class="vision-terminal-feed__field">
        <span>Description</span>
        <textarea
          :value="description"
          class="input vision-terminal-feed__textarea"
          rows="5"
          @input="emit('update:description', ($event.target as HTMLTextAreaElement).value)"
        />
      </label>
    </section>

    <section class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">media</p>
      <label class="vision-terminal-feed__link-button">
        Add photos
        <input class="visually-hidden" type="file" accept="image/*" multiple @change="emit('change:images', $event)" />
      </label>
      <div v-if="images.length" class="vision-terminal-feed__stack">
        <button
          v-for="(image, index) in images"
          :key="`${index}-${image.slice(0, 20)}`"
          class="vision-terminal-feed__list-button"
          type="button"
          @click="emit('remove:image', index)"
        >
          <span>Photo {{ index + 1 }}</span>
          <span>Remove</span>
        </button>
      </div>
      <p v-else class="vision-terminal-feed__line vision-terminal-feed__line--soft">No photos.</p>
    </section>

    <section class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">pricing and timing</p>
      <label class="vision-terminal-feed__field">
        <span>Reward</span>
        <input :value="awardAmount" class="input" inputmode="decimal" placeholder="50" @input="emit('update:awardAmount', ($event.target as HTMLInputElement).value)" />
      </label>
      <label class="vision-terminal-feed__field">
        <span>Workers needed</span>
        <input :value="assigneeTarget" class="input" inputmode="numeric" min="1" placeholder="1" @input="emit('update:assigneeTarget', ($event.target as HTMLInputElement).value)" />
      </label>
      <label class="vision-terminal-feed__field">
        <span>Show approved workers</span>
        <select :value="showApprovedApplicants ? 'yes' : 'no'" class="input" @change="emit('update:showApprovedApplicants', ($event.target as HTMLSelectElement).value === 'yes')">
          <option value="no">Hidden</option>
          <option value="yes">Visible</option>
        </select>
      </label>
      <label class="vision-terminal-feed__field">
        <span>Term</span>
        <select :value="termMode" class="input" @change="emit('update:termMode', ($event.target as HTMLSelectElement).value as 'flexible' | 'start-only' | 'start-end')">
          <option value="flexible">Flexible</option>
          <option value="start-only">Start</option>
          <option value="start-end">Start + end</option>
        </select>
      </label>
      <label v-if="termMode !== 'flexible'" class="vision-terminal-feed__field">
        <span>Start</span>
        <input :value="scheduledAt" :min="minimumScheduleValue" class="input" type="datetime-local" @input="emit('update:scheduledAt', ($event.target as HTMLInputElement).value)" />
      </label>
      <label v-if="termMode === 'start-end'" class="vision-terminal-feed__field">
        <span>End</span>
        <input :value="endsAt" :min="scheduledAt || minimumScheduleValue" class="input" type="datetime-local" @input="emit('update:endsAt', ($event.target as HTMLInputElement).value)" />
      </label>
    </section>

    <section class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">visibility</p>
      <label class="vision-terminal-feed__field">
        <span>Audience</span>
        <select :value="audience" class="input" @change="emit('update:audience', ($event.target as HTMLSelectElement).value as 'EVERYONE' | 'CIRCLES')">
          <option v-for="option in visibilityOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
        </select>
      </label>

      <div v-if="audience === 'CIRCLES'" class="vision-terminal-feed__stack">
        <button
          v-for="circle in circles"
          :key="circle.id"
          type="button"
          class="vision-terminal-feed__list-button"
          :class="{ 'vision-terminal-feed__list-button--active': selectedCircleIds.includes(circle.id) }"
          @click="emit('toggle:circle', circle.id)"
        >
          <span>{{ circle.name }}</span>
          <span>{{ selectedCircleIds.includes(circle.id) ? "selected" : "add" }}</span>
        </button>
        <p v-if="!circles.length" class="vision-terminal-feed__line vision-terminal-feed__line--soft">No circles yet.</p>
      </div>
    </section>

    <section class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">location</p>
      <label class="vision-terminal-feed__field">
        <span>Source</span>
        <select :value="locationSource" class="input" @change="$emit('update:locationSource', ($event.target as HTMLSelectElement).value as NonNullable<Quest['locationSource']>)">
          <option value="PROFILE">Use my profile location</option>
          <option value="CUSTOM">Set different location</option>
        </select>
      </label>

      <template v-if="locationSource === 'CUSTOM'">
        <p v-if="customLocationSuggestions.loading" class="vision-terminal-feed__line vision-terminal-feed__line--soft">Searching address suggestions...</p>
        <p v-else-if="customLocationSuggestions.error" class="vision-terminal-feed__line vision-terminal-feed__line--error">{{ customLocationSuggestions.error }}</p>

        <div v-else-if="customLocationSuggestions.items.length" class="vision-terminal-feed__stack">
          <button
            v-for="candidate in customLocationSuggestions.items"
            :key="`${candidate.latitude}:${candidate.longitude}:${candidate.label}`"
            type="button"
            class="vision-terminal-feed__list-button"
            @click="applyCustomLocationSuggestion(candidate)"
          >
            <span>{{ candidate.label }}</span>
            <span>{{ candidate.locality || candidate.country || "Address suggestion" }}</span>
          </button>
        </div>

        <label class="vision-terminal-feed__field">
          <span>Street</span>
          <input :value="locationStreet" class="input" @input="emit('update:locationStreet', ($event.target as HTMLInputElement).value)" />
        </label>
        <label class="vision-terminal-feed__field">
          <span>House number</span>
          <input :value="locationHouseNumber" class="input" @input="emit('update:locationHouseNumber', ($event.target as HTMLInputElement).value)" />
        </label>
        <label class="vision-terminal-feed__field">
          <span>Postal code</span>
          <input :value="locationPostalCode" class="input" @input="emit('update:locationPostalCode', ($event.target as HTMLInputElement).value)" />
        </label>
        <label class="vision-terminal-feed__field">
          <span>City</span>
          <input :value="locationLocality" class="input" @input="emit('update:locationLocality', ($event.target as HTMLInputElement).value)" />
        </label>
        <label class="vision-terminal-feed__field">
          <span>Country</span>
          <input :value="locationCountry" class="input" @input="emit('update:locationCountry', ($event.target as HTMLInputElement).value)" />
        </label>
      </template>

      <label class="vision-terminal-feed__field">
        <span>Quest location</span>
        <select :value="locationVisibility" class="input" @change="emit('update:locationVisibility', ($event.target as HTMLSelectElement).value as NonNullable<Quest['locationVisibility']>)">
          <option v-for="option in locationVisibilityOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
        </select>
      </label>
      <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">Location: {{ locationVisibilityLabel }}</p>
    </section>

    <section v-if="showCreator" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">creator</p>
      <label class="vision-terminal-feed__field">
        <span>Creator</span>
        <select :value="creatorId" class="input" @change="emit('update:creatorId', ($event.target as HTMLSelectElement).value)">
          <option v-for="user in creatorOptions" :key="user.id" :value="String(user.id)">
            {{ user.username }} ({{ user.email }})
          </option>
        </select>
      </label>
    </section>

    <section v-if="showStatus" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">status</p>
      <label class="vision-terminal-feed__field">
        <span>Status</span>
        <select :value="status" class="input" @change="emit('update:status', ($event.target as HTMLSelectElement).value as QuestStatus)">
          <option v-for="option in statusOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
        </select>
      </label>
      <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">{{ statusLabel }}</p>
    </section>

    <section class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">summary</p>
      <p class="vision-terminal-feed__line">Title: {{ title || "Untitled" }}</p>
      <p class="vision-terminal-feed__line">Reward: {{ awardAmount ? formatQuestReward(Number(awardAmount)) : "Not set" }}</p>
      <p class="vision-terminal-feed__line">Workers: {{ assigneeTarget?.trim() ? assigneeTarget.trim() : "1" }}</p>
      <p class="vision-terminal-feed__line">Term: {{ termMode }}</p>
      <p v-if="selectedCircleNames.length" class="vision-terminal-feed__line">Circles: {{ selectedCircleNames.join(", ") }}</p>
      <p v-if="locationSource === 'CUSTOM'" class="vision-terminal-feed__line">Location preview: {{ [locationStreet, locationHouseNumber, locationPostalCode, locationLocality, locationCountry].filter(Boolean).join(", ") || "Not set" }}</p>
    </section>

    <section class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">actions</p>
      <div class="vision-terminal-feed__action-row">
        <button v-if="submitVisible" class="vision-terminal-feed__link-button" type="submit" :disabled="isSubmitDisabled">
          {{ submitLabel }}
        </button>
        <button v-if="showCancel && submitVisible" class="vision-terminal-feed__link-button" type="button" @click="emit('cancel')">
          {{ cancelLabel }}
        </button>
      </div>
    </section>
  </form>
</template>
