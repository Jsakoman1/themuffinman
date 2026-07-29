<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {RouterLink, useRoute} from "vue-router"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"
import {useShellSurfaceData} from "../shellSurfaceData.ts"
import AppLoadingState from "../components/AppLoadingState.vue"
import AppStatus from "../components/AppStatus.vue"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import HomeCalendarPreview from "../components/HomeCalendarPreview.vue"
import {userShellApi, type AttentionCenter} from "../api/userShellApi.ts"

const route = useRoute()
const surface = getAppSurfaceConfig("home")
const {model, isLoading, error, reload} = useShellSurfaceData("home", route)
const homeSections = computed(() => model.value.sections)
const attention = ref<AttentionCenter | null>(null)
const attentionItems = computed(() => attention.value?.items.filter(item => item.resumable || item.route).slice(0, 4) ?? [])
const nextAttentionItem = computed(() => attentionItems.value[0] ?? null)
onMounted(async () => { try { attention.value = await userShellApi.getAttentionCenter() } catch { attention.value = null } })
</script>

<template>
  <section class="home-hub" aria-live="polite" :aria-busy="isLoading || undefined" data-native-frame="calm-content-canvas" data-home-model="personal-day-next-action">
    <SurfaceHeader :config="surface" />
    <AppLoadingState v-if="isLoading" label="Loading your active workspace" :rows="5" />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="reload" />
    <div v-else class="home-hub__content">
      <section class="home-hub__day" aria-label="Your day"><p>Today</p><h1>What needs your attention?</h1><span>{{ nextAttentionItem ? 'Start with the next decision below, then return whenever you need an overview.' : 'You are up to date. Browse a space when you are ready.' }}</span></section>
      <section v-if="attention" class="home-hub__attention" aria-labelledby="home-attention-title"><header><div><p>Needs attention</p><h2 id="home-attention-title">{{ attention.unreadCount ? `${attention.unreadCount} updates need a decision` : 'You are up to date' }}</h2></div><RouterLink to="/notifications">Open attention</RouterLink></header><div v-if="attentionItems.length" class="home-hub__rows"><SurfaceRow v-for="item in attentionItems" :key="`${item.kind}-${item.resumeKey || item.occurredAt}`" :row="{title: item.title, description: item.summary, badge: item.kind, to: item.route || '/notifications'}" density="compact" /></div><p v-else>There is nothing you need to act on right now.</p></section>
      <div class="home-hub__sections">
        <section v-for="section in homeSections" :key="section.title" class="home-hub__section" :aria-labelledby="`home-${section.title.toLowerCase()}-title`">
          <header class="home-hub__section-header">
            <div>
              <h2 :id="`home-${section.title.toLowerCase()}-title`">{{ section.title }}</h2>
            </div>
            <nav v-if="section.actions?.length" class="home-hub__actions" :aria-label="`${section.title} actions`">
              <RouterLink v-for="action in section.actions" :key="action.id" :to="action.route" class="home-hub__action">{{ action.label }}</RouterLink>
            </nav>
          </header>
          <template v-if="section.groups?.length">
            <div v-for="group in section.groups" :key="group.title" class="home-hub__group">
              <h3>{{ group.title }}</h3>
              <div v-if="group.rows.length" class="home-hub__rows"><SurfaceRow v-for="row in group.rows" :key="row.id" :row="row" density="compact" /></div>
              <p v-else class="home-hub__group-empty">Nothing new</p>
            </div>
          </template>
          <div v-else-if="section.rows.length" class="home-hub__rows"><SurfaceRow v-for="row in section.rows" :key="row.id" :row="row" density="compact" /></div>
          <AppStatus v-if="!section.groups?.length && !section.rows.length" :message="section.emptyState" />
        </section>
      </div>
    </div>
    <HomeCalendarPreview />
  </section>
</template>

<style scoped>
.home-hub{display:grid;gap:var(--space-4);min-width:0;max-width:72rem}.home-hub__content{display:grid;gap:var(--space-4)}.home-hub__day{display:grid;gap:var(--space-1);max-width:48rem}.home-hub__day p{margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.home-hub__day h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.home-hub__day span{color:var(--text-muted)}.home-hub__attention{display:grid;gap:var(--space-2);border:1px solid var(--accent);border-radius:var(--radius-surface);background:var(--accent-muted);overflow:hidden}.home-hub__attention header{display:flex;justify-content:space-between;gap:var(--space-3);align-items:end;padding:var(--space-3)}.home-hub__attention p,.home-hub__attention h2{margin:0}.home-hub__attention header p{color:var(--text-muted);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.home-hub__attention h2{font-size:var(--text-size-title)}.home-hub__attention>a{color:var(--accent);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}.home-hub__attention>p{margin:0;padding:0 var(--space-3) var(--space-3);color:var(--text-muted)}.home-hub__sections{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:var(--space-3)}.home-hub__section{display:grid;align-content:start;min-width:0;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base);overflow:hidden}.home-hub__section-header{display:flex;align-items:flex-start;justify-content:space-between;gap:var(--space-2);padding:var(--space-3);border-bottom:1px solid var(--border-subtle)}.home-hub__section-header h2{margin:0;color:var(--text);font-size:var(--text-size-title);letter-spacing:var(--tracking-tight)}.home-hub__actions{display:flex;flex-wrap:wrap;justify-content:flex-end;gap:var(--space-1)}.home-hub__action{padding:var(--space-1) var(--space-2);border:1px solid var(--border-subtle);border-radius:var(--radius-control);color:var(--text);font-size:var(--text-size-meta);text-decoration:none;white-space:nowrap}.home-hub__action:hover{border-color:var(--accent);color:var(--accent)}.home-hub__group{border-bottom:1px solid var(--border-subtle)}.home-hub__group:last-child{border-bottom:0}.home-hub__group h3{margin:0;padding:var(--space-2) var(--space-3) var(--space-1);color:var(--text-muted);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.home-hub__group-empty{margin:0;padding:var(--space-2) var(--space-3);color:var(--text-soft);font-size:var(--text-size-meta)}.home-hub__rows :deep(.surface-row:last-child){border-bottom:0}.home-hub__section>.app-status{margin:var(--space-3)}
@media(max-width:760px){.home-hub__sections{grid-template-columns:1fr}.home-hub__section-header{align-items:flex-start;flex-direction:column}.home-hub__actions{justify-content:flex-start}}
</style>
