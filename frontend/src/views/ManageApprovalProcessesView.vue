<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { toast } from 'vue-sonner';
import apiClient from '@/services/api';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Plus, Pencil, Trash2 } from 'lucide-vue-next';

const processes = ref<any[]>([]);
const users = ref<any[]>([]);
const posts = ref<any[]>([]);
const isLoading = ref(true);
const showCreateDialog = ref(false);
const showEditDialog = ref(false);
const editingProcessId = ref<number | null>(null);
const createForm = ref({
  processName: '',
  applyType: 'LEAVE' as 'LEAVE' | 'REIMBURSE',
  remark: '',
  nodes: [] as Array<{ nodeOrder: number; postId: number | null; userId: number | null; remark: string }>
});
const editForm = ref({
  processName: '',
  applyType: 'LEAVE' as 'LEAVE' | 'REIMBURSE',
  remark: '',
  nodes: [] as Array<{ nodeOrder: number; postId: number | null; userId: number | null; remark: string }>
});

const fetchProcesses = async () => {
  isLoading.value = true;
  try {
    const response = await apiClient.get('/approval-processes');
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

const fetchUsers = async () => {
  try {
    const response = await apiClient.get('/admin/users');
    // 过滤掉普通用户（EMPLOYEE角色），只保留审批人和管理员
    const allUsers = Array.isArray(response.data) ? response.data : [];
    users.value = allUsers.filter((user: any) => {
      // 检查用户角色，过滤掉EMPLOYEE
      // UserDTO中的roles是Set<String>，JSON序列化后可能变成数组
      if (user.roles) {
        // 处理Set或数组
        const roleArray = Array.isArray(user.roles) ? user.roles : Array.from(user.roles);
        if (roleArray.length > 0) {
          const role = roleArray[0] || '';
          const roleName = role.startsWith('ROLE_') ? role.substring(5) : role;
          return roleName !== 'EMPLOYEE';
        }
      }
      // 如果没有roles字段，检查role字段（向后兼容）
      if (user.role) {
        const roleName = user.role.name || user.role;
        return roleName !== 'EMPLOYEE';
      }
      return false; // 如果没有角色信息，过滤掉
    });
  } catch (error: any) {
    console.error('获取用户列表失败:', error);
    toast.error('获取用户列表失败');
  }
};

const fetchPosts = async () => {
  try {
    const response = await apiClient.get('/admin/posts');
    posts.value = Array.isArray(response.data) ? response.data : [];
  } catch (error: any) {
    console.error('获取岗位列表失败:', error);
    toast.error('获取岗位列表失败');
  }
};

onMounted(async () => {
  await Promise.all([fetchProcesses(), fetchUsers(), fetchPosts()]);
});

const getApplyTypeLabel = (type: string) => {
  return type === 'LEAVE' ? '请假' : type === 'REIMBURSE' ? '报销' : type;
};

const getUserName = (userId: number | null) => {
  if (!userId) return '';
  const user = users.value.find(u => u.userId === userId);
  return user ? user.userName : '';
};

const getPostName = (postId: number | null) => {
  if (!postId) return '';
  const post = posts.value.find(p => p.postId === postId);
  return post ? post.postName : '';
};

const resetCreateForm = () => {
  createForm.value = {
    processName: '',
    applyType: 'LEAVE',
    remark: '',
    nodes: []
  };
};

const resetEditForm = () => {
  editForm.value = {
    processName: '',
    applyType: 'LEAVE',
    remark: '',
    nodes: []
  };
};

const handleCreateProcess = async () => {
  // 验证至少有一个节点
  if (createForm.value.nodes.length === 0) {
    toast.error('至少需要添加一个审批节点');
    return;
  }

  // 验证每个节点都指定了审批人（默认是指定用户）
  for (let i = 0; i < createForm.value.nodes.length; i++) {
    const node = createForm.value.nodes[i];
    if (!node.userId) {
      toast.error(`第 ${i + 1} 个审批节点必须指定审批人`);
      return;
    }
  }

  try {
    const payload = {
      ...createForm.value,
      nodes: createForm.value.nodes.map(node => ({
        nodeOrder: node.nodeOrder,
        postId: null, // 不再使用岗位
        userId: node.userId || null,
        remark: node.remark || ''
      }))
    };
    await apiClient.post('/approval-processes', payload);
    toast.success('创建审批流程成功');
    showCreateDialog.value = false;
    resetCreateForm();
    fetchProcesses();
  } catch (error: any) {
    toast.error('创建审批流程失败', {
      description: error.response?.data?.message || error.message || 'Network Error',
    });
  }
};

const handleEditProcess = async (processId: number) => {
  try {
    const response = await apiClient.get(`/approval-processes/${processId}`);
    const process = response.data;
    
    console.log('获取到的流程数据:', process);
    console.log('节点数据:', process.nodes);
    
    editingProcessId.value = processId;
    
    // 处理节点数据
    let nodes: any[] = [];
    if (process.nodes && Array.isArray(process.nodes) && process.nodes.length > 0) {
      nodes = process.nodes.map((node: any) => ({
        nodeOrder: node.nodeOrder,
        postId: null, // 不再使用岗位
        userId: node.userId || null, // 只使用 userId
        remark: node.remark || ''
      })).sort((a: any, b: any) => a.nodeOrder - b.nodeOrder);
    }
    
    console.log('处理后的节点数据:', nodes);
    
    editForm.value = {
      processName: process.processName || '',
      applyType: process.applyType || 'LEAVE',
      remark: process.remark || '',
      nodes: nodes
    };
    
    showEditDialog.value = true;
  } catch (error: any) {
    console.error('获取流程详情失败:', error);
    toast.error('获取流程详情失败', {
      description: error.response?.data?.message || error.message || 'Network Error',
    });
  }
};

const handleUpdateProcess = async () => {
  if (!editingProcessId.value) return;

  // 验证至少有一个节点
  if (editForm.value.nodes.length === 0) {
    toast.error('至少需要添加一个审批节点');
    return;
  }

  // 验证每个节点都指定了审批人（默认是指定用户）
  for (let i = 0; i < editForm.value.nodes.length; i++) {
    const node = editForm.value.nodes[i];
    if (!node.userId) {
      toast.error(`第 ${i + 1} 个审批节点必须指定审批人`);
      return;
    }
  }

  try {
    const payload = {
      ...editForm.value,
      nodes: editForm.value.nodes.map(node => ({
        nodeOrder: node.nodeOrder,
        postId: null, // 不再使用岗位
        userId: node.userId || null,
        remark: node.remark || ''
      }))
    };
    await apiClient.put(`/approval-processes/${editingProcessId.value}`, payload);
    toast.success('更新审批流程成功');
    showEditDialog.value = false;
    editingProcessId.value = null;
    resetEditForm();
    fetchProcesses();
  } catch (error: any) {
    toast.error('更新审批流程失败', {
      description: error.response?.data?.message || error.message || 'Network Error',
    });
  }
};

const addNode = (form: any) => {
  const nextOrder = form.nodes.length + 1;
  form.nodes.push({
    nodeOrder: nextOrder,
    postId: null,
    userId: null,
    remark: ''
  });
};

const removeNode = (form: any, index: number) => {
  form.nodes.splice(index, 1);
  // 重新排序
  form.nodes.forEach((node: any, idx: number) => {
    node.nodeOrder = idx + 1;
  });
};

// 移除审批人类型相关的逻辑，默认使用用户
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

    <!-- 创建流程对话框 -->
    <Dialog v-model:open="showCreateDialog">
      <DialogContent class="max-w-3xl max-h-[90vh] overflow-y-auto">
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
            
            <!-- 审批节点配置 -->
            <div class="border-t pt-4">
              <div class="flex justify-between items-center mb-4">
                <Label class="text-base font-semibold">审批节点配置</Label>
                <Button type="button" variant="outline" size="sm" @click="addNode(createForm)">
                  <Plus class="mr-2 h-4 w-4" />
                  添加节点
                </Button>
              </div>
              
              <div v-if="createForm.nodes.length === 0" class="text-sm text-muted-foreground py-4 text-center">
                暂无审批节点，请添加至少一个审批节点
              </div>
              
              <div v-else class="space-y-4">
                <div 
                  v-for="(node, index) in createForm.nodes" 
                  :key="index"
                  class="border rounded-lg p-4 space-y-3"
                >
                  <div class="flex justify-between items-center">
                    <Label class="font-semibold">节点 {{ node.nodeOrder }}</Label>
                    <Button 
                      type="button" 
                      variant="ghost" 
                      size="icon"
                      @click="removeNode(createForm, index)"
                    >
                      <Trash2 class="h-4 w-4 text-red-500" />
                    </Button>
                  </div>
                  
                  <div>
                    <Label>选择审批人</Label>
                    <Select 
                      :model-value="node.userId ? String(node.userId) : ''"
                      @update:model-value="(val) => { node.userId = val ? Number(val) : null; }"
                      class="mt-2"
                      required
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="选择审批人" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem 
                          v-for="user in users" 
                          :key="user.userId" 
                          :value="String(user.userId)"
                        >
                          {{ user.userName }} ({{ user.email }})
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <DialogFooter class="mt-6">
            <Button type="button" variant="ghost" @click="() => { showCreateDialog = false; resetCreateForm(); }">取消</Button>
            <Button type="submit">创建</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>

    <!-- 编辑流程对话框 -->
    <Dialog v-model:open="showEditDialog">
      <DialogContent class="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>编辑审批流程</DialogTitle>
        </DialogHeader>
        <form @submit.prevent="handleUpdateProcess">
          <div class="space-y-4">
            <div>
              <Label for="editProcessName">流程名称</Label>
              <Input id="editProcessName" v-model="editForm.processName" required class="mt-2" />
            </div>
            <div class="space-y-2">
              <Label for="editApplyType">申请类型</Label>
              <Select v-model="editForm.applyType" class="mt-2">
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
              <Label for="editRemark">备注</Label>
              <Input id="editRemark" v-model="editForm.remark" class="mt-2" />
            </div>
            
            <!-- 审批节点配置 -->
            <div class="border-t pt-4">
              <div class="flex justify-between items-center mb-4">
                <Label class="text-base font-semibold">审批节点配置</Label>
                <Button type="button" variant="outline" size="sm" @click="addNode(editForm)">
                  <Plus class="mr-2 h-4 w-4" />
                  添加节点
                </Button>
              </div>
              
              <div v-if="editForm.nodes.length === 0" class="text-sm text-muted-foreground py-4 text-center">
                暂无审批节点，请添加至少一个审批节点
              </div>
              
              <div v-else class="space-y-4">
                <div 
                  v-for="(node, index) in editForm.nodes" 
                  :key="index"
                  class="border rounded-lg p-4 space-y-3"
                >
                  <div class="flex justify-between items-center">
                    <Label class="font-semibold">节点 {{ node.nodeOrder }}</Label>
                    <Button 
                      type="button" 
                      variant="ghost" 
                      size="icon"
                      @click="removeNode(editForm, index)"
                    >
                      <Trash2 class="h-4 w-4 text-red-500" />
                    </Button>
                  </div>
                  <div class="space-y-2">
                    <Label>选择审批人</Label>
                    <Select 
                      :model-value="node.userId ? String(node.userId) : ''"
                      @update:model-value="(val) => { node.userId = val ? Number(val) : null; }"
                      class="mt-2"
                      required
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="选择审批人" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem 
                          v-for="user in users" 
                          :key="user.userId" 
                          :value="String(user.userId)"
                        >
                          {{ user.userName }} ({{ user.email }})
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <DialogFooter class="mt-6">
            <Button type="button" variant="ghost" @click="() => { showEditDialog = false; editingProcessId = null; resetEditForm(); }">取消</Button>
            <Button type="submit">更新</Button>
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
            <TableHead>审批节点数</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>备注</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-if="processes.length === 0">
            <TableCell colspan="6" class="text-center text-muted-foreground py-8">暂无审批流程</TableCell>
          </TableRow>
          <TableRow v-else v-for="process in processes" :key="process.processId">
            <TableCell>{{ process.processName || 'N/A' }}</TableCell>
            <TableCell>{{ getApplyTypeLabel(process.applyType) }}</TableCell>
            <TableCell>{{ process.nodes ? process.nodes.length : 0 }}</TableCell>
            <TableCell>{{ process.status === '0' ? '启用' : '停用' }}</TableCell>
            <TableCell>{{ process.remark || 'N/A' }}</TableCell>
            <TableCell>
              <Button variant="ghost" size="icon" @click="handleEditProcess(process.processId)">
                <Pencil class="h-4 w-4" />
              </Button>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  </div>
</template>
