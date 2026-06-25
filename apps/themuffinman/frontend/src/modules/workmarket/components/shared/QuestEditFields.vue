<script setup lang="ts">
import RichTextEditor from "../../../../components/editor/AsyncRichTextEditor.vue"
import UiAmountField from "../../../../components/ui/UiAmountField.vue"
import UiChoiceChips from "../../../../components/ui/UiChoiceChips.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import type {CircleGroup, QuestAudienceOption} from "../../api/workmarketApi.ts"
import type {QuestStatus} from "../../domain/workmarketDomain.ts"

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
  showAdminFields?: boolean
  creatorId?: string
  creatorOptions?: Array<{id: number; username: string; email: string}>
  status?: QuestStatus
  statusOptions?: Array<{value: QuestStatus; label: string}>
}>(), {
  showImages: false,
  showAudience: true,
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
</script>

<template>
  <div class="ui-edit-form ui-edit-form--dialog ui-edit-form--quest">
    <div class="quest-edit-layout">
      <div class="quest-edit-layout__main">
        <UiFieldGroup label="Title" field-class="ui-edit-field ui-edit-field--title ui-edit-field--price">
          <input :value="title" class="input" maxlength="120" @input="emit('update:title', ($event.target as HTMLInputElement).value)" />
        </UiFieldGroup>

        <UiFieldGroup label="Description" tag="div" field-class="ui-edit-field ui-edit-field--description">
          <RichTextEditor
            :model-value="description"
            placeholder=""
            toolbar-label="Description tools"
            @update:model-value="emit('update:description', $event)"
          />
        </UiFieldGroup>

        <UiFieldGroup v-if="showAudience !== false && audience === 'CIRCLES'" label="Visible to circles" field-class="ui-edit-field ui-edit-field--circles">
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
        </UiFieldGroup>
      </div>

      <div class="quest-edit-layout__side">
        <div class="quest-edit-form__compact-grid">
          <UiAmountField
            :model-value="awardAmount"
            label="Award amount"
            placeholder="50"
            field-class="ui-edit-field ui-edit-field--amount"
            @update:model-value="emit('update:awardAmount', $event)"
          />

          <UiFieldGroup label="Term" field-class="ui-edit-field">
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

          <UiFieldGroup v-if="termMode !== 'flexible'" label="Start" field-class="ui-edit-field">
            <input :value="scheduledAt" :min="minimumScheduleValue" class="input" type="datetime-local" @input="emit('update:scheduledAt', ($event.target as HTMLInputElement).value)" />
          </UiFieldGroup>

          <UiFieldGroup v-if="termMode === 'start-end'" label="End" field-class="ui-edit-field">
            <input :value="endsAt" :min="scheduledAt || minimumScheduleValue" class="input" type="datetime-local" @input="emit('update:endsAt', ($event.target as HTMLInputElement).value)" />
          </UiFieldGroup>

          <UiFieldGroup v-if="showAudience !== false" label="Visibility" field-class="ui-edit-field">
            <select :value="audience" class="input" @change="emit('update:audience', ($event.target as HTMLSelectElement).value as 'EVERYONE' | 'CIRCLES')">
              <option v-for="option in audienceOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
            </select>
          </UiFieldGroup>

          <template v-if="showAdminFields">
            <UiFieldGroup label="Creator" field-class="ui-edit-field">
              <select :value="creatorId" class="input" @change="emit('update:creatorId', ($event.target as HTMLSelectElement).value)">
                <option v-for="user in creatorOptions ?? []" :key="user.id" :value="String(user.id)">{{ user.username }} ({{ user.email }})</option>
              </select>
            </UiFieldGroup>

            <UiFieldGroup label="Status" field-class="ui-edit-field">
              <select :value="status" class="input" @change="emit('update:status', ($event.target as HTMLSelectElement).value as QuestStatus)">
                <option v-for="option in statusOptions ?? []" :key="option.value" :value="option.value">{{ option.label }}</option>
              </select>
            </UiFieldGroup>
          </template>
        </div>

        <UiFieldGroup v-if="showImages" label="Photos" tag="div" field-class="ui-edit-field ui-edit-field--gallery">
          <template #headerAction>
            <span class="badge badge--accent">{{ images?.length ?? 0 }}/10</span>
          </template>

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
        </UiFieldGroup>
      </div>
    </div>
  </div>
</template>
