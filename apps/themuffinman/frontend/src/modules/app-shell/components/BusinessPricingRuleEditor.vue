<script setup lang="ts">
import AppButton from "./AppButton.vue"

const props = defineProps<{rules: Record<string, unknown>[]}>()
const emit = defineEmits<{"update:rules": [rules: Record<string, unknown>[]]}>()

const updateRule = (index: number, key: string, value: unknown) => emit("update:rules", props.rules.map((rule, ruleIndex) => ruleIndex === index ? {...rule, [key]: value} : rule))
const addRule = () => emit("update:rules", [...props.rules, {ruleKey: "", ruleType: "BASE", billingUnit: "BOOKING", amount: null, currency: "CHF", active: true, sortOrder: props.rules.length}])
const removeRule = (index: number) => emit("update:rules", props.rules.filter((_, ruleIndex) => ruleIndex !== index).map((rule, ruleIndex) => ({...rule, sortOrder: ruleIndex})))
</script>

<template>
  <section class="business-rule-editor" data-pricing-model="backend-rule-editor">
    <div class="business-rule-editor__heading"><div><h3>Extra price rules</h3><p>Add one only when the amount changes for a clear reason, such as another person or an extra hour.</p></div><AppButton tone="secondary" @click="addRule">Add price rule</AppButton></div>
    <p v-if="rules.length === 0" class="business-rule-editor__empty">This service uses its base price. Customers will see any extra charge you add here before they book.</p>
    <div v-for="(rule, index) in rules" :key="String(rule.id ?? index)" class="business-rule-editor__rule">
      <label>What is this for?<input :value="String(rule.ruleKey ?? '')" placeholder="e.g. Extra guest" @input="updateRule(index, 'ruleKey', ($event.target as HTMLInputElement).value)"></label>
      <label>Charged per<select :value="String(rule.billingUnit ?? 'BOOKING')" @change="updateRule(index, 'billingUnit', ($event.target as HTMLSelectElement).value)"><option value="BOOKING">Booking</option><option value="QUANTITY">Person or item</option><option value="DURATION">Hour</option></select></label>
      <label>Extra amount<input :value="String(rule.amount ?? '')" type="number" min="0" step="0.01" placeholder="0.00" @input="updateRule(index, 'amount', ($event.target as HTMLInputElement).value || null)"></label>
      <label>Currency<input :value="String(rule.currency ?? 'CHF')" maxlength="3" @input="updateRule(index, 'currency', ($event.target as HTMLInputElement).value.toUpperCase())"></label>
      <AppButton tone="danger" @click="removeRule(index)">Remove</AppButton>
    </div>
    <p class="business-rule-editor__note">The backend quote service remains authoritative for totals, ranges, and quote-required outcomes.</p>
  </section>
</template>

<style scoped>
.business-rule-editor{display:grid;gap:var(--space-3)}.business-rule-editor h3,.business-rule-editor p{margin:0}.business-rule-editor__heading{display:flex;justify-content:space-between;align-items:start;gap:var(--space-3)}.business-rule-editor__heading p,.business-rule-editor__empty,.business-rule-editor__note{color:var(--text-muted);line-height:1.5}.business-rule-editor__rule{display:grid;grid-template-columns:minmax(12rem,2fr) minmax(9rem,1fr) minmax(8rem,1fr) 5rem auto;gap:var(--space-2);align-items:end;padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-control)}.business-rule-editor label{display:grid;gap:var(--space-1);color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}.business-rule-editor input,.business-rule-editor select{border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.business-rule-editor__note{font-size:var(--text-size-meta)}@media(max-width:800px){.business-rule-editor__heading{flex-direction:column}.business-rule-editor__rule{grid-template-columns:1fr 1fr}.business-rule-editor__rule .app-button{justify-self:start}}@media(max-width:520px){.business-rule-editor__rule{grid-template-columns:1fr}}
</style>
