package cn.edu.shiep.backend.approvalsystem.repository;

import cn.edu.shiep.backend.approvalsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
