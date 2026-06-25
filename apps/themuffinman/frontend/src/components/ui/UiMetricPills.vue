<script setup lang="ts">
defineProps<{
  items: Array<{
    label: string
    value: string
  }>
}>()
</script>

<template>
  <div class="ui-metric-pills">
    <div v-for="item in items" :key="`${item.label}-${item.value}`" class="ui-metric-pills__item">
      <span class="ui-metric-pills__label">{{ item.label }}</span>
      <strong class="ui-metric-pills__value">{{ item.value }}</strong>
    </div>
  </div>
</template>
