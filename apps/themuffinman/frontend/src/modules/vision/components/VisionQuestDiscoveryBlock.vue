<script setup lang="ts">
import type {VisionCanvasBlock} from "../api/visionConversationApi.ts"
import VisionCanvasSection from "./VisionCanvasSection.vue"

defineProps<{
  block: VisionCanvasBlock
}>()
</script>

<template>
  <VisionCanvasSection
    :title="block.title"
    :body="block.body"
    tone="info"
  >
    <div v-if="block.questDiscovery" class="vision-discovery">
      <div class="vision-discovery__meta">
        <span class="vision-discovery__pill">{{ block.questDiscovery.sort }}</span>
        <span class="vision-discovery__count">{{ block.questDiscovery.totalItems }} open quests</span>
        <span v-if="block.questDiscovery.query" class="vision-discovery__query">Query: {{ block.questDiscovery.query }}</span>
      </div>
      <div v-if="block.questDiscovery.items.length" class="vision-discovery__grid">
        <article
          v-for="item in block.questDiscovery.items"
          :key="item.questId"
          class="vision-discovery__card"
        >
          <div class="vision-discovery__card-head">
            <span class="vision-discovery__rank">#{{ item.rank }}</span>
            <span class="vision-discovery__status">{{ item.statusLabel }}</span>
          </div>
          <h3 class="vision-discovery__title">{{ item.title }}</h3>
          <p class="vision-discovery__body">{{ item.description }}</p>
          <dl class="vision-discovery__facts">
            <div>
              <dt>Creator</dt>
              <dd>{{ item.creatorUsername }}</dd>
            </div>
            <div>
              <dt>Reward</dt>
              <dd>{{ item.rewardLabel }}</dd>
            </div>
            <div>
              <dt>Location</dt>
              <dd>{{ item.locationLabel || "Not set" }}</dd>
            </div>
            <div>
              <dt>Match</dt>
              <dd>{{ item.matchSummary }}</dd>
            </div>
          </dl>
        </article>
      </div>
      <p v-else class="vision-discovery__empty">
        No matching open quests were found.
      </p>
    </div>
  </VisionCanvasSection>
</template>

<style scoped>
.vision-discovery {
  display: grid;
  gap: 0.8rem;
}

.vision-discovery__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  align-items: center;
}

.vision-discovery__pill,
.vision-discovery__status,
.vision-discovery__rank {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 0.32rem 0.64rem;
  border: 1px solid rgba(24, 36, 47, 0.08);
  background: rgba(255, 255, 255, 0.84);
  color: var(--vision-surface-ink-soft);
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.vision-discovery__count,
.vision-discovery__query {
  color: var(--vision-surface-ink-muted);
  font-size: 0.88rem;
}

.vision-discovery__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(14rem, 1fr));
  gap: 0.8rem;
}

.vision-discovery__card {
  display: grid;
  gap: 0.55rem;
  padding: 0.95rem 1rem;
  border-radius: 1.2rem;
  border: 1px solid rgba(24, 36, 47, 0.08);
  background: rgba(255, 255, 255, 0.82);
}

.vision-discovery__card-head {
  display: flex;
  justify-content: space-between;
  gap: 0.5rem;
  align-items: center;
}

.vision-discovery__title {
  margin: 0;
  font-size: 1rem;
  letter-spacing: -0.02em;
}

.vision-discovery__body {
  margin: 0;
  color: var(--vision-surface-ink-soft);
  line-height: 1.5;
}

.vision-discovery__facts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(7rem, 1fr));
  gap: 0.45rem 0.75rem;
  margin: 0;
}

.vision-discovery__facts dt {
  font-size: 0.68rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: var(--vision-surface-ink-muted);
}

.vision-discovery__facts dd {
  margin: 0.28rem 0 0;
  color: var(--vision-surface-ink);
}

.vision-discovery__empty {
  margin: 0;
  color: var(--vision-surface-ink-soft);
}
</style>
