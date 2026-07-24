<script setup lang="ts">
defineProps<{quote: {pricingState: string; totalAmount: number | null; currency: string | null; explanations: string[]}|null}>()
</script>
<template><section data-quote-model="transparent"><h3>Quote</h3><p v-if="!quote">Choose a start time to calculate the price.</p><template v-else><strong>{{ quote.totalAmount === null ? 'Quote required' : `${quote.totalAmount} ${quote.currency ?? ''}` }}</strong><ul><li v-for="explanation in quote.explanations" :key="explanation">{{ explanation }}</li></ul><p v-if="quote.pricingState === 'UNAVAILABLE'">The selected configuration cannot be quoted yet. Review the service rules or request a quote.</p></template></section></template>
