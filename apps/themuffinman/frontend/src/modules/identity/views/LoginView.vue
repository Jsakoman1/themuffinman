<script setup lang="ts">
import {ref} from "vue"
import {useRouter} from "vue-router"
import AppBrand from "../../../components/app/AppBrand.vue"
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
    await router.push("/vision")
  } catch {
    error.value = "Invalid email or password"
  }
}
</script>

<template>
  <div class="auth-layout">
    <div class="card auth-card">
      <div class="auth-brand">
        <AppBrand class="auth-brand__mark" to="/" />
        <p class="auth-brand__subtitle">One account, multiple everyday modules.</p>
      </div>

      <div class="surface-stack">
        <div class="surface-stack">
          <h1 class="auth-card__title">Login</h1>
        </div>

        <form class="form-stack" @submit.prevent="login">
          <label class="field">
            <span class="label">Email</span>
            <input v-model="email" class="input" />
          </label>

          <label class="field">
            <span class="label">Password</span>
            <input v-model="password" class="input" type="password" />
          </label>

          <button class="button" type="submit">Login</button>
        </form>

        <div v-if="error" class="alert alert--error mt-4">{{ error }}</div>
        <div class="auth-footer">
          Need an account? <RouterLink to="/register">Register here</RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>
