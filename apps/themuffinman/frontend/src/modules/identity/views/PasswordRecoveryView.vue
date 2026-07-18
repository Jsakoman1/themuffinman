<script setup lang="ts">
import {ref} from "vue"
import {authApi} from "../api/authApi.ts"
import AppFormField from "../../app-shell/components/AppFormField.vue"
import AppFormFooter from "../../app-shell/components/AppFormFooter.vue"
import AppStatus from "../../app-shell/components/AppStatus.vue"

const email = ref("")
const error = ref("")
const submitted = ref(false)

const requestRecovery = async () => {
  error.value = ""
  try {
    await authApi.requestPasswordRecovery({email: email.value.trim().toLowerCase()})
    submitted.value = true
  } catch {
    error.value = "We could not start recovery. Try again."
  }
}
</script>

<template>
  <main class="auth-terminal">
    <section class="auth-terminal__panel">
      <p class="auth-terminal__eyebrow">TheMuffinMan / access</p>
      <h1 class="auth-terminal__title">Recover access</h1>
      <p class="auth-terminal__copy">Enter your email and we will continue the secure recovery flow.</p>
      <AppStatus v-if="submitted" message="If an account exists, recovery instructions will be sent to that address." tone="success" />
      <form v-else class="auth-terminal__form" @submit.prevent="requestRecovery">
        <AppFormField label="Email" required><input v-model="email" aria-label="Email" class="auth-terminal__input" type="email" autocomplete="email" required /></AppFormField>
        <AppStatus v-if="error" :message="error" tone="error" />
        <AppFormFooter><template #primary><button type="submit">Continue</button></template></AppFormFooter>
      </form>
      <p class="auth-terminal__linkline"><RouterLink to="/login">Back to login</RouterLink></p>
    </section>
  </main>
</template>

<style scoped>
.auth-terminal {min-height:100vh;display:grid;place-items:center;padding:var(--space-4);background:var(--canvas);color:var(--text)}
.auth-terminal__panel {width:min(100%,31rem);display:grid;gap:var(--space-3);padding:var(--space-5);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}
.auth-terminal__eyebrow {margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}
.auth-terminal__title {margin:0;color:var(--text);font-size:var(--text-size-page-title);line-height:1;letter-spacing:var(--tracking-tight)}
.auth-terminal__copy,.auth-terminal__linkline {margin:0;color:var(--text-muted);line-height:1.6}
.auth-terminal__form {display:grid;gap:var(--space-3);margin-top:var(--space-1)}
.auth-terminal__input {width:100%;box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}
.auth-terminal__linkline a {color:var(--accent);font-weight:var(--text-weight-semibold);text-decoration:none}
</style>
