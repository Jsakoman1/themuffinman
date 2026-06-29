<script setup lang="ts">
import AppPageHeader from "../app/AppPageHeader.vue"

withDefaults(defineProps<{
  admin?: boolean
  pageClass?: string
  title?: string
  subtitle?: string
  eyebrow?: string
}>(), {
  admin: false,
  pageClass: "",
  title: "",
  subtitle: "",
  eyebrow: "",
})
</script>

<template>
  <div :class="['page page--dashboard', pageClass]">
    <div class="dashboard-shell">
      <main :class="['dashboard-main', { 'dashboard-main--admin': admin }]">
        <AppPageHeader
          v-if="title"
          :title="title"
          :subtitle="subtitle || undefined"
          :eyebrow="eyebrow || undefined"
        >
          <template v-if="$slots.actions" #actions>
            <slot name="actions" />
          </template>
        </AppPageHeader>

        <slot />
      </main>
    </div>
  </div>
</template>
