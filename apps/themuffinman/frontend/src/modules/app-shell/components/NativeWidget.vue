<script setup lang="ts">
import {RouterLink} from "vue-router"
withDefaults(defineProps<{label: string; value: string; detail?: string; to?: string; tone?: "plain" | "accent"}>(), {tone: "plain"})
</script>
<template><component :is="to ? RouterLink : 'article'" :to="to" class="native-widget" :class="`native-widget--${tone}`"><span>{{ label }}</span><strong>{{ value }}</strong><small v-if="detail">{{ detail }}</small></component></template>
<style scoped>.native-widget{display:grid;gap:.25rem;min-width:0;padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base);color:var(--text);text-decoration:none}.native-widget:hover{background:var(--surface-hover)}.native-widget--accent{border-color:color-mix(in srgb,var(--accent) 30%,var(--border-subtle));background:var(--accent-muted)}.native-widget span,.native-widget small{color:var(--text-muted);font-size:var(--text-size-meta)}.native-widget strong{font-size:var(--text-size-title);letter-spacing:var(--tracking-tight);overflow:hidden;text-overflow:ellipsis;white-space:nowrap}</style>
