package cn.edu.shiep.backend.approvalsystem.dto;

import cn.edu.shiep.backend.approvalsystem.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalTaskDTO {
    private Long taskId;
    private Long applyId;
    private String applyType;
    private String applicantName;
    private Integer nodeOrder;
    private Long approverId;
    private String approverName;
    private TaskStatus status;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
    
    // 申请基本信息（用于待办列表显示）
    private String applyTitle; // 请假事由或报销事由
    private LocalDateTime applyCreateTime;
}
