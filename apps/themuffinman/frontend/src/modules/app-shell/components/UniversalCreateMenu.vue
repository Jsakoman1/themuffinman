<script setup lang="ts">
import {onBeforeUnmount, onMounted, ref} from "vue"
import {RouterLink} from "vue-router"
import {userShellApi, type WorkspaceCommandCatalog} from "../api/userShellApi.ts"

const open = ref(false)
const catalog = ref<WorkspaceCommandCatalog | null>(null)
const error = ref("")
const loading = ref(false)
const loadCatalog = async () => { loading.value = true; error.value = ""; try { catalog.value = await userShellApi.getWorkspaceCommandCatalog() } catch { error.value = "Could not load create options." } finally { loading.value = false } }
const openMenu = async () => { open.value = true; if (!catalog.value && !loading.value) await loadCatalog() }
onMounted(() => window.addEventListener("app:open-create", openMenu))
onBeforeUnmount(() => window.removeEventListener("app:open-create", openMenu))
</script>

<template>
  <details :open="open" class="universal-create-menu" @toggle="open = ($event.currentTarget as HTMLDetailsElement).open">
    <summary class="universal-create-menu__summary" aria-label="Open create menu" title="Open create menu" @click.prevent="open ? open = false : openMenu()">Create</summary>
    <div class="universal-create-menu__panel">
      <div><strong>Direct create</strong><p>Each option opens its existing direct flow. Command navigation and Vision stay separate; drafts are not shared or saved until that flow says so.</p></div>
      <p v-if="loading" class="universal-create-menu__loading" role="status" aria-live="polite">Loading available create flows…</p>
      <RouterLink v-for="option in catalog?.create ?? []" :key="option.id" :to="option.route" class="universal-create-menu__option" @click="open = false">
        <strong>{{ option.label }}</strong><span>{{ option.description }}</span>
      </RouterLink>
      <p v-if="error" class="universal-create-menu__error" role="alert">{{ error }} <button type="button" @click="loadCatalog">Retry</button></p>
      <p v-else-if="catalog && !catalog.create.length" class="universal-create-menu__loading" role="status">No create flows are available for this account.</p>
    </div>
  </details>
</template>

<style scoped>
.universal-create-menu{position:relative}.universal-create-menu__summary{cursor:pointer;list-style:none;padding:var(--space-1) var(--space-2);border:1px solid var(--accent);border-radius:var(--radius-control);background:var(--accent);color:var(--canvas);font-size:var(--text-size-body);font-weight:var(--text-weight-semibold)}.universal-create-menu__summary::-webkit-details-marker{display:none}.universal-create-menu__panel{position:fixed;inset-inline-start:max(var(--space-3),env(safe-area-inset-left));inset-inline-end:max(var(--space-3),env(safe-area-inset-right));top:calc(var(--workspace-header-height) + var(--space-2));z-index:var(--z-popover);display:grid;gap:var(--space-2);width:min(24rem,calc(100vw - (2 * var(--space-3))));max-height:calc(100dvh - var(--workspace-header-height) - (2 * var(--space-3)));overflow:auto;padding:var(--space-3);border:1px solid var(--border-strong);border-radius:var(--radius-surface);background:var(--surface-raised);box-shadow:var(--shadow-overlay);box-sizing:border-box}.universal-create-menu__panel p{margin:var(--space-1) 0;color:var(--text-muted);font-size:var(--text-size-body)}.universal-create-menu__option{display:grid;gap:var(--space-1);padding:var(--space-2);border-radius:var(--radius-control)}.universal-create-menu__option:hover{background:var(--surface-hover)}.universal-create-menu__option span{color:var(--text-muted);font-size:var(--text-size-meta)}.universal-create-menu__loading{color:var(--text-muted)}.universal-create-menu__error{color:var(--danger)}.universal-create-menu__error button{margin-left:var(--space-1);border:0;background:transparent;color:inherit;text-decoration:underline;cursor:pointer;font:inherit}
</style>
