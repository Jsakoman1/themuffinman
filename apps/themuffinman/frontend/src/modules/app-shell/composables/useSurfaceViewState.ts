import {computed, ref, watch, type Ref} from "vue"

export type SurfaceViewState = {
  compactDensity: boolean
  selectedId: number | null
  previewId: number | null
  scrollY: number
}

const blankState = (): SurfaceViewState => ({compactDensity: true, selectedId: null, previewId: null, scrollY: 0})

const readState = (key: string): SurfaceViewState => {
  if (typeof window === "undefined") return blankState()
  try {
    const value = JSON.parse(window.sessionStorage.getItem(key) ?? "") as Partial<SurfaceViewState>
    return {
      compactDensity: typeof value.compactDensity === "boolean" ? value.compactDensity : true,
      selectedId: typeof value.selectedId === "number" ? value.selectedId : null,
      previewId: typeof value.previewId === "number" ? value.previewId : null,
      scrollY: typeof value.scrollY === "number" && value.scrollY >= 0 ? value.scrollY : 0
    }
  } catch {
    return blankState()
  }
}

export const useSurfaceViewState = (surface: string, viewerId: Ref<number | undefined>, context: Ref<string>) => {
  const storageKey = computed(() => `surface-view-state:${surface}:viewer:${viewerId.value ?? "anonymous"}:${context.value}`)
  const state = ref<SurfaceViewState>(readState(storageKey.value))

  const persist = () => {
    if (typeof window !== "undefined") window.sessionStorage.setItem(storageKey.value, JSON.stringify(state.value))
  }

  watch(storageKey, (nextKey) => { state.value = readState(nextKey) })
  watch(state, persist, {deep: true})

  return {state, persist}
}
