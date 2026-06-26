<script setup lang="ts">
import AppPageHeader from "../app/AppPageHeader.vue"
import UiSurfaceSection from "./UiSurfaceSection.vue"

withDefaults(defineProps<{
  title: string
  subtitle?: string
}>(), {
  subtitle: "",
})
</script>

<template>
  <UiSurfaceSection plain>
    <AppPageHeader :title="title" :subtitle="subtitle">
      <template v-if="$slots.actions" #actions>
        <slot name="actions" />
      </template>
    </AppPageHeader>

    <slot name="filters" />
    <slot />
  </UiSurfaceSection>
</template>
