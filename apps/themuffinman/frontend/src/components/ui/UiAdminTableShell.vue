<script setup lang="ts">
import UiPagination from "./UiPagination.vue"

withDefaults(defineProps<{
  topLabel?: string
  bottomLabel?: string
  hasPrevious: boolean
  hasNext: boolean
  showBottomPagination?: boolean
  compact?: boolean
}>(), {
  topLabel: "",
  bottomLabel: "",
  showBottomPagination: true,
  compact: false,
})

const emit = defineEmits<{
  (event: "previous"): void
  (event: "next"): void
}>()
</script>

<template>
  <div :class="['admin-table-shell', {'admin-table-shell--compact': compact}]">
    <UiPagination
      v-if="topLabel"
      class="mb-4"
      :label="topLabel"
      :has-previous="hasPrevious"
      :has-next="hasNext"
      @previous="emit('previous')"
      @next="emit('next')"
    />

    <slot />

    <UiPagination
      v-if="showBottomPagination && bottomLabel"
      class="mt-4"
      :label="bottomLabel"
      :has-previous="hasPrevious"
      :has-next="hasNext"
      @previous="emit('previous')"
      @next="emit('next')"
    />
  </div>
</template>
