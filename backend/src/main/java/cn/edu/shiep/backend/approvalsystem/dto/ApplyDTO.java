package cn.edu.shiep.backend.approvalsystem.dto;

import cn.edu.shiep.backend.approvalsystem.enums.ApplyStatus;
import cn.edu.shiep.backend.approvalsystem.enums.ApplyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyDTO {
    private Long applyId;
    private ApplyType applyType;
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private ApplyStatus status;
    private Integer currentNode;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;
    
    // 请假申请详情
    private LeaveApplyDTO leaveApply;
    
    // 报销申请详情
    private ReimburseApplyDTO reimburseApply;
    
    // 审批记录
    private java.util.List<ApprovalRecordDTO> records;
}
