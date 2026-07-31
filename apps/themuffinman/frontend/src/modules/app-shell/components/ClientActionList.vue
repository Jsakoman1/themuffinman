<script setup lang="ts">
import type {ClientActionDTO} from "../../../contracts/index.ts"
import AppButton from "./AppButton.vue"
import {confirmAction} from "../composables/useActionDialog.ts"

const props = withDefaults(defineProps<{
  actions: ClientActionDTO[]
  busy?: boolean
  align?: "start" | "end"
}>(), {busy: false, align: "end"})

const emit = defineEmits<{execute: [actionId: string]}>()

const buttonTone = (tone: ClientActionDTO["tone"]): "primary" | "secondary" | "danger" => {
  if (tone === "DANGER") return "danger"
  return tone === "PRIMARY" ? "primary" : "secondary"
}

const execute = async (action: ClientActionDTO) => {
  if (!action.enabled) return
  if (action.requiresConfirmation && !await confirmAction(
    action.confirmationMessage ?? "Are you sure you want to continue?",
    action.confirmationTitle ?? action.label
  )) return
  emit("execute", action.id)
}
</script>

<template>
  <div v-if="props.actions.length" class="client-action-list" :class="`client-action-list--${props.align}`" aria-label="Available actions" @click.stop>
    <p v-for="action in props.actions" :key="action.id" class="client-action-list__item">
      <AppButton :tone="buttonTone(action.tone)" :disabled="!action.enabled" :loading="props.busy" @click="execute(action)">{{ action.label }}</AppButton>
      <small v-if="!action.enabled && action.disabledReason">{{ action.disabledReason }}</small>
    </p>
  </div>
</template>

<style scoped>
.client-action-list{display:flex;flex-wrap:wrap;gap:var(--space-1);justify-content:end}.client-action-list--start{justify-content:start}.client-action-list__item{display:grid;gap:var(--space-1);margin:0}.client-action-list small{max-width:15rem;color:var(--text-muted);font-size:var(--text-size-meta)}@media(max-width:620px){.client-action-list{justify-content:start}}
</style>
