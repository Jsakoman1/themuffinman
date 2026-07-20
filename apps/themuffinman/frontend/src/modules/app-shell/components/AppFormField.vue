<script setup lang="ts">
import {nextTick, onMounted, ref} from "vue"

const props = withDefaults(defineProps<{label: string; hint?: string; error?: string; required?: boolean; forId?: string; optional?: boolean; descriptionId?: string}>(), {required: false, optional: false})
const fieldRoot = ref<HTMLElement | null>(null)

// AppFormField accepts arbitrary slotted controls, so callers historically
// could render a visual label without a native label association. Supply an
// accessible-name fallback for every native control while preserving any
// explicit aria-label/aria-labelledby supplied by the caller.
const ensureAccessibleName = () => {
  const controls = fieldRoot.value?.querySelectorAll<HTMLElement>("input:not([type='hidden']), textarea, select, [contenteditable='true']") ?? []
  controls.forEach((control, index) => {
    if (control.hasAttribute("aria-label") || control.hasAttribute("aria-labelledby")) return
    control.setAttribute("aria-label", index === 0 ? props.label : `${props.label} ${index + 1}`)
  })
}

onMounted(() => { void nextTick(ensureAccessibleName) })
</script>

<template>
  <div ref="fieldRoot" class="app-form-field" :class="{'app-form-field--invalid': Boolean(props.error)}">
    <label v-if="props.forId" class="app-form-field__label" :for="props.forId">{{ props.label }}<span v-if="props.required" aria-hidden="true"> *</span><span v-if="props.optional" class="app-form-field__optional">Optional</span></label>
    <span v-else class="app-form-field__label">{{ props.label }}<span v-if="props.required" aria-hidden="true"> *</span><span v-if="props.optional" class="app-form-field__optional">Optional</span></span>
    <span v-if="props.hint" :id="props.descriptionId" class="app-form-field__hint">{{ props.hint }}</span>
    <slot />
    <span v-if="props.error" :id="props.descriptionId ? `${props.descriptionId}-error` : undefined" class="app-form-field__error" role="alert">{{ props.error }}</span>
  </div>
</template>

<style scoped>
.app-form-field { display: grid; gap: var(--space-1); }.app-form-field__label { color: var(--text); font-size: var(--text-size-body); font-weight: var(--text-weight-semibold); }.app-form-field__optional { margin-left: var(--space-1); color: var(--text-soft); font-size: var(--text-size-meta); font-weight: var(--text-weight-regular); }.app-form-field__hint { color: var(--text-muted); font-size: var(--text-size-meta); line-height: 1.5; }.app-form-field__error { color: var(--danger); font-size: var(--text-size-meta); font-weight: var(--text-weight-medium); }.app-form-field--invalid :deep(input), .app-form-field--invalid :deep(textarea), .app-form-field--invalid :deep(select) { border-color: var(--danger); }
</style>
