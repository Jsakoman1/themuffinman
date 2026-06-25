<script setup lang="ts">
import {computed} from "vue"

const props = withDefaults(defineProps<{
  tag?: string
  eyebrow?: string
  title?: string
  subtitle?: string
  soft?: boolean
  compact?: boolean
}>(), {
  tag: "section",
  eyebrow: "",
  title: "",
  subtitle: "",
  soft: false,
  compact: false,
})

const sectionClass = computed(() => [
  "surface-section",
  {
    "surface-section--soft": props.soft,
    "surface-section--compact": props.compact,
  },
])
</script>

<template>
  <component :is="tag" :class="sectionClass">
    <div v-if="eyebrow || title || subtitle || $slots.actions" class="surface-section__header">
      <div class="surface-section__heading">
        <div v-if="eyebrow" class="surface-section__eyebrow">{{ eyebrow }}</div>
        <h3 v-if="title" class="surface-section__title">{{ title }}</h3>
        <p v-if="subtitle" class="surface-section__subtitle">{{ subtitle }}</p>
      </div>

      <div v-if="$slots.actions" class="surface-section__actions">
        <slot name="actions" />
      </div>
    </div>

    <slot />
  </component>
</template>
