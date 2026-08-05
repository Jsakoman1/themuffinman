<script setup lang="ts">
import VisionForWebHost from "./VisionForWebHost.vue"

defineProps<{context: string; source: string; returnTo: string}>()
</script>

<template>
  <aside class="contextual-assistant-composer" :aria-label="`Ask Vision about ${context}`" data-assistant-model="siri-like-persistent-bottom-composer" data-mental-model="ask-result-confirm-recover" data-vision-boundary="quiet-contextual-entry" data-responsive-model="desktop-dock-mobile-safe-area">
    <VisionForWebHost :context="context" :source="source" :return-to="returnTo" persistent />
  </aside>
</template>

<style scoped>.contextual-assistant-composer{position:relative;z-index:var(--z-popover);min-height:0}.contextual-assistant-composer :deep(.vision-web-host__panel--persistent){bottom:max(1rem,env(safe-area-inset-bottom))}@media(max-width:980px){.contextual-assistant-composer :deep(.vision-web-host__panel--persistent){bottom:calc(4.85rem + env(safe-area-inset-bottom));width:min(34rem,calc(100vw - 1.5rem))}}</style>
