<script setup lang="ts">
import {onMounted, ref, watch} from "vue"
import {RouterLink} from "vue-router"
import type {BusinessOwnerDashboardDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import {formatDateTime} from "../../../services/formatters.ts"

const props = defineProps<{businessId: number}>()
const dashboard = ref<BusinessOwnerDashboardDTO | null>(null)
const hasWorkingHours = ref(false)
const loading = ref(true)
const error = ref("")
const businessQuery = () => ({businessId: String(props.businessId)})
const load = async () => {
  loading.value = true
  error.value = ""
  try {
    const [dashboardValue, availability] = await Promise.all([
      userShellApi.getBusinessDashboard(props.businessId),
      userShellApi.getBusinessAvailabilityRules(props.businessId)
    ])
    dashboard.value = dashboardValue
    hasWorkingHours.value = availability.items.some(rule => rule.active)
  }
  catch { error.value = "Could not load this business workspace." }
  finally { loading.value = false }
}
watch(() => props.businessId, () => void load())
onMounted(() => void load())
</script>

<template>
  <section class="business-owner-overview" aria-label="Business overview">
    <AppStatus v-if="loading" message="Loading business overview." busy />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" />
    <template v-else-if="dashboard">
      <section class="business-owner-overview__setup" aria-label="Business setup checklist">
        <div><p class="business-owner-overview__eyebrow">Get ready to take bookings</p><h2>{{ dashboard.activeOfferingCount && hasWorkingHours ? 'Your business can accept bookings' : 'Finish your first setup' }}</h2><p>{{ dashboard.activeOfferingCount && hasWorkingHours ? 'You can now decide when to start accepting customer bookings.' : 'Complete these three essentials once. You can refine everything later.' }}</p></div>
        <ol>
          <li :class="{complete: dashboard.activeOfferingCount > 0}"><span>{{ dashboard.activeOfferingCount > 0 ? '✓' : '1' }}</span><div><strong>Add a service</strong><small>{{ dashboard.activeOfferingCount > 0 ? 'Done' : 'Tell customers what they can book.' }}</small></div><RouterLink v-if="dashboard.activeOfferingCount === 0" :to="{path: '/business/offerings', query: businessQuery()}">Add service</RouterLink></li>
          <li :class="{complete: hasWorkingHours}"><span>{{ hasWorkingHours ? '✓' : '2' }}</span><div><strong>Set your working hours</strong><small>{{ hasWorkingHours ? 'Done' : 'Choose when appointments are available.' }}</small></div><RouterLink v-if="!hasWorkingHours" :to="{path: '/business/settings', query: businessQuery()}">Set hours</RouterLink></li>
          <li :class="{complete: dashboard.activeOfferingCount > 0 && hasWorkingHours}"><span>{{ dashboard.activeOfferingCount > 0 && hasWorkingHours ? '✓' : '3' }}</span><div><strong>Start accepting bookings</strong><small>{{ dashboard.activeOfferingCount > 0 && hasWorkingHours ? 'Turn bookings on when you are ready.' : 'Available after your service and hours are ready.' }}</small></div><RouterLink :to="{path: '/business/settings', query: businessQuery()}">Review settings</RouterLink></li>
        </ol>
      </section>
      <section class="business-owner-overview__summary" aria-label="Business schedule summary">
        <RouterLink :to="{path: '/business/calendar', query: businessQuery()}"><strong>{{ dashboard.todayCount }}</strong><span>Today</span></RouterLink>
        <RouterLink :to="{path: '/business/bookings', query: businessQuery()}"><strong>{{ dashboard.pendingConfirmationCount }}</strong><span>Needs confirmation</span></RouterLink>
        <RouterLink :to="{path: '/business/calendar', query: businessQuery()}"><strong>{{ dashboard.upcomingCount }}</strong><span>Upcoming</span></RouterLink>
        <RouterLink :to="{path: '/business/offerings', query: businessQuery()}"><strong>{{ dashboard.activeOfferingCount }}</strong><span>Active services</span></RouterLink>
      </section>
      <RouterLink v-if="dashboard.pendingConfirmationCount > 0" class="business-owner-overview__attention" :to="{path: '/business/bookings', query: businessQuery()}"><strong>{{ dashboard.pendingConfirmationCount }} booking request{{ dashboard.pendingConfirmationCount === 1 ? '' : 's' }} need{{ dashboard.pendingConfirmationCount === 1 ? 's' : '' }} your decision</strong><span>Review requests</span></RouterLink>
      <CollectionToolbar title="Next bookings" :count="dashboard.scheduleSummary.nextItems.length"><template #actions><RouterLink :to="{path: '/business/bookings', query: businessQuery()}">View bookings</RouterLink></template></CollectionToolbar>
      <AppStatus v-if="dashboard.scheduleSummary.nextItems.length === 0" message="No upcoming bookings for this business." />
      <section v-else class="business-owner-overview__list"><SurfaceRow v-for="item in dashboard.scheduleSummary.nextItems" :key="item.bookingId" :row="{id: String(item.bookingId), title: item.businessOfferingTitle, description: `${item.customerUsername} · ${formatDateTime(item.startsAt)}`, meta: item.statusLabel, to: {path: `/business/bookings/${item.bookingId}`, query: businessQuery()}}" /></section>
    </template>
  </section>
</template>

<style scoped>
.business-owner-overview{display:grid;gap:var(--space-4)}.business-owner-overview__attention{display:flex;justify-content:space-between;gap:var(--space-3);padding:var(--space-3);border:1px solid var(--accent);border-radius:var(--radius-surface);background:var(--accent-muted);color:var(--text);text-decoration:none}.business-owner-overview__attention span{color:var(--accent);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}.business-owner-overview__setup{display:grid;gap:var(--space-3);padding:var(--space-4);border:1px solid color-mix(in srgb,var(--launcher-book-ink) 15%,transparent);border-radius:calc(var(--radius-card) + .2rem);background:var(--launcher-book-bg);color:var(--launcher-book-ink);box-shadow:0 1px 0 color-mix(in srgb,var(--launcher-book-ink) 10%,transparent)}.business-owner-overview__setup>div{display:grid;gap:var(--space-1)}.business-owner-overview__setup h2,.business-owner-overview__setup p{margin:0}.business-owner-overview__setup>div>p:last-child,.business-owner-overview__setup small{color:var(--launcher-book-ink);opacity:.78}.business-owner-overview__eyebrow{color:var(--launcher-book-ink);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.business-owner-overview__setup ol{display:grid;gap:var(--space-2);margin:0;padding:0;list-style:none}.business-owner-overview__setup li{display:grid;grid-template-columns:auto minmax(0,1fr) auto;align-items:center;gap:var(--space-2);padding:var(--space-2);border-top:1px solid color-mix(in srgb,var(--launcher-book-ink) 13%,transparent)}.business-owner-overview__setup li>span{display:grid;place-items:center;width:1.6rem;height:1.6rem;border:1px solid color-mix(in srgb,var(--launcher-book-ink) 40%,transparent);border-radius:50%;font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}.business-owner-overview__setup li.complete>span{border-color:var(--success);background:var(--success-muted);color:var(--success)}.business-owner-overview__setup li div{display:grid;gap:2px}.business-owner-overview__setup a{color:var(--launcher-book-ink);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}.business-owner-overview__summary{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:var(--space-2)}.business-owner-overview__summary a{display:grid;gap:var(--space-1);padding:var(--space-3);border:1px solid color-mix(in srgb,var(--launcher-book-ink) 14%,transparent);border-radius:var(--radius-card);background:color-mix(in srgb,var(--launcher-book-bg) 62%,var(--surface-raised));color:var(--launcher-book-ink);text-decoration:none}.business-owner-overview__summary a:hover{transform:translateY(-1px);background:var(--launcher-book-bg)}.business-owner-overview__summary strong{font-size:var(--text-size-title)}.business-owner-overview__summary span{color:var(--launcher-book-ink);opacity:.75;font-size:var(--text-size-meta)}.business-owner-overview__list{overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.business-owner-overview__list :deep(.surface-row:last-child){border-bottom:0}@media(max-width:700px){.business-owner-overview__setup li{grid-template-columns:auto minmax(0,1fr)}.business-owner-overview__setup li>a{grid-column:2}.business-owner-overview__summary{grid-template-columns:repeat(2,minmax(0,1fr))}}
</style>
