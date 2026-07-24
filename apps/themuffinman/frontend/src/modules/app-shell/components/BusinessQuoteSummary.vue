<script setup lang="ts">
defineProps<{quote: {pricingState: string; totalAmount: number | null; currency: string | null; explanations: string[]}|null}>()
</script>
<template><section><h3>Quote</h3><p v-if="!quote">Choose a start time to calculate the price.</p><template v-else><strong>{{ quote.totalAmount === null ? 'Quote required' : `${quote.totalAmount} ${quote.currency ?? ''}` }}</strong><ul><li v-for="explanation in quote.explanations" :key="explanation">{{ explanation }}</li></ul></template></section></template>
