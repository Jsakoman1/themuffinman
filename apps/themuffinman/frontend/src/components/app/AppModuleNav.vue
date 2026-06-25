<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"

type AppModuleNavItem = {
  key: string
  shortTitle: string
  path: RouteLocationRaw
  state: "live" | "planned"
  active: boolean
}

defineProps<{
  items: AppModuleNavItem[]
}>()
</script>

<template>
  <nav class="app-module-nav" aria-label="Product modules">
    <RouterLink
      v-for="item in items"
      :key="item.key"
      :to="item.path"
      class="app-module-nav__item"
      :class="{
        'app-module-nav__item--planned': item.state === 'planned',
        'app-module-nav__item--active': item.active
      }"
    >
      <span>{{ item.shortTitle }}</span>
      <small>{{ item.state === "live" ? "Live" : "Planned" }}</small>
    </RouterLink>
  </nav>
</template>
