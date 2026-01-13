package cn.edu.shiep.backend.approvalsystem.repository;

import cn.edu.shiep.backend.approvalsystem.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByParentId(Long parentId);
    
    List<Department> findByStatusAndDelFlag(String status, String delFlag);
    
    Optional<Department> findByDeptName(String deptName);
    
    List<Department> findByDelFlag(String delFlag);
}
