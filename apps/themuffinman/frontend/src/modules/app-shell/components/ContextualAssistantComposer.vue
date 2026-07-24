<script setup lang="ts">
import VisionForWebHost from "./VisionForWebHost.vue"

defineProps<{context: string; source: string; returnTo: string}>()
</script>

<template>
  <aside class="contextual-assistant-composer" aria-label="Persistent Vision composer" data-assistant-model="persistent-bottom-composer">
    <VisionForWebHost :context="context" :source="source" :return-to="returnTo" persistent />
  </aside>
</template>

<style scoped>.contextual-assistant-composer{position:relative;z-index:var(--z-popover);min-height:0}</style>
