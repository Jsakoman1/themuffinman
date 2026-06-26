<script setup lang="ts">
import {computed, ref} from "vue"

type Option = {
  id: number
  label: string
  meta?: string
}

const props = withDefaults(defineProps<{
  options: Option[]
  selectedIds: number[]
  placeholder?: string
  emptyLabel?: string
}>(), {
  placeholder: "Search",
  emptyLabel: "No options found."
})

const emit = defineEmits<{
  toggle: [id: number]
}>()

const query = ref("")

const filteredOptions = computed(() => {
  const normalizedQuery = query.value.trim().toLowerCase()
  if (!normalizedQuery) {
    return props.options
  }

  return props.options.filter((option) => {
    const haystack = `${option.label} ${option.meta ?? ""}`.toLowerCase()
    return haystack.includes(normalizedQuery)
  })
})
</script>

<template>
  <div class="ui-searchable-multi-select">
    <input
      v-model="query"
      class="input ui-searchable-multi-select__search"
      :placeholder="placeholder"
      type="text"
    />

    <div class="ui-searchable-multi-select__list">
      <label
        v-for="option in filteredOptions"
        :key="option.id"
        class="ui-searchable-multi-select__option"
      >
        <input
          type="checkbox"
          :checked="selectedIds.includes(option.id)"
          @change="emit('toggle', option.id)"
        />
        <span class="ui-searchable-multi-select__copy">
          <strong>{{ option.label }}</strong>
          <small v-if="option.meta">{{ option.meta }}</small>
        </span>
      </label>

      <div v-if="!filteredOptions.length" class="muted">
        {{ emptyLabel }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.ui-searchable-multi-select {
  display: grid;
  gap: 8px;
}

.ui-searchable-multi-select__list {
  display: grid;
  gap: 8px;
  max-height: 180px;
  overflow: auto;
  padding-right: 4px;
}

.ui-searchable-multi-select__option {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 0.84rem;
}

.ui-searchable-multi-select__copy {
  display: grid;
  gap: 2px;
}

.ui-searchable-multi-select__copy small {
  color: var(--text-muted);
  font-size: 0.72rem;
  font-weight: 600;
}
</style>
