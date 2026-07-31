<script setup lang="ts">
import type {QuestNewsItemResponseDTO} from "../../../contracts/index.ts"
import NotificationItem from "./NotificationItem.vue"
defineProps<{open: boolean; items: QuestNewsItemResponseDTO[]}>()
defineEmits<{close: []; openItem: [item: QuestNewsItemResponseDTO]}>()
</script>
<template><Teleport to="body"><div v-if="open" class="notification-drawer__backdrop" @click.self="$emit('close')"><aside class="notification-drawer" aria-label="Updates"><header><strong>Updates</strong><button type="button" aria-label="Close updates" @click="$emit('close')">×</button></header><NotificationItem v-for="item in items.slice(0, 12)" :key="item.id" :item="item" @open="$emit('openItem', item)" /><RouterLink to="/notifications" @click="$emit('close')">See all updates</RouterLink></aside></div></Teleport></template>
<style scoped>.notification-drawer__backdrop{position:fixed;inset:0;z-index:var(--z-drawer);display:grid;justify-items:end;background:rgba(18,45,80,.18)}.notification-drawer{width:min(25rem,100vw);height:100%;overflow:auto;padding:var(--space-3);background:var(--surface-raised);box-shadow:var(--shadow-overlay)}.notification-drawer header{display:flex;align-items:center;justify-content:space-between;padding:var(--space-2)}.notification-drawer header strong{font-size:var(--text-size-title)}.notification-drawer header button{border:0;background:transparent;font:inherit;font-size:1.5rem;cursor:pointer}.notification-drawer>a{display:block;padding:var(--space-3);color:var(--accent);font-weight:var(--text-weight-semibold)}</style>
