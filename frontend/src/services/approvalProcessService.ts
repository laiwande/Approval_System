import apiClient from './api';

// 获取所有审批流程
export const getAllProcesses = () => {
  return apiClient.get('/approval-processes');
};

// 根据申请类型获取启用的审批流程列表
export const getProcessesByType = (applyType: string) => {
  return apiClient.get(`/approval-processes/by-type/${applyType}`);
};

// 获取审批流程详情
export const getProcessById = (processId: number) => {
  return apiClient.get(`/approval-processes/${processId}`);
};
