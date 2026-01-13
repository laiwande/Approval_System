package cn.edu.shiep.backend.approvalsystem.entity;

import cn.edu.shiep.backend.approvalsystem.enums.ApplyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "approval_process")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long processId;

    @Column(name = "process_name", length = 50, nullable = false)
    private String processName;

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_type", length = 20, nullable = false)
    private ApplyType applyType;

    @Column(length = 1, nullable = false)
    private String status; // "0"启用 "1"停用

    @Column(length = 500)
    private String remark;

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApprovalNode> nodes;
}