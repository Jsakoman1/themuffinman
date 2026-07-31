<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import type {CalendarEvent} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "./AppStatus.vue"
import {formatInTimezone} from "../../../services/formatters.ts"
import HomeFollowUpCard from "./HomeFollowUpCard.vue"

const events = ref<CalendarEvent[]>([])
const timezone = ref("UTC")
const loading = ref(true)
const error = ref("")
const startOfWeek = (value: Date) => { const start = new Date(value); start.setHours(0, 0, 0, 0); start.setDate(start.getDate() - start.getDay()); return start }
const range = computed(() => { const from = startOfWeek(new Date()); const to = new Date(from); to.setDate(to.getDate() + 7); return {from: from.toISOString(), to: to.toISOString()} })
const visibleEvents = computed(() => events.value)
const nextEvent = computed(() => visibleEvents.value[0])
const load = async () => { loading.value = true; error.value = ""; try { const projection = await userShellApi.getCalendarProjection({...range.value, view: "week"}); events.value = projection.events; timezone.value = projection.timezone || timezone.value } catch { error.value = "Could not load your calendar." } finally { loading.value = false } }
onMounted(() => void load())
</script>

<template>
  <section class="home-calendar" aria-label="Your schedule">
    <HomeFollowUpCard v-if="!loading && !error" eyebrow="Your schedule" :title="nextEvent?.title || 'Nothing scheduled next'" :description="nextEvent ? formatInTimezone(nextEvent.startsAt, timezone, 'Unknown time') : 'Your calendar is clear for the next few days.'" to="/calendar" action-label="Open calendar" icon="activity" tone="schedule" />
    <AppStatus v-if="loading" message="Loading your next event." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" />
  </section>
</template>

<style scoped>
.home-calendar{display:grid;gap:var(--space-2)}
</style>
