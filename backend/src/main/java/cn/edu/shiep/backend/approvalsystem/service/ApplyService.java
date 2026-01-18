package cn.edu.shiep.backend.approvalsystem.service;

import cn.edu.shiep.backend.approvalsystem.dto.*;
import cn.edu.shiep.backend.approvalsystem.dto.request.ApplyRequest;
import cn.edu.shiep.backend.approvalsystem.entity.*;
import cn.edu.shiep.backend.approvalsystem.enums.ApplyStatus;
import cn.edu.shiep.backend.approvalsystem.enums.ApplyType;
import cn.edu.shiep.backend.approvalsystem.enums.ERole;
import cn.edu.shiep.backend.approvalsystem.enums.TaskStatus;
import cn.edu.shiep.backend.approvalsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ApplyService {

    @Autowired
    private ApplyRepository applyRepository;

    @Autowired
    private LeaveApplyRepository leaveApplyRepository;

    @Autowired
    private ReimburseApplyRepository reimburseApplyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApprovalProcessRepository approvalProcessRepository;

    @Autowired
    private ApprovalNodeRepository approvalNodeRepository;

    @Autowired
    private ApprovalTaskRepository approvalTaskRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ApprovalRecordRepository approvalRecordRepository;

    @Autowired
    private RoleRepository roleRepository;


    // 创建申请
    @Transactional
    public ApplyDTO createApply(ApplyRequest request, Long userId) {
        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户未找到"));

        // 创建申请主记录
        Apply apply = new Apply();
        apply.setApplyType(request.getApplyType());
        apply.setApplicantId(userId);
        apply.setStatus(ApplyStatus.DRAFT);
        apply.setCurrentNode(0);
        apply.setCreateTime(LocalDateTime.now());
        apply.setRemark(request.getRemark());

        Apply savedApply = applyRepository.save(apply);

        // 根据申请类型创建具体申请
        if (request.getApplyType() == ApplyType.LEAVE) {
            LeaveApply leaveApply = new LeaveApply();
            // leaveApply.setApplyId(savedApply.getApplyId());
            leaveApply.setLeaveType(request.getLeaveType());
            leaveApply.setStartTime(request.getStartTime());
            leaveApply.setEndTime(request.getEndTime());
            
            // 计算请假天数
            if (request.getLeaveDays() != null) {
                leaveApply.setLeaveDays(request.getLeaveDays());
            } else {
                // 按自然日计算：1天 = 24小时
                Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
                long hours = duration.toHours();
                BigDecimal days = BigDecimal.valueOf(hours).divide(BigDecimal.valueOf(24), 2, BigDecimal.ROUND_HALF_UP);
                leaveApply.setLeaveDays(days);
            }
            
            leaveApply.setReason(request.getReason());
            // 设置附件URL
            if (request.getAttachmentUrl() != null && !request.getAttachmentUrl().trim().isEmpty()) {
                leaveApply.setAttachmentUrl(request.getAttachmentUrl().trim());
            }
            // 设置双向关系
            leaveApply.setApply(savedApply);
            savedApply.setLeaveApply(leaveApply);
            leaveApplyRepository.save(leaveApply);
        } else if (request.getApplyType() == ApplyType.REIMBURSE) {
            // 验证必填字段
            if (request.getExpenseType() == null || request.getExpenseType().trim().isEmpty()) {
                throw new RuntimeException("费用类型不能为空");
            }
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("报销金额必须大于0");
            }
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new RuntimeException("报销事由不能为空");
            }
            
            ReimburseApply reimburseApply = new ReimburseApply();
            // 不要手动设置applyId，因为使用了@MapsId，JPA会自动从关联的Apply实体中获取ID
            reimburseApply.setExpenseType(request.getExpenseType().trim());
            reimburseApply.setAmount(request.getAmount());
            reimburseApply.setReason(request.getReason().trim());
            // 设置附件URL
            if (request.getAttachmentUrl() != null && !request.getAttachmentUrl().trim().isEmpty()) {
                reimburseApply.setAttachmentUrl(request.getAttachmentUrl().trim());
            }
            // 设置双向关系
            reimburseApply.setApply(savedApply);
            savedApply.setReimburseApply(reimburseApply);
            reimburseApplyRepository.save(reimburseApply);
        }

        return toApplyDTO(savedApply);
    }

    // 提交申请（从草稿状态变为待审批）
    @Transactional
    public ApplyDTO submitApply(Long applyId, Long userId, Long processId) {
        try {
            Apply apply = applyRepository.findById(applyId)
                    .orElseThrow(() -> new RuntimeException("申请未找到"));

            if (!Objects.equals(apply.getApplicantId(), userId)) {
                throw new RuntimeException("权限不足，您无法提交不属于您的申请");
            }

            if (apply.getStatus() != ApplyStatus.DRAFT) {
                throw new RuntimeException("只能提交草稿状态的申请");
            }

            // 查找对应的审批流程
            ApprovalProcess process;
            if (processId != null) {
                // 使用指定的流程
                process = approvalProcessRepository.findById(processId)
                        .orElseThrow(() -> new RuntimeException("指定的审批流程不存在"));
                if (!process.getApplyType().equals(apply.getApplyType())) {
                    throw new RuntimeException("审批流程的申请类型与申请不匹配");
                }
                if (!"0".equals(process.getStatus())) {
                    throw new RuntimeException("指定的审批流程未启用");
                }
            } else {
                // 使用默认流程（第一个启用的流程）
                process = approvalProcessRepository
                        .findByApplyTypeAndStatus(apply.getApplyType(), "0")
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("未找到对应的审批流程，请先配置审批流程"));
            }

            // 获取审批节点
            List<ApprovalNode> nodes = approvalNodeRepository.findByProcessIdOrderByNodeOrderAsc(process.getProcessId());
            
            // 保存流程ID
            apply.setProcessId(process.getProcessId());
            
            if (nodes.isEmpty()) {
                // 没有节点的流程：使用旧逻辑，给所有 APPROVER 和 ADMIN 创建任务
                apply.setStatus(ApplyStatus.PENDING);
                apply.setCurrentNode(0); // 使用0表示没有节点
                apply.setUpdateTime(LocalDateTime.now());
                applyRepository.save(apply);
                
                // 创建审批任务（给所有审批人和管理员）
                createApprovalTasksForAllApprovers(apply);
            } else {
                // 有节点的流程：使用新逻辑，按节点创建任务
                ApprovalNode firstNode = nodes.get(0);
                apply.setStatus(ApplyStatus.PENDING);
                apply.setCurrentNode(firstNode.getNodeOrder());
                apply.setUpdateTime(LocalDateTime.now());
                applyRepository.save(apply);

                // 创建审批任务（只给节点指定的用户）
                createApprovalTasks(apply, firstNode);
            }

            return toApplyDTO(apply);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("提交申请失败: " + e.getMessage(), e);
        }
    }

    // 创建审批任务（基于节点指定的用户）
    private void createApprovalTasks(Apply apply, ApprovalNode node) {
        try {
            if (node.getUserId() != null) {
                // 节点指定了用户，只给该用户创建任务
                User approver = userRepository.findById(node.getUserId())
                        .orElseThrow(() -> new RuntimeException("节点指定的审批人不存在"));
                
                ApprovalTask task = new ApprovalTask();
                task.setApplyId(apply.getApplyId());
                task.setNodeOrder(node.getNodeOrder());
                task.setApproverId(approver.getUserId());
                task.setStatus(TaskStatus.PENDING);
                task.setCreateTime(LocalDateTime.now());
                approvalTaskRepository.save(task);
            } else {
                // 节点没有指定用户（旧流程兼容），给所有 APPROVER 和 ADMIN 创建任务
                createApprovalTasksForAllApprovers(apply, node.getNodeOrder());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("创建审批任务失败: " + e.getMessage(), e);
        }
    }

    // 创建审批任务（给所有审批人和管理员）- 用于没有节点的流程
    private void createApprovalTasksForAllApprovers(Apply apply) {
        createApprovalTasksForAllApprovers(apply, 0);
    }

    // 创建审批任务（给所有审批人和管理员）- 用于没有节点的流程或节点未指定用户的情况
    private void createApprovalTasksForAllApprovers(Apply apply, Integer nodeOrder) {
        try {
            // 获取所有具有 APPROVER 或 ADMIN 角色的用户
            List<ERole> approverRoles = List.of(ERole.APPROVER, ERole.ADMIN);
            List<User> approvers = userRepository.findByRoleNameInAndActive(approverRoles);

            if (approvers.isEmpty()) {
                throw new RuntimeException("系统中没有可用的审批人（APPROVER）或管理员（ADMIN），无法创建审批任务。请确保系统中有用户被分配了审批人或管理员角色。");
            }

            // 为每个审批人创建任务
            for (User approver : approvers) {
                ApprovalTask task = new ApprovalTask();
                task.setApplyId(apply.getApplyId());
                task.setNodeOrder(nodeOrder);
                task.setApproverId(approver.getUserId());
                task.setStatus(TaskStatus.PENDING);
                task.setCreateTime(LocalDateTime.now());
                approvalTaskRepository.save(task);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("创建审批任务失败: " + e.getMessage(), e);
        }
    }

    // 撤回申请
    @Transactional
    public void withdrawApply(Long applyId, Long userId) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new RuntimeException("申请未找到"));

        if (!Objects.equals(apply.getApplicantId(), userId)) {
            throw new RuntimeException("权限不足，您无法撤回不属于您的申请");
        }

        if (apply.getStatus() != ApplyStatus.DRAFT && apply.getStatus() != ApplyStatus.PENDING) {
            throw new RuntimeException("只能撤回草稿或待审批状态的申请");
        }

        // 检查是否有审批任务已处理
        List<ApprovalTask> tasks = approvalTaskRepository.findByApplyId(applyId);
        boolean hasProcessedTask = tasks.stream()
                .anyMatch(task -> task.getStatus() == TaskStatus.DONE);

        if (hasProcessedTask) {
            throw new RuntimeException("申请已进入审批流程，无法撤回");
        }

        apply.setStatus(ApplyStatus.WITHDRAWN);
        apply.setUpdateTime(LocalDateTime.now());
        applyRepository.save(apply);

        // 删除未处理的审批任务
        tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.PENDING)
                .forEach(approvalTaskRepository::delete);
    }

    // 获取我的申请列表
    public List<ApplyDTO> getMyApplies(Long userId) {
        return applyRepository.findByApplicantId(userId).stream()
                .map(this::toApplyDTO)
                .collect(Collectors.toList());
    }

    // 获取申请详情
    public ApplyDTO getApplyDetail(Long applyId, Long userId) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new RuntimeException("申请未找到"));

        // 权限检查：申请人可以查看，审批人可以查看
        if (!Objects.equals(apply.getApplicantId(), userId)) {
            // 检查是否是审批人
            boolean isApprover = approvalTaskRepository.findByApplyId(applyId).stream()
                    .anyMatch(task -> Objects.equals(task.getApproverId(), userId));
            if (!isApprover) {
                throw new RuntimeException("权限不足，您无法查看此申请");
            }
        }

        return toApplyDTO(apply);
    }

    // 转换为 DTO（供外部调用）
    public ApplyDTO toApplyDTO(Apply apply) {
        ApplyDTO.ApplyDTOBuilder builder = ApplyDTO.builder()
                .applyId(apply.getApplyId())
                .applyType(apply.getApplyType())
                .applicantId(apply.getApplicantId())
                .status(apply.getStatus())
                .currentNode(apply.getCurrentNode())
                .createTime(apply.getCreateTime())
                .updateTime(apply.getUpdateTime())
                .remark(apply.getRemark());

        // 加载申请人信息
        User applicant = userRepository.findById(apply.getApplicantId()).orElse(null);
        if (applicant != null) {
            builder.applicantName(applicant.getNickName() != null ? applicant.getNickName() : applicant.getUserName());
            builder.applicantEmail(applicant.getEmail());
        }

        // 加载请假申请详情
        if (apply.getLeaveApply() != null) {
            LeaveApply leaveApply = apply.getLeaveApply();
            builder.leaveApply(LeaveApplyDTO.builder()
                    .applyId(leaveApply.getApplyId())
                    .leaveType(leaveApply.getLeaveType())
                    .startTime(leaveApply.getStartTime())
                    .endTime(leaveApply.getEndTime())
                    .leaveDays(leaveApply.getLeaveDays())
                    .reason(leaveApply.getReason())
                    .attachmentUrl(leaveApply.getAttachmentUrl())
                    .build());
        }

        // 加载报销申请详情
        if (apply.getReimburseApply() != null) {
            ReimburseApply reimburseApply = apply.getReimburseApply();
            builder.reimburseApply(ReimburseApplyDTO.builder()
                    .applyId(reimburseApply.getApplyId())
                    .expenseType(reimburseApply.getExpenseType())
                    .amount(reimburseApply.getAmount())
                    .reason(reimburseApply.getReason())
                    .attachmentUrl(reimburseApply.getAttachmentUrl())
                    .build());
        }

        // 加载审批记录
        List<ApprovalRecord> records = approvalRecordRepository.findByApplyIdOrderByActionTimeAsc(apply.getApplyId());
        builder.records(records.stream()
                .map(this::toApprovalRecordDTO)
                .collect(Collectors.toList()));

        return builder.build();
    }

    private ApprovalRecordDTO toApprovalRecordDTO(ApprovalRecord record) {
        ApprovalRecordDTO.ApprovalRecordDTOBuilder builder = ApprovalRecordDTO.builder()
                .recordId(record.getRecordId())
                .applyId(record.getApplyId())
                .nodeOrder(record.getNodeOrder())
                .approverId(record.getApproverId())
                .action(record.getAction())
                .comment(record.getComment())
                .actionTime(record.getActionTime());

        User approver = userRepository.findById(record.getApproverId()).orElse(null);
        if (approver != null) {
            builder.approverName(approver.getNickName() != null ? approver.getNickName() : approver.getUserName());
        }

        return builder.build();
    }
}
