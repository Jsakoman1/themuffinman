<script setup lang="ts">
import UiDialog from "./UiDialog.vue"
import UiFormActions from "./UiFormActions.vue"

withDefaults(defineProps<{
  open: boolean
  title?: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  confirmTone?: "danger" | "primary"
  busy?: boolean
}>(), {
  title: "Please confirm",
  confirmLabel: "Confirm",
  cancelLabel: "Cancel",
  confirmTone: "primary",
  busy: false
})

const emit = defineEmits<{
  (event: "confirm"): void
  (event: "close"): void
}>()
</script>

<template>
  <UiDialog :open="open" :title="title" size="sm" @close="emit('close')">
    <div class="stack dialog-sheet">
      <p class="dialog-panel__subtitle">{{ message }}</p>

      <UiFormActions>
        <button class="button button--ghost" type="button" :disabled="busy" @click="emit('close')">
          {{ cancelLabel }}
        </button>
        <button
          :class="['button', confirmTone === 'danger' ? 'button--danger' : 'button--action']"
          type="button"
          :disabled="busy"
          @click="emit('confirm')"
        >
          {{ confirmLabel }}
        </button>
      </UiFormActions>
    </div>
  </UiDialog>
</template>
