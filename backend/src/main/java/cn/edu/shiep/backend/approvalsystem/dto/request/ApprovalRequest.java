package cn.edu.shiep.backend.approvalsystem.dto.request;

import cn.edu.shiep.backend.approvalsystem.enums.ApprovalAction;
import lombok.Data;

@Data
public class ApprovalRequest {
    private Long taskId;
    private ApprovalAction action; // APPROVE 或 REJECT
    private String comment; // 审批意见
}
