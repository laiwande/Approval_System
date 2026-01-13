package cn.edu.shiep.backend.approvalsystem.repository;

import cn.edu.shiep.backend.approvalsystem.entity.ApprovalRecord;
import cn.edu.shiep.backend.approvalsystem.enums.ApprovalAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord, Long> {
    List<ApprovalRecord> findByApplyId(Long applyId);
    
    List<ApprovalRecord> findByApplyIdOrderByActionTimeAsc(Long applyId);
    
    List<ApprovalRecord> findByApproverId(Long approverId);
    
    List<ApprovalRecord> findByApproverIdAndAction(Long approverId, ApprovalAction action);
    
    @Query("SELECT r FROM ApprovalRecord r WHERE r.applyId = :applyId AND r.nodeOrder = :nodeOrder")
    List<ApprovalRecord> findByApplyIdAndNodeOrder(
        @Param("applyId") Long applyId,
        @Param("nodeOrder") Integer nodeOrder
    );
}
