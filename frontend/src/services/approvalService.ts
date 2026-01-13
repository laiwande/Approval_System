import apiClient from './api';

// 获取我的待办任务
export const getMyPendingTasks = () => {
  return apiClient.get('/approvals/tasks/pending');
};

// 处理审批任务
export const processApproval = (data: any) => {
  return apiClient.post('/approvals/tasks/process', data);
};

// 获取我的已处理记录
export const getMyProcessedRecords = () => {
  return apiClient.get('/approvals/records/my');
};

// 获取所有申请（管理员）
export const getAllApplies = () => {
  return apiClient.get('/approvals/all');
};
