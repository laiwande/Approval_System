package cn.edu.shiep.backend.approvalsystem.entity;

import cn.edu.shiep.backend.approvalsystem.enums.ApplyStatus;
import cn.edu.shiep.backend.approvalsystem.enums.ApplyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "apply")
@Getter
@Setter
@NoArgsConstructor
public class Apply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_type", length = 20, nullable = false)
    private ApplyType applyType;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ApplyStatus status;

    @Column(name = "current_node", nullable = false)
    private Integer currentNode;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(length = 500)
    private String remark;

    // 关系映射
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", insertable = false, updatable = false)
    private User applicant;

    @OneToOne(mappedBy = "apply", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LeaveApply leaveApply;

    @OneToOne(mappedBy = "apply", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReimburseApply reimburseApply;

    @OneToMany(mappedBy = "apply", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Set<ApprovalTask> tasks = new java.util.HashSet<>();

    @OneToMany(mappedBy = "apply", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Set<ApprovalRecord> records = new java.util.HashSet<>();
}