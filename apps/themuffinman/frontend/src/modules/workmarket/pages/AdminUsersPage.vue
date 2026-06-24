<script setup lang="ts">
import {onMounted} from "vue"
import AppPageHeader from "../../../components/app/AppPageHeader.vue"
import AppUsersCreateForm from "../components/app-users/AppUsersCreateForm.vue"
import AppUsersList from "../components/app-users/AppUsersList.vue"
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import UiFilterBar from "../../../components/ui/UiFilterBar.vue"
import UiConfirmDialog from "../../../components/ui/UiConfirmDialog.vue"
import UiDialog from "../../../components/ui/UiDialog.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import {useAppUsersPage} from "../composables/useAppUsersPage.ts"

const usersPage = useAppUsersPage()

onMounted(() => {
  void usersPage.init()
})
</script>

<template>
  <div class="page page--dashboard">
    <div class="dashboard-shell">
      <main class="dashboard-main dashboard-main--admin">
        <AdminShellHeader
          title="Users"
          subtitle="Search, edit, create, promote, reset passwords, or delete accounts."
        />

        <UiToast :message="usersPage.feedback" :tone="usersPage.feedbackType" />

        <UiRequestError :message="usersPage.pageError" :details="usersPage.pageErrorDetails" summary="User request debug details" :copied="usersPage.copiedDebug" @copy="usersPage.copyDebugInfo" />

        <div v-if="usersPage.isLoadingUsers" class="empty-state">
          Loading users...
        </div>

        <article class="card admin-users-card">
          <AppPageHeader
            title="All users"
            subtitle="Search, edit, create, promote, reset passwords, or delete accounts."
          />

          <UiFilterBar :columns="2">
            <label class="field">
              <span class="label">Search</span>
              <input v-model="usersPage.userSearch" class="input" placeholder="Username, email, role..." />
            </label>

            <div class="button-row admin-users-actions">
              <button class="button" type="button" @click="usersPage.openCreateUserDialog">Create user</button>
            </div>
          </UiFilterBar>

          <div v-if="!usersPage.isLoadingUsers && !usersPage.pageError && !usersPage.appUsers.length" class="empty-state">
            No users match this search.
          </div>

          <AppUsersList
            v-else
            :users="usersPage.appUsers"
            :editing-user-id="usersPage.editingAppUserId"
            :edit-email="usersPage.editAppUserEmail"
            :edit-username="usersPage.editAppUserUsername"
            :edit-role="usersPage.editAppUserRole"
            :edit-password="usersPage.editAppUserPassword"
            :role-options="usersPage.roleOptions"
            @edit="usersPage.startEdit"
            @delete="usersPage.handleDelete"
            @save="usersPage.updateAppUser"
            @cancel="usersPage.cancelEdit"
            @update:edit-email="usersPage.editAppUserEmail = $event"
            @update:edit-username="usersPage.editAppUserUsername = $event"
            @update:edit-role="usersPage.editAppUserRole = $event"
            @update:edit-password="usersPage.editAppUserPassword = $event"
          />
        </article>

        <UiDialog
          :open="usersPage.isCreateUserDialogOpen"
          title="Create user"
          subtitle="Create a new admin-managed account."
          @close="usersPage.closeCreateUserDialog"
        >
          <AppUsersCreateForm
            v-model:email="usersPage.email"
            v-model:username="usersPage.username"
            v-model:password="usersPage.password"
            v-model:role="usersPage.role"
            :role-options="usersPage.roleOptions"
            @submit="usersPage.createAppUser"
          />
        </UiDialog>

        <UiConfirmDialog
          :open="usersPage.deleteCandidateUserId !== null"
          title="Delete user"
          message="Delete this user account? This action cannot be undone."
          confirm-label="Delete"
          cancel-label="Cancel"
          @confirm="usersPage.confirmDelete"
          @cancel="usersPage.cancelDelete"
          @close="usersPage.cancelDelete"
        />
      </main>
    </div>
  </div>
</template>
