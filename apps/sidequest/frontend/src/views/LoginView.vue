<script setup lang="ts">
import {ref} from "vue";
import {loginUser} from "../auth.ts";
import {useRouter} from "vue-router";
import {authApi} from "../api/authApi.ts";

const email = ref('')
const password = ref('')
const error = ref('')
const router = useRouter()

const login = async () => {
  error.value = ''
  try {
    const normalizedEmail = email.value.trim().toLowerCase()
    const response = await authApi.login({
      email: normalizedEmail,
      password: password.value
    })
    loginUser(response)
    email.value = ''
    password.value = ''
    await router.push(response.role === 'ADMIN' ? '/admin/quests' : '/quests')
  } catch (e) {
    error.value = 'Invalid email or password'
  }
}
</script>

<template>
  <div class="auth-layout">
    <div class="card auth-card">
      <div class="auth-brand">
        <div class="brand brand--logo auth-brand__mark">
          <div class="brand__mark" aria-hidden="true">
            <span></span>
            <span></span>
          </div>
          <div class="brand__title">SideQuest</div>
        </div>
        <p class="auth-brand__subtitle">Simple work, clear flow.</p>
      </div>

      <h1 class="auth-card__title">Login</h1>
      <p class="auth-card__subtitle">Use your account to continue.</p>

      <form class="stack" @submit.prevent="login">
        <label class="field">
          <span class="label">Email</span>
          <input v-model="email" class="input"/>
        </label>

        <label class="field">
          <span class="label">Password</span>
          <input v-model="password" class="input" type="password"/>
        </label>

        <button class="button" type="submit">Login</button>
      </form>

      <div v-if="error" class="alert alert--error mt-4">{{ error }}</div>

      <div class="auth-footer">
        Need an account? <RouterLink to="/register">Register here</RouterLink>
      </div>
    </div>
  </div>
</template>
