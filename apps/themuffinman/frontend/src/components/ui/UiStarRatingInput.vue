<script setup lang="ts">
const props = withDefaults(defineProps<{
  modelValue: number
  max?: number
  ariaLabel?: string
}>(), {
  max: 5,
  ariaLabel: "Rating",
})

const emit = defineEmits<{
  (event: "update:modelValue", value: number): void
}>()

const selectValue = (value: number) => {
  emit("update:modelValue", props.modelValue === value ? 0 : value)
}
</script>

<template>
  <div class="ui-star-rating" role="radiogroup" :aria-label="ariaLabel">
    <button
      v-for="value in max"
      :key="value"
      class="ui-star-rating__button"
      :class="{ 'ui-star-rating__button--active': value <= modelValue }"
      type="button"
      :aria-pressed="value === modelValue"
      @click="selectValue(value)"
    >
      ★
    </button>
  </div>
</template>
