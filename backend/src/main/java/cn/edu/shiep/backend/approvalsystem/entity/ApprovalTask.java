package cn.edu.shiep.backend.approvalsystem.entity;

import cn.edu.shiep.backend.approvalsystem.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_task")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(name = "apply_id", nullable = false)
    private Long applyId;

    @Column(name = "node_order", nullable = false)
    private Integer nodeOrder;

    @Column(name = "approver_id", nullable = false)
    private Long approverId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TaskStatus status;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "finish_time")
    private LocalDateTime finishTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apply_id", insertable = false, updatable = false)
    private Apply apply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", insertable = false, updatable = false)
    private User approver;
}