<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"
import ProfileAvatar from "./ProfileAvatar.vue"
import ProfileBio from "./ProfileBio.vue"

withDefaults(defineProps<{
  username: string
  avatarDataUrl?: string | null
  meta?: string
  description?: string | null
  descriptionPlaceholder?: string
  size?: number
  linkTo?: RouteLocationRaw | null
}>(), {
  avatarDataUrl: null,
  meta: "",
  description: "",
  descriptionPlaceholder: "No profile description.",
  size: 56,
  linkTo: null,
})

const emit = defineEmits<{
  (event: "open"): void
}>()
</script>

<template>
  <article class="profile-entity-card">
    <div class="profile-entity-card__top">
      <RouterLink v-if="linkTo" class="profile-link" :to="linkTo">
        <ProfileAvatar :username="username" :avatar-data-url="avatarDataUrl" :size="size" />
        <div class="stack profile-entity-card__identity">
          <strong>{{ username }}</strong>
          <div v-if="meta" class="muted">{{ meta }}</div>
        </div>
      </RouterLink>
      <button v-else class="profile-link profile-link--button" type="button" @click="emit('open')">
        <ProfileAvatar :username="username" :avatar-data-url="avatarDataUrl" :size="size" />
        <div class="stack profile-entity-card__identity">
          <strong>{{ username }}</strong>
          <div v-if="meta" class="muted">{{ meta }}</div>
        </div>
      </button>

      <div v-if="$slots.badge" class="profile-entity-card__badge">
        <slot name="badge" />
      </div>
    </div>

    <ProfileBio v-if="description || descriptionPlaceholder" :text="description" :placeholder="descriptionPlaceholder" />

    <div v-if="$slots.default" class="profile-entity-card__body">
      <slot />
    </div>

    <div v-if="$slots.actions" class="profile-entity-card__actions" @click.stop>
      <slot name="actions" />
    </div>
  </article>
</template>
