<script setup lang="ts">
import {computed} from "vue"
import {RouterLink, useRoute} from "vue-router"
import type {WorkspaceNavigationModule} from "../../../contracts/index.ts"

const props = defineProps<{modules: WorkspaceNavigationModule[]; activeModuleId: string | null}>()
const route = useRoute()
const visibleModules = computed(() => props.modules.filter((module) => module.visible))
const childrenFor = (module: WorkspaceNavigationModule) => module.children.filter((child) => child.visible).sort((a, b) => a.order - b.order)
const isChildActive = (path: string) => route.path === path || route.path.startsWith(`${path}/`)
const iconFor = (key: string) => ({home: "⌂", work: "▤", chat: "◌", calendar: "□", business: "◇", circles: "◎", things: "▣", rides: "↗"}[key] ?? "•")
</script>

<template>
  <nav class="workspace-module-rail" aria-label="Modules" :aria-busy="props.modules.length === 0 ? 'true' : 'false'">
    <RouterLink
      v-for="module in visibleModules"
      :key="module.id"
      :to="module.route"
      class="workspace-module-rail__module"
      :class="{'workspace-module-rail__module--active': activeModuleId === module.id}"
      :aria-current="activeModuleId === module.id ? 'page' : undefined"
    >
      <span class="workspace-module-rail__icon" aria-hidden="true">{{ iconFor(module.iconKey) }}</span>
      <span class="workspace-module-rail__label">{{ module.label }}</span>
      <span v-if="module.unreadCount > 0" class="workspace-module-rail__badge" :aria-label="`${module.unreadCount} unread`">{{ module.unreadCount }}</span>
    </RouterLink>
    <div v-for="module in visibleModules.filter((item) => item.id === props.activeModuleId && childrenFor(item).length > 0)" :key="`${module.id}-children`" class="workspace-module-rail__children" :data-module="module.id">
      <RouterLink
        v-for="child in childrenFor(module)"
        :key="child.id"
        :to="child.route"
        class="workspace-module-rail__child"
        :class="{'workspace-module-rail__child--active': isChildActive(child.route)}"
        :aria-current="isChildActive(child.route) ? 'page' : undefined"
      >
        <span>{{ child.label }}</span>
        <span v-if="child.unreadCount > 0" class="workspace-module-rail__badge">{{ child.unreadCount }}</span>
      </RouterLink>
    </div>
    <p v-if="visibleModules.length === 0" class="workspace-module-rail__status">Navigation is loading or unavailable.</p>
  </nav>
</template>

<style scoped>
.workspace-module-rail { display: grid; gap: 0.2rem; }
.workspace-module-rail__module,
.workspace-module-rail__child { display: flex; align-items: center; gap: 0.6rem; min-width: 0; border-radius: var(--radius-control); color: var(--text-muted); }
.workspace-module-rail__module { min-height: 2.25rem; padding: 0.45rem 0.6rem; font-weight: 600; }
.workspace-module-rail__module:hover,
.workspace-module-rail__module--active,
.workspace-module-rail__child:hover,
.workspace-module-rail__child--active { background: var(--surface-selected); color: var(--text); }
.workspace-module-rail__icon { width: 1.2rem; text-align: center; color: var(--text-soft); }
.workspace-module-rail__label { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.workspace-module-rail__children { display: grid; gap: 0.15rem; margin: 0.1rem 0 0.35rem 1.8rem; border-left: 1px solid var(--border-subtle); padding-left: 0.55rem; }
.workspace-module-rail__child { min-height: 1.9rem; padding: 0.3rem 0.45rem; font-size: 0.8125rem; }
.workspace-module-rail__badge { margin-left: auto; min-width: 1.2rem; border-radius: 999px; padding: 0.08rem 0.32rem; background: var(--accent-muted); color: var(--text); font-size: 0.75rem; text-align: center; }
.workspace-module-rail__status { margin: 0.5rem 0; color: var(--text-soft); font-size: var(--text-size-meta); }
</style>
