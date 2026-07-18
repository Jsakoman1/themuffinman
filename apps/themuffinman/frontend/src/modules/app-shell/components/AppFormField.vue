<script setup lang="ts">
withDefaults(defineProps<{label: string; hint?: string; error?: string; required?: boolean}>(), {required: false})
</script>

<template>
  <label class="app-form-field">
    <span class="app-form-field__label">{{ label }}<span v-if="required" aria-hidden="true"> *</span></span>
    <span v-if="hint" class="app-form-field__hint">{{ hint }}</span>
    <slot />
    <span v-if="error" class="app-form-field__error" role="alert">{{ error }}</span>
  </label>
</template>

<style scoped>
.app-form-field{display:grid;gap:.35rem}.app-form-field__label{font-size:.82rem;font-weight:650}.app-form-field__hint{color:var(--text-muted);font-size:.78rem}.app-form-field__error{color:var(--danger);font-size:.78rem}
</style>
