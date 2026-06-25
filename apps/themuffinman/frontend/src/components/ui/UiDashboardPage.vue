<script setup lang="ts">
withDefaults(defineProps<{
  admin?: boolean
}>(), {
  admin: false,
})
</script>

<template>
  <div class="page page--dashboard">
    <div class="dashboard-shell">
      <main :class="['dashboard-main', { 'dashboard-main--admin': admin }]">
        <slot />
      </main>
    </div>
  </div>
</template>
