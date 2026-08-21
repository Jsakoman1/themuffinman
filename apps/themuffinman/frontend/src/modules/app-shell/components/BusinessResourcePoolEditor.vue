<script setup lang="ts">
import {computed, ref} from "vue"
import type {BusinessResourceConfigurationDTO, BusinessResourceDTO, BusinessResourcePoolDTO, BusinessResourcePoolRequestDTO, BusinessResourceRequestDTO, BusinessResourceRequirementDTO, BusinessResourceRequirementRequestDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {confirmAction} from "../composables/useActionDialog.ts"
import AppButton from "./AppButton.vue"
import AppFormField from "./AppFormField.vue"
import AppStatus from "./AppStatus.vue"

const props = defineProps<{profileId: number; offeringId: number; configuration: BusinessResourceConfigurationDTO}>()
const emit = defineEmits<{updated: [configuration: BusinessResourceConfigurationDTO]}>()
type PoolDraft = BusinessResourcePoolRequestDTO
type ResourceDraft = BusinessResourceRequestDTO
type RequirementDraft = BusinessResourceRequirementRequestDTO
const newPool = (): PoolDraft => ({poolKey: "", label: "", resourceType: "EMPLOYEE", capacity: 1, publicLabel: "", active: true})
const newResource = (): ResourceDraft => ({resourcePoolId: null, resourceKey: "", label: "", resourceType: "EMPLOYEE", publicLabel: "", active: true, metadata: {}})
const newRequirement = (): RequirementDraft => ({businessOfferingId: props.offeringId, resourcePoolId: null, resourceType: "EMPLOYEE", requiredCount: 1, assignmentMode: "ANY_AVAILABLE"})
const poolDraft = ref<PoolDraft>(newPool())
const resourceDraft = ref<ResourceDraft>(newResource())
const requirementDraft = ref<RequirementDraft>(newRequirement())
const editingPoolId = ref<number | null>(null)
const editingResourceId = ref<number | null>(null)
const editingRequirementId = ref<number | null>(null)
const busyKey = ref("")
const error = ref("")
const feedback = ref("")
const selectedRequirements = computed(() => props.configuration.requirements.filter(item => item.businessOfferingId === props.offeringId))
const poolLabel = (poolId: number | null) => props.configuration.pools.find(item => item.id === poolId)?.label ?? "No pool"
const resourceTypeLabel = (value: string) => ({EMPLOYEE: "Staff", ROOM: "Room", EQUIPMENT: "Equipment", VEHICLE: "Vehicle", ANIMAL: "Animal", GENERIC: "Other"}[value] ?? value)
const cleanOptional = (value: string | null | undefined) => value?.trim() || null
const apply = (configuration: BusinessResourceConfigurationDTO, message: string) => { emit("updated", configuration); feedback.value = message; error.value = "" }
const run = async (key: string, fallback: string, action: () => Promise<BusinessResourceConfigurationDTO>, success: string) => {
  busyKey.value = key; error.value = ""; feedback.value = ""
  try { apply(await action(), success) } catch (cause) { error.value = userShellApi.actionFailureMessage(fallback, cause) } finally { busyKey.value = "" }
}
const savePool = async () => {
  const request = {...poolDraft.value, publicLabel: cleanOptional(poolDraft.value.publicLabel)}
  const id = editingPoolId.value
  await run(id ? `pool-${id}` : "pool-new", "Could not save this pool.", () => id ? userShellApi.updateBusinessResourcePool(props.profileId, id, request) : userShellApi.createBusinessResourcePool(props.profileId, request), id ? "Pool updated." : "Pool added.")
  if (!error.value) { editingPoolId.value = null; poolDraft.value = newPool() }
}
const editPool = (item: BusinessResourcePoolDTO) => { editingPoolId.value = item.id; poolDraft.value = {poolKey: item.poolKey, label: item.label, resourceType: item.resourceType, capacity: item.capacity, publicLabel: item.publicLabel ?? "", active: item.active} }
const togglePool = (item: BusinessResourcePoolDTO) => run(`pool-${item.id}`, "Could not update this pool.", () => userShellApi.updateBusinessResourcePool(props.profileId, item.id, {...item, active: !item.active}), item.active ? "Pool deactivated." : "Pool activated.")
const removePool = async (item: BusinessResourcePoolDTO) => {
  if (!await confirmAction(`Delete “${item.label}”? Its resources will become unassigned and requirements linked to this pool will be removed.`, "Delete resource pool")) return
  await run(`pool-${item.id}`, "Could not delete this pool.", () => userShellApi.deleteBusinessResourcePool(props.profileId, item.id), "Pool deleted.")
}
const saveResource = async () => {
  const request = {...resourceDraft.value, publicLabel: cleanOptional(resourceDraft.value.publicLabel)}
  const id = editingResourceId.value
  await run(id ? `resource-${id}` : "resource-new", "Could not save this resource.", () => id ? userShellApi.updateBusinessResource(props.profileId, id, request) : userShellApi.createBusinessResource(props.profileId, request), id ? "Resource updated." : "Resource added.")
  if (!error.value) { editingResourceId.value = null; resourceDraft.value = newResource() }
}
const editResource = (item: BusinessResourceDTO) => { editingResourceId.value = item.id; resourceDraft.value = {resourcePoolId: item.resourcePoolId, resourceKey: item.resourceKey, label: item.label, resourceType: item.resourceType, publicLabel: item.publicLabel ?? "", active: item.active, metadata: item.metadata} }
const toggleResource = (item: BusinessResourceDTO) => run(`resource-${item.id}`, "Could not update this resource.", () => userShellApi.updateBusinessResource(props.profileId, item.id, {...item, active: !item.active}), item.active ? "Resource deactivated. Existing booking history is unchanged." : "Resource activated.")
const removeResource = async (item: BusinessResourceDTO) => {
  if (!await confirmAction(`Delete “${item.label}”? If it belongs to booking history, it cannot be deleted; deactivate it instead.`, "Delete resource")) return
  await run(`resource-${item.id}`, "Could not delete this resource. If it is retained by a booking, deactivate it instead.", () => userShellApi.deleteBusinessResource(props.profileId, item.id), "Resource deleted.")
}
const saveRequirement = async () => {
  const request = {...requirementDraft.value, businessOfferingId: props.offeringId}
  const id = editingRequirementId.value
  await run(id ? `requirement-${id}` : "requirement-new", "Could not save this service requirement.", () => id ? userShellApi.updateBusinessResourceRequirement(props.profileId, id, request) : userShellApi.createBusinessResourceRequirement(props.profileId, request), id ? "Service requirement updated." : "Service requirement added.")
  if (!error.value) { editingRequirementId.value = null; requirementDraft.value = newRequirement() }
}
const editRequirement = (item: BusinessResourceRequirementDTO) => { editingRequirementId.value = item.id; requirementDraft.value = {businessOfferingId: item.businessOfferingId, resourcePoolId: item.resourcePoolId, resourceType: item.resourceType, requiredCount: item.requiredCount, assignmentMode: item.assignmentMode} }
const removeRequirement = async (item: BusinessResourceRequirementDTO) => {
  if (!await confirmAction("Remove this resource requirement from the service? Future bookings will no longer reserve it.", "Remove service requirement")) return
  await run(`requirement-${item.id}`, "Could not remove this service requirement.", () => userShellApi.deleteBusinessResourceRequirement(props.profileId, item.id), "Service requirement removed.")
}
</script>

<template>
  <section class="resource-editor" data-testid="business-resource-editor" data-resource-model="capacity-and-conflict-aware">
    <header class="resource-editor__intro"><div><h3>People, rooms and equipment</h3><p>Add only things that must be available before a customer can book this service.</p></div><p class="resource-editor__summary"><strong>{{ configuration.pools.length }}</strong> pools · <strong>{{ configuration.resources.length }}</strong> resources · <strong>{{ selectedRequirements.length }}</strong> for this service</p></header>
    <p class="resource-editor__note">The booking engine assigns an active matching resource automatically. Deactivating preserves booking history; deleting a pool unassigns its resources and removes requirements linked to that pool.</p>
    <AppStatus v-if="error" :message="error" tone="error" /><AppStatus v-else-if="feedback" :message="feedback" tone="success" />

    <section class="resource-editor__group" aria-labelledby="resource-pools-title">
      <header><div><h4 id="resource-pools-title">Resource pools</h4><p>Group interchangeable resources, such as treatment rooms or delivery vans.</p></div></header>
      <div v-if="configuration.pools.length" class="resource-editor__cards">
        <article v-for="item in configuration.pools" :key="item.id" class="resource-card" :data-testid="`resource-pool-${item.id}`">
          <form v-if="editingPoolId === item.id" class="resource-form resource-form--edit" @submit.prevent="savePool"><AppFormField label="Pool name" required><input v-model="poolDraft.label" required maxlength="160"></AppFormField><AppFormField label="Internal key" hint="A stable short name used by booking rules." required><input v-model="poolDraft.poolKey" required maxlength="80"></AppFormField><AppFormField label="Type" required><input v-model="poolDraft.resourceType" list="business-resource-types" required maxlength="40"></AppFormField><AppFormField label="Available at once" required><input v-model.number="poolDraft.capacity" type="number" min="1" required></AppFormField><AppFormField label="Customer-facing name" optional><input v-model="poolDraft.publicLabel" maxlength="160"></AppFormField><div class="resource-form__actions"><AppButton type="button" tone="secondary" @click="editingPoolId = null; poolDraft = newPool()">Cancel</AppButton><AppButton type="submit" tone="primary" :loading="busyKey === `pool-${item.id}`">Save pool</AppButton></div></form>
          <template v-else><div class="resource-card__body"><div class="resource-card__title"><h5>{{ item.label }}</h5><span class="resource-card__state" :class="{'resource-card__state--inactive': !item.active}">{{ item.active ? "Active" : "Inactive" }}</span></div><p>{{ resourceTypeLabel(item.resourceType) }} · {{ item.capacity }} available at once</p><small>Key: {{ item.poolKey }}<template v-if="item.publicLabel"> · Customers see “{{ item.publicLabel }}”</template></small></div><div class="resource-card__actions"><AppButton tone="secondary" @click="editPool(item)">Edit</AppButton><AppButton tone="secondary" :loading="busyKey === `pool-${item.id}`" @click="togglePool(item)">{{ item.active ? "Deactivate" : "Activate" }}</AppButton><AppButton tone="danger" :loading="busyKey === `pool-${item.id}`" @click="removePool(item)">Delete</AppButton></div></template>
        </article>
      </div>
      <p v-else class="resource-editor__empty">No pools yet. Add one when several resources can be used interchangeably.</p>
      <form class="resource-form" data-testid="add-resource-pool-form" @submit.prevent="savePool"><h5>Add a pool</h5><AppFormField label="Pool name" required><input v-model="poolDraft.label" required maxlength="160" placeholder="e.g. Treatment rooms"></AppFormField><AppFormField label="Internal key" hint="Use a stable short name, such as treatment-rooms." required><input v-model="poolDraft.poolKey" required maxlength="80" placeholder="treatment-rooms"></AppFormField><AppFormField label="Type" required><input v-model="poolDraft.resourceType" list="business-resource-types" required maxlength="40"></AppFormField><AppFormField label="Available at once" required><input v-model.number="poolDraft.capacity" type="number" min="1" required></AppFormField><AppFormField label="Customer-facing name" optional><input v-model="poolDraft.publicLabel" maxlength="160" placeholder="e.g. Treatment room"></AppFormField><div class="resource-form__actions"><AppButton type="submit" tone="secondary" :loading="busyKey === 'pool-new'">Add pool</AppButton></div></form>
    </section>

    <section class="resource-editor__group" aria-labelledby="resources-title">
      <header><div><h4 id="resources-title">Individual resources</h4><p>Add each staff member, room, vehicle or piece of equipment that can be reserved.</p></div></header>
      <div v-if="configuration.resources.length" class="resource-editor__cards">
        <article v-for="item in configuration.resources" :key="item.id" class="resource-card" :data-testid="`resource-${item.id}`">
          <form v-if="editingResourceId === item.id" class="resource-form resource-form--edit" @submit.prevent="saveResource"><AppFormField label="Resource name" required><input v-model="resourceDraft.label" required maxlength="160"></AppFormField><AppFormField label="Internal key" hint="A stable short name used by booking rules." required><input v-model="resourceDraft.resourceKey" required maxlength="80"></AppFormField><AppFormField label="Type" required><input v-model="resourceDraft.resourceType" list="business-resource-types" required maxlength="40"></AppFormField><AppFormField label="Pool" optional><select v-model="resourceDraft.resourcePoolId"><option :value="null">No pool</option><option v-for="poolItem in configuration.pools" :key="poolItem.id" :value="poolItem.id">{{ poolItem.label }}</option></select></AppFormField><AppFormField label="Customer-facing name" optional><input v-model="resourceDraft.publicLabel" maxlength="160"></AppFormField><div class="resource-form__actions"><AppButton type="button" tone="secondary" @click="editingResourceId = null; resourceDraft = newResource()">Cancel</AppButton><AppButton type="submit" tone="primary" :loading="busyKey === `resource-${item.id}`">Save resource</AppButton></div></form>
          <template v-else><div class="resource-card__body"><div class="resource-card__title"><h5>{{ item.label }}</h5><span class="resource-card__state" :class="{'resource-card__state--inactive': !item.active}">{{ item.active ? "Active" : "Inactive" }}</span></div><p>{{ resourceTypeLabel(item.resourceType) }} · {{ poolLabel(item.resourcePoolId) }}</p><small>Key: {{ item.resourceKey }}<template v-if="item.publicLabel"> · Customers see “{{ item.publicLabel }}”</template></small></div><div class="resource-card__actions"><AppButton tone="secondary" @click="editResource(item)">Edit</AppButton><AppButton tone="secondary" :loading="busyKey === `resource-${item.id}`" @click="toggleResource(item)">{{ item.active ? "Deactivate" : "Activate" }}</AppButton><AppButton tone="danger" :loading="busyKey === `resource-${item.id}`" @click="removeResource(item)">Delete</AppButton></div></template>
        </article>
      </div>
      <p v-else class="resource-editor__empty">No resources yet. Services without resource requirements continue to use normal availability and capacity.</p>
      <form class="resource-form" data-testid="add-resource-form" @submit.prevent="saveResource"><h5>Add a resource</h5><AppFormField label="Resource name" required><input v-model="resourceDraft.label" required maxlength="160" placeholder="e.g. Room A"></AppFormField><AppFormField label="Internal key" hint="Use a stable short name, such as room-a." required><input v-model="resourceDraft.resourceKey" required maxlength="80" placeholder="room-a"></AppFormField><AppFormField label="Type" required><input v-model="resourceDraft.resourceType" list="business-resource-types" required maxlength="40"></AppFormField><AppFormField label="Pool" optional><select v-model="resourceDraft.resourcePoolId"><option :value="null">No pool</option><option v-for="item in configuration.pools" :key="item.id" :value="item.id">{{ item.label }}</option></select></AppFormField><AppFormField label="Customer-facing name" optional><input v-model="resourceDraft.publicLabel" maxlength="160" placeholder="e.g. Private room"></AppFormField><div class="resource-form__actions"><AppButton type="submit" tone="secondary" :loading="busyKey === 'resource-new'">Add resource</AppButton></div></form>
    </section>

    <section class="resource-editor__group" aria-labelledby="requirements-title">
      <header><div><h4 id="requirements-title">Required for this service</h4><p>Choose what must be free for one booking. The system assigns any active matching resource automatically.</p></div></header>
      <div v-if="selectedRequirements.length" class="resource-editor__cards">
        <article v-for="item in selectedRequirements" :key="item.id" class="resource-card" :data-testid="`resource-requirement-${item.id}`">
          <form v-if="editingRequirementId === item.id" class="resource-form resource-form--requirement" @submit.prevent="saveRequirement"><AppFormField label="Pool" optional><select v-model="requirementDraft.resourcePoolId"><option :value="null">Any unpooled matching resource</option><option v-for="poolItem in configuration.pools" :key="poolItem.id" :value="poolItem.id">{{ poolItem.label }}</option></select></AppFormField><AppFormField label="Type" required><input v-model="requirementDraft.resourceType" list="business-resource-types" required maxlength="40"></AppFormField><AppFormField label="How many" required><input v-model.number="requirementDraft.requiredCount" type="number" min="1" required></AppFormField><div class="resource-form__actions"><AppButton type="button" tone="secondary" @click="editingRequirementId = null; requirementDraft = newRequirement()">Cancel</AppButton><AppButton type="submit" tone="primary" :loading="busyKey === `requirement-${item.id}`">Save requirement</AppButton></div></form>
          <template v-else><div class="resource-card__body"><h5>{{ item.requiredCount }} × {{ resourceTypeLabel(item.resourceType) }}</h5><p>{{ poolLabel(item.resourcePoolId) }} · automatically assigned</p><small v-if="item.assignmentMode !== 'ANY_AVAILABLE'">Saved allocation mode: {{ item.assignmentMode }}. This screen preserves it but does not create new fixed assignments.</small></div><div class="resource-card__actions"><AppButton tone="secondary" @click="editRequirement(item)">Edit</AppButton><AppButton tone="danger" :loading="busyKey === `requirement-${item.id}`" @click="removeRequirement(item)">Remove</AppButton></div></template>
        </article>
      </div>
      <p v-else class="resource-editor__empty">This service does not require a specific resource yet.</p>
      <form class="resource-form resource-form--requirement" data-testid="add-resource-requirement-form" @submit.prevent="saveRequirement"><h5>Add a requirement</h5><AppFormField label="Pool" optional><select v-model="requirementDraft.resourcePoolId"><option :value="null">Any unpooled matching resource</option><option v-for="item in configuration.pools" :key="item.id" :value="item.id">{{ item.label }}</option></select></AppFormField><AppFormField label="Type" required><input v-model="requirementDraft.resourceType" list="business-resource-types" required maxlength="40"></AppFormField><AppFormField label="How many" required><input v-model.number="requirementDraft.requiredCount" type="number" min="1" required></AppFormField><div class="resource-form__actions"><AppButton type="submit" tone="secondary" :loading="busyKey === 'requirement-new'">Add requirement</AppButton></div></form>
    </section>
    <datalist id="business-resource-types"><option value="EMPLOYEE">Staff</option><option value="ROOM">Room</option><option value="EQUIPMENT">Equipment</option><option value="VEHICLE">Vehicle</option><option value="ANIMAL">Animal</option><option value="GENERIC">Other</option></datalist>
  </section>
</template>

<style scoped>
.resource-editor{display:grid;gap:var(--space-4)}.resource-editor h3,.resource-editor h4,.resource-editor h5,.resource-editor p{margin:0}.resource-editor__intro{display:flex;align-items:end;justify-content:space-between;gap:var(--space-3)}.resource-editor__intro>div,.resource-editor__group>header>div{display:grid;gap:var(--space-1)}.resource-editor__intro p,.resource-editor__group header p,.resource-card p,.resource-card small,.resource-editor__empty{color:var(--text-muted);line-height:1.5}.resource-editor__summary{flex:0 0 auto;font-size:var(--text-size-meta)}.resource-editor__note{padding:var(--space-3);border-left:3px solid var(--accent);border-radius:var(--radius-control);background:var(--surface-muted);color:var(--text-muted);font-size:var(--text-size-meta);line-height:1.5}.resource-editor__group{display:grid;gap:var(--space-3);padding-top:var(--space-3);border-top:1px solid var(--border-subtle)}.resource-editor__group h4{font-size:var(--text-size-title)}.resource-editor__cards{display:grid;gap:var(--space-2)}.resource-card{display:flex;align-items:center;justify-content:space-between;gap:var(--space-3);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface-base)}.resource-card__body{display:grid;gap:.2rem;min-width:0}.resource-card__title{display:flex;align-items:center;gap:var(--space-2)}.resource-card__state{padding:.15rem .45rem;border-radius:999px;background:var(--success-muted);color:var(--success);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold)}.resource-card__state--inactive{background:var(--surface-muted);color:var(--text-soft)}.resource-card__actions,.resource-form__actions{display:flex;flex-wrap:wrap;justify-content:flex-end;gap:var(--space-1)}.resource-form{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:var(--space-2);align-items:end;padding:var(--space-3);border:1px dashed var(--border-strong);border-radius:var(--radius-control);background:var(--surface-muted)}.resource-form h5{grid-column:1/-1}.resource-form input,.resource-form select{width:100%;min-width:0;min-height:var(--control-height-default);box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.resource-form__actions{align-self:end}.resource-form--edit{width:100%;box-sizing:border-box;border:0;padding:0;background:transparent}.resource-form--requirement{grid-template-columns:repeat(3,minmax(0,1fr)) auto}.resource-editor__empty{font-size:var(--text-size-meta)}
@media(max-width:900px){.resource-form{grid-template-columns:repeat(2,minmax(0,1fr))}.resource-form--requirement{grid-template-columns:repeat(2,minmax(0,1fr))}.resource-form__actions{grid-column:1/-1;justify-content:start}}
@media(max-width:620px){.resource-editor__intro,.resource-card{align-items:stretch;flex-direction:column}.resource-editor__summary{flex:auto}.resource-card__actions{justify-content:start}.resource-form,.resource-form--requirement{grid-template-columns:1fr}.resource-form__actions{grid-column:auto}.resource-card__actions :deep(.app-button){flex:1}}
</style>
