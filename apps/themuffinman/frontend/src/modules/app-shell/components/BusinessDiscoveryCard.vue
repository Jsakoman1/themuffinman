<script setup lang="ts">
import {RouterLink, type RouteLocationRaw} from "vue-router"
import type {BusinessProfileResponseDTO} from "../../../contracts/index.ts"

defineProps<{
  business: BusinessProfileResponseDTO
  saved: boolean
  to: RouteLocationRaw
}>()
</script>

<template>
  <article class="business-discovery-card" data-services-provider-card>
    <RouterLink :to="to" class="business-discovery-card__main" :aria-label="`View services from ${business.businessName}`">
      <img v-if="business.heroImageUrl" class="business-discovery-card__image" :src="business.heroImageUrl" :alt="`${business.businessName} cover`" />
      <div v-else class="business-discovery-card__fallback" aria-hidden="true">{{ business.businessName.slice(0, 1) }}</div>
      <div class="business-discovery-card__copy">
        <div class="business-discovery-card__title-row">
          <h2>{{ business.businessName }}</h2>
          <span v-if="saved" class="business-discovery-card__saved">Saved</span>
        </div>
        <p>{{ business.headline || business.description || "View this business's services and booking options." }}</p>
        <span v-if="business.publicAddressLabel" class="business-discovery-card__area">{{ business.publicAddressLabel }}</span>
      </div>
      <span class="business-discovery-card__action">{{ business.bookingEnabled ? "View services" : "View business" }} <span aria-hidden="true">→</span></span>
    </RouterLink>
    <p class="business-discovery-card__availability">{{ business.bookingEnabled ? "Appointments can be requested online" : "Business information is available" }}</p>
  </article>
</template>

<style scoped>
.business-discovery-card{display:grid;gap:var(--space-2);min-width:0;padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}
.business-discovery-card__main{display:grid;grid-template-columns:5rem minmax(0,1fr) auto;align-items:center;gap:var(--space-3);min-width:0;color:inherit;text-decoration:none}
.business-discovery-card__main:focus-visible{outline:var(--focus-ring);outline-offset:var(--space-1);border-radius:var(--radius-control)}
.business-discovery-card__main:hover .business-discovery-card__action{color:var(--accent)}
.business-discovery-card__image,.business-discovery-card__fallback{width:5rem;height:5rem;border-radius:var(--radius-control);object-fit:cover;background:var(--surface-sunken)}
.business-discovery-card__fallback{display:grid;place-items:center;color:var(--text-soft);font-size:var(--text-size-title);font-weight:var(--text-weight-semibold)}
.business-discovery-card__copy{display:grid;gap:var(--space-1);min-width:0}
.business-discovery-card__title-row{display:flex;align-items:center;gap:var(--space-2);min-width:0}
.business-discovery-card__title-row h2{margin:0;overflow:hidden;color:var(--text);font-size:var(--text-size-title);letter-spacing:var(--tracking-tight);text-overflow:ellipsis;white-space:nowrap}
.business-discovery-card__copy p{display:-webkit-box;overflow:hidden;margin:0;color:var(--text-muted);font-size:var(--text-size-body);line-height:1.4;-webkit-box-orient:vertical;-webkit-line-clamp:2}
.business-discovery-card__area,.business-discovery-card__availability{color:var(--text-soft);font-size:var(--text-size-meta)}
.business-discovery-card__saved{padding:.1rem var(--space-1);border:1px solid var(--border-subtle);border-radius:var(--radius-control);color:var(--text-muted);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold)}
.business-discovery-card__action{color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);white-space:nowrap}
.business-discovery-card__availability{margin:0;padding-left:calc(5rem + var(--space-3))}
@media(max-width:640px){.business-discovery-card__main{grid-template-columns:4rem minmax(0,1fr)}.business-discovery-card__image,.business-discovery-card__fallback{width:4rem;height:4rem}.business-discovery-card__action{grid-column:2}.business-discovery-card__availability{padding-left:0}}
</style>
