<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getAllApplies } from '@/services/approvalService';
import { getMyApplies } from '@/services/applyService';
import { toast } from 'vue-sonner';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { useAuthStore } from '@/stores/auth';
import { CheckCircle, XCircle, Clock, FileEdit } from 'lucide-vue-next';

const authStore = useAuthStore();
const router = useRouter();

const isAdmin = computed(() => authStore.hasRole('ROLE_ADMIN'));

export interface Apply {
  applyId: number;
  applyType: 'LEAVE' | 'REIMBURSE';
  applicantName: string;
  status: 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN';
  currentNode: number;
  createTime: string;
  updateTime?: string;
  leaveApply?: {
    leaveType: string;
    startTime: string;
    endTime: string;
    leaveDays: number;
    reason: string;
  };
  reimburseApply?: {
    expenseType: string;
    amount: number;
    reason: string;
  };
}

const allApplies = ref<Apply[]>([]);
const isLoading = ref(true);
const filterStatus = ref<string>('ALL'); // ALL, PENDING, APPROVED, REJECTED, DRAFT, WITHDRAWN

const fetchAllApplies = async () => {
  if (!authStore.isLoggedIn) return;
  try {
    let response;
    if (isAdmin.value) {
      // 管理员：获取所有用户的申请
      response = await getAllApplies();
    } else {
      // 审批员和普通员工：获取自己的所有申请
      response = await getMyApplies();
    }
    allApplies.value = response.data;
  } catch (error: any) {
    toast.error('获取申请列表失败', {
      description: error.response?.data?.message || '无法获取申请列表'
    });
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchAllApplies);

const filteredApplies = computed(() => {
  if (filterStatus.value === 'ALL') {
    return allApplies.value;
  }
  return allApplies.value.filter(apply => apply.status === filterStatus.value);
});

const sortedApplies = computed(() => {
  return [...filteredApplies.value].sort((a, b) => {
    return new Date(b.createTime).getTime() - new Date(a.createTime).getTime();
  });
});

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '待审批',
    APPROVED: '已批准',
    REJECTED: '已拒绝',
    WITHDRAWN: '已撤回',
  };
  return statusMap[status] || status;
};

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return CheckCircle;
    case 'REJECTED':
      return XCircle;
    case 'PENDING':
      return Clock;
    case 'DRAFT':
      return FileEdit;
    default:
      return null;
  }
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return 'text-green-500'; // 已批准保持绿色
    case 'REJECTED':
      return 'text-red-500';
    case 'PENDING':
      return 'text-yellow-500';
    case 'DRAFT':
      return 'text-gray-500';
    case 'WITHDRAWN':
      return 'text-gray-400';
    default:
      return '';
  }
};

const getApplyTypeText = (type: string) => {
  return type === 'LEAVE' ? '请假' : '报销';
};

const getApplyTitle = (apply: Apply) => {
  if (apply.leaveApply) {
    return apply.leaveApply.reason || '请假申请';
  } else if (apply.reimburseApply) {
    return apply.reimburseApply.reason || '报销申请';
  }
  return '申请';
};

const viewDetail = (applyId: number) => {
  router.push(`/applies/${applyId}`);
};
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">{{ isAdmin ? '所有申请' : '我的所有申请' }}</h1>
      <div class="flex gap-2">
        <Button 
          variant="outline" 
          size="sm" 
          :class="{ 'bg-primary/10 text-primary border-primary': filterStatus === 'ALL' }"
          @click="filterStatus = 'ALL'"
        >
          全部
        </Button>
        <Button 
          variant="outline" 
          size="sm" 
          :class="{ 'bg-primary/10 text-primary border-primary': filterStatus === 'PENDING' }"
          @click="filterStatus = 'PENDING'"
        >
          待审批
        </Button>
        <Button 
          variant="outline" 
          size="sm" 
          :class="{ 'bg-primary/10 text-primary border-primary': filterStatus === 'APPROVED' }"
          @click="filterStatus = 'APPROVED'"
        >
          已批准
        </Button>
        <Button 
          variant="outline" 
          size="sm" 
          :class="{ 'bg-primary/10 text-primary border-primary': filterStatus === 'REJECTED' }"
          @click="filterStatus = 'REJECTED'"
        >
          已拒绝
        </Button>
        <Button 
          variant="outline" 
          size="sm" 
          :class="{ 'bg-primary/10 text-primary border-primary': filterStatus === 'DRAFT' }"
          @click="filterStatus = 'DRAFT'"
        >
          草稿
        </Button>
      </div>
    </div>
    <div v-if="isLoading" class="text-center py-10">加载中...</div>
    <div v-else class="border rounded-md">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="text-left pl-6">申请类型</TableHead>
            <TableHead>申请人</TableHead>
            <TableHead>申请内容</TableHead>
            <TableHead>申请时间</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>当前节点</TableHead>
            <TableHead class="text-center">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-if="sortedApplies.length === 0">
            <TableCell colspan="7" class="text-center text-muted-foreground">
              {{ filterStatus === 'ALL' ? '暂无申请记录' : `暂无${getStatusText(filterStatus)}状态的申请` }}
            </TableCell>
          </TableRow>
          <TableRow v-for="apply in sortedApplies" :key="apply.applyId">
            <TableCell class="pl-6">{{ getApplyTypeText(apply.applyType) }}</TableCell>
            <TableCell>{{ apply.applicantName }}</TableCell>
            <TableCell>{{ getApplyTitle(apply) }}</TableCell>
            <TableCell>{{ new Date(apply.createTime).toLocaleString('zh-CN') }}</TableCell>
            <TableCell>
              <div class="flex items-center gap-2">
                <component 
                  v-if="getStatusIcon(apply.status)" 
                  :is="getStatusIcon(apply.status)" 
                  :class="['h-4 w-4', getStatusColor(apply.status)]" 
                />
                <span :class="getStatusColor(apply.status)">
                  {{ getStatusText(apply.status) }}
                </span>
              </div>
            </TableCell>
            <TableCell>节点 {{ apply.currentNode }}</TableCell>
            <TableCell class="text-center">
              <Button variant="outline" size="sm" @click="viewDetail(apply.applyId)">
                查看详情
              </Button>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  </div>
</template>
