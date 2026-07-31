<script setup lang="ts">
defineProps<{averageStars: number; reviewCount: number}>()
</script>
<template><p class="rating">{{ reviewCount ? `★ ${averageStars.toFixed(1)} · ${reviewCount} reviews` : "No reviews yet" }}</p></template>
<style scoped>.rating{margin:0;color:var(--text-muted)}</style>
