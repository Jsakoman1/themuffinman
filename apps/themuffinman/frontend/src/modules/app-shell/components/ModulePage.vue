<script setup lang="ts">
import {computed} from "vue"
import ModuleTabs from "./ModuleTabs.vue"
import type {ModuleTab} from "../moduleTabRegistry"

const props = withDefaults(defineProps<{
  title: string
  description?: string
  eyebrow?: string
  tabs?: ModuleTab[]
  activeTabId?: string
  primaryAction?: string
}>(), {description: "", eyebrow: "", tabs: () => [], activeTabId: "", primaryAction: ""})
const hasTabs = computed(() => props.tabs.length > 0)
const emit = defineEmits<{primary: []}>()
</script>

<template>
  <section class="module-page">
    <header class="module-page__header">
      <div class="module-page__heading">
        <p v-if="eyebrow" class="module-page__eyebrow">{{ eyebrow }}</p>
        <h1>{{ title }}</h1>
        <p v-if="description" class="module-page__description">{{ description }}</p>
      </div>
      <div v-if="primaryAction || $slots.actions" class="module-page__actions">
        <slot name="actions"><button class="module-page__primary ui-focusable" type="button" @click="emit('primary')">{{ primaryAction }}</button></slot>
      </div>
    </header>
    <ModuleTabs v-if="hasTabs" :tabs="tabs" :active-id="activeTabId" />
    <div class="module-page__content"><slot /></div>
  </section>
</template>

<style scoped>
.module-page{display:grid;gap:var(--space-4);min-width:0}.module-page__header{display:flex;align-items:flex-start;justify-content:space-between;gap:var(--space-4)}.module-page__heading{min-width:0}.module-page h1,.module-page p{margin:0}.module-page h1{font-size:clamp(1.6rem,2.5vw,2.35rem);letter-spacing:-.03em;line-height:1.08}.module-page__eyebrow{margin-bottom:var(--space-1)!important;color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);text-transform:uppercase;letter-spacing:.06em}.module-page__description{max-width:44rem;margin-top:var(--space-2)!important;color:var(--text-muted);font-size:var(--text-size-body)}.module-page__actions{display:flex;gap:var(--space-2);flex:0 0 auto}.module-page__primary{min-height:var(--control-height-default);padding:var(--space-1) var(--space-3);border:1px solid var(--accent);border-radius:var(--radius-control);background:var(--accent);color:var(--canvas);font-weight:var(--text-weight-semibold);cursor:pointer}.module-page__content{min-width:0}@media(max-width:640px){.module-page__header{display:grid;gap:var(--space-3)}.module-page__actions,.module-page__primary{width:100%}}
</style>
