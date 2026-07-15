<script setup lang="ts">
import {onMounted, ref} from "vue"
import type {BusinessProfileRequestDTO, BusinessProfileResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"

const form = ref<BusinessProfileRequestDTO | null>(null)
const isLoading = ref(true)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")

const load = async () => {
  isLoading.value = true; error.value = ""
  try {
    const value: BusinessProfileResponseDTO = await userShellApi.getBusinessProfile()
    form.value = {businessName: value.businessName, slug: value.slug, headline: value.headline, description: value.description, contactEmail: value.contactEmail, contactPhone: value.contactPhone, websiteUrl: value.websiteUrl, timezone: value.timezone, bookingEnabled: value.bookingEnabled, publicAddressLabel: value.publicAddressLabel, latitude: value.latitude, longitude: value.longitude, contactWhatsapp: value.contactWhatsapp, heroImageUrl: value.heroImageUrl, active: value.active}
  } catch { error.value = "Could not load your business profile." }
  finally { isLoading.value = false }
}

const save = async () => {
  if (!form.value) return
  isSaving.value = true; error.value = ""; feedback.value = ""
  try { await userShellApi.updateBusinessProfile(form.value); feedback.value = "Profile updated." }
  catch { error.value = "Could not update your business profile." }
  finally { isSaving.value = false }
}

onMounted(() => void load())
</script>

<template>
  <section class="business-profile">
    <header><p class="business-profile__eyebrow">Business / Profile</p><h1>Profile</h1><p class="business-profile__intro">Keep the public identity and booking switch in one place.</p></header>
    <div v-if="isLoading" class="business-profile__status" role="status">Loading.</div>
    <div v-else-if="error && !form" class="business-profile__status business-profile__status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div>
    <form v-else-if="form" class="business-profile__form" @submit.prevent="save">
      <label>Business name<input v-model="form.businessName" required maxlength="160"></label>
      <label>Headline<input v-model="form.headline" maxlength="200"></label>
      <label>Description<textarea v-model="form.description" maxlength="4000"></textarea></label>
      <div class="business-profile__grid"><label>Contact email<input v-model="form.contactEmail" type="email"></label><label>Contact phone<input v-model="form.contactPhone"></label></div>
      <label>Public address label<input v-model="form.publicAddressLabel"></label>
      <label class="business-profile__toggle"><input v-model="form.bookingEnabled" type="checkbox"> Accept bookings</label>
      <p v-if="feedback" class="business-profile__feedback" role="status">{{ feedback }}</p><p v-if="error" class="business-profile__status business-profile__status--error" role="alert">{{ error }}</p>
      <button class="business-profile__save" type="submit" :disabled="isSaving">{{ isSaving ? "Saving" : "Save profile" }}</button>
    </form>
  </section>
</template>

<style scoped>
.business-profile{display:grid;gap:1rem;max-width:44rem}.business-profile__eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.business-profile__intro{margin:.45rem 0 0;color:rgba(23,34,26,.6)}.business-profile__form{display:grid;gap:.8rem}.business-profile__form label{display:grid;gap:.35rem;font-size:.83rem;font-weight:650;color:rgba(23,34,26,.72)}.business-profile__form input,.business-profile__form textarea{width:100%;border:1px solid rgba(23,34,26,.14);border-radius:.65rem;padding:.65rem;background:rgba(255,255,255,.55);font:inherit;font-weight:400}.business-profile__form textarea{min-height:7rem;resize:vertical}.business-profile__grid{display:grid;grid-template-columns:1fr 1fr;gap:.8rem}.business-profile__toggle{display:flex!important;align-items:center;gap:.5rem!important}.business-profile__toggle input{width:auto}.business-profile__save{justify-self:start;border:0;border-radius:999px;padding:.65rem 1rem;background:#17221a;color:#f8f8f4;font-weight:650;cursor:pointer}.business-profile__save:disabled{opacity:.55;cursor:wait}.business-profile__feedback{margin:0;color:#2d6846}.business-profile__status{padding:.7rem 0;color:rgba(23,34,26,.65)}.business-profile__status--error{color:#8d2f25}.business-profile__status button{margin-left:.5rem;border:0;background:none;color:inherit;text-decoration:underline;cursor:pointer}@media(max-width:620px){.business-profile__grid{grid-template-columns:1fr}}
</style>
