<script setup lang="ts">
import {RouterLink} from "vue-router"

const options = [
  {label: "Offer work", description: "Ask for useful help", to: "/work/offer"},
  {label: "Offer a ride", description: "Share a journey", to: {path: "/rides", query: {create: "1"}}},
  {label: "List a thing", description: "Let someone borrow it", to: {path: "/things", query: {create: "1"}}},
  {label: "Create a business", description: "Set up a public service profile", to: "/business/profile"},
  {label: "Start a chat", description: "Talk to someone", to: "/chat"}
]
</script>

<template>
  <details class="universal-create-menu">
    <summary class="universal-create-menu__summary" aria-label="Open create menu">Create</summary>
    <div class="universal-create-menu__panel">
      <div><strong>What do you want to start?</strong><p>Choose a direct web flow or use Vision for guidance.</p></div>
      <RouterLink v-for="option in options" :key="option.label" :to="option.to" class="universal-create-menu__option">
        <strong>{{ option.label }}</strong><span>{{ option.description }}</span>
      </RouterLink>
    </div>
  </details>
</template>

<style scoped>
.universal-create-menu{position:relative}.universal-create-menu__summary{cursor:pointer;list-style:none;padding:.7rem .85rem;border:1px solid rgba(23,34,26,.14);border-radius:999px;background:rgba(255,255,255,.82);font-weight:650}.universal-create-menu__summary::-webkit-details-marker{display:none}.universal-create-menu__panel{position:absolute;right:0;top:calc(100% + .55rem);z-index:20;display:grid;gap:.45rem;width:min(24rem,calc(100vw - 2rem));padding:1rem;border:1px solid rgba(23,34,26,.12);border-radius:1rem;background:#fcfcf8;box-shadow:0 18px 38px rgba(23,34,26,.16)}.universal-create-menu__panel p{margin:.25rem 0 .4rem;color:rgba(23,34,26,.62);font-size:.8rem}.universal-create-menu__option{display:grid;gap:.15rem;padding:.65rem .7rem;border-radius:.7rem}.universal-create-menu__option:hover{background:rgba(214,228,218,.55)}.universal-create-menu__option span{color:rgba(23,34,26,.6);font-size:.78rem}@media(max-width:640px){.universal-create-menu__panel{position:fixed;right:1rem;top:4.5rem}}
</style>
