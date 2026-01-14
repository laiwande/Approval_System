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

const posts = ref<any[]>([]);
const isLoading = ref(true);
const showCreateDialog = ref(false);
const createForm = ref({
  postName: '',
  postCode: '',
  postSort: 0,
  status: '0',
  remark: '',
});

const fetchPosts = async () => {
  try {
    const response = await apiClient.get('/admin/posts');
    posts.value = response.data;
  } catch (error: any) {
    toast.error('获取岗位列表失败', {
      description: error.response?.data || 'Network Error',
    });
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchPosts);

const handleCreatePost = async () => {
  try {
    await apiClient.post('/admin/posts', createForm.value);
    toast.success('创建岗位成功');
    showCreateDialog.value = false;
    createForm.value = {
      postName: '',
      postCode: '',
      postSort: 0,
      status: '0',
      remark: '',
    };
    fetchPosts();
  } catch (error: any) {
    toast.error('创建岗位失败', {
      description: error.response?.data || 'Network Error',
    });
  }
};

const handleDeletePost = async (postId: number) => {
  if (!confirm('确定要删除这个岗位吗？')) return;
  try {
    await apiClient.delete(`/admin/posts/${postId}`);
    toast.success('删除岗位成功');
    fetchPosts();
  } catch (error: any) {
    toast.error('删除岗位失败', {
      description: error.response?.data || 'Network Error',
    });
  }
};
</script>

<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold">岗位管理</h1>
      <Button @click="showCreateDialog = true">
        <Plus class="mr-2 h-4 w-4" />
        新建岗位
      </Button>
    </div>

    <Dialog v-model:open="showCreateDialog">
      <DialogContent class="max-w-md">
        <DialogHeader>
          <DialogTitle>新建岗位</DialogTitle>
        </DialogHeader>
        <form @submit.prevent="handleCreatePost">
          <div class="space-y-4">
            <div>
              <Label for="postName">岗位名称</Label>
              <Input id="postName" v-model="createForm.postName" required class="mt-2" />
            </div>
            <div>
              <Label for="postCode">岗位编码</Label>
              <Input id="postCode" v-model="createForm.postCode" required class="mt-2" />
            </div>
            <div>
              <Label for="postSort">显示顺序</Label>
              <Input id="postSort" v-model.number="createForm.postSort" type="number" class="mt-2" />
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
            <TableHead>岗位名称</TableHead>
            <TableHead>岗位编码</TableHead>
            <TableHead>显示顺序</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>备注</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-if="posts.length === 0">
            <TableCell colspan="6" class="text-center text-muted-foreground py-8">暂无岗位信息</TableCell>
          </TableRow>
          <TableRow v-else v-for="post in posts" :key="post.postId">
            <TableCell>{{ post.postName }}</TableCell>
            <TableCell>{{ post.postCode }}</TableCell>
            <TableCell>{{ post.postSort }}</TableCell>
            <TableCell>{{ post.status === '0' ? '正常' : '停用' }}</TableCell>
            <TableCell>{{ post.remark || 'N/A' }}</TableCell>
            <TableCell>
              <Button variant="ghost" size="icon" @click="handleDeletePost(post.postId)">
                <Trash2 class="h-4 w-4 text-red-500" />
              </Button>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  </div>
</template>
