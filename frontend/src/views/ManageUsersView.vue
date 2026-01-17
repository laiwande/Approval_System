<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getAllUsers, updateUserRole, deleteUser, createUser, updateUser } from '@/services/adminService';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { toast } from 'vue-sonner';
import { Trash2, Plus, Pencil } from 'lucide-vue-next';
import { useAuthStore } from '@/stores/auth'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

const authStore = useAuthStore()

const users = ref([]);
const isLoading = ref(true);
const roles = ['EMPLOYEE', 'APPROVER', 'ADMIN'];
const roleLabels: Record<string, string> = {
  'EMPLOYEE': '员工',
  'APPROVER': '审批人',
  'ADMIN': '管理员',
  'ROLE_EMPLOYEE': '员工',
  'ROLE_APPROVER': '审批人',
  'ROLE_ADMIN': '管理员'
};
const showCreateDialog = ref(false);
const showEditDialog = ref(false);
const editingUserId = ref<number | null>(null);
const createForm = ref({
  username: '',
  email: '',
  password: '',
  phone: '',
  role: 'EMPLOYEE',
});
const editForm = ref({
  username: '',
  email: '',
  password: '',
  phone: '',
});
const createLoading = ref(false);
const editLoading = ref(false);

const fetchUsers = async () => {
  if (!authStore.isLoggedIn) return
  try {
    const response = await getAllUsers();
    // 假设每个用户只有一个角色，简化处理，移除ROLE_前缀用于显示
    users.value = response.data.map((u: { roles: any[]; }) => {
      const role = u.roles[0] || '';
      const displayRole = role.startsWith('ROLE_') ? role.substring(5) : role;
      return {...u, roles: displayRole };
    });
  } catch (error) {
    toast.error('获取用户列表失败');
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchUsers);

const handleRoleChange = async (userId: number, newRole: string) => {
  try {
    console.log(`准备为用户ID ${userId} 更新角色为:`, newRole);
    console.log('收到的 newRole 的类型是:', typeof newRole);
    console.log(`[调试信息] 用户ID: ${userId}, 准备更新的角色值:`, newRole);
    await updateUserRole(userId, newRole);
    toast.success('用户角色更新成功！');
    await fetchUsers(); // 刷新列表
  } catch (error) {
    toast.error('更新角色失败');
  }
};

const handleDeleteUser = async (userId: number) => {
    if(confirm('确定要删除该用户吗？此操作不可逆！')) {
        try {
            await deleteUser(userId);
            toast.success('用户已删除');
            await fetchUsers(); // 刷新列表
        } catch(error) {
            toast.error('删除用户失败');
        }
    }
};

const handleCreateUser = async () => {
  createLoading.value = true;
  try {
    await createUser(createForm.value);
    toast.success('新用户创建成功！');
    showCreateDialog.value = false;
    createForm.value = { username: '', email: '', password: '', phone: '', role: 'EMPLOYEE' };
    await fetchUsers();
  } catch (e: any) {
    toast.error(e?.response?.data || '创建失败');
  } finally {
    createLoading.value = false;
    }
};

const handleEditUser = (user: any) => {
  editingUserId.value = user.userId;
  editForm.value = {
    username: user.name || '',
    email: user.email || '',
    password: '', // 密码留空，不填写则不更新
    phone: user.phone || '',
  };
  showEditDialog.value = true;
};

const handleUpdateUser = async () => {
  if (!editingUserId.value) return;
  
  editLoading.value = true;
  try {
    // 如果密码为空，则不发送密码字段
    const updateData: any = {
      username: editForm.value.username,
      email: editForm.value.email,
      phonenumber: editForm.value.phone,
    };
    
    if (editForm.value.password && editForm.value.password.trim() !== '') {
      updateData.password = editForm.value.password;
    }
    
    await updateUser(editingUserId.value, updateData);
    toast.success('用户信息更新成功！');
    showEditDialog.value = false;
    editingUserId.value = null;
    editForm.value = { username: '', email: '', password: '', phone: '' };
    await fetchUsers();
  } catch (e: any) {
    toast.error(e?.response?.data || '更新失败');
  } finally {
    editLoading.value = false;
  }
};
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">用户管理</h1>
      <Button @click="showCreateDialog = true">
        <Plus class="mr-2 h-4 w-4" />
        新建用户
      </Button>
    </div>
    <Dialog v-model:open="showCreateDialog">
      <DialogContent class="max-w-md">
        <DialogHeader>
          <DialogTitle>新建用户</DialogTitle>
        </DialogHeader>
        <form @submit.prevent="handleCreateUser">
          <div class="space-y-4">
            <div>
              <Label for="username" class="block mb-2">用户名</Label>
              <Input id="username" v-model="createForm.username" required maxlength="20" class="mt-2" />
            </div>
            <div>
              <Label for="email" class="block mb-2">邮箱</Label>
              <Input id="email" v-model="createForm.email" type="email" required maxlength="50" class="mt-2" />
            </div>
            <div>
              <Label for="password" class="block mb-2">密码</Label>
              <Input id="password" v-model="createForm.password" type="password" required minlength="8" maxlength="50" class="mt-2" />
            </div>
            <div>
              <Label for="phone" class="block mb-2">电话</Label>
              <Input id="phone" v-model="createForm.phone" maxlength="11" class="mt-2" />
            </div>
            <div>
              <Label for="role" class="block mb-2">角色</Label>
              <Select v-model="createForm.role" class="mt-2">
                <SelectTrigger>
                  <SelectValue placeholder="选择角色" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem v-for="role in roles" :key="role" :value="role">{{ roleLabels[role] || role }}</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter class="mt-8">
            <Button type="submit" :loading="createLoading">创建</Button>
            <Button type="button" variant="ghost" @click="showCreateDialog = false">取消</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
    <Dialog v-model:open="showEditDialog">
      <DialogContent class="max-w-md">
        <DialogHeader>
          <DialogTitle>编辑用户</DialogTitle>
        </DialogHeader>
        <form @submit.prevent="handleUpdateUser">
          <div class="space-y-4">
            <div>
              <Label for="edit-username" class="block mb-2">用户名</Label>
              <Input id="edit-username" v-model="editForm.username" required maxlength="20" class="mt-2" />
            </div>
            <div>
              <Label for="edit-email" class="block mb-2">邮箱</Label>
              <Input id="edit-email" v-model="editForm.email" type="email" required maxlength="50" class="mt-2" />
            </div>
            <div>
              <Label for="edit-password" class="block mb-2">密码（留空则不修改）</Label>
              <Input id="edit-password" v-model="editForm.password" type="password" minlength="8" maxlength="50" class="mt-2" placeholder="留空则不修改密码" />
            </div>
            <div>
              <Label for="edit-phone" class="block mb-2">电话</Label>
              <Input id="edit-phone" v-model="editForm.phone" maxlength="11" class="mt-2" />
            </div>
          </div>
          <DialogFooter class="mt-8">
            <Button type="submit" :loading="editLoading">保存</Button>
            <Button type="button" variant="ghost" @click="showEditDialog = false">取消</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
    <div v-if="isLoading" class="text-center">加载中...</div>
    <div v-else class="border rounded-lg">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>用户名</TableHead>
            <TableHead>邮箱</TableHead>
            <TableHead>电话</TableHead>
            <TableHead>角色</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="user in users" :key="user.userId">
            <TableCell>{{ user.name }}</TableCell>
            <TableCell>{{ user.email }}</TableCell>
            <TableCell>{{ user.phone || 'N/A' }}</TableCell>
            <TableCell>
              <Select :model-value="user.roles" @update:model-value="(newRole) => handleRoleChange(user.userId, newRole)">
                <SelectTrigger>
                  <SelectValue placeholder="选择角色" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem v-for="role in roles" :key="role" :value="role">
                    {{ roleLabels[role] || role }}
                  </SelectItem>
                </SelectContent>
              </Select>
            </TableCell>
            <TableCell>
              <div class="flex gap-2">
                <Button variant="ghost" size="icon" @click="handleEditUser(user)">
                  <Pencil class="h-4 w-4 text-blue-500"/>
                </Button>
                <Button variant="ghost" size="icon" @click="handleDeleteUser(user.userId)">
                  <Trash2 class="h-4 w-4 text-red-500"/>
                </Button>
              </div>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  </div>
</template>