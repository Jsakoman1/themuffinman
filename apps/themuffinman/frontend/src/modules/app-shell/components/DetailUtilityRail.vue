<script setup lang="ts">
import {computed, ref} from "vue"
import AppButton from "./AppButton.vue"
import {invokeObjectAction, splitObjectActions, type ObjectActionDescriptor} from "../composables/useObjectActions.ts"

const props = withDefaults(defineProps<{
  actions?: ObjectActionDescriptor[]
  title?: string
}>(), {
  actions: () => [],
  title: "Actions",
})

const runningActionId = ref<string | null>(null)
const actionGroups = computed(() => splitObjectActions(props.actions))
const run = async (action: ObjectActionDescriptor) => {
  runningActionId.value = action.id
  try { await invokeObjectAction(action) } finally { runningActionId.value = null }
}
</script>

<template>
  <aside class="detail-utility-rail" :aria-label="title">
    <header><h2>{{ title }}</h2></header>
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
  </aside>
</template>

<style scoped>
.detail-utility-rail { display: grid; align-content: start; min-width: 0; }.detail-utility-rail header { padding: var(--space-3); border-bottom: 1px solid var(--border-subtle); }.detail-utility-rail h2 { margin: 0; color: var(--text-muted); font-size: var(--text-size-label); font-weight: var(--text-weight-semibold); letter-spacing: var(--tracking-label); text-transform: uppercase; }.detail-utility-rail__group { display: grid; gap: var(--space-2); padding: var(--space-3); border-bottom: 1px solid var(--border-subtle); }.detail-utility-rail__group :deep(.app-button) { justify-content: flex-start; width: 100%; }.detail-utility-rail__group--destructive { background: var(--danger-muted); }
</style>
