package cn.edu.shiep.backend.approvalsystem.repository;

import cn.edu.shiep.backend.approvalsystem.entity.User;
import cn.edu.shiep.backend.approvalsystem.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUserName(String userName);
    
    boolean existsByEmail(String email);
    
    boolean existsByUserName(String userName);
    
    List<User> findByDeptId(Long deptId);
    
    List<User> findByStatusAndDelFlag(String status, String delFlag);
    
    List<User> findByDelFlag(String delFlag);
    
    // 查询具有指定角色的用户（正常状态且未删除）
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.role.name IN :roles AND (u.status = '0' OR u.status IS NULL) AND (u.delFlag = '0' OR u.delFlag IS NULL)")
    List<User> findByRoleNameInAndActive(@Param("roles") List<ERole> roles);
}
