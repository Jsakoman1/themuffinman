<script setup lang="ts">
withDefaults(defineProps<{
  tag?: "article" | "button" | "div"
  clickable?: boolean
}>(), {
  tag: "article",
  clickable: false,
})
</script>

<template>
  <component :is="tag" :class="['ui-list-item', { 'ui-list-item--clickable': clickable }]">
    <div v-if="$slots.header || $slots.badges" class="ui-list-item__top">
      <div v-if="$slots.header" class="ui-list-item__header">
        <slot name="header" />
      </div>
      <div v-if="$slots.badges" class="ui-list-item__badges">
        <slot name="badges" />
      </div>
    </div>

    <div v-if="$slots.meta" class="ui-list-item__meta">
      <slot name="meta" />
    </div>

    <div v-if="$slots.default" class="ui-list-item__body">
      <slot />
    </div>

    <div v-if="$slots.actions" class="ui-list-item__actions">
      <slot name="actions" />
    </div>
  </component>
</template>
