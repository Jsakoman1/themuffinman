<script setup lang="ts">
import {ref, watch} from "vue"
import AppIconButton from "./AppIconButton.vue"

const props = withDefaults(defineProps<{modelValue: string; label: string; multiline?: boolean; placeholder?: string; disabled?: boolean}>(), {multiline: false, placeholder: "Add text"})
const emit = defineEmits<{save: [value: string]; cancel: []}>()
const editing = ref(false)
const draft = ref(props.modelValue)
watch(() => props.modelValue, value => { if (!editing.value) draft.value = value })
const begin = () => { draft.value = props.modelValue; editing.value = true }
const save = () => { emit("save", draft.value.trim()); editing.value = false }
const cancel = () => { draft.value = props.modelValue; editing.value = false; emit("cancel") }
</script>

<template>
  <div class="inline-edit-text">
    <template v-if="editing">
      <textarea v-if="multiline" v-model="draft" :placeholder="placeholder" :aria-label="label" rows="4" @keydown.meta.enter.prevent="save" />
      <input v-else v-model="draft" :placeholder="placeholder" :aria-label="label" @keydown.enter.prevent="save" @keydown.esc="cancel">
      <div class="inline-edit-text__actions"><button type="button" class="inline-edit-text__save" @click="save">Save</button><button type="button" @click="cancel">Cancel</button></div>
    </template>
    <template v-else>
      <span class="inline-edit-text__value">{{ modelValue || placeholder }}</span>
      <AppIconButton :label="`Edit ${label}`" :disabled="disabled" @click="begin"><span aria-hidden="true">✎</span></AppIconButton>
    </template>
  </div>
</template>

<style scoped>
.inline-edit-text{display:flex;align-items:flex-start;gap:.5rem;min-width:0}.inline-edit-text__value{flex:1;white-space:pre-wrap;min-height:2.25rem;padding:.45rem 0;color:var(--text-muted)}.inline-edit-text textarea,.inline-edit-text input{flex:1;width:100%;border:1px solid var(--border-strong);border-radius:var(--radius-control);padding:.65rem;background:var(--surface);font:inherit}.inline-edit-text textarea{resize:vertical}.inline-edit-text__actions{display:flex;gap:.35rem;align-items:center}.inline-edit-text__actions button{border:1px solid var(--border-subtle);border-radius:999px;padding:.45rem .7rem;background:transparent;font:inherit;cursor:pointer}.inline-edit-text__actions .inline-edit-text__save{background:var(--text);color:var(--surface)}
</style>
