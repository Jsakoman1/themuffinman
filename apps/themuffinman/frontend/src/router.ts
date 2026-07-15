import {createRouter, createWebHistory} from "vue-router";
import {isLoggedIn} from "./auth.ts";
import {visionBridgeRouteDefinitions} from "./modules/app-shell/shellRouteRegistry.ts";

const LoginView = () => import("./modules/identity/views/LoginView.vue");
const RegisterView = () => import("./modules/identity/views/RegisterView.vue");
const AuthenticatedShellView = () => import("./modules/app-shell/views/AuthenticatedShellView.vue");
const HomeHubView = () => import("./modules/app-shell/views/HomeHubView.vue");
const SectionHubView = () => import("./modules/app-shell/views/WorkspaceSurfaceView.vue");
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
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'work'}
            },
            {
                path: 'work/quests',
                name: 'work-quests',
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'work-quests'}
            },
            {
                path: 'work/applications',
                name: 'work-applications',
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'work-applications'}
            },
            {
                path: 'chat',
                name: 'chat',
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'chat'}
            },
            {
                path: 'chat/:conversationId',
                name: 'chat-conversation',
                component: SectionHubView,
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
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'business-profile'}
            },
            {
                path: 'business/bookings',
                name: 'business-bookings',
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'business-bookings'}
            },
            {
                path: 'business/calendar',
                name: 'business-calendar',
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'business-calendar'}
            },
            {
                path: 'circles',
                name: 'circles',
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'circles'}
            },
            {
                path: 'profile',
                name: 'profile',
                component: SectionHubView,
                meta: {requiresAuth: true, surfaceId: 'profile'}
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
