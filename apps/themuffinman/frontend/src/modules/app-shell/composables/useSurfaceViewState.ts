import {computed, ref, watch, type Ref} from "vue"
import type {DisplayDensity} from "../api/userShellApi.ts"

export type SurfaceViewState = {
  displayDensity: DisplayDensity
  selectedId: number | null
  previewId: number | null
  scrollY: number
}

// Collection membership, ordering, pagination and permissions stay backend-owned.
// A route may add viewport virtualization only after its backend contract exposes a
// stable paged/cursor window; local display preference and selection never decide
// which objects exist in the collection.
export const collectionPerformancePolicy = Object.freeze({
  membership: "backend-owned",
  pagination: "backend-windowed",
  virtualization: "requires-stable-server-window",
  selection: "presentation-only"
})

export type CollectionKeyboardCallbacks = {open: (id: number) => void; preview?: (id: number) => void; clear: () => void}
export const handleCollectionKeyboard = (event: KeyboardEvent, ids: number[], state: SurfaceViewState, callbacks: CollectionKeyboardCallbacks) => {
  const target = event.target
  if (target instanceof HTMLInputElement || target instanceof HTMLTextAreaElement || target instanceof HTMLSelectElement || (target instanceof HTMLElement && target.isContentEditable) || !ids.length) return false
  const currentIndex = state.selectedId === null ? -1 : ids.indexOf(state.selectedId)
  const move = (delta: number) => { const next = ids[Math.max(0, Math.min(ids.length - 1, currentIndex + delta))]; if (next === undefined) return; event.preventDefault(); state.selectedId = next }
  if (event.key === "ArrowDown" || event.key.toLowerCase() === "j") { move(1); return true }
  if (event.key === "ArrowUp" || event.key.toLowerCase() === "k") { move(-1); return true }
  if (event.key === "Enter" && state.selectedId !== null) { event.preventDefault(); callbacks.open(state.selectedId); return true }
  if (event.key.toLowerCase() === "p" && state.selectedId !== null && callbacks.preview) { event.preventDefault(); state.previewId = state.selectedId; callbacks.preview(state.selectedId); return true }
  if (event.key === "Escape") { event.preventDefault(); callbacks.clear(); return true }
  return false
}

const blankState = (): SurfaceViewState => ({displayDensity: "compact", selectedId: null, previewId: null, scrollY: 0})
const stableContext = (value: string) => {
  try { const url = new URL(value, "https://workspace.local"); url.searchParams.delete("selected"); url.searchParams.delete("preview"); return `${url.pathname}${url.search}` } catch { return value }
}

const readState = (key: string): SurfaceViewState => {
  if (typeof window === "undefined") return blankState()
  try {
    const value = JSON.parse(window.sessionStorage.getItem(key) ?? "") as Partial<SurfaceViewState>
    return {
      displayDensity: value.displayDensity === "default" || value.displayDensity === "comfortable" || value.displayDensity === "compact"
        ? value.displayDensity
        : (value as Partial<SurfaceViewState> & {compactDensity?: boolean}).compactDensity === false ? "comfortable" : "compact",
      selectedId: typeof value.selectedId === "number" ? value.selectedId : null,
      previewId: typeof value.previewId === "number" ? value.previewId : null,
      scrollY: typeof value.scrollY === "number" && value.scrollY >= 0 ? value.scrollY : 0
    }
  } catch {
    return blankState()
  }
}

export const useSurfaceViewState = (surface: string, viewerId: Ref<number | undefined>, context: Ref<string>) => {
  const storageKey = computed(() => `surface-view-state:${surface}:viewer:${viewerId.value ?? "anonymous"}:${stableContext(context.value)}`)
  const state = ref<SurfaceViewState>(readState(storageKey.value))

  const persist = () => {
    if (typeof window !== "undefined") window.sessionStorage.setItem(storageKey.value, JSON.stringify(state.value))
  }

  watch(storageKey, (nextKey) => { state.value = readState(nextKey) })
  watch(state, persist, {deep: true})
  const applyUrlState = () => undefined
  const clearStaleSelection = () => { state.value.selectedId = null; state.value.previewId = null }

  return {state, persist, applyUrlState, clearStaleSelection}
}
