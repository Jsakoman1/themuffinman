<script setup lang="ts">
withDefaults(defineProps<{title?: string; eyebrow?: string; summary?: string; tone?: "plain" | "accent" | "muted"}>(), {tone: "plain"})
</script>
<template>
  <section class="native-block" :class="`native-block--${tone}`">
    <header v-if="eyebrow || title || summary || $slots.actions" class="native-block__header">
      <div><p v-if="eyebrow" class="native-block__eyebrow">{{ eyebrow }}</p><h2 v-if="title">{{ title }}</h2><p v-if="summary" class="native-block__summary">{{ summary }}</p></div>
      <div v-if="$slots.actions" class="native-block__actions"><slot name="actions" /></div>
    </header>
    <div class="native-block__body"><slot /></div>
  </section>
</template>
<style scoped>.native-block{display:grid;gap:var(--space-2);min-width:0;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base);overflow:hidden}.native-block--accent{border-color:color-mix(in srgb,var(--accent) 30%,var(--border-subtle))}.native-block--muted{background:var(--surface-muted)}.native-block__header{display:flex;align-items:flex-start;justify-content:space-between;gap:var(--space-3);padding:var(--space-3) var(--space-3) 0}.native-block__header h2,.native-block__header p{margin:0}.native-block__header h2{font-size:var(--text-size-title);letter-spacing:var(--tracking-tight)}.native-block__eyebrow{margin-bottom:.2rem!important;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);text-transform:uppercase;letter-spacing:var(--tracking-label)}.native-block__summary{margin-top:.25rem!important;color:var(--text-muted);font-size:var(--text-size-meta)}.native-block__actions{display:flex;gap:var(--space-1);flex-wrap:wrap}.native-block__body{min-width:0}</style>
