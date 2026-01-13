package cn.edu.shiep.backend.approvalsystem.repository;

import cn.edu.shiep.backend.approvalsystem.entity.ApprovalProcess;
import cn.edu.shiep.backend.approvalsystem.enums.ApplyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalProcessRepository extends JpaRepository<ApprovalProcess, Long> {
    List<ApprovalProcess> findByApplyType(ApplyType applyType);
    
    List<ApprovalProcess> findByStatus(String status);
    
    List<ApprovalProcess> findByApplyTypeAndStatus(ApplyType applyType, String status);
    
    Optional<ApprovalProcess> findByProcessName(String processName);
}
