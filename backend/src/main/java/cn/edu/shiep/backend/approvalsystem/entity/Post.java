package cn.edu.shiep.backend.approvalsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"users", "approvalNodes"})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(name = "post_code", length = 64)
    private String postCode;

    @Column(name = "post_name", length = 50)
    private String postName;

    @Column(name = "post_sort")
    private Integer postSort;

    @Column(length = 1)
    private String status; // "0"正常 "1"停用

    @Column(name = "create_by", length = 64)
    private String createBy;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_by", length = 64)
    private String updateBy;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(length = 500)
    private String remark;

    @ManyToMany(mappedBy = "posts")
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Set<ApprovalNode> approvalNodes = new HashSet<>();
}