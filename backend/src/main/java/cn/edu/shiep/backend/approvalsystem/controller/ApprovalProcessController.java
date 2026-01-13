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
}
