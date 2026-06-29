<script setup lang="ts">
import UiAppShellPage from "./UiAppShellPage.vue"

withDefaults(defineProps<{
  admin?: boolean
}>(), {
  admin: false,
})
</script>

<template>
  <UiAppShellPage :admin="admin">
    <slot />
  </UiAppShellPage>
</template>
