<script setup lang="ts">
import {computed} from "vue"
import DashboardQuestSummaryRow from "./DashboardQuestSummaryRow.vue"
import DashboardSectionHeader from "./DashboardSectionHeader.vue"
import type {Quest} from "../../api/sidequestApi.ts"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"

const props = withDefaults(defineProps<{
  dashboard: QuestDashboard
  title?: string
  subtitle?: string
  emptyMessage?: string
  quests?: Quest[]
  showHeader?: boolean
  boxed?: boolean
}>(), {
  title: "Your work",
  subtitle: "Manage your quests.",
  emptyMessage: "No quests here yet.",
  quests: undefined,
  showHeader: true,
  boxed: true,
})

const quests = computed(() => props.quests ?? props.dashboard.filteredMyQuests)
</script>

<template>
  <section class="stack">
    <div :class="{ card: props.boxed }">
      <DashboardSectionHeader v-if="props.showHeader" :title="props.title" :subtitle="props.subtitle" />

      <div v-if="!quests.length" class="empty-state">
        {{ props.emptyMessage }}
      </div>

      <div v-else class="quest-list mt-4">
        <button
          v-for="quest in quests"
          :key="quest.id"
          type="button"
          class="compact-disclosure compact-disclosure--tight compact-disclosure--launch"
          :class="[dashboard.statusSurfaceClass(quest.status), { 'ui-pulse': dashboard.successPulseTarget === `quest-${quest.id}` }]"
          @click="dashboard.openQuestDialog(quest.id)"
        >
          <DashboardQuestSummaryRow
            primary-label="Amount"
            :primary-value="quest.awardAmount"
            primary-icon="$"
            money-tone="expense"
            secondary-label="Term"
            :secondary-value="dashboard.formatQuestTermLabel(quest)"
            :title="quest.title"
            :description="quest.description"
          />
        </button>
      </div>
    </div>
  </section>
</template>
