<script setup lang="ts">
import {computed, reactive} from "vue"
import RichTextEditor from "../../../../components/editor/AsyncRichTextEditor.vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import UiChoiceChips from "../../../../components/ui/UiChoiceChips.vue"
import type {CircleGroup, QuestAudienceOption} from "../../api/workmarketApi.ts"
import type {QuestStatus} from "../../domain/workmarketDomain.ts"
import {formatInstantForDisplay} from "../../../../shared/questSchedule.ts"
import InlineEditableField from "./InlineEditableField.vue"
import {formatQuestReward} from "../../shared/pricing.ts"

const minimumScheduleValue = new Date().toISOString().slice(0, 16)

const props = withDefaults(defineProps<{
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
  images?: string[]
  showImages?: boolean
  showAudience?: boolean
  inlineEditable?: boolean
  showAdminFields?: boolean
  creatorId?: string
  creatorOptions?: Array<{id: number; username: string; email: string}>
  status?: QuestStatus
  statusOptions?: Array<{value: QuestStatus; label: string}>
}>(), {
  showImages: false,
  showAudience: true,
  inlineEditable: true,
  showAdminFields: false,
})

const emit = defineEmits<{
  (event: "update:title", value: string): void
  (event: "update:description", value: string): void
  (event: "update:awardAmount", value: string): void
  (event: "update:termMode", value: "flexible" | "start-only" | "start-end"): void
  (event: "update:scheduledAt", value: string): void
  (event: "update:endsAt", value: string): void
  (event: "update:audience", value: "EVERYONE" | "CIRCLES"): void
  (event: "toggle:circle", circleId: number): void
  (name: "change:images", payload: Event): void
  (event: "remove:image", index: number): void
  (event: "update:creatorId", value: string): void
  (event: "update:status", value: QuestStatus): void
}>()

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
  creator: false,
  status: false
})

const termModeLabel = computed(() => ({
  flexible: "Flexible",
  "start-only": "Start",
  "start-end": "Start + end"
})[props.termMode])

const audienceLabel = computed(() =>
  props.audienceOptions.find((option) => option.value === props.audience)?.label ?? props.audience
)

const statusLabel = computed(() =>
  props.statusOptions?.find((option) => option.value === props.status)?.label ?? props.status ?? "Not set"
)

const selectedCircleNames = computed(() => props.circles
  .filter((circle) => props.selectedCircleIds.includes(circle.id))
  .map((circle) => circle.name)
)

const creatorLabel = computed(() => {
  const selectedCreator = props.creatorOptions?.find((user) => String(user.id) === props.creatorId)
  return selectedCreator ? `${selectedCreator.username} (${selectedCreator.email})` : "Not set"
})
</script>

<template>
  <div class="ui-edit-form ui-edit-form--dialog ui-edit-form--quest">
    <InlineEditableField
      label="Title"
      :editing="inlineEditable ? editing.title : true"
      :editable="inlineEditable"
      field-class="ui-edit-field--title ui-edit-field--price"
      @toggle="editing.title = !editing.title"
    >
      <template #editor>
        <input :value="title" class="input" maxlength="120" @input="emit('update:title', ($event.target as HTMLInputElement).value)" />
      </template>
      <template #display>
        <div class="ui-inline-readonly-text">{{ title || "Untitled" }}</div>
      </template>
    </InlineEditableField>

    <InlineEditableField
      label="Description"
      :editing="inlineEditable ? editing.description : true"
      :editable="inlineEditable"
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
          class="ui-content-prose ui-content-prose--flat"
          :text="description"
          placeholder="No description."
        />
      </template>
    </InlineEditableField>

    <div class="quest-edit-form__compact-grid">
      <InlineEditableField
        label="Award amount"
        :editing="inlineEditable ? editing.amount : true"
        :editable="inlineEditable"
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
          <div class="ui-inline-readonly-text">{{ awardAmount?.trim() ? formatQuestReward(Number(awardAmount.trim())) : "Not set" }}</div>
        </template>
      </InlineEditableField>

      <InlineEditableField label="Term" :editing="inlineEditable ? editing.term : true" :editable="inlineEditable" @toggle="editing.term = !editing.term">
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

      <InlineEditableField
        v-if="termMode !== 'flexible'"
        label="Start"
        :editing="inlineEditable ? editing.start : true"
        :editable="inlineEditable"
        @toggle="editing.start = !editing.start"
      >
        <template #editor>
          <input :value="scheduledAt" :min="minimumScheduleValue" class="input" type="datetime-local" @input="emit('update:scheduledAt', ($event.target as HTMLInputElement).value)" />
        </template>
        <template #display>
          <div class="ui-inline-readonly-text">{{ formatInstantForDisplay(scheduledAt) }}</div>
        </template>
      </InlineEditableField>

      <InlineEditableField
        v-if="termMode === 'start-end'"
        label="End"
        :editing="inlineEditable ? editing.end : true"
        :editable="inlineEditable"
        @toggle="editing.end = !editing.end"
      >
        <template #editor>
          <input :value="endsAt" :min="scheduledAt || minimumScheduleValue" class="input" type="datetime-local" @input="emit('update:endsAt', ($event.target as HTMLInputElement).value)" />
        </template>
        <template #display>
          <div class="ui-inline-readonly-text">{{ formatInstantForDisplay(endsAt) }}</div>
        </template>
      </InlineEditableField>

      <InlineEditableField
        v-if="showAudience !== false"
        label="Visibility"
        :editing="inlineEditable ? editing.audience : true"
        :editable="inlineEditable"
        @toggle="editing.audience = !editing.audience"
      >
        <template #editor>
          <select :value="audience" class="input" @change="emit('update:audience', ($event.target as HTMLSelectElement).value as 'EVERYONE' | 'CIRCLES')">
            <option v-for="option in audienceOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
        </template>
        <template #display>
          <div class="ui-inline-readonly-text">{{ audienceLabel }}</div>
        </template>
      </InlineEditableField>

      <template v-if="showAdminFields">
        <InlineEditableField label="Creator" :editing="inlineEditable ? editing.creator : true" :editable="inlineEditable" @toggle="editing.creator = !editing.creator">
          <template #editor>
            <select :value="creatorId" class="input" @change="emit('update:creatorId', ($event.target as HTMLSelectElement).value)">
              <option v-for="user in creatorOptions ?? []" :key="user.id" :value="String(user.id)">{{ user.username }} ({{ user.email }})</option>
            </select>
          </template>
          <template #display>
            <div class="ui-inline-readonly-text">{{ creatorLabel }}</div>
          </template>
        </InlineEditableField>

        <InlineEditableField label="Status" :editing="inlineEditable ? editing.status : true" :editable="inlineEditable" @toggle="editing.status = !editing.status">
          <template #editor>
            <select :value="status" class="input" @change="emit('update:status', ($event.target as HTMLSelectElement).value as QuestStatus)">
              <option v-for="option in statusOptions ?? []" :key="option.value" :value="option.value">{{ option.label }}</option>
            </select>
          </template>
          <template #display>
            <div class="ui-inline-readonly-text">{{ statusLabel }}</div>
          </template>
        </InlineEditableField>
      </template>
    </div>

    <InlineEditableField
      v-if="showAudience !== false && audience === 'CIRCLES'"
      label="Visible to circles"
      :editing="inlineEditable ? editing.circles : true"
      :editable="inlineEditable"
      field-class="ui-edit-field--circles"
      @toggle="editing.circles = !editing.circles"
    >
      <template #editor>
        <div v-if="circles.length" class="ui-chip-group ui-chip-group--stack">
          <button
            v-for="circle in circles"
            :key="circle.id"
            class="ui-chip ui-chip--wide"
            :class="{ 'ui-chip--active': selectedCircleIds.includes(circle.id) }"
            type="button"
            @click="emit('toggle:circle', circle.id)"
          >
            {{ circle.name }}
          </button>
        </div>
        <div v-else class="muted">Create a circle first.</div>
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

    <InlineEditableField
      v-if="showImages"
      label="Photos"
      :editing="inlineEditable ? editing.photos : true"
      :editable="inlineEditable"
      field-class="ui-edit-field--gallery"
      @toggle="editing.photos = !editing.photos"
    >
      <template #editor>
        <div class="ui-inline-readonly-stack">
          <div class="badge badge--accent">{{ images?.length ?? 0 }}/10</div>
          <label class="ui-upload-trigger">
            <input class="visually-hidden" type="file" accept="image/*" multiple @change="emit('change:images', $event)" />
            <span class="ui-upload-trigger__label">Add photos</span>
          </label>

          <div v-if="images?.length" class="quest-gallery">
            <div v-for="(image, index) in images" :key="`${index}-${image.slice(0, 20)}`" class="ui-media-tile ui-media-tile--create">
              <img class="quest-gallery__image" :src="image" alt="Quest image preview" />
              <button class="quest-gallery__remove quest-gallery__remove--overlay" type="button" @click="emit('remove:image', index)">
                Remove
              </button>
            </div>
          </div>
        </div>
      </template>
      <template #display>
        <div v-if="images?.length" class="quest-gallery">
          <div v-for="(image, index) in images" :key="`${index}-${image.slice(0, 20)}`" class="ui-media-tile ui-media-tile--create">
            <img class="quest-gallery__image" :src="image" alt="Quest image preview" />
          </div>
        </div>
        <div v-else class="ui-inline-readonly-text">No photos.</div>
      </template>
    </InlineEditableField>
  </div>
</template>
