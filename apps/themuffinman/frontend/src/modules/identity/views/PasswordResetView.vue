<script setup lang="ts">
import {computed, ref} from "vue"
import {useRoute} from "vue-router"
import {authApi} from "../api/authApi.ts"
import AppFormField from "../../app-shell/components/AppFormField.vue"
import AppFormFooter from "../../app-shell/components/AppFormFooter.vue"
import AppStatus from "../../app-shell/components/AppStatus.vue"

const route = useRoute()
const token = computed(() => String(route.query.token ?? ""))
const password = ref("")
const error = ref("")
const completed = ref(false)
const isSubmitting = ref(false)

const resetPassword = async () => {
  error.value = ""
  if (!token.value) {
    error.value = "This recovery link is missing its token."
    return
  }
  if (isSubmitting.value) return
  isSubmitting.value = true
  try {
    await authApi.resetPassword({token: token.value, password: password.value})
    completed.value = true
  } catch {
    error.value = "This recovery link is invalid or expired."
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <main class="auth-terminal identity-surface">
    <section class="auth-terminal__panel identity-surface__panel">
      <p class="auth-terminal__eyebrow">TheMuffinMan / access</p>
      <h1 class="auth-terminal__title">Set a new password</h1>
      <p class="auth-terminal__copy identity-surface__copy">Use this one-time recovery link to restore access.</p>
      <AppStatus v-if="completed" message="Password updated. You can log in with the new password." tone="success" />
      <AppStatus v-else-if="!token" message="This recovery link is missing its token. Request a new link to continue." tone="error" />
      <form v-else class="auth-terminal__form identity-surface__form" :aria-busy="isSubmitting || undefined" @submit.prevent="resetPassword">
        <AppFormField label="New password" required><input v-model="password" aria-label="New password" class="auth-terminal__input identity-surface__input" type="password" autocomplete="new-password" minlength="8" :disabled="isSubmitting" required /></AppFormField>
        <AppStatus v-if="isSubmitting" message="Saving your password…" busy /><AppStatus v-else-if="error" :message="error" tone="error" />
        <AppFormFooter><template #primary><button type="submit" :disabled="isSubmitting">{{ isSubmitting ? "Saving…" : "Save password" }}</button></template></AppFormFooter>
      </form>
      <p class="auth-terminal__linkline identity-surface__linkline"><RouterLink :to="completed ? '/login' : '/recover'">{{ completed ? 'Back to login' : 'Request a new link' }}</RouterLink></p>
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
