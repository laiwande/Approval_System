package cn.edu.shiep.backend.approvalsystem.utils;

import cn.edu.shiep.backend.approvalsystem.entity.*;
import cn.edu.shiep.backend.approvalsystem.enums.ApplyType;
import cn.edu.shiep.backend.approvalsystem.enums.ERole;
import cn.edu.shiep.backend.approvalsystem.repository.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ApprovalProcessRepository approvalProcessRepository;

    @Autowired
    private ApprovalNodeRepository approvalNodeRepository;

    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdminUser();
        seedPosts();
        seedApprovalProcesses();
    }

    private void seedRoles() {
        if (roleRepository.findByName(ERole.EMPLOYEE).isEmpty()) {
            Roles role = new Roles();
            role.setName(ERole.EMPLOYEE);
            roleRepository.save(role);
        }
        if (roleRepository.findByName(ERole.APPROVER).isEmpty()) {
            Roles role = new Roles();
            role.setName(ERole.APPROVER);
            roleRepository.save(role);
        }
        if (roleRepository.findByName(ERole.ADMIN).isEmpty()) {
            Roles role = new Roles();
            role.setName(ERole.ADMIN);
            roleRepository.save(role);
        }
    }

    private void seedAdminUser() {
        if (userRepository.existsByEmail("admin@approvalsystem.com")) {
            return;
        }

        Roles adminRole = roleRepository.findByName(ERole.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN角色不存在"));

        User adminUser = new User();
        adminUser.setUserName("管理员");
        adminUser.setEmail("admin@approvalsystem.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setPhonenumber("13800138000");
        adminUser.setRole(adminRole);
        userRepository.save(adminUser);
    }

    private void seedPosts() {
        Post approverPost = postRepository.findByPostCode("APPROVER").orElse(null);
        if (approverPost == null) {
            Post post = new Post();
            post.setPostCode("APPROVER");
            post.setPostName("审批人");
            post.setPostSort(1);
            post.setStatus("0");
            post.setCreateTime(LocalDateTime.now());
            post.setRemark("默认审批人岗位");
            approverPost = postRepository.save(post);
        }

        final Post finalApproverPost = approverPost;
        userRepository.findByEmail("admin@approvalsystem.com").ifPresent(adminUser -> {
            entityManager.createNativeQuery(
                    "INSERT IGNORE INTO user_post (user_id, post_id) VALUES (?, ?)")
                    .setParameter(1, adminUser.getUserId())
                    .setParameter(2, finalApproverPost.getPostId())
                    .executeUpdate();
        });
    }

    private void seedApprovalProcesses() {
        Post approverPost = postRepository.findByPostCode("APPROVER")
                .orElseThrow(() -> new RuntimeException("审批人岗位不存在"));

        createProcessIfNotExists(ApplyType.LEAVE, "请假审批流程", approverPost);
        createProcessIfNotExists(ApplyType.REIMBURSE, "报销审批流程", approverPost);
    }

    private void createProcessIfNotExists(ApplyType applyType, String processName, Post approverPost) {
        List<ApprovalProcess> processes = approvalProcessRepository
                .findByApplyTypeAndStatus(applyType, "0");
        
        if (processes.isEmpty()) {
            ApprovalProcess process = new ApprovalProcess();
            process.setProcessName(processName);
            process.setApplyType(applyType);
            process.setStatus("0");
            process.setRemark("默认" + processName);
            ApprovalProcess savedProcess = approvalProcessRepository.save(process);

            ApprovalNode node = new ApprovalNode();
            node.setProcessId(savedProcess.getProcessId());
            node.setNodeOrder(1);
            node.setPostId(approverPost.getPostId());
            node.setRemark("第一级审批");
            approvalNodeRepository.save(node);
        }
    }
}