<script setup lang="ts">
// Public section navigation is keyboard-operable native button navigation.
defineProps<{ active: "overview" | "services" | "reviews" }>()
const emit = defineEmits<{ select: [section: "overview" | "services" | "reviews"] }>()
const tabs = [
  { id: "overview", label: "Overview" },
  { id: "services", label: "Services & prices" },
  { id: "reviews", label: "Reviews" }
] as const
</script>

<template>
  <nav class="section-tabs" aria-label="Business information">
    <button v-for="tab in tabs" :key="tab.id" type="button" :class="{ active: active === tab.id }" :aria-current="active === tab.id ? 'page' : undefined" @click="emit('select', tab.id)">{{ tab.label }}</button>
  </nav>
</template>

<style scoped>
.section-tabs { display: flex; gap: var(--space-1); overflow-x: auto; border-bottom: 1px solid var(--border-subtle); }
button { border: 0; border-bottom: 2px solid transparent; background: transparent; color: var(--text-muted); cursor: pointer; font: inherit; font-weight: var(--text-weight-semibold); padding: var(--space-2) var(--space-1); white-space: nowrap; }
button.active { border-bottom-color: var(--accent); color: var(--text); }
</style>
