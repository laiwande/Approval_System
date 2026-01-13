package cn.edu.shiep.backend.approvalsystem.repository;

import cn.edu.shiep.backend.approvalsystem.entity.ApprovalNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalNodeRepository extends JpaRepository<ApprovalNode, Long> {
    List<ApprovalNode> findByProcessId(Long processId);
    
    List<ApprovalNode> findByProcessIdOrderByNodeOrderAsc(Long processId);
    
    List<ApprovalNode> findByPostId(Long postId);
    
    ApprovalNode findByProcessIdAndNodeOrder(Long processId, Integer nodeOrder);
}
