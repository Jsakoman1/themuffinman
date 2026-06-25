<script setup lang="ts">
import {computed, ref} from "vue"
import RichTextEditor from "../../../../components/editor/AsyncRichTextEditor.vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"

const props = defineProps<{
  description: string
}>()

const emit = defineEmits<{
  (event: "update:description", value: string): void
}>()

const editingDescription = ref(false)
const hasDescription = computed(() => !!props.description?.trim())

const toggleEditingDescription = () => {
  editingDescription.value = !editingDescription.value
}
</script>

<template>
  <UiFieldGroup label="About me" tag="div" field-class="ui-edit-field ui-edit-field--profile-description ui-edit-field--profile-inline">
    <template #headerAction>
      <button class="button button--icon button--secondary button--icon-compact" type="button" aria-label="Edit about me" @click="toggleEditingDescription">✎</button>
    </template>

    <RichTextEditor
      v-if="editingDescription"
      :model-value="description"
      placeholder=""
      toolbar-label="Profile tools"
      @update:model-value="emit('update:description', $event)"
    />

    <ProfileBio
      v-else
      class="ui-content-prose ui-content-prose--flat"
      :text="hasDescription ? description : ''"
      placeholder="Add a profile description."
    />
  </UiFieldGroup>
</template>
