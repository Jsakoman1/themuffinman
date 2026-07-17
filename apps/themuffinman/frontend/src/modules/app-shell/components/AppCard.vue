<script setup lang="ts">
import {useRouter, type RouteLocationRaw} from "vue-router"

const props = withDefaults(defineProps<{
  to?: RouteLocationRaw
  label?: string
  interactive?: boolean
}>(), {
  interactive: true
})

const router = useRouter()

const isNestedAction = (target: EventTarget | null) => target instanceof HTMLElement && Boolean(target.closest("a,button,input,select,textarea,summary"))
const open = () => {
  if (props.to) void router.push(props.to)
}
const handleClick = (event: MouseEvent) => {
  if (props.interactive && props.to && !isNestedAction(event.target)) open()
}
const handleKeydown = (event: KeyboardEvent) => {
  if ((event.key === "Enter" || event.key === " ") && !isNestedAction(event.target)) {
    event.preventDefault()
    open()
  }
}
</script>

<template>
  <article
    class="app-card"
    :class="{ 'app-card--interactive': interactive && to }"
    :tabindex="interactive && to ? 0 : undefined"
    :aria-label="label"
    @click="handleClick"
    @keydown="handleKeydown"
  >
    <div class="app-card__body"><slot /></div>
    <footer v-if="$slots.actions" class="app-card__actions"><slot name="actions" /></footer>
  </article>
</template>

<style scoped>
.app-card{display:grid;gap:var(--space-3, .75rem);min-width:0;padding:1rem;border:1px solid rgba(23,34,26,.1);border-radius:1rem;background:rgba(255,255,255,.72);transition:border-color 160ms ease,box-shadow 160ms ease,transform 160ms ease}.app-card--interactive{cursor:pointer}.app-card--interactive:hover,.app-card--interactive:focus-visible{border-color:rgba(23,34,26,.24);box-shadow:0 14px 28px rgba(23,34,26,.1);outline:none;transform:translateY(-1px)}.app-card__body{min-width:0}.app-card__actions{display:flex;justify-content:flex-end;gap:.4rem;flex-wrap:wrap}
</style>
