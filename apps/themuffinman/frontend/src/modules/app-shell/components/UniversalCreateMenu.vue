<script setup lang="ts">
import {onBeforeUnmount, onMounted, ref} from "vue"
import {RouterLink} from "vue-router"

const options = [
  {label: "Create new work", description: "Ask for useful help", to: "/work/offer"},
  {label: "Offer a ride", description: "Share a journey", to: {path: "/rides", query: {create: "1"}}},
  {label: "List a thing", description: "Let someone borrow it", to: {path: "/things", query: {create: "1"}}},
  {label: "Create a business", description: "Set up a public service profile", to: "/business/profile"},
  {label: "Start a chat", description: "Talk to someone", to: "/chat"}
]
const open = ref(false)
const openMenu = () => { open.value = true }
onMounted(() => window.addEventListener("app:open-create", openMenu))
onBeforeUnmount(() => window.removeEventListener("app:open-create", openMenu))
</script>

<template>
  <details :open="open" class="universal-create-menu" @toggle="open = ($event.currentTarget as HTMLDetailsElement).open">
    <summary class="universal-create-menu__summary" aria-label="Open create menu" title="Open create menu (C)">Create</summary>
    <div class="universal-create-menu__panel">
      <div><strong>Direct create</strong><p>Each option opens its existing direct flow. Command navigation and Vision stay separate; drafts are not shared or saved until that flow says so.</p></div>
      <RouterLink v-for="option in options" :key="option.label" :to="option.to" class="universal-create-menu__option">
        <strong>{{ option.label }}</strong><span>{{ option.description }}</span>
      </RouterLink>
    </div>
  </details>
</template>

<style scoped>
.universal-create-menu{position:relative}.universal-create-menu__summary{cursor:pointer;list-style:none;padding:.48rem .65rem;border:1px solid var(--accent);border-radius:var(--radius-control);background:var(--accent);color:#121217;font-size:.8rem;font-weight:700}.universal-create-menu__summary::-webkit-details-marker{display:none}.universal-create-menu__panel{position:absolute;right:0;top:calc(100% + .55rem);z-index:20;display:grid;gap:.45rem;width:min(24rem,calc(100vw - 2rem));padding:1rem;border:1px solid var(--border-strong);border-radius:var(--radius-card);background:var(--surface-strong);box-shadow:var(--shadow-card)}.universal-create-menu__panel p{margin:.25rem 0 .4rem;color:var(--text-muted);font-size:.8rem}.universal-create-menu__option{display:grid;gap:.15rem;padding:.65rem .7rem;border-radius:var(--radius-control)}.universal-create-menu__option:hover{background:var(--surface-hover)}.universal-create-menu__option span{color:var(--text-muted);font-size:.78rem}@media(max-width:640px){.universal-create-menu__panel{position:fixed;right:1rem;top:4.5rem}}
</style>
