import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import LoginView from '../views/LoginView.vue';
import SignupView from '../views/SignupView.vue';
import DashboardView from '../views/DashboardView.vue';
import NewApplyView from '@/views/NewApplyView.vue';
import MyAppliesView from '@/views/MyAppliesView.vue';
import ApprovalTasksView from '@/views/ApprovalTasksView.vue';
import AdminDashboardView from '@/views/AdminDashboardView.vue';
import ManageUsersView from '@/views/ManageUsersView.vue';
import ManageDepartmentsView from '@/views/ManageDepartmentsView.vue';
import ManagePostsView from '@/views/ManagePostsView.vue';
import ApplyProgressView from '@/views/ApplyProgressView.vue';
import ApplyHistoryView from '@/views/ApplyHistoryView.vue';
import AllAppliesView from '@/views/AllAppliesView.vue';
import ManageApprovalProcessesView from '@/views/ManageApprovalProcessesView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/signup', name: 'signup', component: SignupView },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true }
    },
    {
      path: '/applies/new',
      name: 'new-apply',
      component: NewApplyView,
      meta: { requiresAuth: true }
    },
    {
      path: '/applies/my',
      name: 'my-applies',
      component: MyAppliesView,
      meta: { requiresAuth: true }
    },
    {
      path: '/applies/progress',
      name: 'apply-progress',
      component: ApplyProgressView,
      meta: { requiresAuth: true }
    },
    {
      path: '/applies/history',
      name: 'apply-history',
      component: ApplyHistoryView,
      meta: { requiresAuth: true }
    },
    {
      path: '/applies/all',
      name: 'all-applies',
      component: AllAppliesView,
      meta: { requiresAuth: true }
    },
    {
      path: '/applies/:id',
      name: 'apply-detail',
      component: MyAppliesView,
      meta: { requiresAuth: true }
    },
    {
      path: '/approvals/tasks',
      name: 'approval-tasks',
      component: ApprovalTasksView,
      meta: { requiresAuth: true, roles: ['ROLE_APPROVER', 'ROLE_ADMIN'] }
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: AdminDashboardView,
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] } 
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: ManageUsersView,
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] } 
    },
    {
      path: '/admin/departments',
      name: 'admin-departments',
      component: ManageDepartmentsView,
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] } 
    },
    {
      path: '/admin/posts',
      name: 'admin-posts',
      component: ManagePostsView,
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] } 
    },
    {
      path: '/admin/approval-processes',
      name: 'admin-approval-processes',
      component: ManageApprovalProcessesView,
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] } 
    },
  ]
});

// 全局前置守卫
router.beforeEach(async (to, _from, next) => { 
  const authStore = useAuthStore(); 
  
  // 在首次加载或刷新时检查认证状态
  if (!authStore.isLoggedIn) {
      await authStore.checkAuthStatus();
  }

  const requiresAuth = to.meta.requiresAuth; 
  const isLoggedIn = authStore.isLoggedIn;

  if (requiresAuth && !isLoggedIn) { 
    return next({ name: 'login' }); 
  }

  // 检查基于角色的访问权限
  const requiredRoles = to.meta.roles as string[]; 
  if (isLoggedIn && requiredRoles && requiredRoles.length > 0) { 
    const userHasRequiredRole = requiredRoles.some(role => authStore.hasRole(role));
    if (!userHasRequiredRole) {
      // 如果用户缺少所需角色，则重定向到安全的页面（例如仪表盘）
      return next({ name: 'dashboard' });
    }
  }
  
  next();
});

export default router;