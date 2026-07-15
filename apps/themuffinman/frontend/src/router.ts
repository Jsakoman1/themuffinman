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
const WorkQuestDetailView = () => import("./modules/app-shell/views/WorkQuestDetailView.vue");
const WorkQuestCreateView = () => import("./modules/app-shell/views/WorkQuestCreateView.vue");
const WorkQuestApplicationsView = () => import("./modules/app-shell/views/WorkQuestApplicationsView.vue");
const BusinessBookingsView = () => import("./modules/app-shell/views/BusinessBookingsView.vue");
const BusinessProfileView = () => import("./modules/app-shell/views/BusinessProfileView.vue");
const BusinessOfferingsView = () => import("./modules/app-shell/views/BusinessOfferingsView.vue");
const BusinessAvailabilityView = () => import("./modules/app-shell/views/BusinessAvailabilityView.vue");
const BusinessPublicView = () => import("./modules/app-shell/views/BusinessPublicView.vue");
const BusinessMyBookingsView = () => import("./modules/app-shell/views/BusinessMyBookingsView.vue");
const BusinessAvailabilityExceptionsView = () => import("./modules/app-shell/views/BusinessAvailabilityExceptionsView.vue");
const NotificationsView = () => import("./modules/app-shell/views/NotificationsView.vue");
const CirclesView = () => import("./modules/app-shell/views/CirclesView.vue");
const ChatSurfaceView = () => import("./modules/app-shell/views/ChatSurfaceView.vue");
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
                path: 'work/quests',
                name: 'work-quests',
                component: WorkDiscoveryView,
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
                path: 'work/quests/new',
                name: 'work-quest-create',
                component: WorkQuestCreateView,
                meta: {requiresAuth: true, surfaceId: 'work-quests'}
            },
            {
                path: 'work/applications',
                name: 'work-applications',
                component: WorkApplicationsView,
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
                path: 'profile/settings',
                name: 'profile-settings',
                component: SectionHubView,
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
    routes
})

router.beforeEach((to) => {
    if (to.meta.requiresAuth && !isLoggedIn()) {
        return '/login';
    }

    if (isLoggedIn() && (to.path === '/login' || to.path === '/register')) {
        return '/home';
    }
})
