<script setup lang="ts">
import type { BusinessOfferingResponseDTO } from "../../../contracts/index.ts"
defineProps<{ offering: BusinessOfferingResponseDTO }>()
const emit = defineEmits<{ book: [id: number] }>()
const price = (offering: BusinessOfferingResponseDTO) => offering.pricingType === "FREE" ? "Free" : offering.pricingType === "CUSTOM_QUOTE" ? "Price on request" : `${offering.pricingType === "FROM" ? "From " : ""}${offering.basePriceAmount} ${offering.basePriceCurrency}`
</script>

<template>
  <article class="service-card"><div><h3>{{ offering.title }}</h3><p>{{ offering.summary || "Service details are confirmed when you book." }}</p><p class="service-card__meta">{{ price(offering) }} · {{ offering.defaultDurationMinutes }} min</p></div><button type="button" @click="emit('book', offering.id)">Choose service</button></article>
</template>

<style scoped>
.service-card { display: flex; justify-content: space-between; gap: var(--space-3); padding: var(--space-3); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); }.service-card h3, .service-card p { margin: 0; }.service-card div { display: grid; gap: var(--space-1); }.service-card p { color: var(--text-muted); }.service-card__meta { font-weight: var(--text-weight-semibold); } button { align-self: center; border: 0; border-radius: var(--radius-control); background: var(--accent); color: var(--accent-contrast); cursor: pointer; font: inherit; padding: var(--space-2) var(--space-3); white-space: nowrap; } @media (max-width: 640px) { .service-card { flex-direction: column; } button { align-self: start; } }
</style>
