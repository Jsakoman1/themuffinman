<script setup lang="ts">
import {defineAsyncComponent} from "vue"
import RichTextEditorLoading from "./RichTextEditorLoading.vue"

defineProps<{
  modelValue: string
  placeholder?: string
  toolbarLabel?: string
}>()

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void
}>()

const RichTextEditor = defineAsyncComponent({
  loader: () => import("./RichTextEditor.vue"),
  loadingComponent: RichTextEditorLoading,
  delay: 120,
})
</script>

<template>
  <RichTextEditor
    :model-value="modelValue"
    :placeholder="placeholder"
    :toolbar-label="toolbarLabel"
    @update:model-value="emit('update:modelValue', $event)"
  />
</template>
