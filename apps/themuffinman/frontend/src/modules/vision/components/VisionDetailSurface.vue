<script setup lang="ts">
withDefaults(defineProps<{
  eyebrow?: string | null
  title: string
  subtitle?: string | null
}>(), {
  eyebrow: "Vision detail",
  subtitle: null
})

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <section class="vision-detail-surface">
    <header class="vision-detail-surface__header">
      <div class="vision-detail-surface__heading">
        <p v-if="eyebrow" class="vision-detail-surface__eyebrow">{{ eyebrow }}</p>
        <h1>{{ title }}</h1>
        <p v-if="subtitle" class="vision-detail-surface__subtitle">{{ subtitle }}</p>
      </div>

      <button type="button" class="vision-detail-surface__close" @click="emit('close')">
        > back
      </button>
    </header>

    <div class="vision-detail-surface__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.vision-detail-surface {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at top, rgba(255, 250, 242, 0.96), transparent 38%),
    linear-gradient(180deg, #ffffff 0%, #fffdf9 100%);
  color: var(--vision-surface-ink);
  display: grid;
  justify-items: center;
  align-content: start;
  padding: clamp(1.25rem, 3vw, 2.25rem) 1.25rem 3rem;
}

.vision-detail-surface__header,
.vision-detail-surface__body {
  position: relative;
  z-index: 1;
}

.vision-detail-surface__header {
  width: min(72rem, 100%);
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.vision-detail-surface__heading {
  display: grid;
  gap: 0.3rem;
  max-width: 44rem;
}

.vision-detail-surface__eyebrow {
  margin: 0;
  font-size: 0.72rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
}

.vision-detail-surface__heading h1 {
  margin: 0;
  font-size: clamp(1.45rem, 2.4vw, 2.2rem);
  letter-spacing: -0.04em;
  line-height: 1.02;
}

.vision-detail-surface__subtitle {
  margin: 0;
  max-width: 42rem;
  color: var(--vision-surface-ink-soft);
  line-height: 1.5;
}

.vision-detail-surface__close {
  appearance: none;
  border: 0;
  background: transparent;
  color: var(--vision-surface-ink);
  border-radius: 0;
  padding: 0;
  font: inherit;
  cursor: pointer;
  box-shadow: none;
  color: rgba(24, 36, 47, 0.68);
  font-size: 0.78rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.vision-detail-surface__body {
  width: min(68rem, 100%);
  display: grid;
  gap: 1.1rem;
}

@media (max-width: 720px) {
  .vision-detail-surface {
    padding-inline: 0.8rem;
  }

  .vision-detail-surface__header {
    flex-direction: column;
  }

  .vision-detail-surface__close {
    width: 100%;
  }
}
</style>
