<script setup lang="ts">
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import RichTextEditor from "../editor/RichTextEditor.vue"
import DashboardEditSheet from "./DashboardEditSheet.vue"

defineProps<{ dashboard: QuestDashboard }>()
defineEmits<{ discard: [] }>()
</script>

<template>
  <form class="stack" @submit.prevent="dashboard.saveEditedQuest">
    <DashboardEditSheet minimal>
      <div class="dashboard-edit-form dashboard-edit-form--dialog">
        <label class="field dashboard-edit-field dashboard-edit-field--message">
          <span class="label">Award amount</span>
          <div class="dashboard-edit-amount">
            <span class="dashboard-edit-amount__symbol" aria-hidden="true">$</span>
            <input v-model="dashboard.editQuestAwardAmount" class="input dashboard-edit-amount__input" inputmode="decimal" placeholder="50" />
          </div>
        </label>
        <label class="field dashboard-edit-field dashboard-edit-field--price">
          <span class="label">Title</span>
          <input v-model="dashboard.editQuestTitle" class="input" />
        </label>
        <label class="field dashboard-edit-field">
          <span class="label">Description</span>
          <RichTextEditor v-model="dashboard.editQuestDescription" placeholder="" toolbar-label="Description tools" />
        </label>
        <label class="field dashboard-edit-field">
          <span class="label">Term</span>
          <div class="dashboard-term-mode">
            <button class="segment" :class="{ 'segment--active': dashboard.editQuestTermMode === 'flexible' }" type="button" @click="dashboard.setEditQuestTermMode('flexible')">
              Flexible
            </button>
            <button class="segment" :class="{ 'segment--active': dashboard.editQuestTermMode === 'start-only' }" type="button" @click="dashboard.setEditQuestTermMode('start-only')">
              Start
            </button>
            <button class="segment" :class="{ 'segment--active': dashboard.editQuestTermMode === 'start-end' }" type="button" @click="dashboard.setEditQuestTermMode('start-end')">
              Start + end
            </button>
          </div>
        </label>

        <label v-if="dashboard.editQuestTermMode !== 'flexible'" class="field dashboard-edit-field">
          <span class="label">Start</span>
          <input v-model="dashboard.editQuestScheduledAt" class="input" type="datetime-local" />
        </label>

        <label v-if="dashboard.editQuestTermMode === 'start-end'" class="field dashboard-edit-field">
          <span class="label">End</span>
          <input v-model="dashboard.editQuestEndsAt" class="input" type="datetime-local" />
        </label>
        <label class="field dashboard-edit-field">
          <span class="label">Who can see this</span>
          <select v-model="dashboard.editQuestAudience" class="input">
            <option v-for="option in dashboard.questAudienceOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
        </label>
        <label v-if="dashboard.editQuestAudience === 'CIRCLES'" class="field dashboard-edit-field">
          <span class="label">Circles</span>
          <div v-if="dashboard.circles.length" class="dashboard-create-job__chip-group dashboard-create-job__chip-group--stack">
            <button
              v-for="circle in dashboard.circles"
              :key="circle.id"
              class="dashboard-create-job__chip dashboard-create-job__chip--wide"
              :class="{ 'dashboard-create-job__chip--active': dashboard.editQuestSelectedCircleIds.includes(circle.id) }"
              type="button"
              @click="dashboard.editQuestSelectedCircleIds = dashboard.editQuestSelectedCircleIds.includes(circle.id)
                ? dashboard.editQuestSelectedCircleIds.filter((id) => id !== circle.id)
                : [...dashboard.editQuestSelectedCircleIds, circle.id]"
            >
              {{ circle.name }}
            </button>
          </div>
          <div v-else class="muted">Create a circle first.</div>
        </label>
        <template v-if="dashboard.isAdmin()">
          <label class="field dashboard-edit-field">
            <span class="label">Creator</span>
            <select v-model="dashboard.editQuestCreatorId" class="input">
              <option v-for="user in dashboard.appUsers" :key="user.id" :value="String(user.id)">{{ user.username }} ({{ user.email }})</option>
            </select>
          </label>
          <label class="field dashboard-edit-field">
            <span class="label">Status</span>
            <select v-model="dashboard.editQuestStatus" class="input">
              <option v-for="option in dashboard.questStatusOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
            </select>
          </label>
        </template>
      </div>
      <template #actions>
        <button class="button button--action" type="submit">Save changes</button>
        <button class="button button--ghost" type="button" @click="$emit('discard')">Discard changes</button>
      </template>
    </DashboardEditSheet>
  </form>
</template>
