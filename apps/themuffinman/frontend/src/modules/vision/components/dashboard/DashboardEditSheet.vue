<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    subtitle?: string
    badge?: string
    minimal?: boolean
  }>(),
  {
    title: "",
    subtitle: "",
    badge: "",
    minimal: false,
  },
)
</script>

<template>
  <section :class="['ui-edit-sheet', { 'ui-edit-sheet--minimal': minimal }]">
    <div v-if="!minimal && (title || subtitle || badge)" class="ui-edit-sheet__header">
      <div class="ui-edit-sheet__heading">
        <h3 v-if="title" class="ui-edit-sheet__title">{{ title }}</h3>
        <p v-if="subtitle" class="ui-edit-sheet__subtitle">
          {{ subtitle }}
        </p>
      </div>

      <span v-if="badge" class="badge badge--accent">{{ badge }}</span>
    </div>

    <div class="ui-edit-sheet__body">
      <slot />
    </div>

    <div class="ui-edit-sheet__actions">
      <slot name="actions" />
    </div>
  </section>
</template>
