<script setup lang="ts">
defineProps<{fields: Record<string, unknown>[], modelValue: Record<string, string>}>()
const emit = defineEmits<{"update:modelValue": [Record<string, string>]}>()
</script>
<template><section><h3>Details</h3><div v-for="field in fields" :key="String(field.id ?? field.fieldKey)" class="demand-field"><label>{{ field.label ?? field.fieldKey }}<input :value="modelValue[String(field.fieldKey ?? field.field_key)] ?? ''" :required="Boolean(field.required)" @input="emit('update:modelValue', {...modelValue, [String(field.fieldKey ?? field.field_key)]: ($event.target as HTMLInputElement).value})"></label></div><p v-if="fields.length === 0">No additional details are required.</p></section></template>
