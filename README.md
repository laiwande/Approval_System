# 📝 审批系统

该项目作为上海电力大学信管Java课设

项目后端基于 Spring Boot 构建，前端采用 Vue.js，实现了流畅的单页应用（SPA）交互

# ✨ 功能介绍
#### 👤 普通员工 (EMPLOYEE)
- ✅ 登录系统
- ✅ 发起审批申请（请假、报销）
- ✅ 查看"我的申请"（包含历史申请、正在进行的申请）
- ✅ 查看审批进度与审批历史
- ✅ 在审批尚未开始前撤回申请

#### 👨‍💼 审批人 (APPROVER)
- ✅ 登录系统
- ✅ 查看分配给自己的审批待办（自动过滤自己的申请）
- ✅ 对审批进行同意/拒绝操作
- ✅ 填写审批意见
- ✅ 查看本人已处理的审批记录
- ✅ 查看自己的申请

#### 🔧 系统管理员 (ADMIN)
- ✅ 登录系统
- ✅ 管理用户、部门、岗位信息
- ✅ 为用户分配角色
- ✅ 查看系统全部审批数据（只读）
- ✅ 创建和管理审批流程
- ✅ 查看自己的申请

## 🚀 部署指南

### 环境要求
- JDK 21+
- Node.js 16+
- MySQL 8+

### 数据库配置
Approval_System/backend/src/main/resources/application.properties

### 后端部署
```bash
git clone https://github.com/laiwande/Approval_System.git
cd backend
mvn spring-boot:run
```

### 前端部署
```bash
cd frontend
npm install
npm run dev
```
