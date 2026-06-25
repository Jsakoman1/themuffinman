<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"

defineProps<{
  reviewerUsername: string
  stars: number
  roleLabel: string
  questTitle: string
  questPath: RouteLocationRaw
  comment?: string | null
}>()
</script>

<template>
  <article class="ui-review-item">
    <div class="ui-inline-card-link__top">
      <div class="surface-stack">
        <strong>{{ reviewerUsername }}</strong>
        <div class="muted">{{ "★".repeat(stars) }}{{ "☆".repeat(5 - stars) }} · {{ roleLabel }}</div>
      </div>
      <RouterLink class="badge badge--accent" :to="questPath">
        {{ questTitle }}
      </RouterLink>
    </div>
    <p v-if="comment" class="ui-inline-card-link__description">{{ comment }}</p>
  </article>
</template>
