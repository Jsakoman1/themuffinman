<script setup lang="ts">
import type {QuestNewsItemResponseDTO} from "../../../contracts/index.ts"
import {formatDateTime} from "../../../services/formatters.ts"
const props = defineProps<{item: QuestNewsItemResponseDTO}>()
defineEmits<{open: [item: QuestNewsItemResponseDTO]}>()
</script>
<template><button type="button" class="notification-item" :class="{'notification-item--unread': !props.item.readAt}" @click="$emit('open', props.item)"><span class="notification-item__dot" aria-hidden="true" /><span><strong>{{ props.item.title }}</strong><small>{{ props.item.message }}</small><time>{{ formatDateTime(props.item.createdAt, 'Recently') }}</time></span></button></template>
<style scoped>.notification-item{display:grid;grid-template-columns:auto minmax(0,1fr);width:100%;gap:var(--space-2);padding:var(--space-3);border:0;border-bottom:1px solid var(--border-subtle);background:transparent;color:var(--text);text-align:left;font:inherit;cursor:pointer}.notification-item:hover{background:var(--surface-hover)}.notification-item__dot{width:.55rem;height:.55rem;margin-top:.35rem;border-radius:50%;background:transparent}.notification-item--unread{background:color-mix(in srgb,var(--accent-muted) 42%,transparent)}.notification-item--unread .notification-item__dot{background:var(--accent)}.notification-item strong,.notification-item small,.notification-item time{display:block}.notification-item small{margin-top:.2rem;color:var(--text-muted)}.notification-item time{margin-top:.35rem;color:var(--text-soft);font-size:var(--text-size-meta)}</style>
