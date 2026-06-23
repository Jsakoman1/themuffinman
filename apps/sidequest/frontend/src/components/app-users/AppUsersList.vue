<script setup lang="ts">
import {RouterLink} from "vue-router"
import DashboardEditSheet from "../dashboard/DashboardEditSheet.vue"
import type {AppUser} from "../../api/sidequestApi.ts"
import {appUserRoleOptions, type AppUserRole} from "../../shared/sidequestDomain.ts"
import ProfileAvatar from "../profile/ProfileAvatar.vue"
import ProfileBio from "../profile/ProfileBio.vue"

defineProps<{
  users: AppUser[]
  editingUserId: number | null
  editEmail: string
  editUsername: string
  editRole: AppUserRole
  editPassword: string
}>()

defineEmits<{
  (event: "edit", user: AppUser): void
  (event: "delete", id: number): void
  (event: "save"): void
  (event: "cancel"): void
  (event: "update:editEmail", value: string): void
  (event: "update:editUsername", value: string): void
  (event: "update:editRole", value: AppUserRole): void
  (event: "update:editPassword", value: string): void
}>()
</script>

<template>
  <div class="quest-list">
    <article v-for="user in users" :key="user.id" class="card">
      <div v-if="editingUserId !== user.id" class="split-actions">
        <div class="profile-card__identity">
          <RouterLink class="profile-link" :to="`/users/${user.id}`">
            <ProfileAvatar
              :username="user.username"
              :avatar-data-url="user.profileAvatarDataUrl"
              :size="56"
            />
            <strong>{{ user.username }}</strong>
          </RouterLink>
          <div class="stack">
            <div class="muted mt-1">{{ user.email }}</div>
            <div class="badge badge--accent mt-2">{{ user.role }}</div>
            <ProfileBio :text="user.profileDescription" />
          </div>
        </div>

        <div class="button-row">
          <button class="button button--icon button--secondary" type="button" aria-label="Edit user" @click="$emit('edit', user)">✎</button>
          <button class="button button--icon button--danger" type="button" aria-label="Delete user" @click="$emit('delete', user.id)">×</button>
        </div>
      </div>

      <form v-else @submit.prevent="$emit('save')">
        <DashboardEditSheet
          :minimal="true"
        >
          <div class="dashboard-edit-form dashboard-edit-form--user-admin">
            <label class="field dashboard-edit-field dashboard-edit-field--message">
              <span class="label">Email</span>
              <input
                :value="editEmail"
                class="input"
                @input="$emit('update:editEmail', ($event.target as HTMLInputElement).value)"
              />
            </label>

            <label class="field dashboard-edit-field dashboard-edit-field--price">
              <span class="label">Username</span>
              <input
                :value="editUsername"
                class="input"
                @input="$emit('update:editUsername', ($event.target as HTMLInputElement).value)"
              />
            </label>

            <label class="field dashboard-edit-field dashboard-edit-field--price">
              <span class="label">Role</span>
              <select
                :value="editRole"
                class="input"
                @change="$emit('update:editRole', ($event.target as HTMLSelectElement).value as AppUserRole)"
              >
                <option v-for="option in appUserRoleOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>

            <label class="field dashboard-edit-field dashboard-edit-field--message">
              <span class="label">Reset password</span>
              <input
                :value="editPassword"
                class="input"
                type="password"
                placeholder="Leave blank to keep current password"
                @input="$emit('update:editPassword', ($event.target as HTMLInputElement).value)"
              />
            </label>
          </div>

          <template #actions>
            <button class="button button--action" type="submit">Save changes</button>
            <button class="button button--ghost" type="button" @click="$emit('cancel')">Discard changes</button>
          </template>
        </DashboardEditSheet>
      </form>
    </article>
  </div>
</template>
