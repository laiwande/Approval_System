package cn.edu.shiep.backend.approvalsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approval_node")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nodeId;

    @Column(name = "process_id", nullable = false)
    private Long processId;

    @Column(name = "node_order", nullable = false)
    private Integer nodeOrder;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(length = 255)
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", insertable = false, updatable = false)
    private ApprovalProcess process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;
}