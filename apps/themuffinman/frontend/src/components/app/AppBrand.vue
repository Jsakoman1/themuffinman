<script setup lang="ts">
defineProps<{
  to: string
}>()
</script>

<template>
  <RouterLink class="brand brand--logo brand--interactive app-brand" :to="to">
    <div class="brand__mark" aria-hidden="true">
      <span></span>
      <span></span>
    </div>
    <div class="brand__stack">
      <div class="brand__eyebrow">One App</div>
      <div class="brand__title">TheMuffinMan</div>
    </div>
  </RouterLink>
</template>
