<script setup lang="ts">
import {ref} from "vue"
import {userShellApi} from "../api/userShellApi.ts"
import AppButton from "./AppButton.vue"

const props = defineProps<{profileId: number, offeringId: number | null, pools: Record<string, unknown>[], resources: Record<string, unknown>[], requirements: Record<string, unknown>[]}>()
const emit = defineEmits<{refresh: []}>()
const pool = ref({poolKey: "", label: "", resourceType: "EMPLOYEE", capacity: 1, publicLabel: ""})
const resource = ref({resourcePoolId: null as number | null, resourceKey: "", label: "", resourceType: "EMPLOYEE", publicLabel: ""})
const requirement = ref({resourcePoolId: null as number | null, resourceType: "EMPLOYEE", requiredCount: 1, assignmentMode: "ANY_AVAILABLE"})
// Keep resource setup actions on the owner surface so capacity rules are not hidden behind an admin-only path.
const saving = ref(false)
const save = async (action: () => Promise<unknown>) => { saving.value = true; try { await action(); emit("refresh") } finally { saving.value = false } }
</script>
<template>
  <section class="business-resource-editor" data-resource-model="capacity-and-conflict-aware">
    <h3>Resources and capacity</h3>
    <p>{{ props.pools.length }} pool(s), {{ props.resources.length }} resource(s), {{ props.requirements.length }} offering requirement(s)</p><p class="business-resource-editor__note">A slot is valid only when the backend can allocate the required capacity and resources. Conflicts are explained at booking time.</p>
    <ul><li v-for="item in props.pools" :key="String(item.id)">{{ item.label }} · {{ item.resourceType }} · capacity {{ item.capacity }}</li></ul>
    <form @submit.prevent="save(() => userShellApi.createBusinessResourcePool(props.profileId, pool))">
      <h4>Add resource pool</h4><input v-model="pool.poolKey" required placeholder="Pool key"><input v-model="pool.label" required placeholder="Pool label"><input v-model="pool.resourceType" required placeholder="Resource type"><input v-model.number="pool.capacity" type="number" min="1" required placeholder="Capacity"><input v-model="pool.publicLabel" placeholder="Public label"><AppButton type="submit" tone="secondary" :loading="saving">Add pool</AppButton>
    </form>
    <form @submit.prevent="save(() => userShellApi.createBusinessResource(props.profileId, resource))">
      <h4>Add resource</h4><select v-model="resource.resourcePoolId"><option :value="null">No pool</option><option v-for="item in props.pools" :key="String(item.id)" :value="item.id">{{ item.label }}</option></select><input v-model="resource.resourceKey" required placeholder="Resource key"><input v-model="resource.label" required placeholder="Resource label"><input v-model="resource.resourceType" required placeholder="Resource type"><input v-model="resource.publicLabel" placeholder="Public label"><AppButton type="submit" tone="secondary" :loading="saving">Add resource</AppButton>
    </form>
    <form v-if="props.offeringId" @submit.prevent="save(() => userShellApi.createBusinessResourceRequirement(props.profileId, {...requirement, businessOfferingId: props.offeringId}))">
      <h4>Require resources for this offering</h4><select v-model="requirement.resourcePoolId"><option :value="null">Any matching resource</option><option v-for="item in props.pools" :key="String(item.id)" :value="item.id">{{ item.label }}</option></select><input v-model="requirement.resourceType" required placeholder="Resource type"><input v-model.number="requirement.requiredCount" type="number" min="1" required placeholder="Required count"><select v-model="requirement.assignmentMode"><option value="ANY_AVAILABLE">Any available</option><option value="FIXED">Fixed</option></select><AppButton type="submit" tone="secondary" :loading="saving">Save requirement</AppButton>
    </form>
  </section>
</template>
<style scoped>
.business-resource-editor{display:grid;gap:var(--space-3)}.business-resource-editor h3,.business-resource-editor h4,.business-resource-editor p{margin:0}.business-resource-editor p{color:var(--text-muted)}.business-resource-editor__note{padding:var(--space-2) var(--space-3);border-left:2px solid var(--accent);background:var(--surface-muted);font-size:var(--text-size-meta)}.business-resource-editor ul{display:grid;gap:var(--space-1);margin:0;padding:0;list-style:none}.business-resource-editor li{padding:var(--space-2) 0;border-bottom:1px solid var(--border-subtle)}.business-resource-editor form{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:var(--space-2);align-items:end;padding-top:var(--space-3);border-top:1px solid var(--border-subtle)}.business-resource-editor form h4{grid-column:1/-1}.business-resource-editor input,.business-resource-editor select{min-width:0;min-height:var(--control-height-default);box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}@media(max-width:800px){.business-resource-editor form{grid-template-columns:1fr}.business-resource-editor form h4{grid-column:auto}}
</style>
