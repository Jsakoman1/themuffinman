<script setup lang="ts">
import {computed} from "vue"
import {useRoute, useRouter} from "vue-router"
import AppPageHeader from "../../../../components/app/AppPageHeader.vue"

withDefaults(defineProps<{
  title: string
  subtitle?: string
}>(), {
  subtitle: "",
})

const route = useRoute()
const router = useRouter()

const adminSections = [
  {
    key: "work",
    label: "Work",
    isActive: () => route.path.startsWith("/admin/work") || route.path.startsWith("/admin/quests"),
    target: "/admin/work",
  },
  {
    key: "users",
    label: "Users",
    isActive: () => route.path.startsWith("/admin/users"),
    target: "/admin/users",
  },
  {
    key: "applications",
    label: "Applications",
    isActive: () => route.path.startsWith("/admin/applications"),
    target: "/admin/applications",
  },
  {
    key: "circles",
    label: "Circles",
    isActive: () => route.path.startsWith("/admin/circles"),
    target: "/admin/circles",
  },
] as const

const activeSections = computed(() => new Set(adminSections.filter((section) => section.isActive()).map((section) => section.key)))

const goToSection = (target: string) => {
  void router.push(target)
}
</script>

<template>
  <AppPageHeader class="admin-shell-header" eyebrow="Admin area" :title="title" :subtitle="subtitle">
    <template #actions>
      <div class="admin-shell-header__actions">
        <div class="admin-shell-header__tabs" role="tablist" aria-label="Admin sections">
          <button
            v-for="section in adminSections"
            :key="section.key"
            class="button button--secondary admin-shell-header__tab"
            :class="{ 'button--active': activeSections.has(section.key) }"
            type="button"
            role="tab"
            :aria-selected="activeSections.has(section.key)"
            @click="goToSection(section.target)"
          >
            {{ section.label }}
          </button>
        </div>
      </div>
    </template>
  </AppPageHeader>
</template>
