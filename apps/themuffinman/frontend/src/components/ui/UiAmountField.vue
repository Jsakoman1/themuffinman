<script setup lang="ts">
import UiFieldGroup from "./UiFieldGroup.vue"

const model = defineModel<string>({default: ""})

withDefaults(defineProps<{
  label: string
  placeholder?: string
  compact?: boolean
  fieldClass?: string
}>(), {
  placeholder: "",
  compact: false,
  fieldClass: "",
})
</script>

<template>
  <UiFieldGroup :label="label" :field-class="fieldClass">
    <template v-if="$slots.headerAction" #headerAction>
      <slot name="headerAction" />
    </template>
    <div :class="['ui-amount-input', { 'ui-amount-input--compact': compact }]">
      <span class="ui-amount-input__symbol" aria-hidden="true">$</span>
      <input
        v-model="model"
        class="input ui-amount-input__input"
        inputmode="decimal"
        :placeholder="placeholder"
      />
    </div>
  </UiFieldGroup>
</template>
