<script setup lang="ts">
defineProps<{items: {startsAt: string; endsAt: string}[], modelValue: string}>()
const emit = defineEmits<{"update:modelValue": [string]}>()
</script>
<template><section><h3>Available times</h3><div class="availability-picker"><button v-for="item in items" :key="item.startsAt" type="button" :class="{selected: modelValue === item.startsAt}" @click="emit('update:modelValue', item.startsAt)">{{ new Date(item.startsAt).toLocaleString() }}</button><p v-if="items.length === 0">No matching availability for this range.</p></div></section></template>
