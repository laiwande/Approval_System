package cn.edu.shiep.backend.approvalsystem.dto.request;

import cn.edu.shiep.backend.approvalsystem.enums.ApplyType;
import lombok.Data;

import java.util.List;

@Data
public class ApprovalProcessRequest {
    private String processName;
    private ApplyType applyType;
    private String remark;
    private List<ApprovalNodeRequest> nodes;
    
    @Data
    public static class ApprovalNodeRequest {
        private Integer nodeOrder;
        private Long postId;  // 岗位ID（可选，与userId二选一）
        private Long userId;  // 用户ID（可选，与postId二选一）
        private String remark;
    }
}
