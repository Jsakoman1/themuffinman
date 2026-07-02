<script setup lang="ts">
defineProps<{
  message: string
  price: string
  pricePlaceholder?: string
  quickfillLabel?: string
  canSubmit: boolean
  submitLabel?: string
  showPrice?: boolean
}>()

const emit = defineEmits<{
  (event: "update:message", value: string): void
  (event: "update:price", value: string): void
  (event: "quickfill"): void
  (event: "submit"): void
}>()
</script>

<template>
  <form class="vision-terminal-feed" autocomplete="off" @submit.prevent="emit('submit')">
    <section class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">apply</p>
      <label class="vision-terminal-feed__field">
        <span>Message</span>
        <textarea
          :value="message"
          class="input vision-terminal-feed__textarea"
          rows="4"
          placeholder="Write your application"
          @input="emit('update:message', ($event.target as HTMLTextAreaElement).value)"
        />
      </label>

      <label v-if="showPrice !== false" class="vision-terminal-feed__field">
        <span>Price</span>
        <input
          :value="price"
          class="input"
          :placeholder="pricePlaceholder ?? '0'"
          inputmode="numeric"
          @input="emit('update:price', ($event.target as HTMLInputElement).value)"
        />
      </label>

      <div class="vision-terminal-feed__action-row">
        <button v-if="quickfillLabel" class="vision-terminal-feed__link-button" type="button" @click="emit('quickfill')">
          {{ quickfillLabel }}
        </button>
        <button class="vision-terminal-feed__link-button" type="submit" :disabled="!canSubmit">
          {{ submitLabel ?? "Apply" }}
        </button>
      </div>
    </section>
  </form>
</template>
