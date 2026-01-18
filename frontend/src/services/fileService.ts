import apiClient from './api';

// 上传文件
export const uploadFile = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return apiClient.post('/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

// 获取文件下载URL
export const getFileUrl = (path: string) => {
  // 如果路径已经是完整URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  // 否则拼接基础URL（从apiClient的baseURL提取）
  const baseURL = apiClient.defaults.baseURL || 'http://localhost:8080/api';
  const serverBase = baseURL.replace('/api', '');
  return `${serverBase}${path}`;
};
