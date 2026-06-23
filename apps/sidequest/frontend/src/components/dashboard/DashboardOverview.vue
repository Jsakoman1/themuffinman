<script setup lang="ts">
import {computed} from "vue"
import DashboardWorkPlanner from "./DashboardWorkPlanner.vue"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import {formatRailDateTime} from "../../lib/dashboardGroups.ts"

const props = defineProps<{
  dashboard: QuestDashboard
}>()

type RailItem = {
  id: string
  questId: number
  title: string
  when: string
}

type RailBucket = {
  key: string
  label: string
  tone: "success" | "warning" | "accent"
  items: RailItem[]
}

const toQuestItem = (quest: {id: number; title: string; scheduledAt: string | null; endsAt: string | null}): RailItem => ({
  id: `quest-${quest.id}`,
  questId: quest.id,
  title: quest.title,
  when: formatRailDateTime(quest.scheduledAt, quest.endsAt)
})

const toApplicationItem = (application: {id: number; questId: number; questTitle: string}) => ({
  id: `application-${application.id}`,
  questId: application.questId,
  title: application.questTitle,
  when: formatRailDateTime(
    props.dashboard.questForId(application.questId)?.scheduledAt,
    props.dashboard.questForId(application.questId)?.endsAt
  )
})

const postedBuckets = computed<RailBucket[]>(() => {
  const waiting = props.dashboard.myQuests.filter((quest) => quest.status === "WAITING_CONFIRMATION")
  const assigned = props.dashboard.myQuests.filter((quest) => quest.status === "ASSIGNED")
  const inProgress = props.dashboard.myQuests.filter((quest) => quest.status === "IN_PROGRESS")

  return [
    {key: "posted-waiting", label: "Need confirmation", tone: "warning", items: waiting.map(toQuestItem)},
    {key: "posted-assigned", label: "Ready to start", tone: "success", items: assigned.map(toQuestItem)},
    {key: "posted-progress", label: "In progress", tone: "accent", items: inProgress.map(toQuestItem)}
  ]
})

const workBuckets = computed<RailBucket[]>(() => {
  const waiting = props.dashboard.activeWorkApplications.filter((application) => application.questStatus === "WAITING_CONFIRMATION")
  const assigned = props.dashboard.activeWorkApplications.filter((application) => application.questStatus === "ASSIGNED")
  const inProgress = props.dashboard.activeWorkApplications.filter((application) => application.questStatus === "IN_PROGRESS")

  return [
    {key: "work-waiting", label: "Waiting on agreement", tone: "warning", items: waiting.map(toApplicationItem)},
    {key: "work-assigned", label: "Agreed and ready", tone: "success", items: assigned.map(toApplicationItem)},
    {key: "work-progress", label: "Doing now", tone: "accent", items: inProgress.map(toApplicationItem)}
  ]
})

const openQuest = (questId: number) => {
  props.dashboard.openQuestDialog(questId)
}

</script>

<template>
  <section class="overview-grid overview-grid--tabs">
    <div class="overview-panels overview-panels--triage">
      <article class="overview-rail overview-rail--left">
        <div class="overview-rail__header">
          <div class="overview-rail__title-row">
            <div class="overview-rail__title-block">
              <span class="overview-rail__label">You posted</span>
              <strong class="overview-rail__title">Managed work</strong>
            </div>
          </div>

          <button class="overview-rail__action overview-rail__action--success" type="button" @click="props.dashboard.openCreateJobDialog()">
            New quest
          </button>
        </div>

        <div class="overview-rail__stack">
          <section v-for="bucket in postedBuckets" :key="bucket.key" class="rail-bucket">
            <div class="rail-bucket__header">
              <span :class="['rail-bucket__dot', `rail-bucket__dot--${bucket.tone}`]" />
              <span class="rail-bucket__label">{{ bucket.label }}</span>
            </div>

            <div v-if="bucket.items.length" class="rail-bucket__items">
              <button
                v-for="item in bucket.items"
                :key="item.id"
                :class="['rail-tab', `rail-tab--${bucket.tone}`]"
                type="button"
                @click="openQuest(item.questId)"
              >
                <div class="rail-tab__body">
                  <strong class="rail-tab__title">{{ item.title }}</strong>
                  <div class="rail-tab__datetime">{{ item.when }}</div>
                </div>
              </button>
            </div>

            <div v-else class="rail-empty">Empty</div>
          </section>
        </div>
      </article>

      <DashboardWorkPlanner :dashboard="dashboard" />

      <article class="overview-rail overview-rail--right">
        <div class="overview-rail__header">
          <div class="overview-rail__title-row">
            <div class="overview-rail__title-block">
              <span class="overview-rail__label">You accepted</span>
              <strong class="overview-rail__title">Doing work</strong>
            </div>
          </div>

          <button class="overview-rail__action overview-rail__action--accent" type="button" @click="props.dashboard.openFindWorkDialog()">
            Find work
          </button>
        </div>

        <div class="overview-rail__stack">
          <section v-for="bucket in workBuckets" :key="bucket.key" class="rail-bucket">
            <div class="rail-bucket__header">
              <span :class="['rail-bucket__dot', `rail-bucket__dot--${bucket.tone}`]" />
              <span class="rail-bucket__label">{{ bucket.label }}</span>
            </div>

            <div v-if="bucket.items.length" class="rail-bucket__items">
              <button
                v-for="item in bucket.items"
                :key="item.id"
                :class="['rail-tab', `rail-tab--${bucket.tone}`]"
                type="button"
                @click="openQuest(item.questId)"
              >
                <div class="rail-tab__body">
                  <strong class="rail-tab__title">{{ item.title }}</strong>
                  <div class="rail-tab__datetime">{{ item.when }}</div>
                </div>
              </button>
            </div>

            <div v-else class="rail-empty">Empty</div>
          </section>
        </div>
      </article>
    </div>
  </section>
</template>
