<script setup lang="ts">
import {computed} from "vue"
import type {VisionConversationTurnResponse} from "../api/visionConversationApi.ts"
import type {VisionExecutionCandidate} from "../api/visionConversationApi.ts"
import VisionTerminalRow from "./VisionTerminalRow.vue"
import {
  previewFieldOrderByFamily,
  previewPrimaryFieldIdsByVariant,
  previewSecondaryFieldIdsByVariant,
  resolveVisionFamily
} from "../visionPresentation.ts"
import {
  buildVisionPreviewFields,
  selectVisionPreviewBlock
} from "../visionPreviewSupport.ts"

const props = defineProps<{
  response: VisionConversationTurnResponse | null
  executionCandidate: VisionExecutionCandidate | null
  visible: boolean
}>()

type IntentField = {
  slotId: string
  label: string
  value: string
}

const previewBlock = computed(() => selectVisionPreviewBlock(props.response))

const fieldValues = computed<IntentField[]>(() => buildVisionPreviewFields(props.response))

const filledCount = computed(() => fieldValues.value.filter((field) => field.value.trim().length > 0).length)
const totalCount = computed(() => fieldValues.value.length)

const isComplete = computed(() => {
  const mode = props.response?.canvasMode

  return mode === "review" || mode === "complete" || mode === "results"
})

const activeEntityFamily = computed(() => {
  return resolveVisionFamily(props.response?.intent ?? undefined, props.response?.memoryTrail?.activeEntityFamily ?? undefined)
})

const previewVariant = computed(() => {
  if (!activeEntityFamily.value) {
    return "generic"
  }
  return activeEntityFamily.value
})

const previewFlowLabel = computed(() => {
  const family = previewVariant.value
  if (!props.response?.intent?.trim()) {
    return family
  }
  if (!family || family === "generic") {
    return props.response.intent.toLowerCase().replaceAll("_", " ")
  }
  return family
})

const previewVariantClass = computed(() => previewVariant.value.replaceAll(" ", "-"))

const orderedFieldValues = computed(() => {
  const family = activeEntityFamily.value
  const order = family ? previewFieldOrderByFamily[family] ?? [] : []
  const rank = new Map(order.map((slotId, index) => [slotId, index]))
  return [...fieldValues.value].sort((left, right) => {
    const leftRank = rank.get(left.slotId) ?? 999
    const rightRank = rank.get(right.slotId) ?? 999
    if (leftRank !== rightRank) {
      return leftRank - rightRank
    }
    return left.label.localeCompare(right.label)
  })
})

const fieldBySlotId = computed(() => new Map(fieldValues.value.map((field) => [field.slotId, field])))

const fieldsForIds = (slotIds: string[]) => slotIds
  .map((slotId) => fieldBySlotId.value.get(slotId))
  .filter((field): field is IntentField => Boolean(field))

const visibleFields = computed(() => orderedFieldValues.value.filter((field) => field.value.trim().length > 0 || !isComplete.value))

const previewSummary = computed(() => previewBlock.value?.body?.trim() ?? "")

const variantPrimaryFields = computed(() => fieldsForIds(previewPrimaryFieldIdsByVariant[previewVariant.value] ?? []).slice(0, 4))

const variantSecondaryFields = computed(() => fieldsForIds(previewSecondaryFieldIdsByVariant[previewVariant.value] ?? []).slice(0, 1))

const variantSupplementaryFields = computed(() => {
  const primaryIds = new Set(previewPrimaryFieldIdsByVariant[previewVariant.value] ?? [])
  const secondaryIds = new Set(previewSecondaryFieldIdsByVariant[previewVariant.value] ?? [])
  return orderedFieldValues.value.filter((field) => !primaryIds.has(field.slotId) && !secondaryIds.has(field.slotId))
})

const hasStructuredPreview = computed(() => fieldValues.value.length > 0)
const shouldShowSummary = computed(() => {
  if (!previewSummary.value) {
    return false
  }
  return previewBlock.value?.type === "result_summary" || previewBlock.value?.type === "review_summary"
})
</script>

<template>
  <section class="vision-intent" :class="{
    'vision-intent--visible': visible,
    'vision-intent--ready': filledCount === totalCount,
    'vision-intent--done': isComplete,
    [`vision-intent--${previewVariantClass}`]: previewVariant !== 'generic'
  }">
    <article class="vision-intent__terminal">
      <VisionTerminalRow
        label="Preview"
        :value="previewFlowLabel || 'Active flow'"
        tone="muted"
      />
      <VisionTerminalRow
        v-if="shouldShowSummary"
        label="Summary"
        :value="previewSummary"
        tone="muted"
      />

      <div v-if="hasStructuredPreview" class="vision-intent__sheet">
        <VisionTerminalRow
          v-for="field in variantPrimaryFields"
          :key="field.slotId"
          :label="field.label"
          :value="field.value.trim().length > 0 ? field.value : '—'"
          :active="visible && !isComplete && field.value.trim().length > 0"
          :animate="field.value.trim().length > 0"
          :tone="field.value.trim().length > 0 ? 'strong' : 'muted'"
        />
        <VisionTerminalRow
          v-for="field in variantSecondaryFields"
          :key="`secondary-${field.slotId}`"
          :label="field.label"
          :value="field.value.trim().length > 0 ? field.value : '—'"
          :active="visible && !isComplete && field.value.trim().length > 0"
          :animate="field.value.trim().length > 0"
          :tone="field.value.trim().length > 0 ? 'strong' : 'muted'"
        />
        <details v-if="variantSupplementaryFields.length" class="vision-intent__more">
          <summary class="vision-intent__more-title">More ({{ variantSupplementaryFields.length }})</summary>
          <div class="vision-intent__more-body">
            <VisionTerminalRow
              v-for="field in variantSupplementaryFields"
              :key="`draft-${field.slotId}`"
              :label="field.label"
              :value="field.value.trim().length > 0 ? field.value : '—'"
              :active="visible && !isComplete && field.value.trim().length > 0"
              :animate="field.value.trim().length > 0"
              :tone="field.value.trim().length > 0 ? 'strong' : 'muted'"
            />
          </div>
        </details>
      </div>

      <div v-else class="vision-intent__field-stack">
        <VisionTerminalRow
          v-for="field in visibleFields"
          :key="field.slotId"
          :label="field.label"
          :value="field.value.trim().length > 0 ? field.value : '—'"
          :active="visible && !isComplete && field.value.trim().length > 0"
          :animate="field.value.trim().length > 0"
          :tone="field.value.trim().length > 0 ? 'strong' : 'muted'"
        />
      </div>
    </article>
  </section>
</template>

<style scoped>
.vision-intent {
  position: relative;
  width: 100%;
  min-height: 0;
  display: grid;
  justify-items: end;
  align-items: start;
  padding: 0;
  pointer-events: none;
}

.vision-intent--visible {
  opacity: 1;
}

.vision-intent--done {
  opacity: 0.14;
}

.vision-intent__terminal {
  width: min(31rem, 44vw);
  display: grid;
  gap: 0.08rem;
  padding: 0.35rem 0.1rem 0.1rem;
  border-left: 1px solid rgba(24, 36, 47, 0.08);
  opacity: 0;
  transform: translateY(0.2rem);
  transition: opacity 180ms ease, transform 180ms ease;
}

.vision-intent--visible .vision-intent__terminal {
  opacity: 1;
  transform: translateY(0);
}

.vision-intent--done .vision-intent__terminal {
  opacity: 0;
  transform: translateY(0.15rem);
}

.vision-intent__sheet {
  display: grid;
  gap: 0.04rem;
}

.vision-intent__more {
  display: grid;
  gap: 0.08rem;
  padding-top: 0.1rem;
  margin-top: 0.08rem;
  border-top: 1px solid rgba(24, 36, 47, 0.05);
}

.vision-intent__more-title {
  cursor: pointer;
  list-style: none;
  color: rgba(24, 36, 47, 0.44);
  font-size: 0.58rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  padding: 0.05rem 0 0.1rem;
}

.vision-intent__more-body {
  display: grid;
  gap: 0.08rem;
}

.vision-intent--ready .vision-intent__terminal {
  box-shadow: none;
}
</style>
