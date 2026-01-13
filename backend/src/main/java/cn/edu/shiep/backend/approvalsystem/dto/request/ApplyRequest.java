package cn.edu.shiep.backend.approvalsystem.dto.request;

import cn.edu.shiep.backend.approvalsystem.enums.ApplyType;
import cn.edu.shiep.backend.approvalsystem.enums.LeaveType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ApplyRequest {
    private ApplyType applyType;
    private String remark;
    
    // 请假申请字段
    private LeaveType leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal leaveDays;
    private String reason; // 请假事由
    
    // 报销申请字段
    private String expenseType;
    private BigDecimal amount;
    private String invoiceUrl;
}
