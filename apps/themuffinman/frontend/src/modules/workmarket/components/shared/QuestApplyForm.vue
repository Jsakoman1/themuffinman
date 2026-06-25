<script setup lang="ts">
import DashboardEditSheet from "../dashboard/DashboardEditSheet.vue"
import ApplicationEditFields from "./ApplicationEditFields.vue"

defineProps<{
  message: string
  price: string
  pricePlaceholder?: string
  quickfillLabel?: string
  canSubmit: boolean
  submitLabel?: string
}>()

const emit = defineEmits<{
  (event: "update:message", value: string): void
  (event: "update:price", value: string): void
  (event: "quickfill"): void
  (event: "submit"): void
}>()
</script>

<template>
  <form class="stack calendar-application-form quest-apply-form" autocomplete="off" @submit.prevent="emit('submit')">
    <DashboardEditSheet>
      <ApplicationEditFields
        :message="message"
        :price="price"
        :price-placeholder="pricePlaceholder"
        :quickfill-label="quickfillLabel"
        :inline-editable="false"
        @update:message="emit('update:message', $event)"
        @update:price="emit('update:price', $event)"
        @quickfill="emit('quickfill')"
      />

      <template #actions>
        <button class="button button--action button--flat-primary" type="submit" :disabled="!canSubmit">{{ submitLabel ?? "Apply" }}</button>
      </template>
    </DashboardEditSheet>
  </form>
</template>
