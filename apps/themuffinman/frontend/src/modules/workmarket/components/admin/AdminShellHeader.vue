<script setup lang="ts">
import {computed} from "vue"
import {useRoute, useRouter} from "vue-router"
import AppPageHeader from "../../../../components/app/AppPageHeader.vue"
import {logoutUser} from "../../../identity/auth.ts"

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
    key: "overview",
    label: "Overview",
    isActive: () => route.path.startsWith("/admin/work"),
    target: "/admin/work",
  },
  {
    key: "quests",
    label: "Quests",
    isActive: () => route.path.startsWith("/admin/quests"),
    target: "/admin/quests",
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
  {
    key: "agent",
    label: "Agent",
    isActive: () => route.path.startsWith("/admin/agent"),
    target: "/admin/agent",
  },
] as const

const activeSections = computed(() => new Set(adminSections.filter((section) => section.isActive()).map((section) => section.key)))

const goToSection = (target: string) => {
  void router.push(target)
}

const logout = async () => {
  logoutUser()
  await router.push("/login")
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
        <button class="button button--secondary admin-shell-header__logout" type="button" @click="logout">
          Log out
        </button>
      </div>
    </template>
  </AppPageHeader>
</template>
