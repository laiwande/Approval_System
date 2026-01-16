<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getMyApplies, getApplyDetail } from '@/services/applyService';
import { toast } from 'vue-sonner';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuthStore } from '@/stores/auth';
import { CheckCircle, Clock, XCircle } from 'lucide-vue-next';

const authStore = useAuthStore();
const router = useRouter();

export interface Apply {
  applyId: number;
  applyType: 'LEAVE' | 'REIMBURSE';
  applicantName: string;
  status: 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN';
  currentNode: number;
  createTime: string;
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
  records?: Array<{
    approverName: string;
    action: 'APPROVE' | 'REJECT';
    comment: string;
    actionTime: string;
  }>;
}

const applies = ref<Apply[]>([]);
const isLoading = ref(true);

const fetchApplies = async () => {
  if (!authStore.isLoggedIn) return;
  try {
    const response = await getMyApplies();
    // 只显示待审批和已审批的申请
    applies.value = response.data.filter((apply: Apply) => 
      apply.status === 'PENDING' || apply.status === 'APPROVED' || apply.status === 'REJECTED'
    );
  } catch (error) {
    toast.error('获取申请列表失败');
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchApplies);

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return CheckCircle;
    case 'REJECTED':
      return XCircle;
    default:
      return Clock;
  }
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return 'text-green-500'; // 已批准保持绿色
    case 'REJECTED':
      return 'text-red-500';
    default:
      return 'text-purple-500'; // 改成紫色
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case 'PENDING':
      return '待审批';
    case 'APPROVED':
      return '已批准';
    case 'REJECTED':
      return '已拒绝';
    default:
      return status;
  }
};

const viewDetail = (applyId: number) => {
  router.push(`/applies/${applyId}`);
};
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">审核进度</h1>
    <div v-if="isLoading" class="text-center">加载中...</div>
    <div v-else-if="applies.length === 0" class="text-center text-muted-foreground">
      暂无进行中的申请
    </div>
    <div v-else class="grid gap-4">
      <Card v-for="apply in applies" :key="apply.applyId" class="cursor-pointer hover:shadow-lg transition" @click="viewDetail(apply.applyId)">
        <CardHeader>
          <div class="flex justify-between items-start">
            <div>
              <CardTitle class="text-lg">
                {{ apply.applyType === 'LEAVE' ? '请假申请' : '报销申请' }}
              </CardTitle>
              <p class="text-sm text-muted-foreground mt-1">
                申请时间：{{ new Date(apply.createTime).toLocaleString() }}
              </p>
            </div>
            <component :is="getStatusIcon(apply.status)" :class="['h-5 w-5', getStatusColor(apply.status)]" />
          </div>
        </CardHeader>
        <CardContent>
          <div class="space-y-2">
            <div class="flex items-center gap-2">
              <span class="font-medium">状态：</span>
              <span :class="getStatusColor(apply.status)">{{ getStatusText(apply.status) }}</span>
            </div>
            <div v-if="apply.leaveApply">
              <p class="text-sm">请假类型：{{ apply.leaveApply.leaveType }}</p>
              <p class="text-sm">请假时间：{{ new Date(apply.leaveApply.startTime).toLocaleDateString() }} - {{ new Date(apply.leaveApply.endTime).toLocaleDateString() }}</p>
            </div>
            <div v-if="apply.reimburseApply">
              <p class="text-sm">报销类型：{{ apply.reimburseApply.expenseType }}</p>
              <p class="text-sm">报销金额：¥{{ apply.reimburseApply.amount }}</p>
            </div>
            <div v-if="apply.records && apply.records.length > 0" class="mt-4">
              <p class="text-sm font-medium mb-2">审批记录：</p>
              <div class="space-y-1">
                <div v-for="(record, index) in apply.records" :key="index" class="text-sm">
                  <span>{{ record.approverName }}</span>
                  <span :class="record.action === 'APPROVE' ? 'text-green-500' : 'text-red-500'">
                    {{ record.action === 'APPROVE' ? '批准' : '拒绝' }}
                  </span>
                  <span class="text-muted-foreground text-xs ml-2">
                    {{ new Date(record.actionTime).toLocaleString() }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
