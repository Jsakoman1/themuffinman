<script setup lang="ts">
import type {BusinessReviewResponseDTO} from "../../../contracts/index.ts"

// Text wraps within the review card on narrow screens instead of forcing overflow.
defineProps<{reviews: BusinessReviewResponseDTO[]; loading?: boolean}>()
</script>
<template><p v-if="loading">Loading reviews…</p><p v-else-if="!reviews.length">No completed-booking reviews yet.</p><article v-for="review in reviews" :key="review.id" class="review"><strong>{{ review.reviewerUsername }} · {{ '★'.repeat(review.stars) }}</strong><span>{{ review.serviceTitle }}</span><p v-if="review.comment">{{ review.comment }}</p></article></template>
<style scoped>.review{display:grid;gap:var(--space-1);padding:var(--space-2) 0;border-bottom:1px solid var(--border-subtle)}.review span,.review p{margin:0;color:var(--text-muted)}</style>
