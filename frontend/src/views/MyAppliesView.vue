<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getMyApplies, withdrawApply } from '@/services/applyService';
import { getApplyDetail } from '@/services/applyService';
import { toast } from 'vue-sonner';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from '@/components/ui/alert-dialog';
import { useAuthStore } from '@/stores/auth';

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

const myApplies = ref<Apply[]>([]);

const fetchApplies = async () => {
  if (!authStore.isLoggedIn) return;
  try {
    const response = await getMyApplies();
    myApplies.value = response.data;
  } catch (error) {
    toast.error('获取申请列表失败');
  }
};

onMounted(fetchApplies);

const handleWithdraw = async (id: number) => {
  try {
    await withdrawApply(id);
    toast.success('申请已撤回');
    fetchApplies();
  } catch (error: any) {
    toast.error('撤回失败', { description: error.response?.data?.message || '无法撤回此申请' });
  }
};

const viewDetail = async (id: number) => {
  try {
    const response = await getApplyDetail(id);
    // 可以打开详情对话框或跳转到详情页
    toast.info('申请详情', { description: JSON.stringify(response.data, null, 2) });
  } catch (error: any) {
    toast.error('获取详情失败');
  }
};

const formatDateTime = (datetime: string) => new Date(datetime).toLocaleString('zh-CN');

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

const canWithdraw = (apply: Apply) => {
  return apply.status === 'DRAFT' || apply.status === 'PENDING';
};

const sortedApplies = computed(() => {
  return [...myApplies.value].sort((a, b) => {
    return new Date(b.createTime).getTime() - new Date(a.createTime).getTime();
  });
});
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">我的申请</h1>
    <div class="border rounded-md">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="text-left pl-6">申请类型</TableHead>
            <TableHead>申请内容</TableHead>
            <TableHead>申请时间</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>当前节点</TableHead>
            <TableHead class="text-center">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-if="myApplies.length === 0">
            <TableCell colspan="6" class="text-center text-muted-foreground">您还没有任何申请</TableCell>
          </TableRow>
          <TableRow v-for="apply in sortedApplies" :key="apply.applyId">
            <TableCell class="pl-6">{{ getApplyTypeText(apply.applyType) }}</TableCell>
            <TableCell>{{ getApplyTitle(apply) }}</TableCell>
            <TableCell>{{ formatDateTime(apply.createTime) }}</TableCell>
            <TableCell>{{ getStatusText(apply.status) }}</TableCell>
            <TableCell>节点 {{ apply.currentNode }}</TableCell>
            <TableCell class="text-center">
              <div class="flex gap-2 justify-center">
                <Button variant="outline" size="sm" @click="viewDetail(apply.applyId)">查看详情</Button>
                <AlertDialog v-if="canWithdraw(apply)">
                  <AlertDialogTrigger as-child>
                    <Button variant="destructive" size="sm">撤回</Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>确认撤回吗？</AlertDialogTitle>
                      <AlertDialogDescription>
                        此操作无法撤销。您确定要撤回此申请吗？
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>返回</AlertDialogCancel>
                      <AlertDialogAction @click="handleWithdraw(apply.applyId)">确认撤回</AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              </div>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  </div>
</template>
