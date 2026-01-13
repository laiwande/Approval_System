package cn.edu.shiep.backend.approvalsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "department")
@Getter
@Setter
@NoArgsConstructor
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deptId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(length = 50)
    private String ancestors;

    @Column(name = "dept_name", length = 30)
    private String deptName;

    @Column(name = "order_num")
    private Integer orderNum;

    @Column(length = 20)
    private String leader;

    @Column(length = 11)
    private String phone;

    @Column(length = 50)
    private String email;

    @Column(length = 1)
    private String status; // "0"正常 "1"停用

    @Column(name = "del_flag", length = 1)
    private String delFlag; // "0"存在 "2"删除

    @Column(name = "create_by", length = 64)
    private String createBy;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_by", length = 64)
    private String updateBy;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // 自关联：父子部门关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Department parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Department> children;
}