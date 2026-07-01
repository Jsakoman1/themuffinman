<script setup lang="ts">
import UiWorkspace from "../../../../components/ui/UiWorkspace.vue"
</script>

<template>
  <UiWorkspace variant="detail">
    <section class="surface-stack surface-stack--content">
      <slot name="main" />
    </section>

    <aside class="surface-stack surface-stack--aside">
      <slot name="side" />
    </aside>
  </UiWorkspace>
</template>
