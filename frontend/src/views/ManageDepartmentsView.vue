<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { toast } from 'vue-sonner';
import apiClient from '@/services/api';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Trash2, Plus } from 'lucide-vue-next';

const departments = ref<any[]>([]);
const isLoading = ref(true);
const showCreateDialog = ref(false);
const createForm = ref({
  deptName: '',
  leader: '',
  phone: '',
  email: '',
  status: '0',
  orderNum: 0,
});

const fetchDepartments = async () => {
  try {
    const response = await apiClient.get('/admin/departments');
    departments.value = response.data;
  } catch (error: any) {
    toast.error('获取部门列表失败', {
      description: error.response?.data || 'Network Error',
    });
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchDepartments);

const handleCreateDepartment = async () => {
  try {
    await apiClient.post('/admin/departments', createForm.value);
    toast.success('创建部门成功');
    showCreateDialog.value = false;
    createForm.value = {
      deptName: '',
      leader: '',
      phone: '',
      email: '',
      status: '0',
      orderNum: 0,
    };
    fetchDepartments();
  } catch (error: any) {
    toast.error('创建部门失败', {
      description: error.response?.data || 'Network Error',
    });
  }
};

const handleDeleteDepartment = async (deptId: number) => {
  if (!confirm('确定要删除这个部门吗？')) return;
  try {
    await apiClient.delete(`/admin/departments/${deptId}`);
    toast.success('删除部门成功');
    fetchDepartments();
  } catch (error: any) {
    toast.error('删除部门失败', {
      description: error.response?.data || 'Network Error',
    });
  }
};
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">部门管理</h1>
      <Button @click="showCreateDialog = true">
        <Plus class="mr-2 h-4 w-4" />
        新建部门
      </Button>
    </div>

    <Dialog v-model:open="showCreateDialog">
      <DialogContent class="max-w-md">
        <DialogHeader>
          <DialogTitle>新建部门</DialogTitle>
        </DialogHeader>
        <form @submit.prevent="handleCreateDepartment">
          <div class="space-y-4">
            <div>
              <Label for="deptName">部门名称</Label>
              <Input id="deptName" v-model="createForm.deptName" required class="mt-2" />
            </div>
            <div>
              <Label for="leader">负责人</Label>
              <Input id="leader" v-model="createForm.leader" class="mt-2" />
            </div>
            <div>
              <Label for="phone">联系电话</Label>
              <Input id="phone" v-model="createForm.phone" class="mt-2" />
            </div>
            <div>
              <Label for="email">邮箱</Label>
              <Input id="email" v-model="createForm.email" type="email" class="mt-2" />
            </div>
            <div>
              <Label for="orderNum">显示顺序</Label>
              <Input id="orderNum" v-model.number="createForm.orderNum" type="number" class="mt-2" />
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
            <TableHead>部门名称</TableHead>
            <TableHead>部门编码</TableHead>
            <TableHead>负责人</TableHead>
            <TableHead>联系电话</TableHead>
            <TableHead>邮箱</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="dept in departments" :key="dept.deptId">
            <TableCell>{{ dept.deptName }}</TableCell>
            <TableCell>{{ dept.leader || 'N/A' }}</TableCell>
            <TableCell>{{ dept.phone || 'N/A' }}</TableCell>
            <TableCell>{{ dept.email || 'N/A' }}</TableCell>
            <TableCell>{{ dept.status === '0' ? '正常' : '停用' }}</TableCell>
            <TableCell>
              <Button variant="ghost" size="icon" @click="handleDeleteDepartment(dept.deptId)">
                <Trash2 class="h-4 w-4 text-red-500" />
              </Button>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  </div>
</template>
