<script setup lang="ts">
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"

withDefaults(defineProps<{
  title?: string
  tone?: "default" | "summary" | "actions"
}>(), {
  title: "",
  tone: "default",
})
</script>

<template>
  <UiSurfaceSection
    :class="[
      'ui-utility-section',
      {
        'ui-utility-section--summary': tone === 'summary',
        'ui-utility-section--actions': tone === 'actions',
      },
    ]"
    compact
    :title="title"
  >
    <slot />
  </UiSurfaceSection>
</template>
