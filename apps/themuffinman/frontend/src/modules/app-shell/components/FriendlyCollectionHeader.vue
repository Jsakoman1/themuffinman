<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"
import {computed} from "vue"
import PageOrientationHeader, {type PageOrientationTone} from "./PageOrientationHeader.vue"

type FriendlyHeaderTone = "work" | "share" | "book" | "ride" | "people" | "neutral"
const props = defineProps<{
  eyebrow: string
  title: string
  description: string
  tone?: FriendlyHeaderTone
  primaryAction?: {label: string; to: RouteLocationRaw}
}>()
const toneMap: Record<FriendlyHeaderTone, PageOrientationTone> = {work: "work", share: "things", book: "business", ride: "rides", people: "people", neutral: "neutral"}
const orientationTone = computed(() => toneMap[props.tone ?? "neutral"])
</script>

<template>
  <PageOrientationHeader :eyebrow="props.eyebrow" :title="props.title" :description="props.description" :tone="orientationTone" :primary-action="props.primaryAction">
    <template #actions><slot name="actions" /></template>
  </PageOrientationHeader>
</template>
