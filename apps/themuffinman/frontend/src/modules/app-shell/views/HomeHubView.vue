<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {RouterLink, useRoute} from "vue-router"
import {useShellSurfaceData} from "../shellSurfaceData.ts"
import AppLoadingState from "../components/AppLoadingState.vue"
import AppStatus from "../components/AppStatus.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import HomeCalendarPreview from "../components/HomeCalendarPreview.vue"
import HomeActionTile from "../components/HomeActionTile.vue"
import HomeFollowUpCard from "../components/HomeFollowUpCard.vue"
import type {AppPurposeIconName} from "../components/AppPurposeIcon.vue"
import {userShellApi, type ActivityItem} from "../api/userShellApi.ts"

const route = useRoute()
const {model, isLoading, error, reload} = useShellSurfaceData("home", route)
const launcher = computed(() => model.value.home)
const homeSections = computed(() => model.value.sections)
const recentActivity = ref<ActivityItem[]>([])
const nextResumeItem = computed(() => recentActivity.value.find(item => item.resumable && item.route) ?? null)
const followUp = computed(() => {
  const item = nextResumeItem.value
  if (!item) return {
    eyebrow: "You're all caught up",
    title: "Nothing needs your attention right now",
    description: "Browse local SideJobs whenever you are ready to help or ask for help.",
    to: "/work/find",
    actionLabel: "Find help"
  }
  return {
    eyebrow: "Pick up where you left off",
    title: item.title === "Continue Vision" ? "Continue your guided task" : item.title,
    description: item.summary || "Open this item to see the next step.",
    to: item.route,
    actionLabel: item.primaryActionLabel || "Continue"
  }
})
onMounted(async () => { try { recentActivity.value = await userShellApi.getRecentActivity() } catch { recentActivity.value = [] } })
const iconFor = (key: string): AppPurposeIconName => ({work: "work", things: "things", business: "business", rides: "rides"}[key] as AppPurposeIconName | undefined) ?? "home"
</script>

<template>
  <section class="home-hub" aria-live="polite" :aria-busy="isLoading || undefined" data-native-frame="calm-content-canvas" data-home-model="personal-day-next-action">
    <AppLoadingState v-if="isLoading" label="Loading your active workspace" :rows="5" />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="reload" />
    <div v-else class="home-hub__content">
      <section class="home-hub__welcome" aria-label="Your home"><p>YOUR DAY</p><h1>Hey, {{ launcher?.greetingName || 'there' }}!</h1><span>Choose one useful thing to do, then get on with your day.</span></section>
      <nav class="home-hub__launcher" aria-label="Choose what you want to do">
        <HomeActionTile v-for="action in launcher?.launcherActions ?? []" :key="action.id" :label="action.label" :description="action.description" :icon="iconFor(action.iconKey)" :to="action.route" :tone="action.colourRole" />
      </nav>
      <section class="home-hub__follow-up" aria-label="Continue where you left off">
        <HomeFollowUpCard v-bind="followUp" icon="people" tone="attention" />
      </section>
      <HomeCalendarPreview class="home-hub__follow-up" />
      <div class="home-hub__sections">
        <section v-for="section in homeSections" :key="section.title" class="home-hub__section" :aria-labelledby="`home-${section.title.toLowerCase()}-title`">
          <header class="home-hub__section-header">
            <div>
              <h2 :id="`home-${section.title.toLowerCase()}-title`">{{ section.title }}</h2>
            </div>
            <nav v-if="section.actions?.length" class="home-hub__actions" :aria-label="`${section.title} actions`">
              <RouterLink v-for="action in section.actions" :key="action.id" :to="action.route" class="home-hub__action">{{ action.label }}</RouterLink>
            </nav>
          </header>
          <template v-if="section.groups?.length">
            <div v-for="group in section.groups" :key="group.title" class="home-hub__group">
              <h3>{{ group.title }}</h3>
              <div v-if="group.rows.length" class="home-hub__rows"><SurfaceRow v-for="row in group.rows" :key="row.id" :row="row" density="compact" /></div>
              <p v-else class="home-hub__group-empty">Nothing new</p>
            </div>
          </template>
          <div v-else-if="section.rows.length" class="home-hub__rows"><SurfaceRow v-for="row in section.rows" :key="row.id" :row="row" density="compact" /></div>
          <AppStatus v-if="!section.groups?.length && !section.rows.length" :message="section.emptyState" />
        </section>
      </div>
    </div>
  </section>
</template>

<style scoped>
.home-hub{display:grid;min-width:0;max-width:72rem}.home-hub__content{display:grid;gap:var(--space-5)}.home-hub__welcome{display:grid;gap:var(--space-1);padding:var(--space-3) 0 var(--space-2)}.home-hub__welcome p{margin:0;color:var(--orientation-ink);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label)}.home-hub__welcome h1{margin:0;font-size:clamp(2rem,4vw,3.25rem);letter-spacing:var(--tracking-display);line-height:1}.home-hub__welcome span{max-width:32rem;color:var(--text-muted);line-height:var(--text-leading-body)}.home-hub__launcher{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:var(--space-3)}.home-hub__follow-up{min-width:0}.home-hub__sections{display:none}.home-hub__rows :deep(.surface-row:last-child){border-bottom:0}@media(min-width:900px){.home-hub__launcher{grid-template-columns:repeat(4,minmax(0,1fr))}.home-hub__content{grid-template-columns:repeat(2,minmax(0,1fr));align-items:start}.home-hub__welcome,.home-hub__launcher{grid-column:1 / -1}}
@media(max-width:760px){.home-hub__sections{grid-template-columns:1fr}.home-hub__section-header{align-items:flex-start;flex-direction:column}.home-hub__actions{justify-content:flex-start}}
</style>
