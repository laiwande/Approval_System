package cn.edu.shiep.backend.approvalsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    // 我的申请统计
    private long myTotalApplies;        // 我的申请总数
    private long myPendingApplies;      // 我的待审批申请数
    private long myApprovedApplies;    // 我的已批准申请数
    private long myRejectedApplies;    // 我的已拒绝申请数
    private long myDraftApplies;       // 我的草稿数
    
    // 审批任务统计（审批人）
    private long myPendingTasks;      // 我的待办任务数
    private long myProcessedTasks;     // 我的已处理任务数
    
    // 系统统计（管理员）
    private long totalApplies;         // 系统总申请数
    private long totalPendingApplies;   // 系统待审批数
}