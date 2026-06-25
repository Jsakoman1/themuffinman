<script setup lang="ts">
import {reactive, watch} from "vue"
import UiFormActions from "../../../../components/ui/UiFormActions.vue"
import DashboardEditSheet from "../dashboard/DashboardEditSheet.vue"
import type {AppUser, AppUserRoleOption} from "../../api/workmarketApi.ts"
import type {AppUserRole} from "../../domain/workmarketDomain.ts"
import ProfileEntityCard from "../../../../components/profile/ProfileEntityCard.vue"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"
import InlineEditableField from "../shared/InlineEditableField.vue"

const props = defineProps<{
  users: AppUser[]
  editingUserId: number | null
  editEmail: string
  editUsername: string
  editRole: AppUserRole
  editPassword: string
  roleOptions: AppUserRoleOption[]
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

const editingFields = reactive({
  email: false,
  username: false,
  role: false,
  password: false
})

watch(() => props.editingUserId, () => {
  editingFields.email = false
  editingFields.username = false
  editingFields.role = false
  editingFields.password = false
})
</script>

<template>
  <div class="quest-list">
    <article v-for="user in users" :key="user.id" class="card">
        <div v-if="editingUserId !== user.id" class="split-actions">
          <ProfileEntityCard
            :username="user.username"
            :avatar-data-url="user.profileAvatarDataUrl"
            :meta="user.email"
            :description="user.profileDescription"
            :size="56"
            :link-to="routeForNavigationTarget(user.profileNavigation)"
          >
            <template #badge>
              <div class="badge badge--accent">{{ user.role }}</div>
            </template>
          </ProfileEntityCard>

          <UiFormActions>
            <button class="button button--icon button--icon-compact button--secondary" type="button" aria-label="Edit user" @click="$emit('edit', user)">✎</button>
            <button class="button button--icon button--danger" type="button" aria-label="Delete user" @click="$emit('delete', user.id)">×</button>
          </UiFormActions>
        </div>

      <form v-else class="form-stack" @submit.prevent="$emit('save')">
        <DashboardEditSheet
          :minimal="true"
        >
          <div class="ui-edit-form ui-edit-form--user-admin">
            <InlineEditableField label="Email" :editing="editingFields.email" field-class="ui-edit-field--message" @toggle="editingFields.email = !editingFields.email">
              <template #editor>
                <input
                  :value="editEmail"
                  class="input"
                  @input="$emit('update:editEmail', ($event.target as HTMLInputElement).value)"
                />
              </template>
              <template #display>
                <div class="ui-inline-readonly-text">{{ editEmail || "-" }}</div>
              </template>
            </InlineEditableField>

            <InlineEditableField label="Username" :editing="editingFields.username" field-class="ui-edit-field--price" @toggle="editingFields.username = !editingFields.username">
              <template #editor>
                <input
                  :value="editUsername"
                  class="input"
                  @input="$emit('update:editUsername', ($event.target as HTMLInputElement).value)"
                />
              </template>
              <template #display>
                <div class="ui-inline-readonly-text">{{ editUsername || "-" }}</div>
              </template>
            </InlineEditableField>

            <InlineEditableField label="Role" :editing="editingFields.role" field-class="ui-edit-field--price" @toggle="editingFields.role = !editingFields.role">
              <template #editor>
                <select
                  :value="editRole"
                  class="input"
                  @change="$emit('update:editRole', ($event.target as HTMLSelectElement).value as AppUserRole)"
                >
                  <option v-for="option in roleOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </template>
              <template #display>
                <div class="ui-inline-readonly-text">{{ roleOptions.find((option) => option.value === editRole)?.label ?? editRole }}</div>
              </template>
            </InlineEditableField>

            <InlineEditableField label="Reset password" :editing="editingFields.password" field-class="ui-edit-field--message" @toggle="editingFields.password = !editingFields.password">
              <template #editor>
                <input
                  :value="editPassword"
                  class="input"
                  type="password"
                  placeholder="Leave blank to keep current password"
                  @input="$emit('update:editPassword', ($event.target as HTMLInputElement).value)"
                />
              </template>
              <template #display>
                <div class="ui-inline-readonly-text">{{ editPassword ? "New password ready" : "Leave unchanged" }}</div>
              </template>
            </InlineEditableField>
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
