<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getMyApplies } from '@/services/applyService';
import { toast } from 'vue-sonner';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { useAuthStore } from '@/stores/auth';
import { CheckCircle, XCircle } from 'lucide-vue-next';

const authStore = useAuthStore();
const router = useRouter();

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

const completedApplies = computed(() => {
  return allApplies.value.filter(apply => 
    apply.status === 'APPROVED' || apply.status === 'REJECTED' || apply.status === 'WITHDRAWN'
  );
});

const fetchApplies = async () => {
  if (!authStore.isLoggedIn) return;
  try {
    const response = await getMyApplies();
    allApplies.value = response.data;
  } catch (error) {
    toast.error('获取申请列表失败');
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchApplies);

const getStatusText = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return '已批准';
    case 'REJECTED':
      return '已拒绝';
    case 'WITHDRAWN':
      return '已撤回';
    default:
      return status;
  }
};

const getStatusIcon = (status: string) => {
  if (status === 'APPROVED') return CheckCircle;
  if (status === 'REJECTED') return XCircle;
  return null;
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return 'text-green-500'; // 已批准保持绿色
    case 'REJECTED':
      return 'text-red-500';
    case 'WITHDRAWN':
      return 'text-gray-500';
    default:
      return '';
  }
};

const viewDetail = (applyId: number) => {
  router.push(`/applies/${applyId}`);
};
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">历史审核</h1>
    <div v-if="isLoading" class="text-center">加载中...</div>
    <div v-else-if="completedApplies.length === 0" class="text-center text-muted-foreground">
      暂无历史申请记录
    </div>
    <div v-else class="border rounded-lg">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>申请类型</TableHead>
            <TableHead>申请时间</TableHead>
            <TableHead>完成时间</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>详情</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="apply in completedApplies" :key="apply.applyId">
            <TableCell>
              {{ apply.applyType === 'LEAVE' ? '请假申请' : '报销申请' }}
            </TableCell>
            <TableCell>
              {{ new Date(apply.createTime).toLocaleString() }}
            </TableCell>
            <TableCell>
              {{ apply.updateTime ? new Date(apply.updateTime).toLocaleString() : 'N/A' }}
            </TableCell>
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
            <TableCell>
              <div v-if="apply.leaveApply" class="text-sm">
                <p>类型：{{ apply.leaveApply.leaveType }}</p>
                <p>天数：{{ apply.leaveApply.leaveDays }} 天</p>
              </div>
              <div v-if="apply.reimburseApply" class="text-sm">
                <p>类型：{{ apply.reimburseApply.expenseType }}</p>
                <p>金额：¥{{ apply.reimburseApply.amount }}</p>
              </div>
            </TableCell>
            <TableCell>
              <Button variant="ghost" size="sm" @click="viewDetail(apply.applyId)">
                查看详情
              </Button>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  </div>
</template>
