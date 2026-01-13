package cn.edu.shiep.backend.approvalsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "user_name", length = 30)
    private String userName;

    @Column(name = "nick_name", length = 30)
    private String nickName;

    @Column(name = "user_type", length = 2)
    private String userType; // "00" 系统用户

    @Column(length = 50)
    private String email;

    @Column(name = "phonenumber", length = 11)
    private String phonenumber;

    @Column(length = 1)
    private String sex; // "0"男 "1"女 "2"未知

    @Column(length = 100)
    private String avatar;

    @Column(length = 100)
    private String password;

    @Column(length = 1)
    private String status; // "0"正常 "1"停用

    @Column(name = "del_flag", length = 1)
    private String delFlag; // "0"存在 "2"删除

    @Column(name = "login_ip", length = 128)
    private String loginIp;

    @Column(name = "login_date")
    private LocalDateTime loginDate;

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

    // 关系映射
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", insertable = false, updatable = false)
    private Department department;

    @ManyToMany
    @JoinTable(
        name = "user_post",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Apply> applies = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Roles role;
    
    // 添加构造函数
    public User(String userName, String email, String password, String phonenumber) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.phonenumber = phonenumber;
        // 设置默认值
        this.status = "0"; // 正常状态
        this.delFlag = "0"; // 存在
        this.userType = "00"; // 系统用户
        this.createTime = LocalDateTime.now();
    }
}
