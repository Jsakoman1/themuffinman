import {createRouter, createWebHistory} from "vue-router";
import {isAdmin, isLoggedIn} from "./auth.ts";

const AdminOverviewPage = () => import("./modules/workmarket/pages/AdminOverviewPage.vue");
const AdminQuestsPage = () => import("./modules/workmarket/pages/AdminQuestsPage.vue");
const AdminApplicationsPage = () => import("./modules/workmarket/pages/AdminApplicationsPage.vue");
const AdminCirclesPage = () => import("./modules/social/pages/AdminCirclesPage.vue");
const AdminUsersPage = () => import("./modules/workmarket/pages/AdminUsersPage.vue");
const CirclesView = () => import("./modules/social/views/CirclesView.vue");
const QuestsPage = () => import("./modules/workmarket/pages/QuestsPage.vue");
const QuestDetailView = () => import("./modules/workmarket/views/QuestDetailView.vue");
const ApplicationDetailView = () => import("./modules/workmarket/views/ApplicationDetailView.vue");
const LoginView = () => import("./modules/identity/views/LoginView.vue");
const RegisterView = () => import("./modules/identity/views/RegisterView.vue");
const UserProfileView = () => import("./modules/social/views/UserProfileView.vue");
const UserSettingsView = () => import("./modules/social/views/UserSettingsView.vue");
const ModulePlaceholderView = () => import("./views/ModulePlaceholderView.vue");


const routes = [
    {
        path: '/',
        redirect: '/work'
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
        path: '/work',
        component: QuestsPage,
        meta: {requiresAuth: true}
    },
    {
        path: '/quests',
        redirect: '/work'
    },
    {
        path: '/circles',
        component: CirclesView,
        meta: {requiresAuth: true}
    },
    {
        path: '/work/:id',
        component: QuestDetailView,
        meta: {requiresAuth: true}
    },
    {
        path: '/quests/:id',
        component: QuestDetailView,
        meta: {requiresAuth: true}
    },
    {
        path: '/applications/:id',
        component: ApplicationDetailView,
        meta: {requiresAuth: true}
    },
    {
        path: '/users/:id',
        component: UserProfileView,
        meta: {requiresAuth: true}
    },
    {
        path: '/settings',
        component: UserSettingsView,
        meta: {requiresAuth: true}
    },
    {
        path: '/admin',
        redirect: '/admin/work',
        meta: {requiresAuth: true, requiresAdmin: true}
    },
    {
        path: '/admin/work',
        component: AdminOverviewPage,
        meta: {requiresAuth: true, requiresAdmin: true}
    },
    {
        path: '/admin/quests',
        component: AdminQuestsPage,
        meta: {requiresAuth: true, requiresAdmin: true}
    },
    {
        path: '/admin/users',
        component: AdminUsersPage,
        meta: {requiresAuth: true, requiresAdmin: true}
    },
    {
        path: '/admin/applications',
        component: AdminApplicationsPage,
        meta: {requiresAuth: true, requiresAdmin: true}
    },
    {
        path: '/admin/circles',
        component: AdminCirclesPage,
        meta: {requiresAuth: true, requiresAdmin: true}
    },
    {
        path: '/app-users',
        redirect: '/admin/users'
    },
    {
        path: '/business',
        component: ModulePlaceholderView,
        meta: {
            requiresAuth: true,
            moduleKey: 'business',
            moduleTitle: 'Business Hub',
            moduleDescription: 'Business profiles, mini websites, calendars, and appointment booking will live here.'
        }
    },
    {
        path: '/things',
        component: ModulePlaceholderView,
        meta: {
            requiresAuth: true,
            moduleKey: 'things',
            moduleTitle: 'Thing Sharing',
            moduleDescription: 'Item lending, borrowing, and sharing workflows will live here.'
        }
    },
    {
        path: '/rides',
        component: ModulePlaceholderView,
        meta: {
            requiresAuth: true,
            moduleKey: 'rides',
            moduleTitle: 'Car Sharing',
            moduleDescription: 'Voluntary route-based ride sharing between selected circles will live here.'
        }
    },
    {
        path: '/chat',
        component: ModulePlaceholderView,
        meta: {
            requiresAuth: true,
            moduleKey: 'chat',
            moduleTitle: 'Shared Chat',
            moduleDescription: 'Cross-module messaging and conversation history will live here.'
        }
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

    if (to.meta.requiresAdmin && !isAdmin()) {
        return '/work';
    }

    if (isAdmin() && (to.path === '/work' || to.path === '/quests')) {
        return '/admin/work';
    }

    if (isLoggedIn() && (to.path === '/login' || to.path === '/register')) {
        return isAdmin() ? '/admin/work' : '/work';
    }
})
