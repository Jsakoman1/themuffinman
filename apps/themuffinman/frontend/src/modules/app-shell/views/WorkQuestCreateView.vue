<script setup lang="ts">
import {ref} from "vue"
import {RouterLink, useRouter} from "vue-router"
import type {QuestRequestDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {buildVisionRoute} from "../visionHandoff.ts"
import RichTextEditor from "../components/RichTextEditor.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppButton from "../components/AppButton.vue"
import GuidedIntakePanel from "../components/GuidedIntakePanel.vue"

const router = useRouter()
const form = ref<QuestRequestDTO>({title: "", description: "", awardAmount: 0, termFixed: false})
const isSaving = ref(false)
const error = ref("")
const guidedDraft = ref<Record<string, string> | null>(null)
const guidedComplete = (draft: Record<string, string>) => { guidedDraft.value = draft; form.value = {title: draft.title ?? "", description: draft.description ?? "", awardAmount: Number(draft.awardAmount ?? 0), termFixed: (draft.termFixed ?? "").toLowerCase() === "fixed"} }
const save = async () => {
  isSaving.value = true; error.value = ""
  try { await userShellApi.createQuest(form.value); await router.push("/work/quests") }
  catch { error.value = "Could not create this quest." }
  finally { isSaving.value = false }
}
</script>

<template>
  <section class="quest-create">
    <header class="quest-create__header"><div><p class="quest-create__eyebrow">Work / New quest</p><h1>Create a quest</h1></div><RouterLink :to="buildVisionRoute({prompt: 'help me create a quest', context: 'Work', source: 'work.create', returnTo: '/work/quests/new'})" class="quest-create__vision">Ask Vision</RouterLink></header>
    <div class="quest-create__workspace">
      <GuidedIntakePanel v-if="!guidedDraft" flow="work.quest.create" title="Create a quest" description="Answer one useful question at a time, then review the complete draft." @completed="guidedComplete" @cancel="router.push('/work/quests')" />
      <form v-if="guidedDraft" class="quest-create__form" @submit.prevent="save">
        <p class="quest-create__draft-boundary">This guided draft is still private. Nothing is created or shared until you confirm the final review.</p>
        <p class="quest-create__review-label">Review your guided draft</p>
        <AppFormField label="Title" hint="Backend-validated draft" required><input v-model="form.title" required maxlength="255"></AppFormField>
        <AppFormField label="Description" hint="Backend-validated draft"><RichTextEditor v-model="form.description" label="Quest description" /></AppFormField>
        <AppFormField label="Award" hint="Backend-validated draft" required><input v-model.number="form.awardAmount" type="number" min="0" step="0.01" required></AppFormField>
        <label class="quest-create__checkbox"><input v-model="form.termFixed" type="checkbox"> Fixed terms</label>
        <p v-if="error" class="quest-create__error" role="alert">{{ error }}</p>
        <AppFormFooter sticky><template #secondary><RouterLink to="/work/quests">Cancel</RouterLink></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Creating" : "Create quest" }}</AppButton></template></AppFormFooter>
      </form>
      <aside class="quest-create__summary" aria-label="Quest draft summary">
        <p class="quest-create__summary-eyebrow">Draft summary</p>
        <h2>{{ form.title.trim() || "Untitled quest" }}</h2>
        <p>{{ form.description.trim() || "Add a description to give the worker useful context." }}</p>
        <dl><div><dt>Award</dt><dd>{{ new Intl.NumberFormat(undefined, {style: "currency", currency: "EUR"}).format(form.awardAmount || 0) }}</dd></div><div><dt>Terms</dt><dd>{{ form.termFixed ? "Fixed" : "Flexible" }}</dd></div><div><dt>Visibility</dt><dd>Backend governed</dd></div></dl>
        <p class="quest-create__summary-note">Vision can help refine the draft, but creation only happens after the explicit submit action.</p>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.quest-create{display:grid;gap:var(--space-4);max-width:62rem}.quest-create__header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.quest-create__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.quest-create__vision{display:inline-flex;align-items:center;justify-content:center;min-height:var(--control-height-default);border:1px solid var(--accent);border-radius:var(--radius-control);padding:var(--space-1) var(--space-3);background:var(--accent);color:var(--canvas);font-size:var(--text-size-body);font-weight:var(--text-weight-semibold)}.quest-create__form{display:grid;gap:var(--space-4);max-width:44rem;padding:var(--space-4);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}.quest-create__draft-boundary{margin:0;color:var(--text-muted);font-size:var(--text-size-meta)}.quest-create__form input,.quest-create__form textarea{width:100%;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}.quest-create__form textarea{min-height:10rem;resize:vertical}.quest-create__error{margin:0;color:var(--danger)}@media(max-width:700px){.quest-create__header{align-items:start;flex-direction:column}}
.quest-create__workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(16rem,20rem);gap:var(--space-3);align-items:start}.quest-create__form{max-width:none}.quest-create__summary{display:grid;gap:var(--space-2);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--rail-canvas)}.quest-create__summary-eyebrow{margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.quest-create__summary h2{margin:0;color:var(--text);font-size:var(--text-size-title);letter-spacing:var(--tracking-tight);overflow-wrap:anywhere}.quest-create__summary p{margin:0;color:var(--text-muted);font-size:var(--text-size-body);line-height:1.5;overflow-wrap:anywhere}.quest-create__summary dl{display:grid;gap:var(--space-2);margin:var(--space-2) 0 0}.quest-create__summary dl div{display:flex;justify-content:space-between;gap:var(--space-2);padding-bottom:var(--space-2);border-bottom:1px solid var(--border-subtle)}.quest-create__summary dt{color:var(--text-soft);font-size:var(--text-size-meta)}.quest-create__summary dd{margin:0;color:var(--text);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);text-align:right}.quest-create__summary-note{padding-top:var(--space-2);border-top:1px solid var(--border-subtle);font-size:var(--text-size-meta)!important}@media(max-width:860px){.quest-create__workspace{grid-template-columns:1fr}.quest-create__summary{order:-1}}
</style>
<style scoped>
.quest-create .app-button { border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); }
.quest-create .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
</style>
