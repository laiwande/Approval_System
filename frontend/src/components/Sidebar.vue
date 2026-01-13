<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth';
import { Button } from '@/components/ui/button'
import { ref, watch, onMounted } from 'vue'
import { Collapsible, CollapsibleTrigger, CollapsibleContent } from '@/components/ui/collapsible'

// 导入所有需要的图标
import { 
  Home,               // 总览
  CalendarDays,       // 我的申请
  CalendarPlus,       // 新建申请
  FileText,           // 所有申请
  Users,              // 用户管理
  Building2,          // 部门管理
  Briefcase,          // 岗位管理
  CalendarCheck,      // 我的待办
  ChevronDown,        // 下拉箭头
  ChevronRight        // 右箭头
} from 'lucide-vue-next'

const authStore = useAuthStore();
const route = useRoute();

// 下拉菜单状态
const myAppliesOpen = ref(false);
const userManagementOpen = ref(false);

// 检查当前路由是否在某个路径下
const isActiveRoute = (path: string) => {
  return route.path.startsWith(path);
};

// 根据当前路由自动展开对应的下拉菜单
onMounted(() => {
  if (isActiveRoute('/applies')) {
    myAppliesOpen.value = true;
  }
  if (isActiveRoute('/admin/users') || isActiveRoute('/admin/departments') || isActiveRoute('/admin/posts')) {
    userManagementOpen.value = true;
  }
});

// 监听路由变化
watch(() => route.path, (newPath) => {
  if (newPath.startsWith('/applies')) {
    myAppliesOpen.value = true;
  }
  if (newPath.startsWith('/admin/users') || newPath.startsWith('/admin/departments') || newPath.startsWith('/admin/posts')) {
    userManagementOpen.value = true;
  }
});
</script>

<template>
  <aside class="w-64 border-r bg-background p-4 flex flex-col">
    <h2 class="text-xl font-bold mb-6 px-4">审批系统</h2>
    
    <nav class="flex flex-col gap-1">
      <h3 class="text-sm font-semibold text-muted-foreground px-4 mt-2 mb-1">主菜单</h3>
      <Button variant="ghost" class="justify-start" as-child>
        <RouterLink to="/dashboard" active-class="bg-accent text-accent-foreground">
          <Home class="mr-2 h-4 w-4" />
          总览仪表盘
        </RouterLink>
      </Button>

      <!-- 系统管理员菜单 -->
      <template v-if="authStore.hasRole('ROLE_ADMIN')">
        <!-- 我的申请下拉菜单 -->
        <Collapsible v-model:open="myAppliesOpen" class="w-full">
          <CollapsibleTrigger class="w-full">
            <Button 
              variant="ghost" 
              class="justify-between w-full"
              :class="{ 'bg-accent text-accent-foreground': isActiveRoute('/applies') }"
            >
              <div class="flex items-center">
                <CalendarDays class="mr-2 h-4 w-4" />
                我的申请
              </div>
              <ChevronDown 
                v-if="myAppliesOpen" 
                class="h-4 w-4 transition-transform" 
              />
              <ChevronRight 
                v-else 
                class="h-4 w-4 transition-transform" 
              />
            </Button>
          </CollapsibleTrigger>
          <CollapsibleContent>
            <div class="pl-6 space-y-1">
              <Button variant="ghost" class="justify-start w-full" as-child>
                <RouterLink to="/applies/new" active-class="bg-accent text-accent-foreground">
                  <CalendarPlus class="mr-2 h-4 w-4" />
                  新建申请
                </RouterLink>
              </Button>
              <Button variant="ghost" class="justify-start w-full" as-child>
                <RouterLink to="/applies/all" active-class="bg-accent text-accent-foreground">
                  <FileText class="mr-2 h-4 w-4" />
                  所有申请
                </RouterLink>
              </Button>
            </div>
          </CollapsibleContent>
        </Collapsible>

        <!-- 用户管理下拉菜单 -->
        <h3 class="text-sm font-semibold text-muted-foreground px-4 mt-2 mb-1">系统管理</h3>
        <Collapsible v-model:open="userManagementOpen" class="w-full">
          <CollapsibleTrigger class="w-full">
            <Button 
              variant="ghost" 
              class="justify-between w-full"
              :class="{ 'bg-accent text-accent-foreground': isActiveRoute('/admin/users') || isActiveRoute('/admin/departments') || isActiveRoute('/admin/posts') }"
            >
              <div class="flex items-center">
                <Users class="mr-2 h-4 w-4" />
                用户管理
              </div>
              <ChevronDown 
                v-if="userManagementOpen" 
                class="h-4 w-4 transition-transform" 
              />
              <ChevronRight 
                v-else 
                class="h-4 w-4 transition-transform" 
              />
            </Button>
          </CollapsibleTrigger>
          <CollapsibleContent>
            <div class="pl-6 space-y-1">
              <Button variant="ghost" class="justify-start w-full" as-child>
                <RouterLink to="/admin/users" active-class="bg-accent text-accent-foreground">
                  <Users class="mr-2 h-4 w-4" />
                  管理用户
                </RouterLink>
              </Button>
              <Button variant="ghost" class="justify-start w-full" as-child>
                <RouterLink to="/admin/departments" active-class="bg-accent text-accent-foreground">
                  <Building2 class="mr-2 h-4 w-4" />
                  部门信息
                </RouterLink>
              </Button>
              <Button variant="ghost" class="justify-start w-full" as-child>
                <RouterLink to="/admin/posts" active-class="bg-accent text-accent-foreground">
                  <Briefcase class="mr-2 h-4 w-4" />
                  岗位信息
                </RouterLink>
              </Button>
            </div>
          </CollapsibleContent>
        </Collapsible>
      </template>

      <!-- 审批员菜单 -->
      <template v-else-if="authStore.hasRole('ROLE_APPROVER')">
        <!-- 我的申请下拉菜单 -->
        <Collapsible v-model:open="myAppliesOpen" class="w-full">
          <CollapsibleTrigger class="w-full">
            <Button 
              variant="ghost" 
              class="justify-between w-full"
              :class="{ 'bg-accent text-accent-foreground': isActiveRoute('/applies') }"
            >
              <div class="flex items-center">
                <CalendarDays class="mr-2 h-4 w-4" />
                我的申请
              </div>
              <ChevronDown 
                v-if="myAppliesOpen" 
                class="h-4 w-4 transition-transform" 
              />
              <ChevronRight 
                v-else 
                class="h-4 w-4 transition-transform" 
              />
            </Button>
          </CollapsibleTrigger>
          <CollapsibleContent>
            <div class="pl-6 space-y-1">
              <Button variant="ghost" class="justify-start w-full" as-child>
                <RouterLink to="/applies/new" active-class="bg-accent text-accent-foreground">
                  <CalendarPlus class="mr-2 h-4 w-4" />
                  新建申请
                </RouterLink>
              </Button>
              <Button variant="ghost" class="justify-start w-full" as-child>
                <RouterLink to="/applies/all" active-class="bg-accent text-accent-foreground">
                  <FileText class="mr-2 h-4 w-4" />
                  所有申请
                </RouterLink>
              </Button>
            </div>
          </CollapsibleContent>
        </Collapsible>

        <!-- 我的待批 -->
        <h3 class="text-sm font-semibold text-muted-foreground px-4 mt-2 mb-1">审批管理</h3>
        <Button variant="ghost" class="justify-start" as-child>
          <RouterLink to="/approvals/tasks" active-class="bg-accent text-accent-foreground">
            <CalendarCheck class="mr-2 h-4 w-4" />
            我的待批
          </RouterLink>
        </Button>
      </template>

      <!-- 普通员工菜单 -->
      <template v-else>
        <!-- 我的申请下拉菜单 -->
        <Collapsible v-model:open="myAppliesOpen" class="w-full">
          <CollapsibleTrigger class="w-full">
            <Button 
              variant="ghost" 
              class="justify-between w-full"
              :class="{ 'bg-accent text-accent-foreground': isActiveRoute('/applies') }"
            >
              <div class="flex items-center">
                <CalendarDays class="mr-2 h-4 w-4" />
                我的申请
              </div>
              <ChevronDown 
                v-if="myAppliesOpen" 
                class="h-4 w-4 transition-transform" 
              />
              <ChevronRight 
                v-else 
                class="h-4 w-4 transition-transform" 
              />
            </Button>
          </CollapsibleTrigger>
          <CollapsibleContent>
            <div class="pl-6 space-y-1">
              <Button variant="ghost" class="justify-start w-full" as-child>
                <RouterLink to="/applies/new" active-class="bg-accent text-accent-foreground">
                  <CalendarPlus class="mr-2 h-4 w-4" />
                  新建申请
                </RouterLink>
              </Button>
              <Button variant="ghost" class="justify-start w-full" as-child>
                <RouterLink to="/applies/all" active-class="bg-accent text-accent-foreground">
                  <FileText class="mr-2 h-4 w-4" />
                  所有申请
                </RouterLink>
              </Button>
            </div>
          </CollapsibleContent>
        </Collapsible>
      </template>
    </nav>
  </aside>
</template>