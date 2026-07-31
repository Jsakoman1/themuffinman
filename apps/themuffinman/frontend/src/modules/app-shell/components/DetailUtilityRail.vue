<script setup lang="ts">
import {computed, ref} from "vue"
import AppButton from "./AppButton.vue"
import {invokeObjectAction, splitObjectActions, type ObjectActionDescriptor} from "../composables/useObjectActions.ts"

const props = withDefaults(defineProps<{
  actions?: ObjectActionDescriptor[]
  title?: string
}>(), {
  actions: () => [],
  title: "",
})

const runningActionId = ref<string | null>(null)
const actionGroups = computed(() => splitObjectActions(props.actions))
const run = async (action: ObjectActionDescriptor) => {
  runningActionId.value = action.id
  try { await invokeObjectAction(action) } finally { runningActionId.value = null }
}
</script>

<template>
  <section class="detail-action-group" data-archetype="detail-actions" :aria-label="title || 'Object actions'">
    <header v-if="title"><h2>{{ title }}</h2></header>
    <p class="detail-utility-rail__trust">Actions, visibility, availability, pricing, expiry, and undo rules come from the backend. A disabled action has a server-side reason; refresh after another participant changes the object.</p>
    <div v-if="actionGroups.primary.length" class="detail-utility-rail__group">
      <AppButton v-for="action in actionGroups.primary" :key="action.id" tone="primary" :loading="runningActionId === action.id" :disabled="action.disabled" @click="run(action)">{{ action.label }}</AppButton>
    </div>
    <div v-if="actionGroups.secondary.length" class="detail-utility-rail__group">
      <AppButton v-for="action in actionGroups.secondary" :key="action.id" :loading="runningActionId === action.id" :disabled="action.disabled" @click="run(action)">{{ action.label }}</AppButton>
    </div>
    <div v-if="actionGroups.destructive.length" class="detail-utility-rail__group detail-utility-rail__group--destructive">
      <AppButton v-for="action in actionGroups.destructive" :key="action.id" tone="danger" :loading="runningActionId === action.id" :disabled="action.disabled" @click="run(action)">{{ action.label }}</AppButton>
    </div>
    <slot />
  </section>
</template>

<style scoped>
.detail-action-group { display:grid;gap:var(--space-2);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-muted); }.detail-action-group header { display:grid;gap:var(--space-1); }.detail-action-group h2 { margin:0;color:var(--text-muted);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase; }.detail-utility-rail__group { display:flex;flex-wrap:wrap;gap:var(--space-2); }.detail-utility-rail__group--destructive { background:var(--danger-muted);padding:var(--space-2);border-radius:var(--radius-control); }
.detail-utility-rail__trust { margin:0;color:var(--text-soft);font-size:var(--text-size-meta);line-height:1.45; }
</style>
