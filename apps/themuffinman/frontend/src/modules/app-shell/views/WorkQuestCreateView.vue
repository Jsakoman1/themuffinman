<script setup lang="ts">
import {onMounted, ref} from "vue"
import {RouterLink, useRouter} from "vue-router"
import type {CircleGroupResponseDTO, QuestRequestDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import RichTextEditor from "../components/RichTextEditor.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppButton from "../components/AppButton.vue"
import GuidedIntakePanel from "../components/GuidedIntakePanel.vue"
import {formatCurrency} from "../../../services/formatters.ts"
import AppStatus from "../components/AppStatus.vue"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"

const router = useRouter()
const form = ref<QuestRequestDTO>({title: "", description: "", awardAmount: 0, termFixed: false, audience: "CIRCLES", selectedCircleIds: [], images: [], locationSource: "PROFILE", locationVisibility: "INHERIT"})
const circleGroups = ref<CircleGroupResponseDTO[]>([])
const scheduledAtInput = ref("")
const endsAtInput = ref("")
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const guidedDraft = ref<Record<string, string> | null>(null)
const surface = getAppSurfaceConfig("work-quests")
const guidedComplete = (draft: Record<string, string>) => { guidedDraft.value = draft; form.value = {title: draft.title ?? "", description: draft.description ?? "", awardAmount: Number(draft.awardAmount ?? 0), termFixed: (draft.termFixed ?? "").toLowerCase() === "fixed", audience: "CIRCLES", selectedCircleIds: [], images: [], locationSource: "PROFILE", locationVisibility: "INHERIT"} }
const toIso = (value: string) => value ? new Date(value).toISOString() : null
const readImage = (file: File) => new Promise<string>((resolve, reject) => { const reader = new FileReader(); reader.onload = () => typeof reader.result === "string" ? resolve(reader.result) : reject(new Error("Image could not be read.")); reader.onerror = () => reject(reader.error ?? new Error("Image could not be read.")); reader.readAsDataURL(file) })
const selectImages = async (event: Event) => { const files = Array.from((event.target as HTMLInputElement).files ?? []).slice(0, 10); try { form.value.images = await Promise.all(files.map(readImage)); error.value = "" } catch { form.value.images = []; error.value = "Could not read the selected images." } }
const save = async () => {
  if (isSaving.value) return
  isSaving.value = true; error.value = ""
  try { await userShellApi.createQuest({...form.value, scheduledAt: toIso(scheduledAtInput.value), endsAt: toIso(endsAtInput.value)}); await router.push("/work/quests") }
  catch (cause) { error.value = userShellApi.actionFailureMessage("Could not create this SideJob. Review the draft and try again.", cause) }
  finally { isSaving.value = false }
}
onMounted(async () => { circleGroups.value = await userShellApi.getCircleGroups().catch(() => []) })
</script>
<style scoped>
.quest-create{border-top:1px solid var(--orientation-line)}

.quest-create{display:grid;gap:var(--space-4);max-width:62rem}.quest-create__header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.quest-create__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.quest-create__vision{display:inline-flex;align-items:center;justify-content:center;min-height:var(--control-height-default);border:1px solid var(--accent);border-radius:var(--radius-control);padding:var(--space-1) var(--space-3);background:var(--accent);color:var(--canvas);font-size:var(--text-size-body);font-weight:var(--text-weight-semibold)}.quest-create__form{display:grid;gap:var(--space-4);max-width:44rem;padding:var(--space-4);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}.quest-create__draft-boundary{margin:0;color:var(--text-muted);font-size:var(--text-size-meta)}.quest-create__form input,.quest-create__form textarea{width:100%;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}.quest-create__form textarea{min-height:10rem;resize:vertical}.quest-create__error{margin:0;color:var(--danger)}@media(max-width:700px){.quest-create__header{align-items:start;flex-direction:column}}
.quest-create__workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(16rem,20rem);gap:var(--space-3);align-items:start}.quest-create__form{max-width:none}.quest-create__summary{display:grid;gap:var(--space-2);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--rail-canvas)}.quest-create__summary-eyebrow{margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.quest-create__summary h2{margin:0;color:var(--text);font-size:var(--text-size-title);letter-spacing:var(--tracking-tight);overflow-wrap:anywhere}.quest-create__summary p{margin:0;color:var(--text-muted);font-size:var(--text-size-body);line-height:1.5;overflow-wrap:anywhere}.quest-create__summary dl{display:grid;gap:var(--space-2);margin:var(--space-2) 0 0}.quest-create__summary dl div{display:flex;justify-content:space-between;gap:var(--space-2);padding-bottom:var(--space-2);border-bottom:1px solid var(--border-subtle)}.quest-create__summary dt{color:var(--text-soft);font-size:var(--text-size-meta)}.quest-create__summary dd{margin:0;color:var(--text);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);text-align:right}.quest-create__summary-note{padding-top:var(--space-2);border-top:1px solid var(--border-subtle);font-size:var(--text-size-meta)!important}@media(max-width:860px){.quest-create__workspace{grid-template-columns:1fr}.quest-create__summary{order:-1}}
.quest-create{container-type:inline-size}

.quest-create .app-button { border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); }
.quest-create__grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:var(--space-3)}.quest-create__fieldset{display:grid;gap:var(--space-3);margin:0;border:1px solid var(--border-subtle);border-radius:var(--radius-control);padding:var(--space-3)}.quest-create__fieldset legend{padding:0 var(--space-1);color:var(--text);font-weight:var(--text-weight-semibold)}
.quest-create .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
.quest-create select { min-height:var(--control-height-default); border:1px solid var(--control-border); border-radius:var(--radius-control); padding:var(--space-2); background:var(--control-bg); color:var(--control-ink); font:inherit; }
</style>

<template>
  <section class="quest-create" data-form-model="guided-review-before-commit" data-typography-model="shared-content-hierarchy">
    <SurfaceHeader :config="surface" title="Post a SideJob" description="Ask for the help you need, then review what people will see before posting." />
    <div class="quest-create__workspace">
      <GuidedIntakePanel v-if="!guidedDraft" flow="work.quest.create" title="Post a SideJob" description="Answer one useful question at a time. You can review and edit everything before posting." @completed="guidedComplete" @cancel="router.push('/work/quests')" />
      <form v-if="guidedDraft" class="quest-create__form" @submit.prevent="save">
        <p class="quest-create__draft-boundary">This draft is private. Nothing is posted until you choose Post SideJob below.</p>
        <p class="quest-create__review-label">Review your SideJob</p>
        <AppFormField label="What help do you need?" hint="For example, “Help me move a sofa”." required><input v-model="form.title" required maxlength="255"></AppFormField>
        <AppFormField label="Describe the SideJob" hint="Include the useful details a potential helper should know."><RichTextEditor v-model="form.description" label="SideJob description" /></AppFormField>
        <AppFormField label="Payment" hint="Enter the amount you will pay." required><input v-model.number="form.awardAmount" type="number" min="0" step="0.01" required></AppFormField>
        <div class="quest-create__grid"><AppFormField label="How many helpers do you need?" hint="Usually one."><input v-model.number="form.assigneeTarget" type="number" min="1"></AppFormField><label class="quest-create__checkbox"><input v-model="form.showApprovedApplicants" type="checkbox"> Show selected helpers</label></div>
        <label class="quest-create__checkbox"><input v-model="form.termFixed" type="checkbox"> This SideJob has a fixed time</label>
        <div v-if="form.termFixed" class="quest-create__grid"><AppFormField label="Starts" required><input v-model="scheduledAtInput" type="datetime-local" required></AppFormField><AppFormField label="Ends" optional><input v-model="endsAtInput" type="datetime-local"></AppFormField></div>
        <fieldset class="quest-create__fieldset"><legend>Who can see it and where?</legend><div class="quest-create__grid"><AppFormField label="Who can see this SideJob?"><select v-model="form.audience"><option value="EVERYONE">Everyone</option><option value="CIRCLES">My circles</option><option value="PRIVATE">Only me</option></select></AppFormField><AppFormField label="How much location should helpers see?"><select v-model="form.locationVisibility"><option value="INHERIT">Use my profile setting</option><option value="APPROXIMATE">Approximate area</option><option value="EXACT">Exact location</option><option value="OFF">Do not show location</option></select></AppFormField></div><AppFormField label="Location"><select v-model="form.locationSource"><option value="PROFILE">Use my profile location</option><option value="CUSTOM">Add a different location</option></select></AppFormField><div v-if="form.locationSource === 'CUSTOM'" class="quest-create__grid"><AppFormField label="Place or address" required><input v-model="form.locationLabel" required maxlength="255"></AppFormField><AppFormField label="City or locality" optional><input v-model="form.locationLocality" maxlength="120"></AppFormField></div></fieldset>
        <fieldset v-if="form.audience === 'CIRCLES' && circleGroups.length" class="quest-create__fieldset"><legend>Choose circles</legend><p class="quest-create__field-help">Select the circles that can see this SideJob.</p><label v-for="circle in circleGroups" :key="circle.id" class="quest-create__checkbox"><input v-model="form.selectedCircleIds" type="checkbox" :value="circle.id"> {{ circle.name }}</label></fieldset>
        <AppFormField label="Photos" optional hint="Up to 10 photos. They are private until the SideJob is posted."><input type="file" accept="image/*" multiple @change="selectImages"></AppFormField>
        <AppStatus v-if="isSaving" message="Posting SideJob…" busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="save" /><AppStatus v-if="feedback" :message="feedback" tone="success" />
        <AppFormFooter sticky><template #secondary><RouterLink to="/work/quests">Cancel</RouterLink></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Posting…" : "Post SideJob" }}</AppButton></template></AppFormFooter>
      </form>
      <aside class="quest-create__summary" aria-label="SideJob draft summary">
        <p class="quest-create__summary-eyebrow">Draft summary</p>
        <h2>{{ form.title.trim() || "Untitled SideJob" }}</h2>
        <p>{{ form.description.trim() || "Add a description so potential helpers know what you need." }}</p>
        <dl><div><dt>Payment</dt><dd>{{ formatCurrency(form.awardAmount || 0) }}</dd></div><div><dt>Time</dt><dd>{{ form.termFixed ? "Fixed" : "Flexible" }}</dd></div><div><dt>Visibility</dt><dd>{{ form.audience === "CIRCLES" ? "Selected circles" : form.audience === "EVERYONE" ? "Everyone" : "Only you" }}</dd></div></dl>
        <p class="quest-create__summary-note">Review this summary before posting. You can change the details later if the SideJob is still editable.</p>
      </aside>
    </div>
  </section>
</template>

