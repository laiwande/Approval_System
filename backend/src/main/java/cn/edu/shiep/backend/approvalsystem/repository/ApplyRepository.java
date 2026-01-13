package cn.edu.shiep.backend.approvalsystem.repository;

import cn.edu.shiep.backend.approvalsystem.entity.Apply;
import cn.edu.shiep.backend.approvalsystem.enums.ApplyStatus;
import cn.edu.shiep.backend.approvalsystem.enums.ApplyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplyRepository extends JpaRepository<Apply, Long> {
    List<Apply> findByApplicantId(Long applicantId);
    
    List<Apply> findByApplicantIdAndStatus(Long applicantId, ApplyStatus status);
    
    List<Apply> findByStatus(ApplyStatus status);
    
    List<Apply> findByApplyType(ApplyType applyType);
    
    List<Apply> findByApplyTypeAndStatus(ApplyType applyType, ApplyStatus status);
    
    @Query("SELECT a FROM Apply a WHERE a.applicantId = :applicantId AND a.createTime BETWEEN :startTime AND :endTime")
    List<Apply> findByApplicantIdAndCreateTimeBetween(
        @Param("applicantId") Long applicantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    long countByApplicantIdAndStatus(Long applicantId, ApplyStatus status);
    
    long countByStatus(ApplyStatus status);
}
