package cn.edu.shiep.backend.approvalsystem.service;

import cn.edu.shiep.backend.approvalsystem.dto.ApprovalRecordDTO;
import cn.edu.shiep.backend.approvalsystem.dto.ApprovalTaskDTO;
import cn.edu.shiep.backend.approvalsystem.dto.ApplyDTO;
import cn.edu.shiep.backend.approvalsystem.dto.request.ApprovalRequest;
import cn.edu.shiep.backend.approvalsystem.entity.*;
import cn.edu.shiep.backend.approvalsystem.enums.ApplyStatus;
import cn.edu.shiep.backend.approvalsystem.enums.ApprovalAction;
import cn.edu.shiep.backend.approvalsystem.enums.TaskStatus;
import cn.edu.shiep.backend.approvalsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    @Autowired
    private ApprovalTaskRepository approvalTaskRepository;

    @Autowired
    private ApprovalRecordRepository approvalRecordRepository;

    @Autowired
    private ApplyRepository applyRepository;

    @Autowired
    private ApprovalNodeRepository approvalNodeRepository;

    @Autowired
    private ApprovalProcessRepository approvalProcessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplyService applyService;

    @Autowired
    private PostRepository postRepository;

    // 获取我的待办任务
    public List<ApprovalTaskDTO> getMyPendingTasks(Long approverId) {
        return approvalTaskRepository.findPendingTasksByApprover(approverId, TaskStatus.PENDING).stream()
                .map(this::toApprovalTaskDTO)
                .collect(Collectors.toList());
    }

    // 处理审批任务
    @Transactional
    public void processApproval(ApprovalRequest request, Long approverId) {
        ApprovalTask task = approvalTaskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("审批任务未找到"));

        if (!Objects.equals(task.getApproverId(), approverId)) {
            throw new RuntimeException("权限不足，您无法处理不属于您的审批任务");
        }

        if (task.getStatus() != TaskStatus.PENDING) {
            throw new RuntimeException("该任务已完成，无法重复处理");
        }

        Apply apply = applyRepository.findById(task.getApplyId())
                .orElseThrow(() -> new RuntimeException("申请未找到"));

        if (apply.getStatus() != ApplyStatus.PENDING) {
            throw new RuntimeException("申请状态异常，无法处理");
        }

        // 更新任务状态
        task.setStatus(TaskStatus.DONE);
        task.setFinishTime(LocalDateTime.now());
        approvalTaskRepository.save(task);

        // 创建审批记录
        ApprovalRecord record = new ApprovalRecord();
        record.setApplyId(apply.getApplyId());
        record.setNodeOrder(task.getNodeOrder());
        record.setApproverId(approverId);
        record.setAction(request.getAction());
        record.setComment(request.getComment());
        record.setActionTime(LocalDateTime.now());
        approvalRecordRepository.save(record);

        // 处理审批结果
        if (request.getAction() == ApprovalAction.REJECT) {
            // 拒绝：申请状态变为已拒绝
            apply.setStatus(ApplyStatus.REJECTED);
            apply.setUpdateTime(LocalDateTime.now());
            applyRepository.save(apply);
        } else {
            // 同意：检查是否所有当前节点的任务都已完成
            List<ApprovalTask> currentNodeTasks = approvalTaskRepository.findByApplyIdAndNodeOrder(apply.getApplyId(), task.getNodeOrder());
            boolean allTasksDone = currentNodeTasks.stream()
                    .allMatch(t -> t.getStatus() == TaskStatus.DONE);

            if (allTasksDone) {
                // 当前节点所有任务都完成，进入下一节点
                moveToNextNode(apply);
            }
        }
    }

    // 移动到下一个审批节点
    private void moveToNextNode(Apply apply) {
        // 查找审批流程
        ApprovalProcess process = approvalProcessRepository
                .findByApplyTypeAndStatus(apply.getApplyType(), "0")
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到对应的审批流程"));

        // 获取下一个节点
        List<ApprovalNode> nodes = approvalNodeRepository.findByProcessIdOrderByNodeOrderAsc(process.getProcessId());
        ApprovalNode nextNode = nodes.stream()
                .filter(node -> node.getNodeOrder() > apply.getCurrentNode())
                .findFirst()
                .orElse(null);

        if (nextNode == null) {
            // 没有下一个节点，审批完成
            apply.setStatus(ApplyStatus.APPROVED);
            apply.setUpdateTime(LocalDateTime.now());
            applyRepository.save(apply);
        } else {
            // 移动到下一个节点
            apply.setCurrentNode(nextNode.getNodeOrder());
            apply.setUpdateTime(LocalDateTime.now());
            applyRepository.save(apply);

            // 创建下一节点的审批任务
            createApprovalTasks(apply, nextNode);
        }
    }

    // 创建审批任务
    private void createApprovalTasks(Apply apply, ApprovalNode node) {
        Post post = postRepository.findById(node.getPostId())
                .orElseThrow(() -> new RuntimeException("岗位未找到"));

        // 获取该岗位的所有用户
        List<User> approvers = userRepository.findAll().stream()
                .filter(user -> user.getPosts().contains(post))
                .collect(Collectors.toList());

        if (approvers.isEmpty()) {
            throw new RuntimeException("该岗位没有分配用户，无法创建审批任务");
        }

        // 为每个审批人创建任务
        for (User approver : approvers) {
            ApprovalTask task = new ApprovalTask();
            task.setApplyId(apply.getApplyId());
            task.setNodeOrder(node.getNodeOrder());
            task.setApproverId(approver.getUserId());
            task.setStatus(TaskStatus.PENDING);
            task.setCreateTime(LocalDateTime.now());
            approvalTaskRepository.save(task);
        }
    }

    // 获取我的已处理记录
    public List<ApprovalRecordDTO> getMyProcessedRecords(Long approverId) {
        return approvalRecordRepository.findByApproverId(approverId).stream()
                .map(this::toApprovalRecordDTO)
                .collect(Collectors.toList());
    }

    // 转换为 DTO
    private ApprovalTaskDTO toApprovalTaskDTO(ApprovalTask task) {
        ApprovalTaskDTO.ApprovalTaskDTOBuilder builder = ApprovalTaskDTO.builder()
                .taskId(task.getTaskId())
                .applyId(task.getApplyId())
                .nodeOrder(task.getNodeOrder())
                .approverId(task.getApproverId())
                .status(task.getStatus())
                .createTime(task.getCreateTime())
                .finishTime(task.getFinishTime());

        // 加载审批人信息
        User approver = userRepository.findById(task.getApproverId()).orElse(null);
        if (approver != null) {
            builder.approverName(approver.getNickName() != null ? approver.getNickName() : approver.getUserName());
        }

        // 加载申请信息
        Apply apply = applyRepository.findById(task.getApplyId()).orElse(null);
        if (apply != null) {
            builder.applyType(apply.getApplyType().name());
            builder.applyCreateTime(apply.getCreateTime());

            User applicant = userRepository.findById(apply.getApplicantId()).orElse(null);
            if (applicant != null) {
                builder.applicantName(applicant.getNickName() != null ? applicant.getNickName() : applicant.getUserName());
            }

            // 设置申请标题
            if (apply.getLeaveApply() != null) {
                builder.applyTitle(apply.getLeaveApply().getReason());
            } else if (apply.getReimburseApply() != null) {
                builder.applyTitle(apply.getReimburseApply().getReason());
            }
        }

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

    // 获取所有申请（管理员查看）
    public List<ApplyDTO> getAllApplies() {
        return applyRepository.findAll().stream()
                .map(apply -> {
                    try {
                        return applyService.getApplyDetail(apply.getApplyId(), apply.getApplicantId());
                    } catch (Exception e) {
                        // 如果权限检查失败，使用 toApplyDTO 方法
                        return applyService.toApplyDTO(apply);
                    }
                })
                .collect(Collectors.toList());
    }
}
