<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import {type ModuleTab} from "../moduleTabRegistry"

const props = withDefaults(defineProps<{tabs: ModuleTab[]; activeId?: string}>(), {activeId: ""})
const route = useRoute()
const activeTabId = computed(() => props.activeId || props.tabs.find((tab) => route.path === tab.route)?.id || props.tabs[0]?.id)
</script>

<template>
  <nav class="module-tabs" aria-label="Module sections" data-navigation-model="standard-tabs" data-accessibility-model="named-current-tab">
    <RouterLink v-for="tab in tabs" :key="tab.id" class="module-tabs__tab ui-focusable" :class="{ 'module-tabs__tab--active': activeTabId === tab.id }" :to="tab.route" :aria-current="activeTabId === tab.id ? 'page' : undefined">
      <span>{{ tab.label }}</span><span v-if="tab.badge" class="module-tabs__badge" :aria-label="tab.badgeLabel ?? `${tab.badge} items need attention`">{{ tab.badge }}</span>
    </RouterLink>
  </nav>
</template>

<style scoped>
.module-tabs{display:flex;gap:var(--space-1);align-items:center;overflow-x:auto;padding:var(--space-1);border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface-subtle);scrollbar-width:none}.module-tabs::-webkit-scrollbar{display:none}.module-tabs__tab{flex:0 0 auto;min-height:var(--control-height-default);display:inline-flex;align-items:center;gap:var(--space-1);padding:var(--space-1) var(--space-3);border-radius:var(--radius-control);color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);text-decoration:none;white-space:nowrap}.module-tabs__tab:hover{color:var(--text);background:var(--surface-raised)}.module-tabs__tab--active{color:var(--text);background:var(--surface-base);box-shadow:var(--shadow-control)}.module-tabs__badge{display:grid;place-items:center;min-width:1.25rem;height:1.25rem;padding:0 .2rem;border-radius:999px;background:var(--accent);color:var(--canvas);font-size:var(--text-size-label);line-height:1}
</style>
