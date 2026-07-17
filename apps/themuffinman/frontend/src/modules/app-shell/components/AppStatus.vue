<script setup lang="ts">
withDefaults(defineProps<{message: string; tone?: "neutral" | "success" | "error"; retry?: boolean}>(), {tone: "neutral", retry: false})
const emit = defineEmits<{retry: []}>()
</script>

<template>
  <p class="app-status" :class="`app-status--${tone}`" :role="tone === 'error' ? 'alert' : 'status'">
    {{ message }}<button v-if="retry" type="button" @click="emit('retry')">Retry</button>
  </p>
</template>

<style scoped>
.app-status{margin:0;padding:.65rem 0;color:var(--text-muted)}.app-status--success{color:#2d6846}.app-status--error{color:var(--danger)}.app-status button{margin-left:.55rem;border:0;background:transparent;color:inherit;text-decoration:underline;font:inherit;cursor:pointer}
</style>
