package cn.edu.shiep.backend.approvalsystem.service;

import cn.edu.shiep.backend.approvalsystem.dto.request.ApprovalProcessRequest;
import cn.edu.shiep.backend.approvalsystem.entity.ApprovalNode;
import cn.edu.shiep.backend.approvalsystem.entity.ApprovalProcess;
import cn.edu.shiep.backend.approvalsystem.repository.ApprovalNodeRepository;
import cn.edu.shiep.backend.approvalsystem.repository.ApprovalProcessRepository;
import cn.edu.shiep.backend.approvalsystem.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApprovalProcessService {

    @Autowired
    private ApprovalProcessRepository approvalProcessRepository;

    @Autowired
    private ApprovalNodeRepository approvalNodeRepository;

    @Autowired
    private PostRepository postRepository;

    // 创建审批流程
    @Transactional
    public ApprovalProcess createProcess(ApprovalProcessRequest request) {
        ApprovalProcess process = new ApprovalProcess();
        process.setProcessName(request.getProcessName());
        process.setApplyType(request.getApplyType());
        process.setStatus("0");
        process.setRemark(request.getRemark());

        ApprovalProcess savedProcess = approvalProcessRepository.save(process);

        // 创建审批节点
        if (request.getNodes() != null && !request.getNodes().isEmpty()) {
            for (ApprovalProcessRequest.ApprovalNodeRequest nodeRequest : request.getNodes()) {
                // 必须指定 userId
                if (nodeRequest.getUserId() == null) {
                    throw new IllegalArgumentException("审批节点必须指定审批人(userId)");
                }
                ApprovalNode node = new ApprovalNode();
                node.setProcessId(savedProcess.getProcessId());
                node.setNodeOrder(nodeRequest.getNodeOrder());
                node.setPostId(null); // 不再使用岗位
                node.setUserId(nodeRequest.getUserId());
                node.setRemark(nodeRequest.getRemark());
                approvalNodeRepository.save(node);
            }
        }

        return savedProcess;
    }

    // 获取所有审批流程
    public List<ApprovalProcess> getAllProcesses() {
        return approvalProcessRepository.findAll();
    }

    // 获取指定类型的审批流程
    public List<ApprovalProcess> getProcessesByType(cn.edu.shiep.backend.approvalsystem.enums.ApplyType applyType) {
        return approvalProcessRepository.findByApplyType(applyType);
    }

    // 获取指定类型和状态的审批流程
    public List<ApprovalProcess> getProcessesByTypeAndStatus(cn.edu.shiep.backend.approvalsystem.enums.ApplyType applyType, String status) {
        return approvalProcessRepository.findByApplyTypeAndStatus(applyType, status);
    }

    // 根据ID获取审批流程详情（包含节点）
    public ApprovalProcess getProcessById(Long processId) {
        ApprovalProcess process = approvalProcessRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("审批流程不存在"));
        // 加载节点信息
        process.setNodes(approvalNodeRepository.findByProcessIdOrderByNodeOrderAsc(processId));
        return process;
    }

    // 更新审批流程
    @Transactional
    public ApprovalProcess updateProcess(Long processId, ApprovalProcessRequest request) {
        ApprovalProcess process = approvalProcessRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("审批流程不存在"));
        
        process.setProcessName(request.getProcessName());
        process.setApplyType(request.getApplyType());
        process.setRemark(request.getRemark());
        
        ApprovalProcess savedProcess = approvalProcessRepository.save(process);

        // 删除旧节点
        List<ApprovalNode> oldNodes = approvalNodeRepository.findByProcessId(processId);
        approvalNodeRepository.deleteAll(oldNodes);

        // 创建新节点
        if (request.getNodes() != null && !request.getNodes().isEmpty()) {
            for (ApprovalProcessRequest.ApprovalNodeRequest nodeRequest : request.getNodes()) {
                // 必须指定 userId
                if (nodeRequest.getUserId() == null) {
                    throw new IllegalArgumentException("审批节点必须指定审批人(userId)");
                }
                ApprovalNode node = new ApprovalNode();
                node.setProcessId(savedProcess.getProcessId());
                node.setNodeOrder(nodeRequest.getNodeOrder());
                node.setPostId(null); // 不再使用岗位
                node.setUserId(nodeRequest.getUserId());
                node.setRemark(nodeRequest.getRemark());
                approvalNodeRepository.save(node);
            }
        }

        // 重新加载节点信息
        savedProcess.setNodes(approvalNodeRepository.findByProcessIdOrderByNodeOrderAsc(processId));
        return savedProcess;
    }
}
