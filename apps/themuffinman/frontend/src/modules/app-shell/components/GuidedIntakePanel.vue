<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import AppButton from "./AppButton.vue"
import AppFormField from "./AppFormField.vue"
import {userShellApi, type GuidedIntakeResponse} from "../api/userShellApi.ts"

const props = defineProps<{flow: string; title?: string; description?: string}>()
const emit = defineEmits<{completed: [draft: Record<string, string>]; cancel: []}>()
const response = ref<GuidedIntakeResponse | null>(null)
const value = ref("")
const draft = ref<Record<string, string>>({})
const error = ref("")
const loading = ref(false)
const step = computed(() => response.value?.step)

const advance = async (action = "next") => {
  loading.value = true; error.value = ""
  try {
    const result = await userShellApi.advanceGuidedIntake({flow: props.flow, draft: draft.value, fieldId: step.value?.fieldId, fieldValue: action === "next" ? value.value : undefined, action})
    response.value = result; draft.value = result.draft; value.value = result.step?.currentValue ?? ""
    if (result.step?.error) error.value = result.step.error
    if (result.reviewReady) emit("completed", result.draft)
  } catch { error.value = "Could not load the next step." }
  finally { loading.value = false }
}
onMounted(() => advance("start"))
</script>

<template>
  <section class="guided-intake" aria-live="polite">
    <header><p class="guided-intake__eyebrow">Guided setup</p><h2>{{ title || "Let's do this one step at a time" }}</h2><p>{{ description || "The backend will guide the next useful question." }}</p></header>
    <div v-if="loading && !step" class="guided-intake__status">Loading the first step…</div>
    <form v-else-if="step && !step.complete" @submit.prevent="advance()">
      <AppFormField :label="step.label" :hint="step.placeholder" required>
        <select v-if="step.inputKind === 'choice'" v-model="value"><option value="" disabled>{{ step.placeholder }}</option><option v-for="choice in step.choices" :key="choice" :value="choice">{{ choice }}</option></select>
        <textarea v-else-if="step.inputKind === 'textarea'" v-model="value" :placeholder="step.placeholder" rows="5" />
        <input v-else v-model="value" :type="step.inputKind === 'number' ? 'number' : step.inputKind === 'datetime-local' ? 'datetime-local' : 'text'" :placeholder="step.placeholder" />
      </AppFormField>
      <p v-if="error" class="guided-intake__error" role="alert">{{ error }}</p>
      <footer><AppButton type="button" tone="secondary" @click="advance('back')">Back</AppButton><span class="guided-intake__progress">{{ Object.keys(draft).length }} completed</span><AppButton type="submit" tone="primary" :loading="loading">Next</AppButton></footer>
    </form>
    <div v-else class="guided-intake__review"><h3>Review ready</h3><p>All guided fields are complete. Review the summary before creating.</p><AppButton type="button" tone="secondary" @click="emit('cancel')">Cancel</AppButton></div>
  </section>
</template>

<style scoped>
.guided-intake{display:grid;gap:var(--space-4);max-width:44rem;padding:var(--space-5);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}
.guided-intake header{display:grid;gap:var(--space-1)}.guided-intake h2,.guided-intake h3,.guided-intake p{margin:0}.guided-intake__eyebrow{color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.guided-intake header p:last-child{color:var(--text-muted);font-size:var(--text-size-body)}.guided-intake input,.guided-intake textarea,.guided-intake select{width:100%;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}.guided-intake textarea{resize:vertical}.guided-intake footer{display:flex;align-items:center;justify-content:space-between;gap:var(--space-2)}.guided-intake__progress{color:var(--text-muted);font-size:var(--text-size-meta)}.guided-intake__error{color:var(--danger);font-size:var(--text-size-body)}.guided-intake__status{color:var(--text-muted)}
</style>
