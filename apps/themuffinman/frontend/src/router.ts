import {createRouter, createWebHistory} from "vue-router";
import {isLoggedIn} from "./auth.ts";
import {visionBridgeRouteDefinitions} from "./modules/app-shell/shellRouteRegistry.ts";

const LoginView = () => import("./modules/identity/views/LoginView.vue");
const RegisterView = () => import("./modules/identity/views/RegisterView.vue");
const AuthenticatedShellView = () => import("./modules/app-shell/views/AuthenticatedShellView.vue");
const HomeHubView = () => import("./modules/app-shell/views/HomeHubView.vue");
const SectionHubView = () => import("./modules/app-shell/views/WorkspaceSurfaceView.vue");
const WorkDiscoveryView = () => import("./modules/app-shell/views/WorkDiscoveryView.vue");
const WorkApplicationsView = () => import("./modules/app-shell/views/WorkApplicationsView.vue");
const WorkApplicationDetailView = () => import("./modules/app-shell/views/WorkApplicationDetailView.vue");
const WorkQuestDetailView = () => import("./modules/app-shell/views/WorkQuestDetailView.vue");
const WorkQuestCreateView = () => import("./modules/app-shell/views/WorkQuestCreateView.vue");
const WorkQuestApplicationsView = () => import("./modules/app-shell/views/WorkQuestApplicationsView.vue");
const BusinessBookingsView = () => import("./modules/app-shell/views/BusinessBookingsView.vue");
const BusinessProfileView = () => import("./modules/app-shell/views/BusinessProfileView.vue");
const BusinessOfferingsView = () => import("./modules/app-shell/views/BusinessOfferingsView.vue");
const BusinessAvailabilityView = () => import("./modules/app-shell/views/BusinessAvailabilityView.vue");
const BusinessPublicView = () => import("./modules/app-shell/views/BusinessPublicView.vue");
const BusinessDiscoveryView = () => import("./modules/app-shell/views/BusinessDiscoveryView.vue");
const BusinessMyBookingsView = () => import("./modules/app-shell/views/BusinessMyBookingsView.vue");
const BusinessAvailabilityExceptionsView = () => import("./modules/app-shell/views/BusinessAvailabilityExceptionsView.vue");
const NotificationsView = () => import("./modules/app-shell/views/NotificationsView.vue");
const SavedSearchIntentsView = () => import("./modules/app-shell/views/SavedSearchIntentsView.vue");
const OnboardingView = () => import("./modules/app-shell/views/OnboardingView.vue");
const ActivityView = () => import("./modules/app-shell/views/ActivityView.vue");
const CirclesView = () => import("./modules/app-shell/views/CirclesView.vue");
const PeopleDiscoveryView = () => import("./modules/app-shell/views/PeopleDiscoveryView.vue");
const PeopleProfileView = () => import("./modules/app-shell/views/PeopleProfileView.vue");
const ChatSurfaceView = () => import("./modules/app-shell/views/ChatSurfaceView.vue");
const ProfileLocationSettingsView = () => import("./modules/app-shell/views/ProfileLocationSettingsView.vue");
const NotificationPreferencesView = () => import("./modules/app-shell/views/NotificationPreferencesView.vue");
const ThingsDiscoveryView = () => import("./modules/app-shell/views/ThingsDiscoveryView.vue");
const ThingDetailView = () => import("./modules/app-shell/views/ThingDetailView.vue");
const RidesView = () => import("./modules/app-shell/views/RidesView.vue");
const VisionSurfaceModernView = () => import("./modules/vision/views/VisionSurfaceModernView.vue");

const visionBridgeRoutes = visionBridgeRouteDefinitions.map((definition) => ({
    path: definition.path,
    redirect: (to: any) => ({
        path: '/vision',
        query: {
            prompt: typeof definition.prompt === "function" ? definition.prompt(to) : definition.prompt,
            autorun: '1'
        }
    }),
    meta: {requiresAuth: true}
}));


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
                meta: {requiresAuth: true, surfaceId: 'home'}
            },
            {
                path: 'work',
                name: 'work',
                component: WorkDiscoveryView,
                meta: {requiresAuth: true, surfaceId: 'work'}
            },
            {
                path: 'work/find',
                name: 'work-find',
                component: WorkDiscoveryView,
                meta: {requiresAuth: true, surfaceId: 'work'}
            },
            {
                path: 'work/quests',
                name: 'work-quests',
                component: WorkDiscoveryView,
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
                component: WorkApplicationsView,
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
                meta: {requiresAuth: true, surfaceId: 'chat'}
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
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'calendar'}
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
                component: BusinessProfileView,
                meta: {requiresAuth: true, surfaceId: 'business-profile'}
            },
            {
                path: 'business/offerings',
                name: 'business-offerings',
                component: BusinessOfferingsView,
                meta: {requiresAuth: true, surfaceId: 'business-profile'}
            },
            {
                path: 'business/bookings',
                name: 'business-bookings',
                component: BusinessBookingsView,
                meta: {requiresAuth: true, surfaceId: 'business-bookings'}
            },
            {
                path: 'business/calendar',
                name: 'business-calendar',
                component: BusinessAvailabilityView,
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
                path: 'things/mine',
                name: 'things-mine',
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
                path: 'rides/:rideId', name: 'rides-detail', component: RidesView,
                meta: {requiresAuth: true, surfaceId: 'rides'}
            },
            {
                path: 'profile',
                name: 'profile',
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'profile'}
            },
            {
                path: 'notifications',
                name: 'notifications',
                component: NotificationsView,
                meta: {requiresAuth: true, surfaceId: 'home'}
            },
            {
                path: 'search/saved',
                name: 'saved-searches',
                component: SavedSearchIntentsView,
                meta: {requiresAuth: true, surfaceId: 'home'}
            },
            {
                path: 'profile/settings',
                name: 'profile-settings',
                component: ProfileLocationSettingsView,
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
                meta: {requiresAuth: true, surfaceId: 'profile-settings'}
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
        path: '/vision',
        component: VisionSurfaceModernView,
        meta: {requiresAuth: true}
    },
    ...visionBridgeRoutes
];

export const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior(_to, _from, savedPosition) {
        return savedPosition ?? {top: 0}
    }
})

router.beforeEach((to) => {
    if (to.meta.requiresAuth && !isLoggedIn()) {
        return '/login';
    }

    if (isLoggedIn() && (to.path === '/login' || to.path === '/register')) {
        return '/home';
    }
})
