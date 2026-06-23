<script setup lang="ts">
defineProps<{
  message: string
  details: string[]
  summary: string
  copied: boolean
}>()

defineEmits<{
  copy: []
}>()
</script>

<template>
  <div v-if="message" class="alert alert--error">
    <div>{{ message }}</div>
    <details class="debug-details mt-2">
      <summary class="debug-summary">{{ summary }}</summary>
      <ul class="debug-list">
        <li v-for="line in details" :key="line">{{ line }}</li>
      </ul>
      <div class="button-row mt-3">
        <button class="button button--secondary debug-copy" type="button" @click="$emit('copy')">
          {{ copied ? "Copied" : "Copy debug info" }}
        </button>
      </div>
    </details>
  </div>
</template>
