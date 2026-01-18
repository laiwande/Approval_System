package cn.edu.shiep.backend.approvalsystem.controller;

import cn.edu.shiep.backend.approvalsystem.dto.request.ApprovalProcessRequest;
import cn.edu.shiep.backend.approvalsystem.entity.ApprovalProcess;
import cn.edu.shiep.backend.approvalsystem.service.ApprovalProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval-processes")
public class ApprovalProcessController {

    @Autowired
    private ApprovalProcessService approvalProcessService;

    // 创建审批流程
    @PostMapping
    public ResponseEntity<ApprovalProcess> createProcess(@RequestBody ApprovalProcessRequest request) {
        ApprovalProcess process = approvalProcessService.createProcess(request);
        return ResponseEntity.ok(process);
    }

    // 获取所有审批流程
    @GetMapping
    public ResponseEntity<List<ApprovalProcess>> getAllProcesses() {
        List<ApprovalProcess> processes = approvalProcessService.getAllProcesses();
        return ResponseEntity.ok(processes);
    }

    // 根据申请类型获取启用的审批流程列表
    @GetMapping("/by-type/{applyType}")
    public ResponseEntity<List<ApprovalProcess>> getProcessesByType(@PathVariable String applyType) {
        cn.edu.shiep.backend.approvalsystem.enums.ApplyType type = 
            cn.edu.shiep.backend.approvalsystem.enums.ApplyType.valueOf(applyType.toUpperCase());
        List<ApprovalProcess> processes = approvalProcessService.getProcessesByTypeAndStatus(type, "0");
        return ResponseEntity.ok(processes);
    }

    // 获取审批流程详情（包含节点）
    @GetMapping("/{processId}")
    public ResponseEntity<ApprovalProcess> getProcessById(@PathVariable Long processId) {
        ApprovalProcess process = approvalProcessService.getProcessById(processId);
        return ResponseEntity.ok(process);
    }

    // 更新审批流程
    @PutMapping("/{processId}")
    public ResponseEntity<ApprovalProcess> updateProcess(
            @PathVariable Long processId,
            @RequestBody ApprovalProcessRequest request) {
        ApprovalProcess process = approvalProcessService.updateProcess(processId, request);
        return ResponseEntity.ok(process);
    }
}
