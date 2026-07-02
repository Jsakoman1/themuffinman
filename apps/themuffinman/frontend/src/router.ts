import {createRouter, createWebHistory} from "vue-router";
import {isLoggedIn} from "./auth.ts";

const LoginView = () => import("./modules/identity/views/LoginView.vue");
const RegisterView = () => import("./modules/identity/views/RegisterView.vue");
const VisionSurfaceModernView = () => import("./modules/vision/views/VisionSurfaceModernView.vue");


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
        redirect: (to: any) => ({path: '/vision', query: {prompt: `show user #${to.params.id}`, autorun: '1'}}),
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/profile',
        redirect: {path: '/vision', query: {prompt: 'show profile', autorun: '1'}},
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/settings',
        redirect: {path: '/vision', query: {prompt: 'show settings', autorun: '1'}},
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/circles',
        redirect: {path: '/vision', query: {prompt: 'show circles', autorun: '1'}},
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/chat',
        redirect: {path: '/vision', query: {prompt: 'show chat', autorun: '1'}},
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/quests/:id',
        redirect: (to: any) => ({path: '/vision', query: {prompt: `show quest #${to.params.id}`, autorun: '1'}}),
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/applications/:id',
        redirect: (to: any) => ({path: '/vision', query: {prompt: `show application #${to.params.id}`, autorun: '1'}}),
        meta: {requiresAuth: true}
    },
    {
        path: '/vision/applications',
        redirect: {path: '/vision', query: {prompt: 'show applications', autorun: '1'}},
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
