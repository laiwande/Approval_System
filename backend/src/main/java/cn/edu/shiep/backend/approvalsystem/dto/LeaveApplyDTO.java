package cn.edu.shiep.backend.approvalsystem.dto;

import cn.edu.shiep.backend.approvalsystem.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApplyDTO {
    private Long applyId;
    private LeaveType leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal leaveDays;
    private String reason;
    private String attachmentUrl; // 附件URL
}
