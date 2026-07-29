<script setup lang="ts">
defineProps<{quote: {pricingState: string; totalAmount: number | null; currency: string | null; explanations: string[]}|null}>()
</script>
<template><section data-quote-model="transparent"><h3>Price</h3><p v-if="!quote">Choose a time to see the current price.</p><template v-else><strong>{{ quote.totalAmount === null ? 'Price confirmed after review' : `${quote.totalAmount} ${quote.currency ?? ''}` }}</strong><ul><li v-for="explanation in quote.explanations" :key="explanation">{{ explanation }}</li></ul><p v-if="quote.pricingState === 'UNAVAILABLE'">The business needs to review this request before it can confirm a price.</p></template></section></template>
