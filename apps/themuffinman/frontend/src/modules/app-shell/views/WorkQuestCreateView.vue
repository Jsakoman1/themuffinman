<script setup lang="ts">
import {ref} from "vue"
import {RouterLink, useRouter} from "vue-router"
import type {QuestRequestDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {buildVisionRoute} from "../visionHandoff.ts"
import RichTextEditor from "../components/RichTextEditor.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"

const router = useRouter()
const form = ref<QuestRequestDTO>({title: "", description: "", awardAmount: 0, termFixed: false})
const isSaving = ref(false)
const error = ref("")
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
    <form class="quest-create__form" @submit.prevent="save">
      <AppFormField label="Title" hint="Use a short, outcome-focused title." required><input v-model="form.title" required maxlength="255" placeholder="What needs doing?"></AppFormField>
      <AppFormField label="Description" hint="Add the useful detail."><RichTextEditor v-model="form.description" label="Quest description" placeholder="Add the useful detail." /></AppFormField>
      <AppFormField label="Award" hint="Enter the amount offered." required><input v-model.number="form.awardAmount" type="number" min="0" step="0.01" required></AppFormField>
      <label class="quest-create__checkbox"><input v-model="form.termFixed" type="checkbox"> Fixed terms</label>
      <p v-if="error" class="quest-create__error" role="alert">{{ error }}</p>
      <AppFormFooter><template #secondary><RouterLink to="/work/quests">Cancel</RouterLink></template><template #primary><button type="submit" :disabled="isSaving">{{ isSaving ? "Creating" : "Create quest" }}</button></template></AppFormFooter>
    </form>
  </section>
</template>

<style scoped>
.quest-create{display:grid;gap:1rem}.quest-create__header{display:flex;justify-content:space-between;align-items:end;gap:1rem}.quest-create__eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.quest-create__vision,.quest-create__actions a,.quest-create__actions button{display:inline-flex;align-items:center;justify-content:center;min-height:2.3rem;border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.45rem .8rem;font-size:.82rem;font-weight:650;background:transparent}.quest-create__vision,.quest-create__actions button{background:#17221a;color:#f8f8f4}.quest-create__form{display:grid;gap:.85rem;max-width:44rem;padding:1rem;border:1px solid rgba(23,34,26,.08);border-radius:1rem;background:rgba(255,255,255,.62)}.quest-create__form label{display:grid;gap:.3rem;font-size:.82rem;font-weight:650}.quest-create__form input,.quest-create__form textarea{width:100%;border:1px solid rgba(23,34,26,.14);border-radius:.7rem;padding:.7rem;font:inherit}.quest-create__form textarea{min-height:10rem;resize:vertical}.quest-create__actions{display:flex;justify-content:flex-end;gap:.45rem}.quest-create__error{margin:0;color:#8d2f25}@media(max-width:700px){.quest-create__header{align-items:start;flex-direction:column}}
</style>
