<script setup lang="ts">
import type {CircleCandidate} from "../../api/sidequestApi.ts"
import {circleRelationLabels} from "../../shared/sidequestDomain.ts"
import ProfileAvatar from "../profile/ProfileAvatar.vue"
import ProfileBio from "../profile/ProfileBio.vue"

defineProps<{
  user: CircleCandidate
  saving: boolean
}>()

defineEmits<{
  openProfile: [id: number]
  invite: [id: number]
  block: [id: number]
  unblock: [id: number]
}>()
</script>

<template>
  <article class="profile-open-quest circles-person-card">
    <div class="profile-open-quest__top">
      <button class="profile-link profile-link--button" type="button" @click="$emit('openProfile', user.id)">
        <ProfileAvatar :username="user.username" :avatar-data-url="user.profileAvatarDataUrl" :size="56" />
        <div class="stack circles-person-card__identity">
          <strong>{{ user.username }}</strong>
          <div class="muted">{{ user.email }}</div>
        </div>
      </button>
      <span
        class="badge"
        :class="{
          'badge--accent': user.relationStatus === 'NONE',
          'badge--warning': user.relationStatus === 'OUTGOING_REQUEST' || user.relationStatus === 'INCOMING_REQUEST',
          'badge--danger': user.relationStatus === 'BLOCKED'
        }"
      >
        {{ circleRelationLabels[user.relationStatus] }}
      </span>
    </div>

    <ProfileBio :text="user.profileDescription" placeholder="No profile description." />
    <div class="button-row circles-person-card__actions">
      <button v-if="user.relationStatus === 'NONE'" class="button" type="button" :disabled="saving" @click="$emit('invite', user.id)">Send invite</button>
      <button v-if="user.relationStatus === 'BLOCKED' && user.blockedByCurrentUser" class="button button--secondary" type="button" :disabled="saving" @click="$emit('unblock', user.id)">Unblock</button>
      <button v-else-if="user.relationStatus === 'BLOCKED'" class="button button--secondary" type="button" disabled>Blocked by them</button>
      <button v-else class="button button--secondary" type="button" :disabled="saving" @click="$emit('block', user.id)">Block</button>
    </div>
  </article>
</template>
