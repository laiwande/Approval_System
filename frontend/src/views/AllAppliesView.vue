<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { getAllApplies } from '@/services/approvalService';
import { getMyApplies, withdrawApply } from '@/services/applyService';
import { getMyPendingTasks, processApproval } from '@/services/approvalService';
import { toast } from 'vue-sonner';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from '@/components/ui/alert-dialog';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useAuthStore } from '@/stores/auth';
import { CheckCircle, XCircle, Clock, FileEdit } from 'lucide-vue-next';

const authStore = useAuthStore();

const isAdmin = computed(() => authStore.hasRole('ROLE_ADMIN'));
const currentUserId = computed(() => authStore.user?.userId);

export interface Apply {
  applyId: number;
  applyType: 'LEAVE' | 'REIMBURSE';
  applicantId?: number;
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

export interface ApprovalTask {
  taskId: number;
  applyId: number;
  status: 'PENDING' | 'DONE';
}

const allApplies = ref<Apply[]>([]);
const pendingTasks = ref<ApprovalTask[]>([]);
const isLoading = ref(true);
const filterStatus = ref<string>('ALL'); // ALL, PENDING, APPROVED, REJECTED, DRAFT, WITHDRAWN

// 审批对话框状态
const isApprovalDialogOpen = ref(false);
const selectedApply = ref<Apply | null>(null);
const approvalAction = ref<'APPROVE' | 'REJECT'>('APPROVE');
const approvalComment = ref('');

const fetchAllApplies = async () => {
  if (!authStore.isLoggedIn) return;
  try {
    let response;
    if (isAdmin.value) {
      // 管理员：获取所有用户的申请
      response = await getAllApplies();
    } else {
      // 其他用户：获取自己的所有申请
      response = await getMyApplies();
    }
    allApplies.value = response.data;
    
    // 如果是管理员，获取待办任务以便显示同意/拒绝按钮
    if (isAdmin.value) {
      try {
        const tasksResponse = await getMyPendingTasks();
        pendingTasks.value = tasksResponse.data;
      } catch (error) {
        // 如果获取任务失败，不影响申请列表显示
        console.error('获取待办任务失败', error);
      }
    }
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
      return 'text-green-500';
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

// 判断是否是当前用户的申请
const isMyApply = (apply: Apply) => {
  if (!currentUserId.value || !apply.applicantId) return false;
  return apply.applicantId === currentUserId.value;
};

// 判断是否可以撤回
const canWithdraw = (apply: Apply) => {
  // 只有申请人自己可以撤回，且状态为草稿或待审批
  if (!isMyApply(apply)) return false;
  return (apply.status === 'DRAFT' || apply.status === 'PENDING');
};

// 判断管理员是否可以审批（有对应的待办任务且状态为待审批）
const canAdminApprove = (apply: Apply) => {
  if (!isAdmin.value) return false;
  if (isMyApply(apply)) return false; // 自己的申请不能审批
  if (apply.status !== 'PENDING') return false;
  // 检查是否有对应的待办任务
  return pendingTasks.value.some(task => task.applyId === apply.applyId && task.status === 'PENDING');
};

// 获取申请的待办任务ID（用于审批）
const getTaskIdForApply = (applyId: number): number | null => {
  const task = pendingTasks.value.find(t => t.applyId === applyId && t.status === 'PENDING');
  return task ? task.taskId : null;
};

// 撤回申请
const handleWithdraw = async (id: number) => {
  try {
    await withdrawApply(id);
    toast.success('申请已撤回');
    fetchAllApplies();
  } catch (error: any) {
    toast.error('撤回失败', { 
      description: error.response?.data?.message || '无法撤回此申请'
    });
  }
};

// 打开审批对话框
const openApprovalDialog = (apply: Apply, action: 'APPROVE' | 'REJECT') => {
  selectedApply.value = apply;
  approvalAction.value = action;
  approvalComment.value = '';
  isApprovalDialogOpen.value = true;
};

// 处理审批
const handleApproval = async () => {
  if (!selectedApply.value) return;
  
  const taskId = getTaskIdForApply(selectedApply.value.applyId);
  if (!taskId) {
    toast.error('审批失败', { description: '未找到对应的审批任务' });
    return;
  }
  
  try {
    await processApproval({
      taskId: taskId,
      action: approvalAction.value,
      comment: approvalComment.value,
    });
    toast.success(approvalAction.value === 'APPROVE' ? '审批通过' : '审批拒绝');
    isApprovalDialogOpen.value = false;
    fetchAllApplies();
  } catch (error: any) {
    toast.error('处理失败', { 
      description: error.response?.data?.message || '无法处理此审批任务'
    });
  }
};

</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">{{ isAdmin ? '所有申请' : '我的申请' }}</h1>
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
            <TableHead v-if="isAdmin">申请人</TableHead>
            <TableHead>申请内容</TableHead>
            <TableHead>申请时间</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>当前节点</TableHead>
            <TableHead class="text-center">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-if="sortedApplies.length === 0">
            <TableCell :colspan="isAdmin ? 7 : 6" class="text-center text-muted-foreground">
              {{ filterStatus === 'ALL' ? '暂无申请记录' : `暂无${getStatusText(filterStatus)}状态的申请` }}
            </TableCell>
          </TableRow>
          <TableRow v-for="apply in sortedApplies" :key="apply.applyId">
            <TableCell class="pl-6">{{ getApplyTypeText(apply.applyType) }}</TableCell>
            <TableCell v-if="isAdmin">{{ apply.applicantName }}</TableCell>
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
              <div class="flex gap-2 justify-center">
                <!-- 撤回操作（自己的申请） -->
                <AlertDialog v-if="canWithdraw(apply)">
                  <AlertDialogTrigger as-child>
                    <Button variant="outline" size="sm" class="bg-purple-600 hover:bg-purple-700 text-white border-purple-600">撤回</Button>
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
                
                <!-- 管理员审批操作（其他人的申请） -->
                <template v-if="canAdminApprove(apply)">
                  <Button 
                    variant="default" 
                    size="sm" 
                    class="bg-green-600 hover:bg-green-600 text-white"
                    @click="openApprovalDialog(apply, 'APPROVE')"
                  >
                    同意
                  </Button>
                  <Button 
                    variant="destructive" 
                    size="sm"
                    @click="openApprovalDialog(apply, 'REJECT')"
                  >
                    拒绝
                  </Button>
                </template>
              </div>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>

    <!-- 审批对话框 -->
    <Dialog v-model:open="isApprovalDialogOpen">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{{ approvalAction === 'APPROVE' ? '同意审批' : '拒绝审批' }}</DialogTitle>
          <DialogDescription>
            请填写审批意见
          </DialogDescription>
        </DialogHeader>
        <div class="space-y-4 py-4">
          <div class="space-y-2">
            <Label for="approval-comment">审批意见</Label>
            <Input 
              id="approval-comment" 
              v-model="approvalComment" 
              placeholder="请输入审批意见（可选）" 
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="isApprovalDialogOpen = false">取消</Button>
          <Button 
            @click="handleApproval"
            :class="approvalAction === 'APPROVE' ? 'bg-green-600 hover:bg-green-600 text-white' : ''"
          >
            {{ approvalAction === 'APPROVE' ? '确认同意' : '确认拒绝' }}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
