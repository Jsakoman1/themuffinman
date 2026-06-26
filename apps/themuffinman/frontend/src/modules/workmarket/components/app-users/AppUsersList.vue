<script setup lang="ts">
import UiFormActions from "../../../../components/ui/UiFormActions.vue"
import type {AppUser} from "../../api/workmarketApi.ts"

defineProps<{
  users: AppUser[]
}>()

defineEmits<{
  (event: "open", user: AppUser): void
  (event: "delete", id: number): void
}>()
</script>

<template>
  <table class="admin-table admin-table--compact">
    <thead>
      <tr>
        <th>ID</th>
        <th>User</th>
        <th>Email</th>
        <th>Role</th>
        <th>Created</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="user in users" :key="user.id">
        <td>{{ user.id }}</td>
        <td>
          <strong>{{ user.username }}</strong>
        </td>
        <td>{{ user.email }}</td>
        <td><span class="badge badge--accent">{{ user.role }}</span></td>
        <td>{{ user.createdAt ? new Date(user.createdAt).toLocaleDateString() : "-" }}</td>
        <td>
          <UiFormActions>
            <button class="button button--icon button--icon-compact button--secondary" type="button" aria-label="Open user admin details" @click="$emit('open', user)">✎</button>
            <button class="button button--icon button--danger" type="button" aria-label="Delete user" @click="$emit('delete', user.id)">×</button>
          </UiFormActions>
        </td>
      </tr>
    </tbody>
  </table>
</template>
