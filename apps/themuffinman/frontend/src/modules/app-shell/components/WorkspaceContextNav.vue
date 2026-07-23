<script setup lang="ts">
import {computed} from "vue"
import {RouterLink, useRoute} from "vue-router"
import type {WorkspaceNavigationModule} from "../../../contracts/index.ts"

const props = defineProps<{modules: WorkspaceNavigationModule[]; activeModuleId: string | null}>()
const route = useRoute()
const activeModule = computed(() => props.modules.find(module => module.id === props.activeModuleId && module.visible))
const activeChildren = computed(() => activeModule.value?.children.filter(item => item.visible).sort((a, b) => a.order - b.order) ?? [])
const isActive = (path: string) => route.path === path || route.path.startsWith(`${path}/`)
</script>

<template>
  <nav v-if="activeModule" class="workspace-context-nav" :aria-label="`${activeModule.label} section navigation`">
    <RouterLink :to="activeModule.route" class="workspace-context-nav__title" :class="{'workspace-context-nav__title--active': route.path === activeModule.route}">{{ activeModule.label }}</RouterLink>
    <template v-if="activeChildren.length">
      <span class="workspace-context-nav__divider" aria-hidden="true">/</span>
      <RouterLink v-for="child in activeChildren" :key="child.id" :to="child.route" class="workspace-context-nav__link" :class="{'workspace-context-nav__link--active': isActive(child.route)}" :aria-current="isActive(child.route) ? 'page' : undefined">{{ child.label }}</RouterLink>
    </template>
  </nav>
</template>

<style scoped>
.workspace-context-nav { display:flex; align-items:center; gap:var(--space-2); min-height:2.5rem; overflow:auto; border-bottom:1px solid var(--border-subtle); padding:0 var(--workspace-content-gutter); background:var(--surface-base); white-space:nowrap; }
.workspace-context-nav__title, .workspace-context-nav__link { display:inline-flex; align-items:center; min-height:2.5rem; color:var(--text-muted); font-size:var(--text-size-meta); font-weight:var(--text-weight-semibold); }
.workspace-context-nav__title { color:var(--text); }
.workspace-context-nav__title--active, .workspace-context-nav__link--active { color:var(--accent); }
.workspace-context-nav__link { border-bottom:2px solid transparent; }
.workspace-context-nav__link--active { border-bottom-color:var(--accent); }
.workspace-context-nav__divider { color:var(--text-soft); }
</style>
