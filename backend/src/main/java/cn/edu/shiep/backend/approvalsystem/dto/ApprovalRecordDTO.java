package cn.edu.shiep.backend.approvalsystem.dto;

import cn.edu.shiep.backend.approvalsystem.enums.ApprovalAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRecordDTO {
    private Long recordId;
    private Long applyId;
    private Integer nodeOrder;
    private Long approverId;
    private String approverName;
    private ApprovalAction action;
    private String comment;
    private LocalDateTime actionTime;
}
