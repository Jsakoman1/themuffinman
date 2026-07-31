<script setup lang="ts">
import type {CircleSearchResultDTO} from "../../../contracts/index.ts"
import AppButton from "./AppButton.vue"
import AppDialog from "./AppDialog.vue"
import AppFormField from "./AppFormField.vue"
import AppFormFooter from "./AppFormFooter.vue"

const props = defineProps<{open: boolean; directQuery: string; groupTitle: string; participantQuery: string; directCandidates: CircleSearchResultDTO[]; participantCandidates: CircleSearchResultDTO[]; selectedParticipantIds: number[]; searching: boolean; openingDirect: boolean; creatingGroup: boolean}>()
const emit = defineEmits<{close: []; "update:directQuery": [string]; "update:groupTitle": [string]; "update:participantQuery": [string]; searchDirect: []; searchParticipants: []; openDirect: [number]; toggleParticipant: [number]; createGroup: []}>()
const updateDirect = (event: Event) => { emit("update:directQuery", (event.target as HTMLInputElement).value); emit("searchDirect") }
const updateParticipant = (event: Event) => { emit("update:participantQuery", (event.target as HTMLInputElement).value); emit("searchParticipants") }
</script>

<template>
  <AppDialog :open="props.open" title="Start a conversation" layout="workspace" @close="emit('close')"><div class="chat-new-dialog"><section><h2>Message someone</h2><AppFormField label="Find a person"><input :value="props.directQuery" placeholder="Search people" @input="updateDirect"></AppFormField><p v-if="props.searching">Searching…</p><div class="chat-new-dialog__candidates"><AppButton v-for="candidate in props.directCandidates" :key="candidate.id" type="button" tone="secondary" :loading="props.openingDirect" @click="emit('openDirect', candidate.id)">{{ candidate.username }}</AppButton></div></section><section><h2>Create a group</h2><AppFormField label="Group name"><input :value="props.groupTitle" maxlength="120" placeholder="e.g. Weekend plans" @input="emit('update:groupTitle', ($event.target as HTMLInputElement).value)"></AppFormField><AppFormField label="Add people"><input :value="props.participantQuery" placeholder="Search people" @input="updateParticipant"></AppFormField><label v-for="candidate in props.participantCandidates" :key="candidate.id" class="chat-new-dialog__person"><input type="checkbox" :checked="props.selectedParticipantIds.includes(candidate.id)" @change="emit('toggleParticipant', candidate.id)">{{ candidate.username }}</label><AppFormFooter><template #primary><AppButton type="button" tone="primary" :loading="props.creatingGroup" :disabled="!props.groupTitle.trim() || !props.selectedParticipantIds.length" @click="emit('createGroup')">Create group</AppButton></template></AppFormFooter></section></div><template #utility><p>Start with one person, or create a group only when several people need the same conversation.</p></template></AppDialog>
</template>

<style scoped>
.chat-new-dialog{display:grid;gap:var(--space-5)}.chat-new-dialog section{display:grid;gap:var(--space-2)}.chat-new-dialog h2{margin:0;font-size:var(--text-size-title)}.chat-new-dialog p{margin:0;color:var(--text-muted);font-size:var(--text-size-meta)}.chat-new-dialog input:not([type=checkbox]){width:100%;box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.chat-new-dialog__candidates{display:flex;gap:var(--space-1);flex-wrap:wrap}.chat-new-dialog__person{display:flex;align-items:center;gap:var(--space-2);color:var(--text);font-size:var(--text-size-body)}
</style>
