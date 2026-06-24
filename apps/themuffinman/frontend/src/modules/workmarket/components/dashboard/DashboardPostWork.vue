<script setup lang="ts">
import DashboardEditSheet from "./DashboardEditSheet.vue"
import RichTextEditor from "../../../../components/editor/RichTextEditor.vue"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import {useDashboardPostWorkState} from "../../composables/dashboard/useDashboardPostWorkState.ts"

const props = withDefaults(defineProps<{
  dashboard: QuestDashboard
  boxed?: boolean
}>(), {
  boxed: true,
})

const {
  handleQuestImagesChange,
  toggleQuestAudience,
  toggleQuestCircle
} = useDashboardPostWorkState(props.dashboard)
</script>

<template>
  <section class="stack">
    <article :class="[
      'dashboard-work-panel',
      'dashboard-work-panel--create',
      {
        card: props.boxed,
        'dashboard-work-panel--dialog': !props.boxed,
        'ui-pulse': dashboard.successPulseTarget === 'create-job'
      }
    ]">
      <form @submit.prevent="dashboard.createQuest">
        <DashboardEditSheet :minimal="true">
          <div class="dashboard-create-job__layout">
            <div class="dashboard-create-job__column dashboard-create-job__column--main">
              <label class="field dashboard-edit-field dashboard-edit-field--title">
                <span class="label">Title</span>
                <input v-model="dashboard.questTitle" class="input" maxlength="120" placeholder="Need help moving a sofa" />
              </label>

              <label class="field dashboard-edit-field dashboard-edit-field--description">
                <span class="label">Description</span>
                <RichTextEditor
                  v-model="dashboard.questDescription"
                  placeholder=""
                  toolbar-label="Description tools"
                />
              </label>

              <div class="field dashboard-edit-field dashboard-edit-field--gallery">
                <div class="field__header">
                  <span class="label">Photos</span>
                  <span class="badge badge--accent">{{ dashboard.questImages.length }}/10</span>
                </div>
                <label class="dashboard-create-job__upload">
                  <input
                    class="visually-hidden"
                    type="file"
                    accept="image/*"
                    multiple
                    @change="handleQuestImagesChange"
                  />
                  <span class="dashboard-create-job__upload-action">Add photos</span>
                </label>
                <div v-if="dashboard.questImages.length" class="quest-gallery">
                  <div v-for="(image, index) in dashboard.questImages" :key="`${index}-${image.slice(0, 20)}`" class="quest-gallery__item quest-gallery__item--create">
                    <img class="quest-gallery__image" :src="image" alt="Quest image preview" />
                    <button class="quest-gallery__remove quest-gallery__remove--overlay" type="button" @click="dashboard.removeQuestImage(index)">
                      Remove
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div class="dashboard-create-job__column dashboard-create-job__column--side">
              <label class="field dashboard-edit-field dashboard-edit-field--amount">
                <span class="label">Pay</span>
                <div class="dashboard-edit-amount dashboard-edit-amount--compact">
                  <span class="dashboard-edit-amount__symbol" aria-hidden="true">$</span>
                  <input
                    v-model="dashboard.questAwardAmount"
                    class="input dashboard-edit-amount__input"
                    inputmode="decimal"
                    placeholder="50"
                  />
                </div>
              </label>

              <label class="field dashboard-edit-field dashboard-create-job__compact-field">
                <span class="label">Timing</span>
                <div class="dashboard-create-job__chip-group">
                  <button class="dashboard-create-job__chip" :class="{ 'dashboard-create-job__chip--active': dashboard.questTermMode === 'flexible' }" type="button" @click="dashboard.setQuestTermMode('flexible')">
                    Any time
                  </button>
                  <button class="dashboard-create-job__chip" :class="{ 'dashboard-create-job__chip--active': dashboard.questTermMode === 'start-only' }" type="button" @click="dashboard.setQuestTermMode('start-only')">
                    Start
                  </button>
                  <button class="dashboard-create-job__chip" :class="{ 'dashboard-create-job__chip--active': dashboard.questTermMode === 'start-end' }" type="button" @click="dashboard.setQuestTermMode('start-end')">
                    Range
                  </button>
                </div>
              </label>

              <label v-if="dashboard.questTermMode !== 'flexible'" class="field dashboard-edit-field dashboard-create-job__compact-field">
                <span class="label">Start</span>
                <input v-model="dashboard.questScheduledAt" class="input" type="datetime-local" />
              </label>

              <label v-if="dashboard.questTermMode === 'start-end'" class="field dashboard-edit-field dashboard-create-job__compact-field">
                <span class="label">End</span>
                <input v-model="dashboard.questEndsAt" class="input" type="datetime-local" />
              </label>

              <label class="field dashboard-edit-field dashboard-create-job__compact-field">
                <span class="label">Visibility</span>
                <div class="dashboard-create-job__chip-group dashboard-create-job__chip-group--stack">
                  <button
                    v-for="option in dashboard.questAudienceOptions"
                    :key="option.value"
                    class="dashboard-create-job__chip dashboard-create-job__chip--wide"
                    :class="{ 'dashboard-create-job__chip--active': dashboard.questAudience === option.value }"
                    type="button"
                    @click="toggleQuestAudience(option.value)"
                  >
                    {{ option.label }}
                  </button>
                </div>
              </label>

              <label v-if="dashboard.questAudience === 'CIRCLES'" class="field dashboard-edit-field dashboard-create-job__compact-field">
                <span class="label">Circles</span>
                <div v-if="dashboard.circles.length" class="dashboard-create-job__chip-group dashboard-create-job__chip-group--stack">
                  <button
                    v-for="circle in dashboard.circles"
                    :key="circle.id"
                    class="dashboard-create-job__chip dashboard-create-job__chip--wide"
                    :class="{ 'dashboard-create-job__chip--active': dashboard.questSelectedCircleIds.includes(circle.id) }"
                    type="button"
                    @click="toggleQuestCircle(circle.id)"
                  >
                    {{ circle.name }}
                  </button>
                </div>
                <div v-else class="muted">Create a circle first.</div>
              </label>

              <label v-if="dashboard.adminModeEnabled" class="field">
                <span class="label">Creator</span>
                <select v-model="dashboard.questCreatorId" class="input">
                  <option v-for="user in dashboard.appUsers" :key="user.id" :value="String(user.id)">
                    {{ user.username }} ({{ user.email }})
                  </option>
                </select>
              </label>
            </div>
          </div>

          <template #actions>
            <button class="button button--action button--flat-primary" type="submit">Publish job</button>
          </template>
        </DashboardEditSheet>
      </form>
    </article>
  </section>
</template>
