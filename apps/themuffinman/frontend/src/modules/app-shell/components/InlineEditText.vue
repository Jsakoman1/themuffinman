<script setup lang="ts">
import {ref, watch} from "vue"
import AppIconButton from "./AppIconButton.vue"
import {canBeginInlineEdit} from "../composables/useObjectActions.ts"

const props = withDefaults(defineProps<{modelValue: string; label: string; multiline?: boolean; placeholder?: string; disabled?: boolean; saving?: boolean; serverAllowed?: boolean}>(), {multiline: false, placeholder: "Add text", saving: false})
const emit = defineEmits<{save: [value: string]; cancel: []}>()
const editing = ref(false)
const draft = ref(props.modelValue)
watch(() => props.modelValue, value => { if (!editing.value) draft.value = value })
const begin = () => { if (!canBeginInlineEdit({serverAllowed: props.serverAllowed !== false, disabled: props.disabled, saving: props.saving})) { return }; draft.value = props.modelValue; editing.value = true }
const save = () => { if (!props.saving) emit("save", draft.value.trim()) }
const cancel = () => { draft.value = props.modelValue; editing.value = false; emit("cancel") }
const saveMultiline = (event: KeyboardEvent) => { if ((event.metaKey || event.ctrlKey) && event.key === "Enter") { event.preventDefault(); save() } }
</script>

<template>
  <div class="inline-edit-text">
    <template v-if="editing">
      <textarea v-if="multiline" v-model="draft" :placeholder="placeholder" :aria-label="label" rows="4" :disabled="saving" @keydown="saveMultiline" @keydown.esc="cancel" />
      <input v-else v-model="draft" :placeholder="placeholder" :aria-label="label" :disabled="saving" @keydown.enter.prevent="save" @keydown.esc="cancel">
      <div class="inline-edit-text__actions"><button type="button" class="inline-edit-text__save" :disabled="saving" @click="save">{{ saving ? 'Saving…' : 'Save' }}</button><button type="button" :disabled="saving" @click="cancel">Cancel</button></div>
    </template>
    <template v-else>
      <span class="inline-edit-text__value">{{ modelValue || placeholder }}</span>
      <AppIconButton :label="`Edit ${label}`" :disabled="disabled || serverAllowed === false" @click="begin"><span aria-hidden="true">✎</span></AppIconButton>
    </template>
  </div>
</template>

<style scoped>
.inline-edit-text { display: flex; align-items: flex-start; gap: var(--space-2); min-width: 0; }.inline-edit-text__value { flex: 1; min-height: var(--control-height-default); padding: var(--space-1) 0; color: var(--text-muted); white-space: pre-wrap; }.inline-edit-text textarea, .inline-edit-text input { flex: 1; width: 100%; border: 1px solid var(--control-border); border-radius: var(--radius-control); padding: var(--space-2); background: var(--control-bg); color: var(--text); font: inherit; }.inline-edit-text textarea { resize: vertical; }.inline-edit-text__actions { display: flex; gap: var(--space-1); align-items: center; }.inline-edit-text__actions button { min-height: var(--control-height-default); border: 1px solid var(--control-border); border-radius: var(--radius-control); padding: var(--space-1) var(--space-2); background: transparent; color: var(--text-muted); font: inherit; cursor: pointer; }.inline-edit-text__actions .inline-edit-text__save { border-color: var(--accent); background: var(--accent); color: var(--canvas); }.inline-edit-text__actions button:disabled { cursor: not-allowed; opacity: .55; }
</style>
