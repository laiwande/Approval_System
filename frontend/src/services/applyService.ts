import apiClient from './api';

// 创建申请（草稿）
export const createApply = (data: any) => {
  return apiClient.post('/applies', data);
};

// 提交申请
export const submitApply = (id: number) => {
  return apiClient.post(`/applies/${id}/submit`);
};

// 撤回申请
export const withdrawApply = (id: number) => {
  return apiClient.post(`/applies/${id}/withdraw`);
};

// 获取我的申请列表
export const getMyApplies = () => {
  return apiClient.get('/applies/my');
};

// 获取申请详情
export const getApplyDetail = (id: number) => {
  return apiClient.get(`/applies/${id}`);
};
