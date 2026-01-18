<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getMyPendingTasks, processApproval } from '@/services/approvalService';
import { getApplyDetail } from '@/services/applyService';
import { getFileUrl } from '@/services/fileService';
import { toast } from 'vue-sonner';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useAuthStore } from '@/stores/auth';
import { FileText, Download } from 'lucide-vue-next';

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
const isDetailDialogOpen = ref(false);
const applyDetail = ref<any>(null);
const isLoadingDetail = ref(false);

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
  isLoadingDetail.value = true;
  isDetailDialogOpen.value = true;
  try {
    const response = await getApplyDetail(applyId);
    applyDetail.value = response.data;
  } catch (error: any) {
    toast.error('获取详情失败');
    isDetailDialogOpen.value = false;
  } finally {
    isLoadingDetail.value = false;
  }
};

const formatDateTime = (datetime: string) => new Date(datetime).toLocaleString('zh-CN');

const formatDate = (date: string) => new Date(date).toLocaleDateString('zh-CN');

const downloadAttachment = (url: string) => {
  if (!url) return;
  const fullUrl = getFileUrl(url);
  window.open(fullUrl, '_blank');
};
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
            <TableCell>
              <button
                @click="viewApplyDetail(task.applyId)"
                class="text-blue-600 hover:text-blue-800 hover:underline cursor-pointer"
              >
                {{ task.applyTitle }}
              </button>
            </TableCell>
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

    <!-- 申请详情对话框 -->
    <Dialog v-model:open="isDetailDialogOpen">
      <DialogContent class="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>申请详情</DialogTitle>
        </DialogHeader>
        <div v-if="isLoadingDetail" class="py-8 text-center">加载中...</div>
        <div v-else-if="applyDetail" class="space-y-4 py-4">
          <!-- 基本信息 -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <Label class="text-sm font-semibold">申请类型</Label>
              <p class="mt-1">{{ applyDetail.applyType === 'LEAVE' ? '请假' : '报销' }}</p>
            </div>
            <div>
              <Label class="text-sm font-semibold">申请人</Label>
              <p class="mt-1">{{ applyDetail.applicantName }} ({{ applyDetail.applicantEmail }})</p>
            </div>
            <div>
              <Label class="text-sm font-semibold">申请状态</Label>
              <p class="mt-1">
                <span v-if="applyDetail.status === 'PENDING'" class="text-yellow-600">待审批</span>
                <span v-else-if="applyDetail.status === 'APPROVED'" class="text-green-600">已通过</span>
                <span v-else-if="applyDetail.status === 'REJECTED'" class="text-red-600">已拒绝</span>
                <span v-else>{{ applyDetail.status }}</span>
              </p>
            </div>
            <div>
              <Label class="text-sm font-semibold">申请时间</Label>
              <p class="mt-1">{{ formatDateTime(applyDetail.createTime) }}</p>
            </div>
          </div>

          <!-- 请假申请详情 -->
          <div v-if="applyDetail.applyType === 'LEAVE' && applyDetail.leaveApply" class="border-t pt-4">
            <h3 class="font-semibold mb-3">请假信息</h3>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <Label class="text-sm font-semibold">请假类型</Label>
                <p class="mt-1">
                  <span v-if="applyDetail.leaveApply.leaveType === 'ANNUAL'">年假</span>
                  <span v-else-if="applyDetail.leaveApply.leaveType === 'SICK'">病假</span>
                  <span v-else-if="applyDetail.leaveApply.leaveType === 'PERSONAL'">事假</span>
                  <span v-else>{{ applyDetail.leaveApply.leaveType }}</span>
                </p>
              </div>
              <div>
                <Label class="text-sm font-semibold">请假天数</Label>
                <p class="mt-1">{{ applyDetail.leaveApply.leaveDays }} 天</p>
              </div>
              <div>
                <Label class="text-sm font-semibold">开始时间</Label>
                <p class="mt-1">{{ formatDateTime(applyDetail.leaveApply.startTime) }}</p>
              </div>
              <div>
                <Label class="text-sm font-semibold">结束时间</Label>
                <p class="mt-1">{{ formatDateTime(applyDetail.leaveApply.endTime) }}</p>
              </div>
              <div class="col-span-2">
                <Label class="text-sm font-semibold">请假事由</Label>
                <p class="mt-1">{{ applyDetail.leaveApply.reason }}</p>
              </div>
              <div v-if="applyDetail.leaveApply.attachmentUrl" class="col-span-2">
                <Label class="text-sm font-semibold">附件</Label>
                <div class="mt-2">
                  <Button
                    variant="outline"
                    size="sm"
                    @click="downloadAttachment(applyDetail.leaveApply.attachmentUrl)"
                  >
                    <Download class="h-4 w-4 mr-2" />
                    下载附件
                  </Button>
                </div>
              </div>
            </div>
          </div>

          <!-- 报销申请详情 -->
          <div v-if="applyDetail.applyType === 'REIMBURSE' && applyDetail.reimburseApply" class="border-t pt-4">
            <h3 class="font-semibold mb-3">报销信息</h3>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <Label class="text-sm font-semibold">费用类型</Label>
                <p class="mt-1">{{ applyDetail.reimburseApply.expenseType }}</p>
              </div>
              <div>
                <Label class="text-sm font-semibold">报销金额</Label>
                <p class="mt-1">¥{{ applyDetail.reimburseApply.amount }}</p>
              </div>
              <div class="col-span-2">
                <Label class="text-sm font-semibold">报销事由</Label>
                <p class="mt-1">{{ applyDetail.reimburseApply.reason }}</p>
              </div>
              <div v-if="applyDetail.reimburseApply.attachmentUrl" class="col-span-2">
                <Label class="text-sm font-semibold">附件</Label>
                <div class="mt-2">
                  <Button
                    variant="outline"
                    size="sm"
                    @click="downloadAttachment(applyDetail.reimburseApply.attachmentUrl)"
                  >
                    <Download class="h-4 w-4 mr-2" />
                    下载附件
                  </Button>
                </div>
              </div>
            </div>
          </div>

          <!-- 审批记录 -->
          <div v-if="applyDetail.records && applyDetail.records.length > 0" class="border-t pt-4">
            <h3 class="font-semibold mb-3">审批记录</h3>
            <div class="space-y-2">
              <div
                v-for="record in applyDetail.records"
                :key="record.recordId"
                class="border rounded p-3"
              >
                <div class="flex justify-between items-start">
                  <div>
                    <p class="font-medium">{{ record.approverName }}</p>
                    <p class="text-sm text-muted-foreground mt-1">
                      {{ record.action === 'APPROVE' ? '同意' : '拒绝' }}
                      <span class="ml-2">{{ formatDateTime(record.actionTime) }}</span>
                    </p>
                    <p v-if="record.comment" class="text-sm mt-2">{{ record.comment }}</p>
                  </div>
                  <span
                    :class="record.action === 'APPROVE' ? 'text-green-600' : 'text-red-600'"
                    class="font-semibold"
                  >
                    {{ record.action === 'APPROVE' ? '✓' : '✗' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="isDetailDialogOpen = false">关闭</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
