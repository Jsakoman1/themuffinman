import {createRouter, createWebHistory} from "vue-router";
import {isLoggedIn} from "./auth.ts";

const LoginView = () => import("./modules/identity/views/LoginView.vue");
const RegisterView = () => import("./modules/identity/views/RegisterView.vue");
const VisionSurfaceModernView = () => import("./modules/vision/views/VisionSurfaceModernView.vue");
const VisionQuestDetailView = () => import("./modules/vision/views/VisionQuestDetailView.vue");
const VisionApplicationDetailView = () => import("./modules/vision/views/VisionApplicationDetailView.vue");
const VisionUserProfileView = () => import("./modules/vision/views/VisionUserProfileView.vue");
const VisionUserSettingsView = () => import("./modules/vision/views/VisionUserSettingsView.vue");
const VisionCirclesView = () => import("./modules/vision/views/VisionCirclesView.vue");
const VisionChatWorkspaceView = () => import("./modules/vision/views/VisionChatWorkspaceView.vue");


const routes = [
    {
        path: '/',
        redirect: '/vision'
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
    {
        path: '/vision/users/:id',
        component: VisionUserProfileView,
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/settings',
        component: VisionUserSettingsView,
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/circles',
        component: VisionCirclesView,
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/chat',
        component: VisionChatWorkspaceView,
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/quests/:id',
        component: VisionQuestDetailView,
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/applications/:id',
        component: VisionApplicationDetailView,
        meta: {requiresAuth: true}
    }
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
        return '/vision';
    }
})
