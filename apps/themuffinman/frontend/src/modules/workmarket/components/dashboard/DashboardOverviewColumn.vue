<script setup lang="ts">
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import type {NavigationTarget} from "../../api/workmarketApi.ts"

type ColumnItem = {
  id: string
  title: string
  when: string
  navigation: NavigationTarget | null
}

type ColumnBucket = {
  key: string
  label: string
  tone: "success" | "warning" | "accent"
  items: ColumnItem[]
}

defineProps<{
  eyebrow: string
  title: string
  actionLabel: string
  actionTone?: "success" | "accent"
  buckets: ColumnBucket[]
}>()

const emit = defineEmits<{
  (event: "action"): void
  (event: "open", navigation: NavigationTarget | null): void
}>()

const isCollapsedByDefault = (key: string) => key.endsWith("completed")
</script>

<template>
  <UiSurfaceSection tag="article" class="dashboard-overview__column" compact :eyebrow="eyebrow" :title="title">
    <template #actions>
      <button
        class="button dashboard-overview__column-action"
        :class="actionTone === 'accent' ? 'dashboard-overview__column-action--accent' : 'dashboard-overview__column-action--success'"
        type="button"
        @click="emit('action')"
      >
        {{ actionLabel }}
      </button>
    </template>

    <div class="dashboard-overview__stack">
      <component
        :is="isCollapsedByDefault(bucket.key) ? 'details' : 'section'"
        v-for="bucket in buckets"
        :key="bucket.key"
        class="dashboard-overview__bucket"
        :open="isCollapsedByDefault(bucket.key) ? false : undefined"
      >
        <summary v-if="isCollapsedByDefault(bucket.key)" class="dashboard-overview__bucket-header dashboard-overview__bucket-header--summary">
          <span :class="['dashboard-overview__bucket-dot', `dashboard-overview__bucket-dot--${bucket.tone}`]" />
          <strong class="dashboard-overview__bucket-title">{{ bucket.label }}</strong>
          <span class="dashboard-overview__bucket-toggle">Show</span>
        </summary>

        <header v-else class="dashboard-overview__bucket-header">
          <span :class="['dashboard-overview__bucket-dot', `dashboard-overview__bucket-dot--${bucket.tone}`]" />
          <strong class="dashboard-overview__bucket-title">{{ bucket.label }}</strong>
        </header>

        <div v-if="bucket.items.length" class="dashboard-overview__bucket-list">
          <button
            v-for="item in bucket.items"
            :key="item.id"
            :class="['dashboard-overview__item', `dashboard-overview__item--${bucket.tone}`]"
            type="button"
            @click="emit('open', item.navigation)"
          >
            <strong class="dashboard-overview__item-title">{{ item.title }}</strong>
            <span class="dashboard-overview__item-meta">{{ item.when }}</span>
          </button>
        </div>

        <div v-else class="dashboard-overview__empty">Empty</div>
      </component>
    </div>
  </UiSurfaceSection>
</template>
