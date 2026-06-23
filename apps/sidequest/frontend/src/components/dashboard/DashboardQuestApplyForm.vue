<script setup lang="ts">
import type {Quest} from "../../api/sidequestApi.ts"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import RichTextEditor from "../editor/RichTextEditor.vue"
import DashboardEditSheet from "./DashboardEditSheet.vue"

defineProps<{
  dashboard: QuestDashboard
  quest: Quest
  canSubmit: boolean
}>()
</script>

<template>
  <form class="stack calendar-application-form" autocomplete="off" @submit.prevent="dashboard.applyForQuest(quest.id)">
    <DashboardEditSheet minimal>
      <div class="dashboard-edit-form dashboard-edit-form--dialog dashboard-edit-form--application">
        <label class="field dashboard-edit-field">
          <span class="label">Message</span>
          <RichTextEditor v-model="dashboard.applicationMessages[quest.id]" autocomplete="off" placeholder="" toolbar-label="Message tools" />
        </label>

        <label class="field dashboard-edit-field">
          <div class="field__header">
            <span class="label">Proposed price</span>
            <button class="button button--ghost calendar-application-form__quickfill" type="button" @click="dashboard.proposedPrices[quest.id] = String(quest.awardAmount ?? '')">
              Use suggested
            </button>
          </div>
          <div class="dashboard-edit-amount">
            <span class="dashboard-edit-amount__symbol" aria-hidden="true">$</span>
            <input
              v-model="dashboard.proposedPrices[quest.id]"
              class="input dashboard-edit-amount__input"
              inputmode="decimal"
              autocomplete="off"
              :placeholder="String(quest.awardAmount ?? '')"
            />
          </div>
        </label>
      </div>

      <template #actions>
        <button class="button button--action" type="submit" :disabled="!canSubmit">Apply</button>
      </template>
    </DashboardEditSheet>
  </form>
</template>
