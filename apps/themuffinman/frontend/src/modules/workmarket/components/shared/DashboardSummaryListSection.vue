<script setup lang="ts">
import UiSectionHeader from "../../../../components/ui/UiSectionHeader.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"

withDefaults(defineProps<{
  title?: string
  subtitle?: string
  emptyMessage: string
  hasItems: boolean
  showHeader?: boolean
  boxed?: boolean
}>(), {
  title: "",
  subtitle: "",
  showHeader: true,
  boxed: true,
})
</script>

<template>
  <section class="surface-stack">
    <UiSurfaceSection :class="{ card: boxed }" :soft="boxed" :plain="!boxed">
      <UiSectionHeader v-if="showHeader" :title="title" :subtitle="subtitle" />

      <div v-if="!hasItems" class="empty-state">
        {{ emptyMessage }}
      </div>

      <div v-else class="quest-list mt-4">
        <slot />
      </div>
    </UiSurfaceSection>
  </section>
</template>
