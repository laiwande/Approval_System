package cn.edu.shiep.backend.approvalsystem.repository;

import cn.edu.shiep.backend.approvalsystem.entity.ReimburseApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReimburseApplyRepository extends JpaRepository<ReimburseApply, Long> {
    List<ReimburseApply> findByExpenseType(String expenseType);
    
    @Query("SELECT ra FROM ReimburseApply ra WHERE ra.apply.applicantId = :applicantId AND ra.apply.createTime BETWEEN :startTime AND :endTime")
    List<ReimburseApply> findByApplicantIdAndTimeRange(
        @Param("applicantId") Long applicantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT SUM(ra.amount) FROM ReimburseApply ra WHERE ra.apply.applicantId = :applicantId AND ra.apply.createTime BETWEEN :startTime AND :endTime")
    BigDecimal sumAmountByApplicantIdAndTimeRange(
        @Param("applicantId") Long applicantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
