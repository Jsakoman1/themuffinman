<script setup lang="ts">
import {computed, ref, watch} from "vue"
import type {CircleGroup} from "../../../workmarket/api/workmarketApi.ts"

const props = defineProps<{
  circles: CircleGroup[]
  activeCircleFilter: number | "all" | "unassigned"
  connectionsCount: number
  overviewUnassignedConnectionCount: number
  newCircleName: string
  isSaving: boolean
}>()

const emit = defineEmits<{
  (event: "select-filter", value: number | "all" | "unassigned"): void
  (event: "open-user", userId: number): void
  (event: "delete-circle", circleId: number): void
  (event: "rename-circle", payload: {circleId: number; name: string}): void
  (event: "update:new-circle-name", value: string): void
  (event: "create-circle"): void
}>()

const isRenaming = ref(false)
const renameValue = ref("")

const activeCircle = computed(() => props.circles.find((circle) => circle.id === props.activeCircleFilter) ?? null)

watch(activeCircle, (circle) => {
  isRenaming.value = false
  renameValue.value = circle?.name ?? ""
}, {immediate: true})

const submitRename = () => {
  if (!activeCircle.value) {
    return
  }

  emit("rename-circle", {circleId: activeCircle.value.id, name: renameValue.value})
  isRenaming.value = false
}
</script>

<template>
  <div class="circles-directory">
    <form class="ui-inline-form circles-directory__create" @submit.prevent="emit('create-circle')">
      <input
        :value="newCircleName"
        class="input"
        maxlength="80"
        placeholder="New circle name"
        @input="emit('update:new-circle-name', ($event.target as HTMLInputElement).value)"
      />
      <button class="button" type="submit" :disabled="isSaving">Add</button>
    </form>

    <div class="circles-filter-tabs">
      <button
        class="circles-filter-tabs__button"
        :class="{ 'circles-filter-tabs__button--active': activeCircleFilter === 'all' }"
        type="button"
        @click="emit('select-filter', 'all')"
      >
        All
        <span class="badge">{{ connectionsCount }}</span>
      </button>

      <button
        class="circles-filter-tabs__button"
        :class="{ 'circles-filter-tabs__button--active': activeCircleFilter === 'unassigned' }"
        type="button"
        @click="emit('select-filter', 'unassigned')"
      >
        Unassigned
        <span class="badge">{{ overviewUnassignedConnectionCount }}</span>
      </button>

      <button
        v-for="circle in circles"
        :key="circle.id"
        class="circles-filter-tabs__button"
        :class="{ 'circles-filter-tabs__button--active': activeCircleFilter === circle.id }"
        type="button"
        @click="emit('select-filter', circle.id)"
      >
        <span>{{ circle.name }}</span>
        <span class="circles-filter-tabs__meta">
          <span class="badge badge--accent">{{ circle.memberCount }}</span>
        </span>
      </button>
    </div>

    <div v-if="activeCircle" class="circles-directory__meta">
      <div v-if="!isRenaming" class="circles-directory__rename">
        <strong>{{ activeCircle.name }}</strong>
        <button class="button button--ghost button--icon" type="button" :disabled="isSaving" @click="isRenaming = true">
          ✎
        </button>
      </div>

      <form v-else class="ui-inline-form" @submit.prevent="submitRename">
        <input v-model="renameValue" class="input" maxlength="80" />
        <button class="button" type="submit" :disabled="isSaving || !renameValue.trim()">Save</button>
        <button class="button button--ghost" type="button" :disabled="isSaving" @click="isRenaming = false">Cancel</button>
      </form>

      <button class="button button--ghost" type="button" :disabled="isSaving" @click="emit('delete-circle', activeCircle.id)">
        Delete
      </button>
    </div>

    <div v-if="!circles.length" class="empty-state empty-state--soft">Create your first circle to start grouping people.</div>
  </div>
</template>
