<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { toast } from 'vue-sonner';
import { createApply, submitApply } from '@/services/applyService';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const router = useRouter();

const applyType = ref<'LEAVE' | 'REIMBURSE'>('LEAVE');
const apply = ref({
  applyType: 'LEAVE' as 'LEAVE' | 'REIMBURSE',
  remark: '',
  // 请假申请字段
  leaveType: 'ANNUAL' as 'ANNUAL' | 'SICK' | 'PERSONAL',
  startTime: '',
  endTime: '',
  leaveDays: '',
  reason: '',
  // 报销申请字段
  expenseType: '',
  amount: '',
  invoiceUrl: '',
});

const handleSubmit = async () => {
  try {
    const requestData: any = {
      applyType: applyType.value,
      remark: apply.value.remark,
    };

    if (applyType.value === 'LEAVE') {
      requestData.leaveType = apply.value.leaveType;
      requestData.startTime = apply.value.startTime;
      requestData.endTime = apply.value.endTime;
      requestData.reason = apply.value.reason;
      if (apply.value.leaveDays) {
        requestData.leaveDays = parseFloat(apply.value.leaveDays);
      }
    } else {
      requestData.expenseType = apply.value.expenseType;
      requestData.amount = parseFloat(apply.value.amount);
      requestData.reason = apply.value.reason;
      requestData.invoiceUrl = apply.value.invoiceUrl;
    }

    const response = await createApply(requestData);
    const applyId = response.data.applyId;
    
    // 自动提交申请
    await submitApply(applyId);
    
    toast.success('申请提交成功！');
    router.push('/applies/my');
  } catch (error: any) {
    toast.error('申请失败', { description: error.response?.data?.message || '请检查您的输入信息。' });
  }
};

// 计算请假天数
const calculateLeaveDays = () => {
  if (apply.value.startTime && apply.value.endTime) {
    const start = new Date(apply.value.startTime);
    const end = new Date(apply.value.endTime);
    const diffTime = Math.abs(end.getTime() - start.getTime());
    // 按自然日计算：1天 = 24小时
    const diffDays = diffTime / (1000 * 60 * 60 * 24);
    apply.value.leaveDays = diffDays.toFixed(2);
  }
};
</script>

<template>
  <Card class="w-full mx-auto max-w-2xl">
    <CardHeader>
      <CardTitle>新建审批申请</CardTitle>
      <CardDescription>请选择申请类型并填写相关信息</CardDescription>
    </CardHeader>
    <CardContent>
      <form @submit.prevent="handleSubmit" class="space-y-6">
        <div class="space-y-2">
          <Label for="applyType">申请类型</Label>
          <Select v-model="applyType" @update:model-value="apply.applyType = applyType">
            <SelectTrigger>
              <SelectValue placeholder="请选择申请类型" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="LEAVE">请假申请</SelectItem>
              <SelectItem value="REIMBURSE">报销申请</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <!-- 请假申请表单 -->
        <template v-if="applyType === 'LEAVE'">
          <div class="space-y-2">
            <Label for="leaveType">请假类型</Label>
            <Select v-model="apply.leaveType">
              <SelectTrigger>
                <SelectValue placeholder="请选择请假类型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ANNUAL">年假</SelectItem>
                <SelectItem value="SICK">病假</SelectItem>
                <SelectItem value="PERSONAL">事假</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label for="startTime">开始时间</Label>
              <Input id="startTime" v-model="apply.startTime" type="datetime-local" required @change="calculateLeaveDays" />
            </div>
            <div class="space-y-2">
              <Label for="endTime">结束时间</Label>
              <Input id="endTime" v-model="apply.endTime" type="datetime-local" required @change="calculateLeaveDays" />
            </div>
          </div>

          <div class="space-y-2">
            <Label for="leaveDays">请假天数</Label>
            <Input id="leaveDays" v-model="apply.leaveDays" type="number" step="0.5" min="0" readonly />
          </div>

          <div class="space-y-2">
            <Label for="reason">请假事由</Label>
            <Input id="reason" v-model="apply.reason" placeholder="请输入请假事由" required />
          </div>
        </template>

        <!-- 报销申请表单 -->
        <template v-else>
          <div class="space-y-2">
            <Label for="expenseType">费用类型</Label>
            <Input id="expenseType" v-model="apply.expenseType" placeholder="如：差旅费、餐费等" required />
          </div>

          <div class="space-y-2">
            <Label for="amount">报销金额</Label>
            <Input id="amount" v-model="apply.amount" type="number" step="0.01" min="0" placeholder="请输入金额" required />
          </div>

          <div class="space-y-2">
            <Label for="reason">报销事由</Label>
            <Input id="reason" v-model="apply.reason" placeholder="请输入报销事由" required />
          </div>

          <div class="space-y-2">
            <Label for="invoiceUrl">发票附件路径</Label>
            <Input id="invoiceUrl" v-model="apply.invoiceUrl" placeholder="请输入发票附件路径（可选）" />
          </div>
        </template>

        <div class="space-y-2">
          <Label for="remark">备注</Label>
          <Input id="remark" v-model="apply.remark" placeholder="其他说明（可选）" />
        </div>

        <Button type="submit" class="w-full">提交申请</Button>
      </form>
    </CardContent>
  </Card>
</template>
