<script setup lang="ts">
import {computed} from "vue"
import type {VisionConversationTurnResponse} from "../api/visionConversationApi.ts"
import type {VisionVoiceState} from "../composables/useVisionConversation.ts"
import VisionTerminalRow from "./VisionTerminalRow.vue"

const props = defineProps<{
  response: VisionConversationTurnResponse | null
  lastTranscript: string
  transcriptTargetLabel: string
  transcriptTargetDetail: string
  currentSlotLabel: string
  currentSlotValue: string
  currentFieldKind: string
  speechStatusLabel: string
  voiceState: VisionVoiceState
}>()

const rows = computed(() => [
  {label: "State", value: props.speechStatusLabel},
  {label: "Route", value: props.transcriptTargetLabel || "Prompt"},
  {label: "Slot", value: props.currentSlotLabel ? `${props.currentSlotLabel}${props.currentSlotValue ? ` · ${props.currentSlotValue}` : ""}` : "None"},
  {label: "Transcript", value: props.lastTranscript.trim() || "None"}
])

const visible = computed(() => !!props.response || !!props.lastTranscript.trim() || props.voiceState !== "idle")
</script>

<template>
  <section v-if="visible" class="vision-flow">
    <div class="vision-flow__terminal">
      <VisionTerminalRow
        v-for="row in rows"
        :key="row.label"
        :label="row.label"
        :value="row.value"
        tone="muted"
      />
    </div>
  </section>
</template>

<style scoped>
.vision-flow {
  display: grid;
  gap: 0.25rem;
  pointer-events: auto;
}

.vision-flow__terminal {
  display: grid;
  gap: 0.06rem;
  padding: 0.35rem 0.1rem 0.1rem;
}
</style>
