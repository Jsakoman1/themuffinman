<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import type {RideOfferResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import AppButton from "../components/AppButton.vue"
import DetailSurface from "../components/DetailSurface.vue"
import DetailUtilityRail from "../components/DetailUtilityRail.vue"

const route = useRoute()
const router = useRouter()
const detail = ref<RideOfferResponseDTO | null>(null)
const loading = ref(true)
const acting = ref(false)
const error = ref("")
const feedback = ref("")
const rideId = computed(() => Number(route.params.rideId))
const returnTo = computed(() => {
  const value = route.query.returnTo
  if (typeof value !== "string") return "/rides"
  try { const target = new URL(value, "https://workspace.local"); return ["/rides", "/rides/mine"].includes(target.pathname) ? `${target.pathname}${target.search}` : "/rides" } catch { return "/rides" }
})
const load = async () => {
  loading.value = true; error.value = ""
  try { detail.value = await userShellApi.getRideOffer(rideId.value) }
  catch { detail.value = null; error.value = "Could not load this ride or you no longer have access." }
  finally { loading.value = false }
}
const run = async (label: string, action: () => Promise<RideOfferResponseDTO>) => {
  acting.value = true; feedback.value = ""; error.value = ""
  try { detail.value = await action(); feedback.value = label }
  catch { error.value = "Could not update this ride. Refresh and try again." }
  finally { acting.value = false }
}
watch(rideId, () => void load())
onMounted(() => void load())
</script>

<template>
  <section class="ride-detail">
    <AppButton type="button" tone="secondary" class="ride-detail__back" @click="router.push(returnTo)">Back to rides</AppButton>
    <AppStatus v-if="loading" message="Loading ride." busy />
    <AppStatus v-else-if="error && !detail" :message="error" tone="error" retry @retry="load" />
    <template v-else-if="detail">
      <AppStatus v-if="feedback" :message="feedback" tone="success" />
      <AppStatus v-if="error" :message="error" tone="error" retry @retry="load" />
      <DetailSurface :title="`${detail.origin} to ${detail.destination}`" utility-label="Ride actions">
        <template #header><header class="ride-detail__header"><p>Rides / Detail</p><h1>{{ detail.origin }} → {{ detail.destination }}</h1></header></template>
        <template #default><dl class="ride-detail__facts"><div><dt>Departure</dt><dd>{{ new Date(detail.departureAt).toLocaleString() }}</dd></div><div><dt>Seats</dt><dd>{{ detail.joinedSeats }} / {{ detail.seats }}</dd></div><div><dt>Driver</dt><dd>{{ detail.driverUsername }}</dd></div><div><dt>Status</dt><dd>{{ detail.status }}</dd></div><div v-if="detail.visibleCircleNames.length"><dt>Visible to</dt><dd>{{ detail.visibleCircleNames.join(', ') }}</dd></div></dl><p v-if="detail.note" class="ride-detail__note">{{ detail.note }}</p></template>
        <template #utility><DetailUtilityRail title="Ride actions"><AppButton v-if="detail.canJoin" type="button" tone="primary" :loading="acting" @click="run('Joined ride.', () => userShellApi.joinRide(detail!.id))">Join</AppButton><AppButton v-if="detail.canLeave" type="button" tone="secondary" :loading="acting" @click="run('Left ride.', () => userShellApi.leaveRide(detail!.id))">Leave</AppButton><AppButton v-if="detail.canManage && detail.status === 'FULL'" type="button" tone="primary" :loading="acting" @click="run('Ride started.', () => userShellApi.startRide(detail!.id))">Start</AppButton><AppButton v-if="detail.canManage && detail.status === 'IN_PROGRESS'" type="button" tone="primary" :loading="acting" @click="run('Ride completed.', () => userShellApi.completeRide(detail!.id))">Complete</AppButton><AppButton v-if="detail.canManage && ['OPEN', 'FULL'].includes(detail.status)" type="button" tone="danger" :loading="acting" class="ride-detail__danger" @click="run('Ride cancelled.', () => userShellApi.cancelRide(detail!.id))">Cancel</AppButton></DetailUtilityRail></template>
      </DetailSurface>
    </template>
  </section>
</template>

<style scoped>
.ride-detail{display:grid;gap:var(--space-3)}.ride-detail__back{justify-self:start;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:transparent;color:var(--text-muted);font:inherit;font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);cursor:pointer}.ride-detail__header{padding:var(--space-4) var(--space-5)}.ride-detail__header p{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.ride-detail__header h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.ride-detail__facts{display:grid;gap:var(--space-2);margin:0}.ride-detail__facts div{display:flex;justify-content:space-between;gap:var(--space-3);padding-bottom:var(--space-2);border-bottom:1px solid var(--border-subtle)}.ride-detail__facts dt{color:var(--text-soft);font-size:var(--text-size-meta)}.ride-detail__facts dd{margin:0;color:var(--text);font-size:var(--text-size-body);text-align:right}.ride-detail__note{margin:var(--space-4) 0 0;color:var(--text-muted);line-height:1.5}.ride-detail :deep(.detail-utility-rail button){width:calc(100% - 2 * var(--space-3));margin:var(--space-3);min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);background:var(--surface-base);color:var(--text);font:inherit;font-weight:var(--text-weight-semibold);cursor:pointer}.ride-detail :deep(.detail-utility-rail .ride-detail__danger){color:var(--danger)}
</style>
<style scoped>
.ride-detail__back:hover { border-color: var(--border-strong); background: var(--surface-hover); color: var(--text); }
.ride-detail :deep(.detail-utility-rail button:hover) { border-color: var(--border-strong); background: var(--surface-hover); }
.ride-detail :deep(.detail-utility-rail .ride-detail__danger:hover) { border-color: var(--danger); background: var(--danger-muted); }
</style>
