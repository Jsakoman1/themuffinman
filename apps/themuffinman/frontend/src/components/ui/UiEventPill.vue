<script setup lang="ts">
withDefaults(defineProps<{
  time: string
  title: string
  tone?: "incoming" | "outgoing"
  month?: boolean
  dialog?: boolean
  muted?: boolean
  range?: boolean
}>(), {
  tone: "outgoing",
  month: false,
  dialog: false,
  muted: false,
  range: false,
})
</script>

<template>
  <div
    :class="[
      'ui-event-pill',
      `ui-event-pill--${tone}`,
      {
        'ui-event-pill--month': month,
        'ui-event-pill--dialog': dialog,
        'ui-event-pill--muted': muted,
        'ui-event-pill--range': range,
      },
    ]"
  >
    <span class="ui-event-pill__time">{{ time }}</span>
    <span class="ui-event-pill__title">{{ title }}</span>
  </div>
</template>
