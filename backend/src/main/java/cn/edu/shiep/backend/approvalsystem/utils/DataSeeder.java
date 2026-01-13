package cn.edu.shiep.backend.approvalsystem.utils;

import cn.edu.shiep.backend.approvalsystem.entity.Roles;
import cn.edu.shiep.backend.approvalsystem.entity.User;
import cn.edu.shiep.backend.approvalsystem.enums.ERole;
import cn.edu.shiep.backend.approvalsystem.repository.RoleRepository;
import cn.edu.shiep.backend.approvalsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdminUser();
    }
    // 填充用户表中的角色
    private void seedRoles() {
        if (roleRepository.findByName(ERole.EMPLOYEE).isEmpty()) {
            Roles employeeRole = new Roles();
            employeeRole.setName(ERole.EMPLOYEE);
            roleRepository.save(employeeRole);
        }

        if (roleRepository.findByName(ERole.APPROVER).isEmpty()) {
            Roles approverRole = new Roles();
            approverRole.setName(ERole.APPROVER);
            roleRepository.save(approverRole);
        }

        if (roleRepository.findByName(ERole.ADMIN).isEmpty()) {
            Roles adminRole = new Roles();
            adminRole.setName(ERole.ADMIN);
            roleRepository.save(adminRole);
        }
    }

    // 创建默认管理员账号
    private void seedAdminUser() {
        if (userRepository.existsByEmail("admin@approvalsystem.com")) {
            return; // 如果管理员已存在，不重复创建
        }

        // 获取管理员角色
        Roles adminRole = roleRepository.findByName(ERole.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN角色不存在"));

        // 创建管理员用户
        User adminUser = new User();
        adminUser.setUserName("管理员");
        adminUser.setEmail("admin@approvalsystem.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setPhonenumber("13800138000");
        adminUser.setRole(adminRole);

        userRepository.save(adminUser);
        System.out.println("默认管理员账号已创建:");
        System.out.println("邮箱: admin@approvalsystem.com");
        System.out.println("密码: admin123");
    }
}