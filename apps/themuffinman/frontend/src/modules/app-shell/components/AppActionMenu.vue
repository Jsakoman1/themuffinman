<script setup lang="ts">
withDefaults(defineProps<{label?: string}>(), {label: "More actions"})
</script>

<template>
  <details class="app-action-menu">
    <summary class="app-action-menu__summary" :aria-label="label">⋯</summary>
    <div class="app-action-menu__items"><slot /></div>
  </details>
</template>

<style scoped>
.app-action-menu{position:relative}.app-action-menu__summary{display:inline-grid;place-items:center;width:2.25rem;height:2.25rem;border:1px solid rgba(23,34,26,.12);border-radius:50%;cursor:pointer;font-size:1.25rem;line-height:1;list-style:none}.app-action-menu__summary::-webkit-details-marker{display:none}.app-action-menu__items{position:absolute;right:0;z-index:3;display:grid;gap:.25rem;min-width:9rem;margin-top:.35rem;padding:.35rem;border:1px solid rgba(23,34,26,.1);border-radius:.8rem;background:#fff;box-shadow:0 14px 28px rgba(23,34,26,.14)}.app-action-menu__items :deep(a),.app-action-menu__items :deep(button){display:block;width:100%;padding:.55rem .65rem;border:0;border-radius:.55rem;background:transparent;color:inherit;text-align:left;font:inherit;cursor:pointer}.app-action-menu__items :deep(a:hover),.app-action-menu__items :deep(button:hover){background:rgba(23,34,26,.07)}
</style>
