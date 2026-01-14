<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { toast } from 'vue-sonner';
import apiClient from '@/services/api';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Plus } from 'lucide-vue-next';

const processes = ref<any[]>([]);
const isLoading = ref(true);
const showCreateDialog = ref(false);
const createForm = ref({
  processName: '',
  applyType: 'LEAVE' as 'LEAVE' | 'REIMBURSE',
  remark: '',
});

const fetchProcesses = async () => {
  isLoading.value = true;
  try {
    const response = await apiClient.get('/approval-processes');
    // 确保返回的是数组
    processes.value = Array.isArray(response.data) ? response.data : [];
  } catch (error: any) {
    console.error('获取审批流程列表失败:', error);
    processes.value = [];
    toast.error('获取审批流程列表失败', {
      description: error.response?.data?.message || error.message || 'Network Error',
    });
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchProcesses);

const getApplyTypeLabel = (type: string) => {
  return type === 'LEAVE' ? '请假' : type === 'REIMBURSE' ? '报销' : type;
};

const handleCreateProcess = async () => {
  try {
    await apiClient.post('/approval-processes', createForm.value);
    toast.success('创建审批流程成功');
    showCreateDialog.value = false;
    createForm.value = {
      processName: '',
      applyType: 'LEAVE',
      remark: '',
    };
    fetchProcesses();
  } catch (error: any) {
    toast.error('创建审批流程失败', {
      description: error.response?.data?.message || error.message || 'Network Error',
    });
  }
};
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">设计审核流程</h1>
      <Button @click="showCreateDialog = true">
        <Plus class="mr-2 h-4 w-4" />
        新建流程
      </Button>
    </div>

    <Dialog v-model:open="showCreateDialog">
      <DialogContent class="max-w-md">
        <DialogHeader>
          <DialogTitle>新建审批流程</DialogTitle>
        </DialogHeader>
        <form @submit.prevent="handleCreateProcess">
          <div class="space-y-4">
            <div>
              <Label for="processName">流程名称</Label>
              <Input id="processName" v-model="createForm.processName" required class="mt-2" />
            </div>
            <div>
              <Label for="applyType">申请类型</Label>
              <Select v-model="createForm.applyType" class="mt-2">
                <SelectTrigger>
                  <SelectValue placeholder="选择申请类型" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="LEAVE">请假</SelectItem>
                  <SelectItem value="REIMBURSE">报销</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label for="remark">备注</Label>
              <Input id="remark" v-model="createForm.remark" class="mt-2" />
            </div>
          </div>
          <DialogFooter class="mt-6">
            <Button type="button" variant="ghost" @click="showCreateDialog = false">取消</Button>
            <Button type="submit">创建</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <div v-if="isLoading" class="text-center">加载中...</div>
    <div v-else class="border rounded-lg">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>流程名称</TableHead>
            <TableHead>申请类型</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>备注</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-if="processes.length === 0">
            <TableCell colspan="4" class="text-center text-muted-foreground py-8">暂无审批流程</TableCell>
          </TableRow>
          <TableRow v-else v-for="process in processes" :key="process.processId">
            <TableCell>{{ process.processName || 'N/A' }}</TableCell>
            <TableCell>{{ getApplyTypeLabel(process.applyType) }}</TableCell>
            <TableCell>{{ process.status === '0' ? '启用' : '停用' }}</TableCell>
            <TableCell>{{ process.remark || 'N/A' }}</TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  </div>
</template>
