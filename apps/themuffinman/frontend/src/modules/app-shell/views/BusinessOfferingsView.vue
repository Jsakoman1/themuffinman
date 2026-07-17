<script setup lang="ts">
import {onMounted, ref} from "vue"
import type {BusinessOfferingRequestDTO, BusinessOfferingResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppDialog from "../components/AppDialog.vue"
import {confirmAction} from "../composables/useActionDialog.ts"

const offerings = ref<BusinessOfferingResponseDTO[]>([])
const form = ref<BusinessOfferingRequestDTO | null>(null)
const editingId = ref<number | null>(null)
const isLoading = ref(true)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")

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
    <header class="offerings-surface__header"><div><p class="offerings-surface__eyebrow">Business / Offerings</p><h1>Offerings</h1></div><button type="button" class="offerings-surface__primary" @click="beginCreate">New offering</button></header>
    <p v-if="feedback" class="offerings-surface__feedback" role="status">{{ feedback }}</p>
    <div v-if="isLoading" class="offerings-surface__status" role="status">Loading.</div>
    <div v-else-if="error && !form" class="offerings-surface__status offerings-surface__status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div>
    <div v-else-if="offerings.length === 0 && !form" class="offerings-surface__status">No offerings yet. Create the first one.</div>
    <div v-else class="offerings-surface__list"><article v-for="offering in offerings" :key="offering.id" class="offerings-surface__row"><div><strong>{{ offering.title }}</strong><span>{{ offering.summary || "No summary" }} · {{ offering.basePriceAmount }} {{ offering.basePriceCurrency }} · {{ offering.active ? "Active" : "Inactive" }}</span></div><div class="offerings-surface__actions"><button type="button" @click="beginEdit(offering)">Edit</button><button type="button" class="offerings-surface__danger" @click="remove(offering)">Delete</button></div></article></div>
    <AppDialog :open="form !== null" :title="editingId ? 'Edit offering' : 'New offering'" @close="form = null"><form class="offerings-surface__form" @submit.prevent="save"><label>Title<input v-model="form!.title" required maxlength="120"></label><label>Slug<input v-model="form!.slug" required pattern="[a-z0-9]+(?:-[a-z0-9]+)*"></label><label>Summary<input v-model="form!.summary" maxlength="240"></label><div class="offerings-surface__grid"><label>Price<input v-model.number="form!.basePriceAmount" type="number" min="0" step="0.01"></label><label>Currency<input v-model="form!.basePriceCurrency" required maxlength="3"></label><label>Duration (minutes)<input v-model.number="form!.defaultDurationMinutes" type="number" min="1"></label></div><label>Description<textarea v-model="form!.description" maxlength="4000"></textarea></label><label class="offerings-surface__toggle"><input v-model="form!.active" type="checkbox"> Active</label><p v-if="error" class="offerings-surface__status offerings-surface__status--error" role="alert">{{ error }}</p><div><button type="submit" class="offerings-surface__primary" :disabled="isSaving">{{ isSaving ? "Saving" : "Save" }}</button><button type="button" class="offerings-surface__secondary" @click="form = null">Cancel</button></div></form></AppDialog>
  </section>
</template>

<style scoped>
.offerings-surface{display:grid;gap:1rem;max-width:52rem}.offerings-surface__header{display:flex;justify-content:space-between;align-items:end;gap:1rem}.offerings-surface__eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}h2{margin:0;font-size:1.1rem}.offerings-surface__list{display:grid;gap:.45rem}.offerings-surface__row{display:flex;justify-content:space-between;gap:1rem;align-items:center;padding:.85rem 0;border-bottom:1px solid rgba(23,34,26,.08)}.offerings-surface__row>div:first-child{display:grid;gap:.28rem;min-width:0}.offerings-surface__row span{color:rgba(23,34,26,.58);font-size:.84rem}.offerings-surface__actions{display:flex;gap:.35rem}.offerings-surface button{border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.45rem .75rem;background:transparent;font:inherit;font-size:.82rem;font-weight:650;cursor:pointer;white-space:nowrap}.offerings-surface__primary{border:0!important;background:#17221a!important;color:#f8f8f4}.offerings-surface__danger{color:#8d2f25}.offerings-surface__secondary{margin-left:.4rem}.offerings-surface__form{display:grid;gap:.75rem;border-top:1px solid rgba(23,34,26,.1);padding-top:1rem}.offerings-surface__form label{display:grid;gap:.35rem;font-size:.83rem;font-weight:650;color:rgba(23,34,26,.72)}.offerings-surface__form input,.offerings-surface__form textarea{border:1px solid rgba(23,34,26,.14);border-radius:.65rem;padding:.65rem;background:rgba(255,255,255,.55);font:inherit;font-weight:400}.offerings-surface__form textarea{min-height:6rem}.offerings-surface__grid{display:grid;grid-template-columns:repeat(3,1fr);gap:.7rem}.offerings-surface__toggle{display:flex!important;align-items:center;gap:.5rem!important}.offerings-surface__toggle input{width:auto}.offerings-surface__feedback{margin:0;color:#2d6846}.offerings-surface__status{padding:.7rem 0;color:rgba(23,34,26,.65)}.offerings-surface__status--error{color:#8d2f25}.offerings-surface__status button{margin-left:.5rem;border:0;background:none;color:inherit;text-decoration:underline}@media(max-width:620px){.offerings-surface__row{align-items:start;flex-direction:column}.offerings-surface__grid{grid-template-columns:1fr}}
</style>
