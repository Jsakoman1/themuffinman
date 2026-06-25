<script setup lang="ts">
import RichTextEditor from "../../../../components/editor/AsyncRichTextEditor.vue"
import UiAmountField from "../../../../components/ui/UiAmountField.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"

defineProps<{
  message: string
  price: string
  pricePlaceholder?: string
  quickfillLabel?: string
}>()

const emit = defineEmits<{
  (event: "update:message", value: string): void
  (event: "update:price", value: string): void
  (event: "quickfill"): void
}>()
</script>

<template>
  <div class="ui-edit-form ui-edit-form--dialog ui-edit-form--application">
    <UiFieldGroup label="Message" tag="div" field-class="ui-edit-field ui-edit-field--message">
      <RichTextEditor
        :model-value="message"
        placeholder=""
        toolbar-label="Message tools"
        @update:model-value="emit('update:message', $event)"
      />
    </UiFieldGroup>

    <UiAmountField
      :model-value="price"
      label="Proposed price"
      :placeholder="pricePlaceholder"
      field-class="ui-edit-field ui-edit-field--price"
      @update:model-value="emit('update:price', $event)"
    >
      <template v-if="quickfillLabel" #headerAction>
        <button class="button button--ghost calendar-application-form__quickfill" type="button" @click="emit('quickfill')">
          {{ quickfillLabel }}
        </button>
      </template>
    </UiAmountField>
  </div>
</template>
