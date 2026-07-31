<script setup lang="ts">
import type {BusinessPublicAvailabilityCalendarDTO} from "../../../contracts/index.ts"
import BusinessAvailabilityCalendar from "./BusinessAvailabilityCalendar.vue"
type CalendarView = "MONTH" | "WEEK" | "DAY"
defineProps<{calendar: BusinessPublicAvailabilityCalendarDTO | null; selectedDate: string; view: CalendarView}>()
const emit = defineEmits<{select: [date: string]; previous: []; next: []; changeView: [view: CalendarView]}>()
</script>
<template><BusinessAvailabilityCalendar :calendar="calendar" :selected-date="selectedDate" :view="view" @select="emit('select', $event)" @previous="emit('previous')" @next="emit('next')" @change-view="emit('changeView', $event)" /></template>
