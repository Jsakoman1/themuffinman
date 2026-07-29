<script setup lang="ts">
export type BusinessSettingsSection = "public" | "hours" | "booking" | "advanced"

defineProps<{active: BusinessSettingsSection}>()
defineEmits<{select: [section: BusinessSettingsSection]}>()

const sections: Array<{id: BusinessSettingsSection; label: string; description: string}> = [
  {id: "public", label: "Public page", description: "What customers see"},
  {id: "hours", label: "Working hours", description: "When people can book"},
  {id: "booking", label: "Booking rules", description: "How requests are handled"},
  {id: "advanced", label: "Advanced", description: "Links, location, and archive"}
]
</script>

<template>
  <nav class="business-settings-nav" aria-label="Business settings sections">
    <button v-for="section in sections" :key="section.id" type="button" :class="{active: active === section.id}" :aria-current="active === section.id ? 'page' : undefined" @click="$emit('select', section.id)">
      <strong>{{ section.label }}</strong><span>{{ section.description }}</span>
    </button>
  </nav>
</template>

<style scoped>
.business-settings-nav{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:var(--space-2)}.business-settings-nav button{display:grid;gap:.2rem;min-width:0;padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface-base);color:var(--text);text-align:left;font:inherit;cursor:pointer}.business-settings-nav button:hover{background:var(--surface-hover)}.business-settings-nav button.active{border-color:var(--accent);background:color-mix(in srgb,var(--accent) 12%,var(--surface-base))}.business-settings-nav strong{font-size:var(--text-size-body)}.business-settings-nav span{color:var(--text-muted);font-size:var(--text-size-meta);line-height:1.35}@media(max-width:760px){.business-settings-nav{grid-template-columns:1fr 1fr}}@media(max-width:440px){.business-settings-nav{grid-template-columns:1fr}}
</style>
