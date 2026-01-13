package cn.edu.shiep.backend.approvalsystem.repository;

import cn.edu.shiep.backend.approvalsystem.entity.LeaveApply;
import cn.edu.shiep.backend.approvalsystem.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveApplyRepository extends JpaRepository<LeaveApply, Long> {
    List<LeaveApply> findByLeaveType(LeaveType leaveType);
    
    @Query("SELECT la FROM LeaveApply la WHERE la.startTime <= :endTime AND la.endTime >= :startTime")
    List<LeaveApply> findOverlappingLeaves(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT la FROM LeaveApply la WHERE la.apply.applicantId = :applicantId AND la.startTime BETWEEN :startTime AND :endTime")
    List<LeaveApply> findByApplicantIdAndTimeRange(
        @Param("applicantId") Long applicantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
