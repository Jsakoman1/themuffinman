<script setup lang="ts">
defineProps<{rules: Record<string, unknown>[]}>()
</script>
<template><section class="business-rule-editor" data-pricing-model="backend-rule-preview"><h3>Pricing rules</h3><p v-if="rules.length === 0">Fixed price can be configured on the offering. Add rules only when quantity, duration, participants, or a formula changes the amount.</p><ul v-else><li v-for="rule in rules" :key="String(rule.id ?? rule.ruleKey)"><strong>{{ rule.ruleKey }}</strong><span>{{ rule.billingUnit }}<template v-if="rule.unitPrice != null"> · {{ rule.unitPrice }}</template></span></li></ul><p class="business-rule-editor__note">The backend quote service remains authoritative for totals, ranges, and quote-required outcomes.</p></section></template>
