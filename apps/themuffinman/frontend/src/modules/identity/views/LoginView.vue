<script setup lang="ts">
import {ref} from "vue"
import {useRouter} from "vue-router"
import {authApi} from "../api/authApi.ts"
import {loginUser} from "../auth.ts"
import AppFormField from "../../app-shell/components/AppFormField.vue"
import AppFormFooter from "../../app-shell/components/AppFormFooter.vue"
import AppStatus from "../../app-shell/components/AppStatus.vue"

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
  <main class="auth-terminal">
    <section class="auth-terminal__panel">
      <p class="auth-terminal__eyebrow">TheMuffinMan / access</p>
      <h1 class="auth-terminal__title">Login</h1>
      <p class="auth-terminal__copy">Enter your account details to open the Vision surface.</p>

      <form class="auth-terminal__form" @submit.prevent="login"><AppFormField label="Email" required><input v-model="email" aria-label="Email" class="auth-terminal__input" type="email" autocomplete="email" required /></AppFormField><AppFormField label="Password" required><input v-model="password" aria-label="Password" class="auth-terminal__input" type="password" autocomplete="current-password" required /></AppFormField><AppStatus v-if="error" :message="error" tone="error" /><AppFormFooter><template #primary><button type="submit">Enter</button></template></AppFormFooter></form>

      <p class="auth-terminal__linkline">
        Need an account?
        <RouterLink to="/register">Register</RouterLink>
      </p>
      <p class="auth-terminal__linkline"><RouterLink to="/recover">Forgot your password?</RouterLink></p>
    </section>
  </main>
</template>

<style scoped>
.auth-terminal {min-height:100vh;display:grid;place-items:center;padding:var(--space-4);background:var(--canvas);color:var(--text)}

.auth-terminal__panel {
  width:min(100%,31rem);display:grid;gap:var(--space-3);padding:var(--space-5);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)
}

.auth-terminal__eyebrow {
  margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase
}

.auth-terminal__title {
  margin:0;color:var(--text);font-size:var(--text-size-page-title);line-height:1;letter-spacing:var(--tracking-tight)
}

.auth-terminal__copy,
.auth-terminal__linkline,
.auth-terminal__error {
  margin:0;line-height:1.6
}

.auth-terminal__copy,
.auth-terminal__linkline {
  color:var(--text-muted)
}

.auth-terminal__form {
  display:grid;gap:var(--space-3);margin-top:var(--space-1)
}

.auth-terminal__input{width:100%;box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}.auth-terminal__linkline a{color:var(--accent);font-weight:var(--text-weight-semibold);text-decoration:none}
</style>
