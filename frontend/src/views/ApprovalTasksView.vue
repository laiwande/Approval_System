<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getMyPendingTasks, processApproval } from '@/services/approvalService';
import { getApplyDetail } from '@/services/applyService';
import { toast } from 'vue-sonner';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

export interface ApprovalTask {
  taskId: number;
  applyId: number;
  applyType: string;
  applicantId?: number;
  applicantName: string;
  applyTitle: string;
  nodeOrder: number;
  approverName: string;
  status: 'PENDING' | 'DONE';
  createTime: string;
}

const pendingTasks = ref<ApprovalTask[]>([]);
const selectedTask = ref<ApprovalTask | null>(null);
const comment = ref('');
const isDialogOpen = ref(false);
const action = ref<'APPROVE' | 'REJECT'>('APPROVE');

const fetchTasks = async () => {
  if (!authStore.isLoggedIn) return;
  try {
    const response = await getMyPendingTasks();
    // 过滤掉当前用户自己的申请
    const currentUserId = authStore.user?.userId;
    if (currentUserId) {
      pendingTasks.value = response.data.filter((task: ApprovalTask) => 
        task.applicantId !== currentUserId
      );
    } else {
      pendingTasks.value = response.data;
    }
  } catch (error) {
    toast.error('获取待办任务失败');
  }
};

onMounted(fetchTasks);

const openDialog = (task: ApprovalTask, act: 'APPROVE' | 'REJECT') => {
  selectedTask.value = task;
  action.value = act;
  comment.value = '';
  isDialogOpen.value = true;
};

const handleProcess = async () => {
  if (!selectedTask.value) return;
  
  try {
    await processApproval({
      taskId: selectedTask.value.taskId,
      action: action.value,
      comment: comment.value,
    });
    toast.success(action.value === 'APPROVE' ? '审批通过' : '审批拒绝');
    isDialogOpen.value = false;
    fetchTasks();
  } catch (error: any) {
    toast.error('处理失败', { description: error.response?.data?.message || '无法处理此审批任务' });
  }
};

const viewApplyDetail = async (applyId: number) => {
  try {
    const response = await getApplyDetail(applyId);
    toast.info('申请详情', { description: JSON.stringify(response.data, null, 2) });
  } catch (error: any) {
    toast.error('获取详情失败');
  }
};

const formatDateTime = (datetime: string) => new Date(datetime).toLocaleString('zh-CN');
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">我的待批</h1>
    <div class="border rounded-md">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="text-left pl-6">申请类型</TableHead>
            <TableHead>申请人</TableHead>
            <TableHead>申请内容</TableHead>
            <TableHead>申请时间</TableHead>
            <TableHead>节点</TableHead>
            <TableHead class="text-center">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-if="pendingTasks.length === 0">
            <TableCell colspan="6" class="text-center text-muted-foreground">暂无待办任务</TableCell>
          </TableRow>
          <TableRow v-for="task in pendingTasks" :key="task.taskId">
            <TableCell class="pl-6">{{ task.applyType === 'LEAVE' ? '请假' : '报销' }}</TableCell>
            <TableCell>{{ task.applicantName }}</TableCell>
            <TableCell>{{ task.applyTitle }}</TableCell>
            <TableCell>{{ formatDateTime(task.createTime) }}</TableCell>
            <TableCell>节点 {{ task.nodeOrder }}</TableCell>
            <TableCell class="text-center">
              <div class="flex gap-2 justify-center">
                <Button variant="default" size="sm" class="bg-green-600 hover:bg-green-600 text-white" @click="openDialog(task, 'APPROVE')">同意</Button>
                <Button variant="destructive" size="sm" @click="openDialog(task, 'REJECT')">拒绝</Button>
              </div>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>

    <!-- 审批对话框 -->
    <Dialog v-model:open="isDialogOpen">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{{ action === 'APPROVE' ? '同意审批' : '拒绝审批' }}</DialogTitle>
          <DialogDescription>
            请填写审批意见
          </DialogDescription>
        </DialogHeader>
        <div class="space-y-4 py-4">
          <div class="space-y-2">
            <Label for="comment">审批意见</Label>
            <Input id="comment" v-model="comment" placeholder="请输入审批意见（可选）" />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="isDialogOpen = false">取消</Button>
          <Button 
            @click="handleProcess"
            :class="action === 'APPROVE' ? 'bg-green-600 hover:bg-green-600 text-white' : ''"
          >
            {{ action === 'APPROVE' ? '确认同意' : '确认拒绝' }}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
