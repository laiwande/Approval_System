package cn.edu.shiep.backend.approvalsystem.controller;

import cn.edu.shiep.backend.approvalsystem.dto.DashboardStatsDTO;
import cn.edu.shiep.backend.approvalsystem.enums.ApplyStatus;
import cn.edu.shiep.backend.approvalsystem.enums.TaskStatus;
import cn.edu.shiep.backend.approvalsystem.repository.ApplyRepository;
import cn.edu.shiep.backend.approvalsystem.repository.ApprovalTaskRepository;
import cn.edu.shiep.backend.approvalsystem.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private ApplyRepository applyRepository;

    @Autowired
    private ApprovalTaskRepository approvalTaskRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        Long userId = userDetailsImpl.getId();

        // 我的申请统计
        long myTotalApplies = applyRepository.findByApplicantId(userId).size();
        long myPendingApplies = applyRepository.countByApplicantIdAndStatus(userId, ApplyStatus.PENDING);
        long myApprovedApplies = applyRepository.countByApplicantIdAndStatus(userId, ApplyStatus.APPROVED);
        long myRejectedApplies = applyRepository.countByApplicantIdAndStatus(userId, ApplyStatus.REJECTED);
        long myDraftApplies = applyRepository.countByApplicantIdAndStatus(userId, ApplyStatus.DRAFT);

        // 审批任务统计（审批人）
        long myPendingTasks = approvalTaskRepository.countByApproverIdAndStatus(userId, TaskStatus.PENDING);
        long myProcessedTasks = approvalTaskRepository.countByApproverIdAndStatus(userId, TaskStatus.DONE);

        // 系统统计（管理员）- 可以根据角色判断是否显示
        long totalApplies = applyRepository.count();
        long totalPendingApplies = applyRepository.countByStatus(ApplyStatus.PENDING);

        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .myTotalApplies(myTotalApplies)
                .myPendingApplies(myPendingApplies)
                .myApprovedApplies(myApprovedApplies)
                .myRejectedApplies(myRejectedApplies)
                .myDraftApplies(myDraftApplies)
                .myPendingTasks(myPendingTasks)
                .myProcessedTasks(myProcessedTasks)
                .totalApplies(totalApplies)
                .totalPendingApplies(totalPendingApplies)
                .build();

        return ResponseEntity.ok(stats);
    }
}