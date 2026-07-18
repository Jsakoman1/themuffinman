<script setup lang="ts">
import {useRouter, type RouteLocationRaw} from "vue-router"

const props = withDefaults(defineProps<{
  to?: RouteLocationRaw
  label?: string
  interactive?: boolean
  selected?: boolean
  previewed?: boolean
}>(), {
  interactive: true
})

const router = useRouter()
const emit = defineEmits<{preview: []}>()

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
  if (event.key.toLowerCase() === "p" && !isNestedAction(event.target)) { event.preventDefault(); emit("preview") }
}
</script>

<template>
  <article
    class="app-card"
    :class="{ 'app-card--interactive': interactive && to, 'app-card--selected': selected, 'app-card--previewed': previewed }"
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
.app-card{display:grid;gap:var(--space-3);min-width:0;padding:1rem;border:1px solid var(--border-subtle);border-radius:var(--radius-card);background:var(--surface);transition:border-color 140ms ease,background-color 140ms ease}.app-card--interactive{cursor:pointer}.app-card--interactive:hover,.app-card--interactive:focus-visible{border-color:var(--border-strong);background:var(--surface-hover);outline:none}.app-card--selected{border-color:var(--accent);background:var(--accent-muted)}.app-card--previewed{box-shadow:inset 2px 0 var(--accent)}.app-card__body{min-width:0}.app-card__actions{display:flex;justify-content:flex-end;gap:.4rem;flex-wrap:wrap}
</style>
