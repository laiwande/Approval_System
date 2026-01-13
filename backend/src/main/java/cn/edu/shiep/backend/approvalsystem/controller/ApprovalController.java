package cn.edu.shiep.backend.approvalsystem.controller;

import cn.edu.shiep.backend.approvalsystem.dto.ApprovalRecordDTO;
import cn.edu.shiep.backend.approvalsystem.dto.ApprovalTaskDTO;
import cn.edu.shiep.backend.approvalsystem.dto.ApplyDTO;
import cn.edu.shiep.backend.approvalsystem.dto.request.ApprovalRequest;
import cn.edu.shiep.backend.approvalsystem.security.services.UserDetailsImpl;
import cn.edu.shiep.backend.approvalsystem.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    // 获取我的待办任务
    @GetMapping("/tasks/pending")
    public ResponseEntity<List<ApprovalTaskDTO>> getMyPendingTasks(@AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        Long approverId = userDetailsImpl.getId();

        List<ApprovalTaskDTO> tasks = approvalService.getMyPendingTasks(approverId);
        return ResponseEntity.ok(tasks);
    }

    // 处理审批任务
    @PostMapping("/tasks/process")
    public ResponseEntity<Void> processApproval(@RequestBody ApprovalRequest request,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        Long approverId = userDetailsImpl.getId();

        approvalService.processApproval(request, approverId);
        return ResponseEntity.noContent().build();
    }

    // 获取我的已处理记录
    @GetMapping("/records/my")
    public ResponseEntity<List<ApprovalRecordDTO>> getMyProcessedRecords(@AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        Long approverId = userDetailsImpl.getId();

        List<ApprovalRecordDTO> records = approvalService.getMyProcessedRecords(approverId);
        return ResponseEntity.ok(records);
    }

    // 获取所有申请（管理员）
    @GetMapping("/all")
    public ResponseEntity<List<ApplyDTO>> getAllApplies(@AuthenticationPrincipal UserDetails userDetails) {
        // 权限检查在 SecurityConfig 中配置
        List<ApplyDTO> applies = approvalService.getAllApplies();
        return ResponseEntity.ok(applies);
    }
}
