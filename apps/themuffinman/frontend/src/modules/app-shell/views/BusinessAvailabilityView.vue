<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {RouterLink} from "vue-router"
import type {BusinessAvailabilityRuleRequestDTO, BusinessAvailabilityRuleResponseDTO, BusinessOfferingResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppButton from "../components/AppButton.vue"
import AppDialog from "../components/AppDialog.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"

const props = defineProps<{ businessId?: number }>()
const rules = ref<BusinessAvailabilityRuleResponseDTO[]>([])
const offerings = ref<BusinessOfferingResponseDTO[]>([])
const form = ref<BusinessAvailabilityRuleRequestDTO | null>(null)
const editingId = ref<number | null>(null)
const loading = ref(true)
const saving = ref(false)
const error = ref("")
const feedback = ref("")
const exceptionsPath = computed(() => `/business/availability-exceptions${props.businessId ? `?businessId=${props.businessId}` : ""}`)
const dayName = (day: number) => ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"][day - 1] ?? "Unknown day"
const load = async () => { loading.value = true; error.value = ""; try { const [ruleResponse, offeringResponse] = await Promise.all([userShellApi.getBusinessAvailabilityRules(), userShellApi.getBusinessOfferings()]); rules.value = ruleResponse.items; offerings.value = offeringResponse.items } catch { error.value = "Could not load availability rules." } finally { loading.value = false } }
const emptyForm = (): BusinessAvailabilityRuleRequestDTO => ({businessOfferingId: offerings.value[0]?.id ?? 0, dayOfWeek: 1, startTimeLocal: "09:00", endTimeLocal: "17:00", slotGranularityMinutes: 60, capacityOverride: 1, validFrom: new Date().toISOString().slice(0, 10), validUntil: "", active: true})
const beginCreate = () => { editingId.value = null; form.value = emptyForm(); feedback.value = "" }
const beginEdit = (rule: BusinessAvailabilityRuleResponseDTO) => { editingId.value = rule.id; form.value = {businessOfferingId: rule.businessOfferingId, dayOfWeek: rule.dayOfWeek, startTimeLocal: rule.startTimeLocal, endTimeLocal: rule.endTimeLocal, slotGranularityMinutes: rule.slotGranularityMinutes, capacityOverride: rule.capacityOverride, validFrom: rule.validFrom, validUntil: rule.validUntil, active: rule.active}; feedback.value = "" }
const save = async () => { if (!form.value || !form.value.businessOfferingId) return; saving.value = true; error.value = ""; try { if (editingId.value) await userShellApi.updateBusinessAvailabilityRule(editingId.value, form.value); else await userShellApi.createBusinessAvailabilityRule(form.value); form.value = null; feedback.value = "Availability rule saved."; await load() } catch { error.value = "Could not save this availability rule." } finally { saving.value = false } }
onMounted(() => void load())
</script>

<template>
  <section class="availability" aria-label="Business availability settings">
    <CollectionToolbar title="Working hours" :count="rules.length" :busy="loading"><template #actions><RouterLink :to="exceptionsPath">Special dates</RouterLink><AppButton type="button" tone="primary" :disabled="!offerings.length" @click="beginCreate">Add working hours</AppButton></template></CollectionToolbar>
    <p class="availability__intro">Set the normal times customers can book each service. Add special dates only for closures or different hours.</p>
    <AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="loading" message="Loading working hours." busy /><AppStatus v-else-if="error && !form" :message="error" tone="error" retry @retry="load" /><AppStatus v-else-if="rules.length === 0 && !form" message="No working hours yet. Add a regular day to start showing appointment times." />
    <div v-else class="availability__list"><SurfaceRow v-for="rule in rules" :key="rule.id" :row="{id: String(rule.id), title: `${rule.businessOfferingTitle} · ${dayName(rule.dayOfWeek)}`, description: `${rule.startTimeLocal}–${rule.endTimeLocal} · appointments every ${rule.slotGranularityMinutes} minutes`, badge: rule.active ? 'Open' : 'Paused', meta: rule.capacityOverride > 1 ? `${rule.capacityOverride} people at once` : 'One booking at a time'}" ><template #actions><AppButton type="button" tone="secondary" @click="beginEdit(rule)">Edit</AppButton></template></SurfaceRow></div>
    <AppDialog :open="form !== null" :title="editingId ? 'Edit working hours' : 'Add working hours'" layout="workspace" @close="form = null"><form v-if="form" class="availability__form" @submit.prevent="save"><p class="availability__dialog-copy">Choose one normal day for one service. You can add another day afterwards.</p><AppFormField label="Service" required><select v-model.number="form.businessOfferingId" required><option v-for="offering in offerings" :key="offering.id" :value="offering.id">{{ offering.title }}</option></select></AppFormField><div class="availability__grid"><AppFormField label="Day" required><select v-model.number="form.dayOfWeek"><option v-for="day in 7" :key="day" :value="day">{{ dayName(day) }}</option></select></AppFormField><AppFormField label="Open from" required><input v-model="form.startTimeLocal" type="time" required></AppFormField><AppFormField label="Open until" required><input v-model="form.endTimeLocal" type="time" required></AppFormField></div><AppFormField label="Appointment spacing (minutes)" hint="How often customers can choose a new start time."><input v-model.number="form.slotGranularityMinutes" type="number" min="1"></AppFormField><details class="availability__advanced"><summary>More scheduling options</summary><div class="availability__grid"><AppFormField label="People or items at the same time"><input v-model.number="form.capacityOverride" type="number" min="1"></AppFormField><AppFormField label="Start using these hours on" required><input v-model="form.validFrom" type="date" required></AppFormField><AppFormField label="Stop using these hours" optional><input v-model="form.validUntil" type="date"></AppFormField></div><label class="availability__toggle"><input v-model="form.active" type="checkbox"> Use these hours</label></details><AppStatus v-if="error" :message="error" tone="error" /><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="form = null">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="saving">Save working hours</AppButton></template></AppFormFooter></form></AppDialog>
  </section>
</template>

<style scoped>
.availability{display:grid;gap:var(--space-3)}.availability__intro,.availability__dialog-copy{margin:0;color:var(--text-muted);line-height:1.5}.availability__list{overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.availability__list :deep(.surface-row:last-child){border-bottom:0}.availability__form{display:grid;gap:var(--space-3)}.availability__grid{display:grid;grid-template-columns:repeat(3,1fr);gap:var(--space-2)}.availability__form input,.availability__form select{width:100%;box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.availability__toggle{display:flex;gap:var(--space-2);align-items:center;font-weight:var(--text-weight-semibold)}.availability__advanced{display:grid;gap:var(--space-3);padding-top:var(--space-2);border-top:1px solid var(--border-subtle)}.availability__advanced summary{cursor:pointer;font-weight:var(--text-weight-semibold)}.availability__advanced[open]{padding-bottom:var(--space-1)}@media(max-width:700px){.availability__grid{grid-template-columns:1fr}}
</style>
