<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"
import ProfileBio from "./ProfileBio.vue"
import UiInlineCardLink from "../ui/UiInlineCardLink.vue"

withDefaults(defineProps<{
  title: string
  meta: string
  statusClass: string
  statusLabel: string
  description?: string | null
  actionLabel?: string
  actionPath: RouteLocationRaw
  interactive?: boolean
}>(), {
  description: "",
  actionLabel: "",
  interactive: false,
})

defineEmits<{
  (event: "open"): void
}>()
</script>

<template>
  <UiInlineCardLink tag="article" :interactive="interactive" @click="$emit('open')">
    <div class="ui-inline-card-link__top">
      <div class="surface-stack">
        <strong>{{ title }}</strong>
        <div class="muted">{{ meta }}</div>
      </div>
      <span :class="statusClass">{{ statusLabel }}</span>
    </div>
    <ProfileBio v-if="description" class="ui-inline-card-link__description" :text="description" />
    <div class="button-row">
      <RouterLink class="button button--secondary" :to="actionPath">
        {{ actionLabel ?? "View job" }}
      </RouterLink>
    </div>
  </UiInlineCardLink>
</template>
