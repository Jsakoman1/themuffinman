<script setup lang="ts">
export type AppPurposeIconName = "home" | "work" | "things" | "business" | "services" | "rides" | "people" | "explore" | "activity" | "calendar" | "chat" | "profile"

const props = withDefaults(defineProps<{
  name: AppPurposeIconName
  label?: string
  size?: number | string
}>(), {size: 20})
</script>

<template>
  <svg
    class="app-purpose-icon"
    :width="props.size"
    :height="props.size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.8"
    stroke-linecap="round"
    stroke-linejoin="round"
    :data-purpose-icon="props.name"
    :role="props.label ? 'img' : undefined"
    :aria-label="props.label"
    :aria-hidden="props.label ? undefined : 'true'"
  >
    <template v-if="props.name === 'home'">
      <path d="m3.5 10 8.5-7 8.5 7v9.5a1.5 1.5 0 0 1-1.5 1.5h-14A1.5 1.5 0 0 1 3.5 19.5Z" /><path d="M9 21v-6h6v6" />
    </template>
    <template v-else-if="props.name === 'work'">
      <rect x="3" y="7" width="18" height="13" rx="2" /><path d="M8.5 7V5.5A1.5 1.5 0 0 1 10 4h4a1.5 1.5 0 0 1 1.5 1.5V7M3 12h18M10 12v2h4v-2" />
    </template>
    <template v-else-if="props.name === 'things'">
      <path d="M5 8.5 12 4l7 4.5v7L12 20l-7-4.5Z" /><path d="m5 8.5 7 4 7-4M12 12.5V20" />
    </template>
    <template v-else-if="props.name === 'business'">
      <path d="M4 10h16v10H4zM3 7h18l-1.5-3h-15Z" /><path d="M8 20v-6h4v6M15.5 13.5h1" />
    </template>
    <template v-else-if="props.name === 'services'">
      <circle cx="12" cy="12" r="7" /><path d="m12 8 1.1 2.9L16 12l-2.9 1.1L12 16l-1.1-2.9L8 12l2.9-1.1Z" />
    </template>
    <template v-else-if="props.name === 'rides'">
      <path d="M5 15 6.5 8A2 2 0 0 1 8.45 6.4h7.1A2 2 0 0 1 17.5 8l1.5 7" /><path d="M4 15h16v3H4zM7 18v2M17 18v2M7.5 11h.01M16.5 11h.01" />
    </template>
    <template v-else-if="props.name === 'people' || props.name === 'profile'">
      <circle cx="12" cy="8" r="3" /><path d="M5.5 20a6.5 6.5 0 0 1 13 0M4 10.5a2.5 2.5 0 0 0 0 4M20 10.5a2.5 2.5 0 0 1 0 4" />
    </template>
    <template v-else-if="props.name === 'explore'">
      <circle cx="11" cy="11" r="6" /><path d="m16 16 4 4M11 8v6M8 11h6" />
    </template>
    <template v-else-if="props.name === 'activity'">
      <path d="M3 12h4l2-5 4 10 2-5h6" />
    </template>
    <template v-else-if="props.name === 'calendar'">
      <rect x="4" y="5.5" width="16" height="14" rx="2" /><path d="M8 3.5v4M16 3.5v4M4 10h16M8 14h.01M12 14h.01M16 14h.01" />
    </template>
    <template v-else>
      <path d="M5 18.5 3.8 21l3.1-1.2A8 8 0 1 0 5 18.5Z" /><path d="M8.5 12h.01M12 12h.01M15.5 12h.01" />
    </template>
  </svg>
</template>

<style scoped>
.app-purpose-icon { display: block; flex: 0 0 auto; }
</style>
