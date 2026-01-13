package cn.edu.shiep.backend.approvalsystem.repository;

import cn.edu.shiep.backend.approvalsystem.entity.ApprovalTask;
import cn.edu.shiep.backend.approvalsystem.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalTaskRepository extends JpaRepository<ApprovalTask, Long> {
    List<ApprovalTask> findByApplyId(Long applyId);
    
    List<ApprovalTask> findByApproverId(Long approverId);
    
    List<ApprovalTask> findByApproverIdAndStatus(Long approverId, TaskStatus status);
    
    List<ApprovalTask> findByApplyIdAndNodeOrder(Long applyId, Integer nodeOrder);
    
    @Query("SELECT t FROM ApprovalTask t WHERE t.approverId = :approverId AND t.status = :status ORDER BY t.createTime DESC")
    List<ApprovalTask> findPendingTasksByApprover(@Param("approverId") Long approverId, @Param("status") TaskStatus status);
    
    long countByApproverIdAndStatus(Long approverId, TaskStatus status);
}
