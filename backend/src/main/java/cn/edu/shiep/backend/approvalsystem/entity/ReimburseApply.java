package cn.edu.shiep.backend.approvalsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "reimburse_apply")
@Getter
@Setter
@NoArgsConstructor
public class ReimburseApply {
    @Id
    @Column(name = "apply_id")
    private Long applyId;

    @Column(name = "expense_type", length = 50, nullable = false)
    private String expenseType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String reason;

    @Column(name = "invoice_url", length = 255)
    private String invoiceUrl;

    @OneToOne
    @JoinColumn(name = "apply_id")
    @MapsId
    private Apply apply;
}