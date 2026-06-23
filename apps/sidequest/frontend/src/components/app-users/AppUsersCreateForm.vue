<script setup lang="ts">
import {appUserRoleOptions, type AppUserRole} from "../../shared/sidequestDomain.ts"

defineProps<{
  email: string
  username: string
  password: string
  role: AppUserRole
}>()

defineEmits<{
  (event: "update:email", value: string): void
  (event: "update:username", value: string): void
  (event: "update:password", value: string): void
  (event: "update:role", value: AppUserRole): void
  (event: "submit"): void
}>()
</script>

<template>
  <form class="stack" @submit.prevent="$emit('submit')">
    <div class="grid grid--two">
      <label class="field">
        <span class="label">Email</span>
        <input :value="email" class="input" @input="$emit('update:email', ($event.target as HTMLInputElement).value)" />
      </label>

      <label class="field">
        <span class="label">Username</span>
        <input :value="username" class="input" @input="$emit('update:username', ($event.target as HTMLInputElement).value)" />
      </label>

      <label class="field">
        <span class="label">Password</span>
        <input
          :value="password"
          class="input"
          type="password"
          @input="$emit('update:password', ($event.target as HTMLInputElement).value)"
        />
      </label>

      <label class="field">
        <span class="label">Role</span>
        <select :value="role" class="input" @change="$emit('update:role', ($event.target as HTMLSelectElement).value as AppUserRole)">
          <option v-for="option in appUserRoleOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </label>
    </div>

    <div class="button-row">
      <button class="button" type="submit">Create user</button>
    </div>
  </form>
</template>
