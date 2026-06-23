<script setup lang="ts">
import {computed} from "vue"
import {renderProfileText} from "../../shared/profileFormatting.ts"

const props = withDefaults(defineProps<{
  text?: string | null
  placeholder?: string
}>(), {
  text: "",
  placeholder: "No profile description yet.",
})

const renderedText = computed(() => renderProfileText(props.text))
</script>

<template>
  <div v-if="renderedText" class="profile-bio" v-html="renderedText" />
  <p v-else class="muted profile-bio__placeholder">
    {{ placeholder }}
  </p>
</template>
