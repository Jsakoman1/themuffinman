import {createRouter, createWebHistory} from "vue-router";
import AdminQuestsPage from "./pages/AdminQuestsPage.vue";
import AdminApplicationsPage from "./pages/AdminApplicationsPage.vue";
import AdminCirclesPage from "./pages/AdminCirclesPage.vue";
import AdminUsersPage from "./pages/AdminUsersPage.vue";
import CirclesView from "./views/CirclesView.vue";
import QuestsPage from "./pages/QuestsPage.vue";
import QuestDetailView from "./views/QuestDetailView.vue";
import ApplicationDetailView from "./views/ApplicationDetailView.vue";
import LoginView from "./views/LoginView.vue";
import RegisterView from "./views/RegisterView.vue";
import UserProfileView from "./views/UserProfileView.vue";
import {isAdmin, isLoggedIn} from "./auth.ts";


const routes = [
    {
        path: '/',
        redirect: '/quests'
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
        path: '/quests',
        component: QuestsPage,
        meta: {requiresAuth: true}
    },
    {
        path: '/circles',
        component: CirclesView,
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
        path: '/admin',
        redirect: '/admin/quests',
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
        return '/quests';
    }

    if (isAdmin() && to.path === '/quests') {
        return '/admin/quests';
    }

    if (isLoggedIn() && (to.path === '/login' || to.path === '/register')) {
        return isAdmin() ? '/admin/quests' : '/quests';
    }
})
