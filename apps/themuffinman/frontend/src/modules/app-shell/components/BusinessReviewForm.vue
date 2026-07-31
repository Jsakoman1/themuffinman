<script setup lang="ts">
import {ref} from "vue"
const emit = defineEmits<{submit: [value: {stars: number; comment: string}]}>()
const stars = ref(5); const comment = ref("")
</script>
<template><form class="review-form" @submit.prevent="emit('submit', {stars, comment})"><label>Rating <select v-model.number="stars"><option v-for="n in 5" :key="n" :value="n">{{ n }} stars</option></select></label><label>Review <textarea v-model="comment" maxlength="2000" placeholder="Optional feedback" /></label><button type="submit">Save review</button></form></template>
<style scoped>.review-form{display:grid;gap:var(--space-2)}label{display:grid;gap:var(--space-1)}textarea,select,button{font:inherit;padding:var(--space-2)}</style>
