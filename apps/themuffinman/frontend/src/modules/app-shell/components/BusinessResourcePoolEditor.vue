<script setup lang="ts">
import {ref} from "vue"
import {userShellApi} from "../api/userShellApi.ts"

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
  <section class="business-resource-editor">
    <h3>Resources and capacity</h3>
    <p>{{ props.pools.length }} pool(s), {{ props.resources.length }} resource(s), {{ props.requirements.length }} offering requirement(s)</p>
    <ul><li v-for="item in props.pools" :key="String(item.id)">{{ item.label }} · {{ item.resourceType }} · capacity {{ item.capacity }}</li></ul>
    <form @submit.prevent="save(() => userShellApi.createBusinessResourcePool(props.profileId, pool))">
      <h4>Add resource pool</h4><input v-model="pool.poolKey" required placeholder="Pool key"><input v-model="pool.label" required placeholder="Pool label"><input v-model="pool.resourceType" required placeholder="Resource type"><input v-model.number="pool.capacity" type="number" min="1" required placeholder="Capacity"><input v-model="pool.publicLabel" placeholder="Public label"><button :disabled="saving">Add pool</button>
    </form>
    <form @submit.prevent="save(() => userShellApi.createBusinessResource(props.profileId, resource))">
      <h4>Add resource</h4><select v-model="resource.resourcePoolId"><option :value="null">No pool</option><option v-for="item in props.pools" :key="String(item.id)" :value="item.id">{{ item.label }}</option></select><input v-model="resource.resourceKey" required placeholder="Resource key"><input v-model="resource.label" required placeholder="Resource label"><input v-model="resource.resourceType" required placeholder="Resource type"><input v-model="resource.publicLabel" placeholder="Public label"><button :disabled="saving">Add resource</button>
    </form>
    <form v-if="props.offeringId" @submit.prevent="save(() => userShellApi.createBusinessResourceRequirement(props.profileId, {...requirement, businessOfferingId: props.offeringId}))">
      <h4>Require resources for this offering</h4><select v-model="requirement.resourcePoolId"><option :value="null">Any matching resource</option><option v-for="item in props.pools" :key="String(item.id)" :value="item.id">{{ item.label }}</option></select><input v-model="requirement.resourceType" required placeholder="Resource type"><input v-model.number="requirement.requiredCount" type="number" min="1" required placeholder="Required count"><select v-model="requirement.assignmentMode"><option value="ANY_AVAILABLE">Any available</option><option value="FIXED">Fixed</option></select><button :disabled="saving">Save requirement</button>
    </form>
  </section>
</template>
