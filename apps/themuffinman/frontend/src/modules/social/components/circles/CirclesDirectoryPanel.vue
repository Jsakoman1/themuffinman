<script setup lang="ts">
import {computed, ref, watch} from "vue"
import type {CircleGroup} from "../../../../contracts/index.ts"

const props = defineProps<{
  circles: CircleGroup[]
  newCircleName: string
  isSaving: boolean
}>()

const emit = defineEmits<{
  (event: "delete-circle", circleId: number): void
  (event: "rename-circle", payload: {circleId: number; name: string}): void
  (event: "update:new-circle-name", value: string): void
  (event: "create-circle"): void
}>()

const isRenaming = ref(false)
const renameValue = ref("")
const editingCircleId = ref<number | null>(null)

const activeCircle = computed(() => props.circles.find((circle) => circle.id === editingCircleId.value) ?? null)

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
  editingCircleId.value = null
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

    <div v-if="circles.length" class="circles-directory__list">
      <article v-for="circle in circles" :key="circle.id" class="circles-directory__item">
        <template v-if="editingCircleId === circle.id && isRenaming">
          <form class="ui-inline-form circles-directory__rename-form" @submit.prevent="submitRename">
            <input v-model="renameValue" class="input" maxlength="80" />
            <button class="button" type="submit" :disabled="isSaving || !renameValue.trim()">Save</button>
            <button class="button button--ghost" type="button" :disabled="isSaving" @click="editingCircleId = null; isRenaming = false">Cancel</button>
          </form>
        </template>
        <template v-else>
          <div class="circles-directory__item-title">
            <strong>{{ circle.name }}</strong>
            <span class="muted">{{ circle.memberCount }}</span>
          </div>
          <div class="circles-directory__item-actions">
            <button class="button button--ghost button--icon" type="button" :disabled="isSaving" @click="editingCircleId = circle.id; isRenaming = true">
              ✎
            </button>
            <button class="button button--ghost" type="button" :disabled="isSaving" @click="emit('delete-circle', circle.id)">
              Delete
            </button>
          </div>
        </template>
      </article>
    </div>

    <div v-if="!circles.length" class="empty-state empty-state--soft">Create your first circle to start grouping people.</div>
  </div>
</template>
