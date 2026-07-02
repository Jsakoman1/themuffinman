<script setup lang="ts">
import {ref} from "vue"
import {useRouter} from "vue-router"
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
  <main class="auth-terminal">
    <section class="auth-terminal__panel">
      <p class="auth-terminal__eyebrow">TheMuffinMan / access</p>
      <h1 class="auth-terminal__title">Login</h1>
      <p class="auth-terminal__copy">Enter your account details to open the Vision surface.</p>

      <form class="auth-terminal__form" @submit.prevent="login">
        <label class="auth-terminal__field">
          <span class="auth-terminal__label">Email</span>
          <input v-model="email" class="auth-terminal__input" type="email" autocomplete="email" />
        </label>

        <label class="auth-terminal__field">
          <span class="auth-terminal__label">Password</span>
          <input v-model="password" class="auth-terminal__input" type="password" autocomplete="current-password" />
        </label>

        <button class="auth-terminal__submit" type="submit">Enter</button>
      </form>

      <p v-if="error" class="auth-terminal__error">{{ error }}</p>

      <p class="auth-terminal__linkline">
        Need an account?
        <RouterLink to="/register">Register</RouterLink>
      </p>
    </section>
  </main>
</template>

<style scoped>
.auth-terminal {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 2rem;
  background:
    radial-gradient(circle at top, rgba(255, 214, 170, 0.24), transparent 34%),
    linear-gradient(180deg, #fffefb 0%, #f7f7f3 100%);
}

.auth-terminal__panel {
  width: min(100%, 31rem);
  display: grid;
  gap: 1rem;
  padding: 1.75rem 1.4rem;
  color: #1a242f;
  font-family: "IBM Plex Mono", "SFMono-Regular", "Menlo", monospace;
}

.auth-terminal__eyebrow {
  margin: 0;
  color: rgba(26, 36, 47, 0.46);
  font-size: 0.78rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.auth-terminal__title {
  margin: 0;
  font-size: clamp(2.1rem, 5vw, 3rem);
  line-height: 0.98;
  letter-spacing: -0.06em;
  font-family: "Iowan Old Style", "Palatino Linotype", serif;
}

.auth-terminal__copy,
.auth-terminal__linkline,
.auth-terminal__error {
  margin: 0;
  line-height: 1.6;
}

.auth-terminal__copy,
.auth-terminal__linkline {
  color: rgba(26, 36, 47, 0.66);
}

.auth-terminal__form {
  display: grid;
  gap: 0.95rem;
  margin-top: 0.35rem;
}

.auth-terminal__field {
  display: grid;
  gap: 0.45rem;
}

.auth-terminal__label {
  font-size: 0.82rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(26, 36, 47, 0.48);
}

.auth-terminal__input {
  width: 100%;
  border: 0;
  border-bottom: 1px solid rgba(26, 36, 47, 0.18);
  background: transparent;
  padding: 0.55rem 0;
  outline: none;
  color: #1a242f;
  font: inherit;
}

.auth-terminal__input:focus {
  border-bottom-color: rgba(36, 74, 122, 0.72);
}

.auth-terminal__submit {
  justify-self: start;
  border: 0;
  background: transparent;
  color: #244a7a;
  padding: 0.35rem 0;
  font: inherit;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.auth-terminal__error {
  color: #b04f43;
}

.auth-terminal__linkline a {
  color: #244a7a;
  text-decoration: none;
}
</style>
