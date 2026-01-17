import apiClient from './api';

// 系统管理员
// 获取系统中的所有用户列表
export const getAllUsers = () => {
    return apiClient.get('/admin/users');
};

// 更新用户的角色
export const updateUserRole = (userId: number, role: string) => {
    return apiClient.put(`/admin/users/${userId}/role`, role,{
      headers: {
        'Content-Type': 'text/plain' 
      }
    });
};

// 删除一个用户
export const deleteUser = (userId: number) => {
    return apiClient.delete(`/admin/users/${userId}`);
};

// 新建用户
export const createUser = (userData: any) => {
  return apiClient.post(`/admin/users?role=${userData.role}`, userData);
};

// 更新用户信息
export const updateUser = (userId: number, userData: any) => {
  return apiClient.put(`/admin/users/${userId}`, userData);
};