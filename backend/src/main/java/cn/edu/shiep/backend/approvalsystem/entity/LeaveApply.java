package cn.edu.shiep.backend.approvalsystem.entity;

import cn.edu.shiep.backend.approvalsystem.enums.LeaveType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_apply")
@Getter
@Setter
@NoArgsConstructor
public class LeaveApply {
    @Id
    @Column(name = "apply_id")
    private Long applyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", length = 20, nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "leave_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal leaveDays;

    @Column(length = 255)
    private String reason;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl; // 附件URL（文件上传后的路径）

    @OneToOne
    @JoinColumn(name = "apply_id")
    @MapsId
    private Apply apply;
}