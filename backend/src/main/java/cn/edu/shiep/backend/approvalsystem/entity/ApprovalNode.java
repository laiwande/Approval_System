package cn.edu.shiep.backend.approvalsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approval_node")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"process", "post", "user"})
public class ApprovalNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nodeId;

    @Column(name = "process_id", nullable = false)
    private Long processId;

    @Column(name = "node_order", nullable = false)
    private Integer nodeOrder;

    @Column(name = "post_id", nullable = true)
    private Long postId;

    @Column(name = "user_id", nullable = true)
    private Long userId;

    @Column(length = 255)
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", insertable = false, updatable = false)
    private ApprovalProcess process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}