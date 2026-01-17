<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { FileText, Clock, CheckCircle, XCircle, FileEdit, Users } from 'lucide-vue-next'
import apiClient from '@/services/api'
import { toast } from 'vue-sonner';
import { useAuthStore } from '@/stores/auth'
import { useRouter, useRoute } from 'vue-router'
import { getMyPendingTasks } from '@/services/approvalService'

const authStore = useAuthStore()
const router = useRouter();
const route = useRoute();

const stats = ref({
  myTotalApplies: 0,        // 我的申请总数
  myPendingApplies: 0,      // 我的待审批申请数
  myApprovedApplies: 0,    // 我的已批准申请数
  myRejectedApplies: 0,    // 我的已拒绝申请数
  myDraftApplies: 0,       // 我的草稿数
  myPendingTasks: 0,       // 我的待办任务数
  myProcessedTasks: 0,     // 我的已处理任务数
  totalApplies: 0,         // 系统总申请数
  totalPendingApplies: 0,   // 系统待审批数
});

const isLoading = ref(true);

const isAdmin = computed(() => authStore.hasRole('ROLE_ADMIN'));
const isApprover = computed(() => authStore.hasRole('ROLE_APPROVER'));
const isEmployee = computed(() => authStore.hasRole('ROLE_EMPLOYEE'));

// 根据当前路由判断哪个卡片应该高亮
const isCardActive = (cardRoute: string) => {
  return route.path === cardRoute || route.path.startsWith(cardRoute + '/');
};

const handleCardClick = (route: string) => {
  router.push(route);
};

onMounted(async () => {
  await authStore.checkAuthStatus()
  if (!authStore.isLoggedIn) {
    isLoading.value = false
    return
  }
  try {
    const response = await apiClient.get('/dashboard/stats');
    stats.value = response.data;
    
    // 如果是审批员或管理员，重新获取过滤后的待办任务数量（排除自己的申请）
    if (isApprover.value || isAdmin.value) {
      try {
        const tasksResponse = await getMyPendingTasks();
        const currentUserId = authStore.user?.userId;
        if (currentUserId && tasksResponse.data) {
          // 过滤掉当前用户自己的申请
          const filteredTasks = tasksResponse.data.filter((task: any) => 
            task.applicantId !== currentUserId
          );
          stats.value.myPendingTasks = filteredTasks.length;
        }
      } catch (error) {
        // 如果获取待办任务失败，使用原始统计值
        console.error('获取过滤后的待办任务数量失败', error);
      }
    }
  } catch (error: any) {
    toast.error('获取仪表盘数据失败', {
      description: error.response?.data || 'Network Error: 无法连接到服务器。',
    });
  } finally {
    isLoading.value = false;
  }
});
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">总览仪表盘</h1>
    <div v-if="isLoading" class="text-center">加载中...</div>
    <div v-else class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
      <!-- 系统管理员仪表盘 -->
      <template v-if="isAdmin">
        <!-- 我的申请 -->
        <Card @click="handleCardClick('/applies/my')" 
              :class="['cursor-pointer hover:shadow-lg transition', isCardActive('/applies/my') ? 'border-primary border-2 bg-primary/5' : '']">
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">
              我的申请
            </CardTitle>
            <FileText class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold -mt-3">{{ stats.myTotalApplies }}</div>
            <p class="text-xs text-muted-foreground mt-4">
              我提交的所有申请总数
            </p>
          </CardContent>
        </Card>
        
        <!-- 所有申请 -->
        <Card @click="handleCardClick('/applies/all')" 
              :class="['cursor-pointer hover:shadow-lg transition', isCardActive('/applies/all') ? 'border-primary border-2 bg-primary/5' : '']">
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">
              所有申请
            </CardTitle>
            <Users class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold -mt-3">{{ stats.totalApplies }}</div>
            <p class="text-xs text-muted-foreground mt-4">
              包含历史申请、正在进行的申请
            </p>
          </CardContent>
        </Card>
      </template>

      <!-- 审批员仪表盘 -->
      <template v-else-if="isApprover">
        <!-- 我的申请 -->
        <Card @click="handleCardClick('/applies/all')" 
              :class="['cursor-pointer hover:shadow-lg transition', isCardActive('/applies/all') ? 'border-primary border-2 bg-primary/5' : '']">
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">
              我的申请
            </CardTitle>
            <FileText class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold -mt-3">{{ stats.myTotalApplies }}</div>
            <p class="text-xs text-muted-foreground mt-4">
              我提交的所有申请总数
            </p>
          </CardContent>
        </Card>

        <!-- 我的待批 -->
        <Card @click="handleCardClick('/approvals/tasks')" 
              :class="['cursor-pointer hover:shadow-lg transition', isCardActive('/approvals/tasks') ? 'border-primary border-2 bg-primary/5' : '']">
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">
              我的待批
            </CardTitle>
            <Clock class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold -mt-3">{{ stats.myPendingTasks }}</div>
            <p class="text-xs text-muted-foreground mt-4">
              可进行通过和不通过的选择
            </p>
          </CardContent>
        </Card>
      </template>

      <!-- 普通员工仪表盘 -->
      <template v-else>
        <!-- 我的申请 -->
        <Card @click="handleCardClick('/applies/all')" 
              :class="['cursor-pointer hover:shadow-lg transition', isCardActive('/applies/all') ? 'border-primary border-2 bg-primary/5' : '']">
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">
              我的申请
            </CardTitle>
            <FileText class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold -mt-3">{{ stats.myTotalApplies }}</div>
            <p class="text-xs text-muted-foreground mt-4">
              我提交的所有申请总数
            </p>
          </CardContent>
        </Card>

        <!-- 待审批申请 -->
        <Card @click="handleCardClick('/applies/all')" 
              :class="['cursor-pointer hover:shadow-lg transition', isCardActive('/applies/all') ? 'border-primary border-2 bg-primary/5' : '']">
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">
              待审批申请
            </CardTitle>
            <Clock class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold -mt-3">{{ stats.myPendingApplies }}</div>
            <p class="text-xs text-muted-foreground mt-4">
              我的申请中待审批的数量
            </p>
          </CardContent>
        </Card>
      </template>
    </div>
  </div>
</template>
