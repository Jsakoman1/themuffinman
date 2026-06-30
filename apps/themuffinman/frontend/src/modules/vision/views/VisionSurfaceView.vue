<script setup lang="ts">
import {computed} from "vue"
import {useMountedAsync} from "../../../composables/useMountedAsync.ts"
import {useVisionSurface, type VisionSurfaceMode, type VisionVoiceState} from "../composables/useVisionSurface.ts"

const {
  isLoading,
  error,
  voiceState,
  surfaceMode,
  showAllResults,
  activeFilter,
  recognizedPrompt,
  filteredCards,
  visibleJobsLabel,
  voiceEnabled,
  speechToTextEnabled,
  textToSpeechEnabled,
  speechRecognitionSupported,
  speechSynthesisSupported,
  speechStatusLabel,
  speechSummary,
  init,
  openQuest,
  startListening,
  stopListening,
  speakSummary,
  stopSpeaking
} = useVisionSurface()

const voiceStates: {key: VisionVoiceState; label: string}[] = [
  {key: "idle", label: "Idle"},
  {key: "listening", label: "Listening"},
  {key: "processing", label: "Processing"},
  {key: "speaking", label: "Speaking"}
]

const surfaceModes: {key: VisionSurfaceMode; label: string}[] = [
  {key: "browse", label: "Browse"},
  {key: "compare", label: "Compare"},
  {key: "focus", label: "Focus"}
]

const voiceCopy = computed(() => {
  if (voiceState.value === "listening") {
    return {
      eyebrow: "Listening",
      title: "I am following your words and confirming them visually.",
      detail: "Speech should never be the only source of truth. The screen mirrors what was heard so noise and ambiguity are easy to catch."
    }
  }

  if (voiceState.value === "processing") {
    return {
      eyebrow: "Processing",
      title: "The canvas is compressing intent into useful structure.",
      detail: "Large result sets should narrow themselves first, then open only when you ask."
    }
  }

  if (voiceState.value === "speaking") {
    return {
      eyebrow: "Speaking",
      title: "The response stays audible and visible at the same time.",
      detail: "Complex information is easier to scan as cards, cues, and highlights than as audio alone."
    }
  }

  return {
    eyebrow: "Ready",
    title: "The surface stays quiet until there is a useful next step.",
    detail: "Blank space is intentional. The screen should not compete with the task."
  }
})

useMountedAsync(init)
</script>

<template>
  <section class="vision-surface">
    <div class="vision-surface__orb vision-surface__orb--left" aria-hidden="true"></div>
    <div class="vision-surface__orb vision-surface__orb--right" aria-hidden="true"></div>

    <div class="vision-surface__shell">
      <header class="vision-surface__hero">
        <div class="vision-surface__intro">
          <p class="vision-surface__eyebrow">Adaptive Canvas Prototype</p>
          <h1 class="vision-surface__title">One long-term screen that listens, responds, filters, and stays visually useful.</h1>
          <p class="vision-surface__summary">
            This route does not replace the legacy screens yet. It is the first live surface for the product direction:
            parallel audio and visual feedback, full-screen intent handling, and fewer rigid forms.
          </p>
        </div>

        <div class="vision-surface__controls">
          <div class="vision-surface__control-group">
            <span class="vision-surface__control-label">Voice controls</span>
            <div class="vision-surface__chips">
              <button
                type="button"
                class="vision-surface__chip"
                :class="{'vision-surface__chip--active': voiceState === 'listening'}"
                :disabled="!voiceEnabled || !speechToTextEnabled || !speechRecognitionSupported"
                @click="startListening"
              >
                Start listening
              </button>
              <button
                type="button"
                class="vision-surface__chip"
                :disabled="voiceState !== 'listening'"
                @click="stopListening"
              >
                Stop listening
              </button>
              <button
                type="button"
                class="vision-surface__chip"
                :class="{'vision-surface__chip--active': voiceState === 'speaking'}"
                :disabled="!voiceEnabled || !textToSpeechEnabled || !speechSynthesisSupported"
                @click="speakSummary"
              >
                Speak summary
              </button>
              <button
                type="button"
                class="vision-surface__chip"
                :disabled="voiceState !== 'speaking'"
                @click="stopSpeaking"
              >
                Stop speaking
              </button>
            </div>
            <p class="vision-surface__status-text">{{ speechStatusLabel }}</p>
          </div>

          <div class="vision-surface__control-group">
            <span class="vision-surface__control-label">Surface mode</span>
            <div class="vision-surface__chips">
              <button
                v-for="option in surfaceModes"
                :key="option.key"
                type="button"
                class="vision-surface__chip"
                :class="{'vision-surface__chip--active': surfaceMode === option.key}"
                @click="surfaceMode = option.key"
              >
                {{ option.label }}
              </button>
            </div>
          </div>
        </div>
      </header>

      <div class="vision-surface__workspace">
        <section class="vision-surface__signal-panel">
          <div class="vision-surface__signal-ring" :class="`vision-surface__signal-ring--${voiceState}`">
            <span class="vision-surface__signal-core"></span>
          </div>

          <div class="vision-surface__signal-copy">
            <p class="vision-surface__eyebrow">{{ voiceCopy.eyebrow }}</p>
            <h2>{{ voiceCopy.title }}</h2>
            <p>{{ voiceCopy.detail }}</p>
            <div class="vision-surface__voice-capabilities">
              <span :class="['vision-surface__capability-pill', {'vision-surface__capability-pill--available': voiceEnabled && speechToTextEnabled && speechRecognitionSupported}]">
                STT
              </span>
              <span :class="['vision-surface__capability-pill', {'vision-surface__capability-pill--available': voiceEnabled && textToSpeechEnabled && speechSynthesisSupported}]">
                TTS
              </span>
              <span class="vision-surface__capability-pill vision-surface__capability-pill--neutral">
                {{ voiceStates.find((item) => item.key === voiceState)?.label ?? "Idle" }}
              </span>
            </div>
          </div>

          <div class="vision-surface__transcript">
            <span class="vision-surface__transcript-label">Recognized intent</span>
            <textarea
              v-model="recognizedPrompt"
              class="vision-surface__transcript-input"
              rows="4"
            ></textarea>
          </div>
        </section>

        <section class="vision-surface__data-panel">
          <div v-if="error" class="vision-surface__inline-error">
            {{ error }}
          </div>

          <div class="vision-surface__data-header">
            <div>
              <p class="vision-surface__eyebrow">Curated result layer</p>
              <h2>Useful first, exhaustive only on demand.</h2>
            </div>

            <div class="vision-surface__chips">
              <button
                type="button"
                class="vision-surface__chip"
                :class="{'vision-surface__chip--active': activeFilter === 'best-match'}"
                @click="activeFilter = 'best-match'"
              >
                Best match
              </button>
              <button
                type="button"
                class="vision-surface__chip"
                :class="{'vision-surface__chip--active': activeFilter === 'today'}"
                @click="activeFilter = 'today'"
              >
                Today
              </button>
              <button
                type="button"
                class="vision-surface__chip"
                :class="{'vision-surface__chip--active': activeFilter === 'nearby'}"
                @click="activeFilter = 'nearby'"
              >
                Nearby
              </button>
              <button
                type="button"
                class="vision-surface__chip"
                :class="{'vision-surface__chip--active': showAllResults}"
                @click="showAllResults = !showAllResults"
              >
                {{ showAllResults ? "Curated only" : "Show more" }}
              </button>
            </div>
          </div>

          <div class="vision-surface__stats">
            <article class="vision-surface__stat-card">
              <span>Visible jobs</span>
              <strong>{{ visibleJobsLabel }}</strong>
            </article>
            <article class="vision-surface__stat-card">
              <span>Surface mode</span>
              <strong>{{ surfaceMode }}</strong>
            </article>
            <article class="vision-surface__stat-card">
              <span>Audio model</span>
              <strong>Parallel AV</strong>
            </article>
          </div>

          <article class="vision-surface__summary-card">
            <span class="vision-surface__summary-label">Spoken summary</span>
            <p>{{ speechSummary }}</p>
          </article>

          <div v-if="isLoading" class="vision-surface__empty-state">Loading adaptive work surface...</div>
          <div v-else-if="!filteredCards.length" class="vision-surface__empty-state">
            No matching jobs for this current filter state.
          </div>
          <div v-else class="vision-surface__cards" :class="`vision-surface__cards--${surfaceMode}`">
            <article
              v-for="card in filteredCards"
              :key="card.id"
              class="vision-surface__job-card"
              :class="`vision-surface__job-card--${card.tone}`"
              @click="openQuest(card)"
            >
              <div class="vision-surface__job-topline">
                <span class="vision-surface__job-payoff">{{ card.payoff }}</span>
                <span class="vision-surface__job-distance">{{ card.distance }}</span>
              </div>
              <h3>{{ card.title }}</h3>
              <p class="vision-surface__job-schedule">{{ card.schedule }}</p>
              <p class="vision-surface__job-trust">{{ card.trust }}</p>
              <div class="vision-surface__job-tags">
                <span v-for="tag in card.tags" :key="tag">{{ tag }}</span>
              </div>
            </article>
          </div>
        </section>
      </div>
    </div>
  </section>
</template>
