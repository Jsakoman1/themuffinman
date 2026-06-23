<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {useRouter} from "vue-router"
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import UiToast from "../components/ui/UiToast.vue"
import {logoutUser} from "../auth.ts"
import {sidequestApi, type AdminCircleOverview} from "../api/sidequestApi.ts"
import {normalizeSearchQuery} from "../lib/searchQuery.ts"

const router = useRouter()
const overview = ref<AdminCircleOverview | null>(null)
const isLoading = ref(false)
const error = ref("")
const searchQuery = ref("")
const feedback = ref("")

const normalizedQuery = computed(() => normalizeSearchQuery(searchQuery.value).toLowerCase())

const filteredCircles = computed(() => {
  const items = overview.value?.circles ?? []
  if (!normalizedQuery.value) {
    return items
  }

  return items.filter((circle) => {
    return [
      circle.name,
      circle.ownerUsername,
      ...circle.members.map((member) => member.username)
    ].some((value) => value.toLowerCase().includes(normalizedQuery.value))
  })
})

const filteredConnections = computed(() => {
  const items = overview.value?.acceptedConnections ?? []
  if (!normalizedQuery.value) {
    return items
  }

  return items.filter((item) => {
    return [
      item.requesterUsername,
      item.recipientUsername
    ].some((value) => value.toLowerCase().includes(normalizedQuery.value))
  })
})

const filteredPending = computed(() => {
  const items = overview.value?.pendingRequests ?? []
  if (!normalizedQuery.value) {
    return items
  }

  return items.filter((item) => {
    return [
      item.requesterUsername,
      item.recipientUsername
    ].some((value) => value.toLowerCase().includes(normalizedQuery.value))
  })
})

const filteredBlocked = computed(() => {
  const items = overview.value?.blockedRelations ?? []
  if (!normalizedQuery.value) {
    return items
  }

  return items.filter((item) => {
    return [
      item.requesterUsername,
      item.recipientUsername
    ].some((value) => value.toLowerCase().includes(normalizedQuery.value))
  })
})

const loadOverview = async () => {
  isLoading.value = true
  error.value = ""

  try {
    overview.value = await sidequestApi.getAdminCircleOverview()
  } catch {
    error.value = "Could not load circles."
  } finally {
    isLoading.value = false
  }
}

const deleteCircle = async (id: number) => {
  try {
    await sidequestApi.deleteAdminCircle(id)
    feedback.value = "Circle deleted."
    await loadOverview()
  } catch {
    feedback.value = "Could not delete circle."
  }
}

const deleteRequest = async (id: number) => {
  try {
    await sidequestApi.deleteAdminCircleRequest(id)
    feedback.value = "Relation deleted."
    await loadOverview()
  } catch {
    feedback.value = "Could not delete relation."
  }
}

const handleLogout = () => {
  logoutUser()
  router.push("/login")
}

onMounted(() => {
  void loadOverview()
})
</script>

<template>
  <div class="page page--dashboard">
    <div class="dashboard-shell">
      <main class="dashboard-main dashboard-main--admin">
        <AdminShellHeader
          title="Circles"
          subtitle="Review every circle and every relation from one admin workspace."
          :on-logout="handleLogout"
        />

        <UiToast :message="feedback" tone="success" />

        <article class="card admin-users-card">
          <div class="grid grid--two admin-toolbar">
            <label class="field">
              <span class="label">Search</span>
              <input v-model="searchQuery" class="input" placeholder="Circle, owner, member..." />
            </label>
          </div>

          <div v-if="isLoading" class="empty-state">Loading circles...</div>
          <div v-else-if="error" class="alert alert--error">{{ error }}</div>
          <div v-else class="stack">
            <section class="stack">
              <div class="card__header">
                <h2 class="card__title">Circles</h2>
              </div>

              <div v-if="filteredCircles.length" class="admin-table-shell">
                <table class="admin-table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Owner</th>
                      <th>Members</th>
                      <th>People</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="circle in filteredCircles" :key="circle.id">
                      <td><strong>{{ circle.name }}</strong></td>
                      <td>{{ circle.ownerUsername }}</td>
                      <td>{{ circle.memberCount }}</td>
                      <td class="admin-table__message">{{ circle.members.map((member) => member.username).join(", ") || "No members" }}</td>
                      <td>
                        <div class="admin-table__actions">
                          <button class="button button--ghost" type="button" @click="deleteCircle(circle.id)">Delete</button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="empty-state">No circles match this search.</div>
            </section>

            <section class="stack">
              <div class="card__header">
                <h2 class="card__title">Accepted connections</h2>
              </div>
              <div v-if="filteredConnections.length" class="admin-table-shell">
                <table class="admin-table">
                  <thead>
                    <tr>
                      <th>Requester</th>
                      <th>Recipient</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in filteredConnections" :key="item.id">
                      <td>{{ item.requesterUsername }}</td>
                      <td>{{ item.recipientUsername }}</td>
                      <td>Accepted</td>
                      <td><div class="admin-table__actions"><button class="button button--ghost" type="button" @click="deleteRequest(item.id)">Delete</button></div></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="empty-state">No accepted connections match this search.</div>
            </section>

            <section class="stack">
              <div class="card__header">
                <h2 class="card__title">Pending requests</h2>
              </div>
              <div v-if="filteredPending.length" class="admin-table-shell">
                <table class="admin-table">
                  <thead>
                    <tr>
                      <th>Requester</th>
                      <th>Recipient</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in filteredPending" :key="item.id">
                      <td>{{ item.requesterUsername }}</td>
                      <td>{{ item.recipientUsername }}</td>
                      <td>Pending</td>
                      <td><div class="admin-table__actions"><button class="button button--ghost" type="button" @click="deleteRequest(item.id)">Delete</button></div></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="empty-state">No pending requests match this search.</div>
            </section>

            <section class="stack">
              <div class="card__header">
                <h2 class="card__title">Blocked relations</h2>
              </div>
              <div v-if="filteredBlocked.length" class="admin-table-shell">
                <table class="admin-table">
                  <thead>
                    <tr>
                      <th>Requester</th>
                      <th>Recipient</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in filteredBlocked" :key="item.id">
                      <td>{{ item.requesterUsername }}</td>
                      <td>{{ item.recipientUsername }}</td>
                      <td>Blocked</td>
                      <td><div class="admin-table__actions"><button class="button button--ghost" type="button" @click="deleteRequest(item.id)">Delete</button></div></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="empty-state">No blocked relations match this search.</div>
            </section>
          </div>
        </article>
      </main>
    </div>
  </div>
</template>
