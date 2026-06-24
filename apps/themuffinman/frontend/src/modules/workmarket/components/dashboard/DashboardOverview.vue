<script setup lang="ts">
import DashboardWorkPlanner from "./DashboardWorkPlanner.vue"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import {createDashboardOverviewState} from "../../composables/dashboard/createDashboardOverviewState.ts"

const props = defineProps<{
  dashboard: QuestDashboard
}>()
const {postedBuckets, workBuckets, openQuest} = createDashboardOverviewState(props.dashboard)
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
                @click="openQuest(item.navigation)"
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
                @click="openQuest(item.navigation)"
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
