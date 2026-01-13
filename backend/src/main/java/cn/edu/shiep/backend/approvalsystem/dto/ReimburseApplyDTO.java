package cn.edu.shiep.backend.approvalsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimburseApplyDTO {
    private Long applyId;
    private String expenseType;
    private BigDecimal amount;
    private String reason;
    private String invoiceUrl;
}
