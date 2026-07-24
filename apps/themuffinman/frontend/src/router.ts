import {createRouter, createWebHistory} from "vue-router";
import {isLoggedIn} from "./auth.ts";
import {visionBridgeRouteDefinitions} from "./modules/app-shell/shellRouteRegistry.ts";
import type {AppSurfaceId} from "./modules/app-shell/shellDefinitions.ts";
import {getCanonicalSurfaceRoute} from "./modules/app-shell/shellDefinitions.ts";

// Vue Router owns these native dynamic imports. Keep the authenticated shell small
// and load each substantial surface only when its canonical route is entered.
// Do not replace them with eagerly imported route components or a global loading
// overlay that discards the active collection context during navigation.
export const workspaceRouteLoadingPolicy = Object.freeze({
    strategy: "native-route-dynamic-import",
    eagerCore: ["router", "auth", "shell-route-registry"],
    preserveActiveSurfaceContext: true
});

const LoginView = () => import("./modules/identity/views/LoginView.vue");
const RegisterView = () => import("./modules/identity/views/RegisterView.vue");
const PasswordRecoveryView = () => import("./modules/identity/views/PasswordRecoveryView.vue");
const PasswordResetView = () => import("./modules/identity/views/PasswordResetView.vue");
const AuthenticatedShellView = () => import("./modules/app-shell/views/AuthenticatedShellView.vue");
const HomeHubView = () => import("./modules/app-shell/views/HomeHubView.vue");
const CalendarPage = () => import("./modules/app-shell/views/CalendarPage.vue");
const SectionHubView = () => import("./modules/app-shell/views/WorkspaceSurfaceView.vue");
const WorkPage = () => import("./modules/app-shell/views/WorkPage.vue");
const WorkApplicationDetailView = () => import("./modules/app-shell/views/WorkApplicationDetailView.vue");
const WorkQuestDetailView = () => import("./modules/app-shell/views/WorkQuestDetailView.vue");
const WorkQuestCreateView = () => import("./modules/app-shell/views/WorkQuestCreateView.vue");
const WorkQuestApplicationsView = () => import("./modules/app-shell/views/WorkQuestApplicationsView.vue");
const BusinessOwnerPage = () => import("./modules/app-shell/views/BusinessOwnerPage.vue");
const BusinessPublicView = () => import("./modules/app-shell/views/BusinessPublicView.vue");
const BusinessDiscoveryView = () => import("./modules/app-shell/views/BusinessDiscoveryView.vue");
const BusinessMyBookingsView = () => import("./modules/app-shell/views/BusinessMyBookingsView.vue");
const BusinessAvailabilityExceptionsView = () => import("./modules/app-shell/views/BusinessAvailabilityExceptionsView.vue");
const BusinessServiceSchemaView = () => import("./modules/app-shell/views/BusinessServiceSchemaView.vue");
const SharePage = () => import("./modules/app-shell/views/SharePage.vue");
const NotificationsView = () => import("./modules/app-shell/views/NotificationsView.vue");
const SavedSearchIntentsView = () => import("./modules/app-shell/views/SavedSearchIntentsView.vue");
const OnboardingView = () => import("./modules/app-shell/views/OnboardingView.vue");
const ActivityView = () => import("./modules/app-shell/views/ActivityView.vue");
const CirclesView = () => import("./modules/app-shell/views/CirclesView.vue");
const PeopleDiscoveryView = () => import("./modules/app-shell/views/PeopleDiscoveryView.vue");
const PeopleProfileView = () => import("./modules/app-shell/views/PeopleProfileView.vue");
const ChatSurfaceView = () => import("./modules/app-shell/views/ChatSurfaceView.vue");
const ProfilePage = () => import("./modules/app-shell/views/ProfilePage.vue");
const NotificationPreferencesView = () => import("./modules/app-shell/views/NotificationPreferencesView.vue");
const ThingsDiscoveryView = () => import("./modules/app-shell/views/ThingsDiscoveryView.vue");
const ThingDetailView = () => import("./modules/app-shell/views/ThingDetailView.vue");
const RidesView = () => import("./modules/app-shell/views/RidesView.vue");
const RideDetailView = () => import("./modules/app-shell/views/RideDetailView.vue");
const visionBridgeRoutes = visionBridgeRouteDefinitions.map((definition) => ({
    path: definition.path,
    redirect: (to: any) => ({
        path: '/home',
        query: {
            visionPrompt: typeof definition.prompt === "function" ? definition.prompt(to) : definition.prompt,
            visionAutorun: '1'
        }
    }),
    meta: {requiresAuth: true}
}));

const workspaceSurfaceMeta = (surfaceId: AppSurfaceId) => ({
    requiresAuth: true,
    surfaceId,
    canonicalRoute: getCanonicalSurfaceRoute(surfaceId)
});


const routes = [
    {
        path: '/',
        component: AuthenticatedShellView,
        meta: {requiresAuth: true},
        children: [
            {
                path: '',
                redirect: '/home'
            },
            {
                path: 'home',
                name: 'home',
                component: HomeHubView,
                meta: workspaceSurfaceMeta('home')
            },
            {
                path: 'work',
                name: 'work',
                component: WorkPage,
                meta: workspaceSurfaceMeta('work')
            },
            {
                path: 'work/find',
                name: 'work-find',
                component: WorkPage,
                meta: {requiresAuth: true, surfaceId: 'work'}
            },
            {
                path: 'work/mine',
                redirect: '/work/quests'
            },
            {
                path: 'work/quests',
                name: 'work-quests',
                component: WorkPage,
                meta: {requiresAuth: true, surfaceId: 'work-quests'}
            },
            {
                path: 'work/quests/new',
                name: 'work-quest-create',
                component: WorkQuestCreateView,
                meta: {requiresAuth: true, surfaceId: 'work-quests'}
            },
            {
                path: 'work/offer',
                name: 'work-offer',
                component: WorkQuestCreateView,
                meta: {requiresAuth: true, surfaceId: 'work-quests'}
            },
            {
                path: 'work/quests/:questId',
                name: 'work-quest-detail',
                component: WorkQuestDetailView,
                meta: {requiresAuth: true, surfaceId: 'work-quests'}
            },
            {
                path: 'work/quests/:questId/applications',
                name: 'work-quest-applications',
                component: WorkQuestApplicationsView,
                meta: {requiresAuth: true, surfaceId: 'work-quests'}
            },
            {
                path: 'work/applications',
                name: 'work-applications',
                component: WorkPage,
                meta: {requiresAuth: true, surfaceId: 'work-applications'}
            },
            {
                path: 'work/applications/:applicationId',
                name: 'work-application-detail',
                component: WorkApplicationDetailView,
                meta: {requiresAuth: true, surfaceId: 'work-applications'}
            },
            {
                path: 'chat',
                name: 'chat',
                component: ChatSurfaceView,
                meta: workspaceSurfaceMeta('chat')
            },
            {
                path: 'chat/:conversationId',
                name: 'chat-conversation',
                component: ChatSurfaceView,
                meta: {requiresAuth: true, surfaceId: 'chat-conversation'}
            },
            {
                path: 'calendar',
                name: 'calendar',
                component: CalendarPage,
                meta: workspaceSurfaceMeta('calendar')
            },
            {
                path: 'business',
                name: 'business',
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'business'}
            },
            {
                path: 'business/profile',
                name: 'business-profile',
                component: BusinessOwnerPage,
                meta: {requiresAuth: true, surfaceId: 'business-profile'}
            },
            {
                path: 'business/offerings',
                name: 'business-offerings',
                component: BusinessOwnerPage,
                meta: {requiresAuth: true, surfaceId: 'business-profile'}
            },
            {
                path: 'business/service-setup',
                name: 'business-service-setup',
                component: BusinessServiceSchemaView,
                meta: {requiresAuth: true, surfaceId: 'business-service-setup'}
            },
            {
                path: 'business/bookings',
                name: 'business-bookings',
                component: BusinessOwnerPage,
                meta: {requiresAuth: true, surfaceId: 'business-bookings'}
            },
            {
                path: 'business/calendar',
                name: 'business-calendar',
                component: BusinessOwnerPage,
                meta: {requiresAuth: true, surfaceId: 'business-calendar'}
            },
            {
                path: 'business/public/:slug',
                name: 'business-public',
                component: BusinessPublicView,
                meta: {requiresAuth: true, surfaceId: 'business'}
            },
            {
                path: 'business/my-bookings',
                name: 'business-my-bookings',
                component: BusinessMyBookingsView,
                meta: {requiresAuth: true, surfaceId: 'business-bookings'}
            },
            {
                path: 'business/availability-exceptions',
                name: 'business-availability-exceptions',
                component: BusinessAvailabilityExceptionsView,
                meta: {requiresAuth: true, surfaceId: 'business-calendar'}
            },
            {
                path: 'circles',
                name: 'circles',
                component: CirclesView,
                meta: {requiresAuth: true, surfaceId: 'circles'}
            },
            {
                path: 'people',
                name: 'people',
                component: PeopleDiscoveryView,
                meta: {requiresAuth: true, surfaceId: 'people'}
            },
            {
                path: 'people/:userId',
                name: 'people-profile',
                component: PeopleProfileView,
                meta: {requiresAuth: true, surfaceId: 'people'}
            },
            {
                path: 'business/find',
                name: 'business-discovery',
                component: BusinessDiscoveryView,
                meta: {requiresAuth: true, surfaceId: 'business-discovery'}
            },
            {
                path: 'things',
                name: 'things',
                component: ThingsDiscoveryView,
                meta: {requiresAuth: true, surfaceId: 'things'}
            },
            {
                path: 'share/things',
                name: 'share-things',
                component: SharePage,
                meta: {requiresAuth: true, surfaceId: 'things'}
            },
            {
                path: 'share/rides',
                name: 'share-rides',
                component: SharePage,
                meta: {requiresAuth: true, surfaceId: 'rides'}
            },
            {
                path: 'share/requests',
                name: 'share-requests',
                component: SharePage,
                meta: {requiresAuth: true, surfaceId: 'things'}
            },
            {
                path: 'things/mine',
                name: 'things-mine',
                component: ThingsDiscoveryView,
                meta: {requiresAuth: true, surfaceId: 'things'}
            },
            {
                path: 'things/requests',
                name: 'things-requests',
                component: ThingsDiscoveryView,
                meta: {requiresAuth: true, surfaceId: 'things'}
            },
            {
                path: 'things/:listingId',
                name: 'things-detail',
                component: ThingDetailView,
                meta: {requiresAuth: true, surfaceId: 'things'}
            },
            {
                path: 'rides', name: 'rides', component: RidesView,
                meta: {requiresAuth: true, surfaceId: 'rides'}
            },
            {
                path: 'rides/mine', name: 'rides-mine', component: RidesView,
                meta: {requiresAuth: true, surfaceId: 'rides'}
            },
            {
                path: 'rides/:rideId', name: 'rides-detail', component: RideDetailView,
                meta: {requiresAuth: true, surfaceId: 'rides'}
            },
            {
                path: 'profile',
                name: 'profile',
                component: ProfilePage,
                meta: {requiresAuth: true, surfaceId: 'profile'}
            },
            {
                path: 'notifications',
                name: 'notifications',
                component: NotificationsView,
                meta: {requiresAuth: true, surfaceId: 'notifications'}
            },
            {
                path: 'search/saved',
                name: 'saved-searches',
                component: SavedSearchIntentsView,
                meta: {requiresAuth: true, surfaceId: 'profile-settings'}
            },
            {
                path: 'profile/settings',
                name: 'profile-settings',
                component: ProfilePage,
                meta: {requiresAuth: true, surfaceId: 'profile-settings'}
            },
            {
                path: 'onboarding',
                name: 'onboarding',
                component: OnboardingView,
                meta: {requiresAuth: true, surfaceId: 'profile-settings'}
            },
            {
                path: 'activity',
                name: 'activity',
                component: ActivityView,
                meta: {requiresAuth: true, surfaceId: 'activity'}
            },
            {
                path: 'profile/settings/notifications',
                name: 'profile-notification-preferences',
                component: NotificationPreferencesView,
                meta: {requiresAuth: true, surfaceId: 'profile-settings'}
            }
        ]
    },
    {
        path: '/login',
        component: LoginView
    },
    {
        path: '/register',
        component: RegisterView
    },
    {
        path: '/recover',
        component: PasswordRecoveryView
    },
    {
        path: '/reset-password',
        component: PasswordResetView
    },
    {
        path: '/vision',
        redirect: (to: any) => {
            const query: Record<string, string> = {}
            if (typeof to.query.prompt === "string" && to.query.prompt.trim()) {
                query.visionPrompt = to.query.prompt.trim()
                query.visionAutorun = "1"
            }
            if (typeof to.query.context === "string" && to.query.context.trim()) query.visionContext = to.query.context.trim()
            if (typeof to.query.source === "string" && to.query.source.trim()) query.visionSource = to.query.source.trim()
            if (typeof to.query.returnTo === "string" && to.query.returnTo.trim()) query.visionReturnTo = to.query.returnTo.trim()
            return Object.keys(query).length > 0 ? {path: "/home", query} : "/home"
        },
        meta: {requiresAuth: true}
    },
    ...visionBridgeRoutes
];

export const collectionSelectionQueryKeys = ["selected", "preview"] as const;
// Disposition rule: aliases preserve deep links; canonical destinations must be the redesigned pages above.
// Duplicate module actions and row-level Vision links are retired; the shell composer is the single visible assistant entry.

export const router = createRouter({
    // Canonical module pages own navigation; legacy aliases only preserve deep links.
    history: createWebHistory(),
    routes,
    scrollBehavior(to, _from, savedPosition) {
        if (to.hash) return {el: to.hash, behavior: 'smooth'}
        return savedPosition ?? {top: 0}
    }
})

router.beforeEach((to) => {
    // Home is the authenticated orientation surface; Web Vision handoffs stay
    // inside VisionForWeb instead of exposing the detached terminal console.
    if (to.meta.requiresAuth && !isLoggedIn()) {
        return '/login';
    }

    if (isLoggedIn() && (to.path === '/login' || to.path === '/register' || to.path === '/recover' || to.path === '/reset-password')) {
        return '/home';
    }
})
