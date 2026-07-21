<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import type {BusinessOfferingRequestDTO, BusinessOfferingResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppDialog from "../components/AppDialog.vue"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import {confirmAction} from "../composables/useActionDialog.ts"

const offerings = ref<BusinessOfferingResponseDTO[]>([])
// Offering visibility is represented by the server-provided active state.
const form = ref<BusinessOfferingRequestDTO | null>(null)
const editingId = ref<number | null>(null)
const isLoading = ref(true)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const selectedOfferingId = ref<number | null>(null)
const selectedOffering = computed(() => offerings.value.find((offering) => offering.id === selectedOfferingId.value) ?? null)

const emptyForm = (): BusinessOfferingRequestDTO => ({title: "", slug: "", summary: "", description: "", pricingType: "FIXED", basePriceAmount: 0, basePriceCurrency: "CHF", durationMode: "FIXED", defaultDurationMinutes: 60, minDurationMinutes: 60, maxDurationMinutes: 60, capacityMode: "SINGLE", slotCapacity: 1, bookingMode: "REQUEST", requiresOwnerConfirmation: true, bufferBeforeMinutes: 0, bufferAfterMinutes: 0, active: true, sortOrder: offerings.value.length})
const load = async () => { isLoading.value = true; error.value = ""; try { offerings.value = (await userShellApi.getBusinessOfferings()).items } catch { error.value = "Could not load offerings." } finally { isLoading.value = false } }
const beginCreate = () => { editingId.value = null; form.value = emptyForm(); feedback.value = "" }
const beginEdit = (offering: BusinessOfferingResponseDTO) => { editingId.value = offering.id; form.value = {...offering}; feedback.value = "" }
const save = async () => { if (!form.value) return; isSaving.value = true; error.value = ""; try { if (editingId.value) await userShellApi.updateBusinessOffering(editingId.value, form.value); else await userShellApi.createBusinessOffering(form.value); form.value = null; feedback.value = "Offering saved."; await load() } catch { error.value = "Could not save this offering." } finally { isSaving.value = false } }
const remove = async (offering: BusinessOfferingResponseDTO) => { if (!await confirmAction(`Delete ${offering.title}?`, "Delete offering")) return; isSaving.value = true; error.value = ""; try { await userShellApi.deleteBusinessOffering(offering.id); feedback.value = "Offering deleted."; await load() } catch { error.value = "Could not delete this offering." } finally { isSaving.value = false } }
onMounted(() => void load())
</script>

<template>
  <section class="offerings-surface">
    <header class="offerings-surface__header"><div><p class="offerings-surface__eyebrow">Business / Offerings</p><h1>Offerings</h1></div></header>
    <CollectionToolbar title="Bookable services" :count="offerings.length" :busy="isLoading"><template #actions><AppButton type="button" tone="primary" @click="beginCreate">New offering</AppButton></template></CollectionToolbar>
    <AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="isLoading" message="Loading." /><AppStatus v-else-if="error && !form" :message="error" tone="error" retry @retry="load" /><AppStatus v-else-if="offerings.length === 0 && !form" message="No offerings yet. Create the first one." />
    <div v-else class="offerings-surface__workspace"><div class="offerings-surface__list"><SurfaceRow v-for="offering in offerings" :key="offering.id" :row="{id: String(offering.id), title: offering.title, description: offering.summary || 'No summary', badge: offering.active ? 'Active' : 'Inactive', meta: `${offering.basePriceAmount} ${offering.basePriceCurrency} · ${offering.defaultDurationMinutes} min`}" :selected="selectedOfferingId === offering.id" @click="selectedOfferingId = offering.id"><template #actions><AppButton type="button" tone="secondary" @click.stop="beginEdit(offering)">Edit</AppButton><AppButton type="button" tone="danger" @click.stop="remove(offering)">Delete</AppButton></template></SurfaceRow></div><aside v-if="selectedOffering" class="offerings-surface__preview" aria-label="Offering preview"><p class="offerings-surface__eyebrow">Selected offering</p><h2>{{ selectedOffering.title }}</h2><p>{{ selectedOffering.summary || "No summary provided." }}</p><dl><div><dt>Status</dt><dd>{{ selectedOffering.active ? "Active" : "Inactive" }}</dd></div><div><dt>Price</dt><dd>{{ selectedOffering.basePriceAmount }} {{ selectedOffering.basePriceCurrency }}</dd></div><div><dt>Duration</dt><dd>{{ selectedOffering.defaultDurationMinutes }} min</dd></div><div><dt>Booking mode</dt><dd>{{ selectedOffering.bookingMode }}</dd></div></dl><p v-if="selectedOffering.description" class="offerings-surface__preview-note">{{ selectedOffering.description }}</p><div class="offerings-surface__preview-actions"><AppButton type="button" tone="secondary" @click="beginEdit(selectedOffering)">Edit offering</AppButton><AppButton type="button" tone="danger" @click="remove(selectedOffering)">Delete offering</AppButton></div></aside><aside v-else class="offerings-surface__preview offerings-surface__preview--empty" aria-label="Offering preview"><p class="offerings-surface__eyebrow">Preview</p><h2>Select an offering</h2><p>Inspect pricing and booking context without leaving the collection.</p></aside></div>
    <AppDialog :open="form !== null" :title="editingId ? 'Edit offering' : 'New offering'" layout="workspace" @close="form = null"><form class="offerings-surface__form" @submit.prevent="save"><AppFormField label="Title" required><input v-model="form!.title" required maxlength="120" aria-label="Title"></AppFormField><AppFormField label="Slug" required hint="Lowercase words separated by hyphens."><input v-model="form!.slug" required pattern="[a-z0-9]+(?:-[a-z0-9]+)*" aria-label="Slug"></AppFormField><AppFormField label="Summary" optional><input v-model="form!.summary" maxlength="240" aria-label="Summary"></AppFormField><div class="offerings-surface__grid"><AppFormField label="Price"><input v-model.number="form!.basePriceAmount" type="number" min="0" step="0.01"></AppFormField><AppFormField label="Currency" required><input v-model="form!.basePriceCurrency" required maxlength="3"></AppFormField><AppFormField label="Duration" required><input v-model.number="form!.defaultDurationMinutes" type="number" min="1"></AppFormField></div><AppFormField label="Description" optional><textarea v-model="form!.description" maxlength="4000"></textarea></AppFormField><label class="offerings-surface__toggle"><input v-model="form!.active" type="checkbox"> Active</label><AppStatus v-if="error" :message="error" tone="error" /><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="form = null">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Saving" : "Save" }}</AppButton></template></AppFormFooter></form><template #utility><p>Pricing and duration remain server-validated. Publishing controls whether this offering is available in the customer booking flow.</p></template></AppDialog>
  </section>
</template>

<style scoped>
.offerings-surface{display:grid;gap:var(--space-3);max-width:none}.offerings-surface__header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.offerings-surface__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}h2{margin:0;font-size:var(--text-size-title)}.offerings-surface__list{display:grid;gap:0;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);overflow:hidden}.offerings-surface__row{display:flex;justify-content:space-between;gap:var(--space-3);align-items:center;padding:var(--space-2) var(--space-3);border:1px solid var(--border-subtle);background:var(--surface-base)}.offerings-surface__row>div:first-child{display:grid;gap:var(--space-1);min-width:0}.offerings-surface__row span{color:var(--text-muted);font-size:var(--text-size-meta)}.offerings-surface__actions{display:flex;gap:var(--space-1)}.offerings-surface button{border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit;font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);cursor:pointer;white-space:nowrap}.offerings-surface button:hover{border-color:var(--control-border-active);background:var(--control-bg-hover)}.offerings-surface__primary{border-color:var(--accent)!important;background:var(--accent)!important;color:var(--canvas)}.offerings-surface__danger{color:var(--danger)}.offerings-surface__secondary{margin-left:var(--space-1)}.offerings-surface__form{display:grid;gap:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base);padding:var(--space-3)}.offerings-surface__form label{display:grid;gap:var(--space-1);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);color:var(--text-muted)}.offerings-surface__form input,.offerings-surface__form textarea{border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit;font-weight:400}.offerings-surface__form textarea{min-height:6rem}.offerings-surface__grid{display:grid;grid-template-columns:repeat(3,1fr);gap:var(--space-2)}.offerings-surface__toggle{display:flex!important;align-items:center;gap:var(--space-2)!important}.offerings-surface__toggle input{width:auto}.offerings-surface__feedback{margin:0;color:var(--success)}.offerings-surface__status{padding:var(--space-2) 0;color:var(--text-muted)}.offerings-surface__status--error{color:var(--danger)}.offerings-surface__status button{margin-left:var(--space-1);border:0;background:transparent;color:inherit;text-decoration:underline}@media(max-width:620px){.offerings-surface__row{align-items:start;flex-direction:column}.offerings-surface__grid{grid-template-columns:1fr}}
/* Workspace visual remediation: retain route behavior, replace legacy pill/card geometry. */
.offerings-surface{gap:var(--space-3);max-width:none}.offerings-surface__header{gap:var(--space-3)}.offerings-surface__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label)}h1{font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.offerings-surface__list{gap:0;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);overflow:hidden}.offerings-surface button{border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);font-size:var(--text-size-meta)}.offerings-surface__form input,.offerings-surface__form textarea{border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg)}
</style>
<style scoped>
/* Offering rows and forms use the same dense collection/control contract as other workspace routes. */
.offerings-surface__row { background: var(--surface-base); }
.offerings-surface__form { gap: var(--space-3); padding-top: var(--space-3); border-color: var(--border-subtle); }
.offerings-surface__form input,
.offerings-surface__form textarea { border-color: var(--control-border); color: var(--control-ink); }
.offerings-surface__primary { border: 1px solid var(--accent) !important; color: var(--canvas); }
.offerings-surface__danger { color: var(--danger); }
.offerings-surface__workspace { display:grid; grid-template-columns:minmax(0,1fr) minmax(16rem,22rem); gap:var(--space-3); align-items:start; }
.offerings-surface__preview { display:grid; gap:var(--space-2); padding:var(--space-3); border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-raised); color:var(--text-muted); }
.offerings-surface__preview h2,.offerings-surface__preview p { margin:0; }
.offerings-surface__preview h2 { color:var(--text); font-size:var(--text-size-title); }
.offerings-surface__preview dl { display:grid; gap:var(--space-2); margin:var(--space-2) 0; }
.offerings-surface__preview dl div { display:flex; justify-content:space-between; gap:var(--space-2); border-top:1px solid var(--border-subtle); padding-top:var(--space-2); }
.offerings-surface__preview dt { color:var(--text-soft); font-size:var(--text-size-meta); }
.offerings-surface__preview dd { margin:0; color:var(--text); font-size:var(--text-size-meta); font-weight:var(--text-weight-semibold); }
.offerings-surface__preview-note { color:var(--text-soft); font-size:var(--text-size-meta); line-height:1.45; }
.offerings-surface__preview-actions { display:flex; gap:var(--space-2); flex-wrap:wrap; }
.offerings-surface__preview--empty { min-height:10rem; align-content:center; }
.offerings-surface .app-button { border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); background:var(--control-bg); color:var(--control-ink); }
.offerings-surface .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
.offerings-surface .app-button--danger { color:var(--danger); }
.offerings-surface__row { padding: var(--space-2) var(--space-3); background: var(--surface-base); }
.offerings-surface__form { border: 1px solid var(--border-subtle); border-radius: var(--radius-surface); background: var(--surface-base); padding: var(--space-3); }
.offerings-surface__form input, .offerings-surface__form textarea { background: var(--control-bg); }
@media(max-width:860px){.offerings-surface__workspace{grid-template-columns:1fr}.offerings-surface__preview{order:2}}
</style>
