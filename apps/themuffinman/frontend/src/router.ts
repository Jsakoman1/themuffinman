import {createRouter, createWebHistory} from "vue-router";
import {isAdmin, isLoggedIn} from "./auth.ts";

const AdminOverviewPage = () => import("./modules/workmarket/pages/AdminOverviewPage.vue");
const AdminQuestsPage = () => import("./modules/workmarket/pages/AdminQuestsPage.vue");
const AdminApplicationsPage = () => import("./modules/workmarket/pages/AdminApplicationsPage.vue");
const AdminCirclesPage = () => import("./modules/social/pages/AdminCirclesPage.vue");
const AdminAgentPage = () => import("./modules/workmarket/pages/AdminAgentPage.vue");
const AdminUsersPage = () => import("./modules/workmarket/pages/AdminUsersPage.vue");
const CirclesView = () => import("./modules/social/views/CirclesView.vue");
const QuestsPage = () => import("./modules/workmarket/pages/QuestsPage.vue");
const QuestDetailView = () => import("./modules/workmarket/views/QuestDetailView.vue");
const ApplicationDetailView = () => import("./modules/workmarket/views/ApplicationDetailView.vue");
const LoginView = () => import("./modules/identity/views/LoginView.vue");
const RegisterView = () => import("./modules/identity/views/RegisterView.vue");
const UserProfileView = () => import("./modules/social/views/UserProfileView.vue");
const UserSettingsView = () => import("./modules/social/views/UserSettingsView.vue");
const BusinessHubView = () => import("./modules/business/views/BusinessHubView.vue");
const ThingSharingView = () => import("./modules/things/views/ThingSharingView.vue");
const RideSharingView = () => import("./modules/rides/views/RideSharingView.vue");
const ChatWorkspaceView = () => import("./modules/chat/views/ChatWorkspaceView.vue");
const VisionSurfaceModernView = () => import("./modules/vision/views/VisionSurfaceModernView.vue");


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
        path: '/admin/agent',
        component: AdminAgentPage,
        meta: {requiresAuth: true, requiresAdmin: true}
    },
    {
        path: '/app-users',
        redirect: '/admin/users'
    },
    {
        path: '/business',
        component: BusinessHubView,
        meta: {requiresAuth: true}
    },
    {
        path: '/things',
        component: ThingSharingView,
        meta: {requiresAuth: true}
    },
    {
        path: '/rides',
        component: RideSharingView,
        meta: {requiresAuth: true}
    },
    {
        path: '/chat',
        component: ChatWorkspaceView,
        meta: {requiresAuth: true}
    },
    {
        path: '/vision',
        component: VisionSurfaceModernView,
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
