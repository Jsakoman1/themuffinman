<script setup lang="ts">
defineProps<{rules: Record<string, unknown>[]}>()
</script>
<template><section class="business-rule-editor"><h3>Pricing rules</h3><p v-if="rules.length === 0">No extra pricing rules configured.</p><ul v-else><li v-for="rule in rules" :key="String(rule.id ?? rule.ruleKey)">{{ rule.ruleKey }} · {{ rule.billingUnit }}</li></ul></section></template>
