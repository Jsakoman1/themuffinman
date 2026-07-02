<script setup lang="ts">
import {ref} from "vue"
import {useRouter} from "vue-router"
import AppBrand from "../../../components/app/AppBrand.vue"
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
    await router.push("/vision")
  } catch {
    error.value = "Registration failed"
  }
}
</script>

<template>
  <div class="auth-layout">
    <div class="card auth-card">
      <div class="auth-brand">
        <AppBrand class="auth-brand__mark" to="/" />
        <p class="auth-brand__subtitle">Start once, use every module.</p>
      </div>

      <div class="surface-stack">
        <div class="surface-stack">
          <h1 class="auth-card__title">Register</h1>
        </div>

        <form class="form-stack" @submit.prevent="register">
          <label class="field">
            <span class="label">Email</span>
            <input v-model="email" class="input" />
          </label>

          <label class="field">
            <span class="label">Username</span>
            <input v-model="username" class="input" />
          </label>

          <label class="field">
            <span class="label">Password</span>
            <input v-model="password" class="input" type="password" />
          </label>

          <button class="button" type="submit">Register</button>
        </form>

        <div v-if="error" class="alert alert--error mt-4">{{ error }}</div>
        <div class="auth-footer">
          Already have an account? <RouterLink to="/login">Login here</RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>
