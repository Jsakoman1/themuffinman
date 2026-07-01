<script setup lang="ts">
import {computed, onBeforeUnmount, ref, watch} from "vue"

const props = defineProps<{
  text: string
  active?: boolean
  speed?: number
}>()

const renderedText = ref("")
const completed = ref(false)
let timer: number | null = null

const clearTimer = () => {
  if (timer !== null) {
    window.clearInterval(timer)
    timer = null
  }
}

const startTyping = () => {
  clearTimer()
  const targetText = props.text ?? ""
  if (!props.active || !targetText.length) {
    renderedText.value = targetText
    completed.value = true
    return
  }

  renderedText.value = ""
  completed.value = false
  let index = 0
  const step = Math.max(8, props.speed ?? 18)
  timer = window.setInterval(() => {
    index += 1
    renderedText.value = targetText.slice(0, index)
    if (index >= targetText.length) {
      clearTimer()
      completed.value = true
    }
  }, step)
}

watch(
  () => [props.text, props.active, props.speed],
  () => {
    startTyping()
  },
  {immediate: true}
)

onBeforeUnmount(() => {
  clearTimer()
})

const showCursor = computed(() => !!props.active && !completed.value)
</script>

<template>
  <span class="vision-typing">
    <span>{{ renderedText }}</span>
    <span v-if="showCursor" class="vision-typing__cursor" aria-hidden="true"></span>
  </span>
</template>

<style scoped>
.vision-typing {
  display: inline-flex;
  align-items: baseline;
  gap: 0.14rem;
  white-space: pre-wrap;
}

.vision-typing__cursor {
  width: 0.55rem;
  height: 1.05em;
  border-radius: 999px;
  background: rgba(24, 36, 47, 0.8);
  display: inline-block;
  animation: visionTypingBlink 900ms steps(1) infinite;
  transform: translateY(0.12em);
}

@keyframes visionTypingBlink {
  0%,
  49% {
    opacity: 1;
  }

  50%,
  100% {
    opacity: 0;
  }
}
</style>
