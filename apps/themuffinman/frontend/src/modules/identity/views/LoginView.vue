<script setup lang="ts">
import {ref} from "vue"
import {useRouter} from "vue-router"
import AppBrand from "../../../components/app/AppBrand.vue"
import UiAuthCard from "../../../components/ui/UiAuthCard.vue"
import UiFieldGroup from "../../../components/ui/UiFieldGroup.vue"
import {authApi} from "../api/authApi.ts"
import {loginUser} from "../auth.ts"

const email = ref("")
const password = ref("")
const error = ref("")
const router = useRouter()

const login = async () => {
  error.value = ""
  try {
    const normalizedEmail = email.value.trim().toLowerCase()
    const response = await authApi.login({
      email: normalizedEmail,
      password: password.value
    })
    loginUser(response)
    email.value = ""
    password.value = ""
    await router.push(response.role === "ADMIN" ? "/admin/work" : "/work")
  } catch {
    error.value = "Invalid email or password"
  }
}
</script>

<template>
  <div class="auth-layout">
    <UiAuthCard title="Login">
      <template #brand>
        <AppBrand class="auth-brand__mark" to="/" />
        <p class="auth-brand__subtitle">One account, multiple everyday modules.</p>
      </template>

      <form class="form-stack" @submit.prevent="login">
        <UiFieldGroup label="Email">
          <input v-model="email" class="input" />
        </UiFieldGroup>

        <UiFieldGroup label="Password">
          <input v-model="password" class="input" type="password" />
        </UiFieldGroup>

        <button class="button" type="submit">Login</button>
      </form>

      <div v-if="error" class="alert alert--error mt-4">{{ error }}</div>

      <template #footer>
        Need an account? <RouterLink to="/register">Register here</RouterLink>
      </template>
    </UiAuthCard>
  </div>
</template>
