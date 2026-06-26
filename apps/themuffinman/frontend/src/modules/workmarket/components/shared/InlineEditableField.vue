<script setup lang="ts">
import {computed} from "vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"

defineEmits<{
  (event: "toggle"): void
}>()

const props = withDefaults(defineProps<{
  label: string
  editing: boolean
  fieldClass?: string
  editable?: boolean
}>(), {
  fieldClass: "",
  editable: true,
})

const useReadonlyRow = computed(() => {
  if (!props.editable || props.editing) {
    return false
  }

  return ![
    "ui-edit-field--title",
    "ui-edit-field--description",
    "ui-edit-field--gallery",
    "ui-edit-field--circles",
  ].some((className) => props.fieldClass.includes(className))
})

const mergedFieldClass = computed(() => {
  return [
    "ui-edit-field",
    "ui-edit-field--inline-editable",
    props.fieldClass,
    useReadonlyRow.value ? "ui-edit-field--readonly-row" : "",
  ].filter(Boolean).join(" ")
})
</script>

<template>
  <div
    v-if="useReadonlyRow"
    :class="mergedFieldClass"
  >
    <button
      class="button button--icon button--secondary button--icon-compact"
      type="button"
      :aria-label="`Edit ${props.label}`"
      @click="$emit('toggle')"
    >
      ✎
    </button>

    <span class="label">{{ props.label }}</span>

    <slot name="display" />
  </div>

  <UiFieldGroup
    v-else
    :label="props.label"
    tag="div"
    :field-class="mergedFieldClass"
  >
    <template v-if="props.editable" #headerAction>
      <button
        class="button button--icon button--secondary button--icon-compact"
        type="button"
        :aria-label="`Edit ${props.label}`"
        @click="$emit('toggle')"
      >
        ✎
      </button>
    </template>

    <slot v-if="props.editing" name="editor" />
    <slot v-else name="display" />
  </UiFieldGroup>
</template>
