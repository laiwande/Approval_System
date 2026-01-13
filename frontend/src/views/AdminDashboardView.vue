<script setup lang="ts">
import { onMounted, ref } from 'vue';
import apiClient from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from 'vue-sonner';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { FileText, Clock, Users, Building2, Briefcase } from 'lucide-vue-next';

const authStore = useAuthStore();
const router = useRouter();

const stats = ref({
  myTotalApplies: 0,
  myPendingApplies: 0,
  myApprovedApplies: 0,
  myRejectedApplies: 0,
  myDraftApplies: 0,
  myPendingTasks: 0,
  myProcessedTasks: 0,
  totalApplies: 0,
  totalPendingApplies: 0,
});

const isLoading = ref(true);

onMounted(async () => {
  if (!authStore.isLoggedIn) {
    isLoading.value = false;
    return;
  }
  try {
    const response = await apiClient.get('/dashboard/stats');
    stats.value = response.data;
  } catch (error: any) {
    toast.error('获取仪表盘数据失败', {
      description: error.response?.data || 'Network Error',
    });
  } finally {
    isLoading.value = false;
  }
});
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">管理员仪表盘</h1>
    <div v-if="isLoading" class="text-center">加载中...</div>
    <div v-else class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
      <!-- 系统总申请数 -->
      <Card class="cursor-pointer hover:shadow-lg transition">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">
            系统总申请数
          </CardTitle>
          <FileText class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ stats.totalApplies }}</div>
          <p class="text-xs text-muted-foreground">
            系统中所有申请的总数
          </p>
        </CardContent>
      </Card>

      <!-- 系统待审批数 -->
      <Card class="cursor-pointer hover:shadow-lg transition">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">
            系统待审批数
          </CardTitle>
          <Clock class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ stats.totalPendingApplies }}</div>
          <p class="text-xs text-muted-foreground">
            系统中所有待审批的申请
          </p>
        </CardContent>
      </Card>

      <!-- 我的申请 -->
      <Card @click="router.push('/applies/my')" class="cursor-pointer hover:shadow-lg transition">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">
            我的申请
          </CardTitle>
          <FileText class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ stats.myTotalApplies }}</div>
          <p class="text-xs text-muted-foreground">
            我提交的所有申请总数
          </p>
        </CardContent>
      </Card>

      <!-- 我的待批 -->
      <Card @click="router.push('/approvals/tasks')" class="cursor-pointer hover:shadow-lg transition">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">
            我的待批
          </CardTitle>
          <Clock class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ stats.myPendingTasks }}</div>
          <p class="text-xs text-muted-foreground">
            需要我处理的审批任务
          </p>
        </CardContent>
      </Card>

      <!-- 快速入口 -->
      <Card @click="router.push('/admin/users')" class="cursor-pointer hover:shadow-lg transition">
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">
            用户管理
          </CardTitle>
          <Users class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <p class="text-xs text-muted-foreground">
            管理用户、部门、岗位信息
          </p>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
