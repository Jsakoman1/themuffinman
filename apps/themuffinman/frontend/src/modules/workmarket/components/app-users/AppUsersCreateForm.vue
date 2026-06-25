<script setup lang="ts">
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import UiFormActions from "../../../../components/ui/UiFormActions.vue"
import type {AppUserRole} from "../../domain/workmarketDomain.ts"
import type {AppUserRoleOption} from "../../api/workmarketApi.ts"

defineProps<{
  email: string
  username: string
  password: string
  role: AppUserRole
  roleOptions: AppUserRoleOption[]
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
  <form class="form-stack" @submit.prevent="$emit('submit')">
    <div class="grid grid--two">
      <UiFieldGroup label="Email">
        <input :value="email" class="input" @input="$emit('update:email', ($event.target as HTMLInputElement).value)" />
      </UiFieldGroup>

      <UiFieldGroup label="Username">
        <input :value="username" class="input" @input="$emit('update:username', ($event.target as HTMLInputElement).value)" />
      </UiFieldGroup>

      <UiFieldGroup label="Password">
        <input
          :value="password"
          class="input"
          type="password"
          @input="$emit('update:password', ($event.target as HTMLInputElement).value)"
        />
      </UiFieldGroup>

      <UiFieldGroup label="Role">
        <select :value="role" class="input" @change="$emit('update:role', ($event.target as HTMLSelectElement).value as AppUserRole)">
          <option v-for="option in roleOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </UiFieldGroup>
    </div>

    <UiFormActions>
      <button class="button" type="submit">Create user</button>
    </UiFormActions>
  </form>
</template>
