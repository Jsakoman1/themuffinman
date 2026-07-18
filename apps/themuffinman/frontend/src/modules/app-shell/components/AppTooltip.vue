<script setup lang="ts">
import {onBeforeUnmount, ref} from "vue"

const props = defineProps<{label: string}>()
const open = ref(false)
const tooltipDelay = 450
let showTimer: ReturnType<typeof setTimeout> | undefined

const show = () => {
  if (showTimer) clearTimeout(showTimer)
  showTimer = setTimeout(() => { open.value = true }, tooltipDelay)
}
const hide = () => {
  if (showTimer) clearTimeout(showTimer)
  showTimer = undefined
  open.value = false
}

onBeforeUnmount(() => { if (showTimer) clearTimeout(showTimer) })
</script>

<template>
  <span class="app-tooltip" @mouseenter="show" @mouseleave="hide" @focusin="show" @focusout="hide">
    <slot />
    <span v-if="open" class="app-tooltip__content" role="tooltip">{{ props.label }}</span>
  </span>
</template>

<style scoped>
.app-tooltip { position: relative; display: inline-flex; }.app-tooltip__content { position: absolute; z-index: var(--z-popover); bottom: calc(100% + var(--space-2)); left: 50%; width: max-content; max-width: min(18rem, 70vw); transform: translateX(-50%); padding: var(--space-1) var(--space-2); border: 1px solid var(--border-strong); border-radius: var(--radius-control); background: var(--surface-raised); box-shadow: var(--shadow-overlay); color: var(--text); font-size: var(--text-size-meta); line-height: 1.35; pointer-events: none; }
</style>
