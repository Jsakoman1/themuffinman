<script setup lang="ts">
import type { BusinessPublicPageDTO } from "../../../contracts/index.ts"
import BusinessContactActions from "./BusinessContactActions.vue"
defineProps<{ page: BusinessPublicPageDTO; saved: boolean; saving: boolean }>()
const emit = defineEmits<{ toggleSave: [] }>()
</script>

<template>
  <header class="profile-header">
    <img v-if="page.heroImageUrl" :src="page.heroImageUrl" :alt="`${page.businessName} logo or cover`" class="profile-header__image">
    <div v-else class="profile-header__fallback" aria-hidden="true">{{ page.businessName.slice(0, 1) }}</div>
    <div class="profile-header__content">
      <div class="profile-header__title"><div><p class="eyebrow">Local service</p><h1>{{ page.businessName }}</h1><p v-if="page.headline">{{ page.headline }}</p></div><button type="button" :disabled="saving" @click="emit('toggleSave')">{{ saved ? "Saved" : "Save" }}</button></div>
      <p v-if="page.ratingSummary.reviewCount" class="rating">★ {{ page.ratingSummary.averageStars.toFixed(1) }} · {{ page.ratingSummary.reviewCount }} reviews</p>
      <p v-else class="rating">New business — no reviews yet</p>
      <BusinessContactActions :address="page.publicAddressLabel" :email="page.contactEmail" :phone="page.contactPhone" :whatsapp="page.contactWhatsapp" :website="page.websiteUrl" />
    </div>
  </header>
</template>

<style scoped>
.profile-header { display: grid; grid-template-columns: minmax(9rem, 18rem) 1fr; gap: var(--space-4); }
.profile-header__image, .profile-header__fallback { width: 100%; min-height: 11rem; border-radius: var(--radius-surface); background: var(--surface-sunken); object-fit: cover; }
.profile-header__fallback { align-content: center; color: var(--text-muted); font-size: 4rem; text-align: center; }
.profile-header__content, .profile-header__title { display: grid; gap: var(--space-2); }.profile-header__title { grid-template-columns: 1fr auto; }.profile-header p, h1 { margin: 0; }.eyebrow { color: var(--text-soft); font-size: var(--text-size-label); text-transform: uppercase; }.rating { color: var(--text-muted); } button { align-self: start; border: 1px solid var(--border-subtle); border-radius: var(--radius-control); background: var(--surface-base); padding: var(--space-1) var(--space-2); font: inherit; }
@media (max-width: 640px) { .profile-header { grid-template-columns: 1fr; }.profile-header__image, .profile-header__fallback { min-height: 9rem; }.profile-header__title { grid-template-columns: 1fr; } }
</style>
