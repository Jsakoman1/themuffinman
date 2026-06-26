<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref, watch} from "vue"

const props = withDefaults(defineProps<{
  open: boolean
  title: string
  subtitle?: string
  leading?: string
  position?: "center" | "drawer"
  size?: "sm" | "md" | "lg" | "xl"
  defaultExpanded?: boolean
  allowExpand?: boolean
  chromeOnlyHeader?: boolean
}>(), {
  subtitle: "",
  leading: "",
  position: "center",
  size: "md",
  defaultExpanded: false,
  allowExpand: true,
  chromeOnlyHeader: false,
})

const emit = defineEmits<{
  (event: "close"): void
}>()

const isExpanded = ref(false)

const canExpand = computed(() => props.position !== "drawer" && props.allowExpand)
const panelClasses = computed(() => [
  "dialog-panel",
  "card",
  {
    "dialog-panel--drawer": props.position === "drawer",
    "dialog-panel--expanded": isExpanded.value,
  },
  `dialog-panel--${props.size}`,
])

const headerClasses = computed(() => [
  "dialog-panel__header",
  {
    "dialog-panel__header--chrome-only": props.chromeOnlyHeader,
  }
])

const closeDialog = () => emit("close")

const expandDialog = () => {
  if (!canExpand.value || isExpanded.value) {
    return
  }

  isExpanded.value = true
}

const shrinkDialog = () => {
  if (!canExpand.value || !isExpanded.value) {
    return
  }

  isExpanded.value = false
}

const handleKeydown = (event: KeyboardEvent) => {
  if (!props.open) {
    return
  }

  if (event.key === "Escape") {
    closeDialog()
  }
}

watch(() => props.open, (open) => {
  if (!open) {
    isExpanded.value = false
    document.body.style.removeProperty("overflow")
    return
  }

  isExpanded.value = props.defaultExpanded && canExpand.value
  document.body.style.setProperty("overflow", "hidden")
}, {immediate: true})

onBeforeUnmount(() => {
  document.body.style.removeProperty("overflow")
})

if (typeof window !== "undefined") {
  onMounted(() => window.addEventListener("keydown", handleKeydown))
  onBeforeUnmount(() => window.removeEventListener("keydown", handleKeydown))
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      :class="['dialog-backdrop', { 'dialog-backdrop--drawer': position === 'drawer' }]"
      @click.self="closeDialog"
    >
      <div :class="panelClasses">
        <div v-if="title || subtitle || leading || $slots.actions || chromeOnlyHeader" :class="headerClasses">
          <div class="dialog-panel__header-main">
            <span v-if="leading" class="card__header-leading">{{ leading }}</span>
            <h2 v-if="title && !chromeOnlyHeader" class="card__title card__title--dialog">{{ title }}</h2>
            <p v-if="subtitle && !chromeOnlyHeader" class="dialog-panel__subtitle">{{ subtitle }}</p>
          </div>

          <div class="dialog-panel__header-actions">
            <slot name="actions" />
            <div class="dialog-panel__window-controls">
              <button
                v-if="canExpand"
                class="dialog-window-control dialog-window-control--shrink"
                type="button"
                aria-label="Shrink dialog"
                title="Shrink dialog"
                :disabled="!isExpanded"
                @click="shrinkDialog"
              />
              <button
                v-if="canExpand"
                class="dialog-window-control dialog-window-control--expand"
                type="button"
                aria-label="Expand dialog"
                title="Expand dialog"
                :disabled="isExpanded"
                @click="expandDialog"
              />
              <button class="dialog-window-control dialog-window-control--close" type="button" aria-label="Close dialog" title="Close" @click="closeDialog" />
            </div>
          </div>
        </div>

        <div class="dialog-panel__body">
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>
