<script setup lang="ts">
import {ref} from "vue"
import {useRouter} from "vue-router"
import AppBrand from "../../../components/app/AppBrand.vue"
import UiAuthCard from "../../../components/ui/UiAuthCard.vue"
import UiFieldGroup from "../../../components/ui/UiFieldGroup.vue"
import {authApi} from "../api/authApi.ts"
import {loginUser} from "../auth.ts"

const email = ref("")
const username = ref("")
const password = ref("")
const error = ref("")
const router = useRouter()

const register = async () => {
  error.value = ""
  try {
    const normalizedEmail = email.value.trim().toLowerCase()
    const response = await authApi.register({
      email: normalizedEmail,
      username: username.value,
      password: password.value
    })
    loginUser(response)

    email.value = ""
    username.value = ""
    password.value = ""
    await router.push("/work")
  } catch {
    error.value = "Registration failed"
  }
}
</script>

<template>
  <div class="auth-layout">
    <UiAuthCard title="Register">
      <template #brand>
        <AppBrand class="auth-brand__mark" to="/" />
        <p class="auth-brand__subtitle">Start once, use every module.</p>
      </template>

      <form class="form-stack" @submit.prevent="register">
        <UiFieldGroup label="Email">
          <input v-model="email" class="input" />
        </UiFieldGroup>

        <UiFieldGroup label="Username">
          <input v-model="username" class="input" />
        </UiFieldGroup>

        <UiFieldGroup label="Password">
          <input v-model="password" class="input" type="password" />
        </UiFieldGroup>

        <button class="button" type="submit">Register</button>
      </form>

      <div v-if="error" class="alert alert--error mt-4">{{ error }}</div>

      <template #footer>
        Already have an account? <RouterLink to="/login">Login here</RouterLink>
      </template>
    </UiAuthCard>
  </div>
</template>
