<script setup lang="ts">
import {computed, onMounted, reactive, ref} from "vue"
import UiAppShellPage from "../../../components/ui/UiAppShellPage.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import UiWorkspace from "../../../components/ui/UiWorkspace.vue"
import {businessApi, type BusinessProfile, type BusinessProfileRequest} from "../api/businessApi.ts"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"

const profiles = ref<BusinessProfile[]>([])
const selectedSlug = ref<string | null>(null)
const selectedProfile = ref<BusinessProfile | null>(null)
const myProfile = ref<BusinessProfile | null>(null)
const loading = ref(true)
const saving = ref(false)
const errorMessage = ref("")
const saveMessage = ref("")

const form = reactive<BusinessProfileRequest>({
  businessName: "",
  slug: "",
  headline: "",
  description: "",
  contactEmail: "",
  contactPhone: "",
  websiteUrl: "",
  active: true,
})

const selectedDirectoryProfile = computed(() => {
  if (!selectedSlug.value) {
    return profiles.value[0] ?? null
  }
  return profiles.value.find((profile) => profile.slug === selectedSlug.value) ?? profiles.value[0] ?? null
})

const hydrateForm = (profile: BusinessProfile | null) => {
  form.businessName = profile?.businessName ?? ""
  form.slug = profile?.slug ?? ""
  form.headline = profile?.headline ?? ""
  form.description = profile?.description ?? ""
  form.contactEmail = profile?.contactEmail ?? ""
  form.contactPhone = profile?.contactPhone ?? ""
  form.websiteUrl = profile?.websiteUrl ?? ""
  form.active = profile?.active ?? true
}

const loadBusinessHub = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    const [directory, mine] = await Promise.all([
      businessApi.getDirectory(),
      businessApi.getMyProfile(),
    ])
    profiles.value = directory.items
    myProfile.value = mine
    hydrateForm(mine)
    selectedSlug.value = mine?.slug ?? directory.items[0]?.slug ?? null
    selectedProfile.value = selectedDirectoryProfile.value
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, "Business Hub could not be loaded.")
  } finally {
    loading.value = false
  }
}

const selectProfile = async (profile: BusinessProfile) => {
  selectedSlug.value = profile.slug
  selectedProfile.value = profile
}

const saveProfile = async () => {
  saving.value = true
  errorMessage.value = ""
  saveMessage.value = ""
  try {
    const saved = await businessApi.saveMyProfile({
      ...form,
      slug: form.slug || null,
      headline: form.headline || null,
      description: form.description || null,
      contactEmail: form.contactEmail || null,
      contactPhone: form.contactPhone || null,
      websiteUrl: form.websiteUrl || null,
      active: form.active,
    })
    myProfile.value = saved
    hydrateForm(saved)
    await loadBusinessHub()
    selectedSlug.value = saved.slug
    selectedProfile.value = saved
    saveMessage.value = "Business profile saved."
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, "Business profile could not be saved.")
  } finally {
    saving.value = false
  }
}

onMounted(loadBusinessHub)
</script>

<template>
  <UiAppShellPage page-class="business-page" eyebrow="Business Hub" title="Business profiles">
    <UiRequestError
      v-if="errorMessage"
      :message="errorMessage"
      :details="[errorMessage]"
      summary="Business Hub error details"
      :copied="false"
    />

    <UiWorkspace variant="detail">
      <UiSurfaceSection title="Directory" subtitle="Active local businesses">
        <div v-if="loading" class="empty-state empty-state--compact">Loading business profiles.</div>
        <div v-else-if="profiles.length === 0" class="empty-state empty-state--compact">No active business profiles yet.</div>
        <div v-else class="business-directory">
          <button
            v-for="profile in profiles"
            :key="profile.id"
            type="button"
            class="business-directory__item"
            :class="{ 'business-directory__item--active': selectedDirectoryProfile?.id === profile.id }"
            @click="selectProfile(profile)"
          >
            <strong>{{ profile.businessName }}</strong>
            <span>{{ profile.headline || profile.ownerUsername }}</span>
          </button>
        </div>
      </UiSurfaceSection>

      <UiSurfaceSection title="Mini site" subtitle="Public business profile">
        <div v-if="selectedProfile" class="business-profile">
          <div>
            <p class="page-subtitle">{{ selectedProfile.ownerUsername }}</p>
            <h2>{{ selectedProfile.businessName }}</h2>
            <p v-if="selectedProfile.headline" class="business-profile__headline">{{ selectedProfile.headline }}</p>
          </div>
          <div v-if="selectedProfile.description" class="business-profile__description" v-html="selectedProfile.description"></div>
          <div class="business-contact-grid">
            <a v-if="selectedProfile.contactEmail" :href="`mailto:${selectedProfile.contactEmail}`">{{ selectedProfile.contactEmail }}</a>
            <a v-if="selectedProfile.contactPhone" :href="`tel:${selectedProfile.contactPhone}`">{{ selectedProfile.contactPhone }}</a>
            <a v-if="selectedProfile.websiteUrl" :href="selectedProfile.websiteUrl" target="_blank" rel="noreferrer">{{ selectedProfile.websiteUrl }}</a>
          </div>
        </div>
        <div v-else class="empty-state empty-state--compact">Select a business profile.</div>
      </UiSurfaceSection>

      <UiSurfaceSection title="My business" subtitle="One profile per account">
        <form class="business-form" @submit.prevent="saveProfile">
          <label class="field">
            <span>Business name</span>
            <input v-model="form.businessName" class="input" required maxlength="120" />
          </label>
          <label class="field">
            <span>Slug</span>
            <input v-model="form.slug" class="input" maxlength="140" placeholder="auto-generated if blank" />
          </label>
          <label class="field">
            <span>Headline</span>
            <input v-model="form.headline" class="input" maxlength="160" />
          </label>
          <label class="field">
            <span>Description</span>
            <textarea v-model="form.description" class="input business-form__textarea" maxlength="4000"></textarea>
          </label>
          <label class="field">
            <span>Email</span>
            <input v-model="form.contactEmail" class="input" type="email" maxlength="160" />
          </label>
          <label class="field">
            <span>Phone</span>
            <input v-model="form.contactPhone" class="input" maxlength="80" />
          </label>
          <label class="field">
            <span>Website</span>
            <input v-model="form.websiteUrl" class="input" maxlength="300" />
          </label>
          <label class="checkbox-row">
            <input v-model="form.active" type="checkbox" />
            <span>Visible in directory</span>
          </label>
          <div class="form-actions">
            <button class="button button--primary" type="submit" :disabled="saving">
              {{ saving ? "Saving..." : "Save profile" }}
            </button>
            <span v-if="saveMessage" class="muted">{{ saveMessage }}</span>
          </div>
        </form>
      </UiSurfaceSection>
    </UiWorkspace>
  </UiAppShellPage>
</template>
