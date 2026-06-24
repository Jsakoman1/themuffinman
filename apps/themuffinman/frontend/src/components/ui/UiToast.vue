<script setup lang="ts">
withDefaults(defineProps<{
  message: string
  tone?: "success" | "error"
}>(), {
  tone: "success",
})
</script>

<template>
  <Transition name="toast">
    <div v-if="message" :class="['dashboard-toast', `dashboard-toast--${tone}`]">
      {{ message }}
    </div>
  </Transition>
</template>
