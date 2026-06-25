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

const mergedFieldClass = computed(() => `ui-edit-field ui-edit-field--inline-editable ${props.fieldClass}`.trim())
</script>

<template>
  <UiFieldGroup
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
