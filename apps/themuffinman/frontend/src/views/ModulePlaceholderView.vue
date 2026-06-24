<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import {getProductModule, productModules} from "../modules/moduleRegistry.ts"

const route = useRoute()

const currentModule = computed(() => getProductModule(String(route.meta.moduleKey ?? "")))
const moduleTitle = computed(() => currentModule.value?.title ?? String(route.meta.moduleTitle ?? "Module"))
const moduleDescription = computed(() => currentModule.value?.description ?? String(route.meta.moduleDescription ?? "This module is planned but not implemented yet."))
</script>

<template>
  <div class="page">
    <div class="card stack module-placeholder">
      <div>
        <p class="page-subtitle">TheMuffinMan Module</p>
        <h1 class="page-title">{{ moduleTitle }}</h1>
        <p class="muted mt-2">{{ moduleDescription }}</p>
      </div>

      <div class="empty-state empty-state--soft">
        This module is planned inside the main app and will reuse the same account, circles, and shared shell.
      </div>

      <section class="module-roadmap">
        <div class="module-roadmap__header">
          <h2 class="card__title">Product map</h2>
          <p class="muted">One application, multiple modules.</p>
        </div>

        <div class="module-roadmap__grid">
          <RouterLink
            v-for="module in productModules"
            :key="module.key"
            :to="module.path"
            class="module-roadmap__card"
            :class="{ 'module-roadmap__card--active': currentModule?.key === module.key }"
          >
            <div class="module-roadmap__top">
              <strong>{{ module.title }}</strong>
              <span :class="['badge', module.state === 'live' ? 'badge--success' : 'badge--warning']">
                {{ module.state === "live" ? "Live" : "Planned" }}
              </span>
            </div>
            <p class="muted">{{ module.description }}</p>
          </RouterLink>
        </div>
      </section>
    </div>
  </div>
</template>
