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
                ApprovalNode node = new ApprovalNode();
                node.setProcessId(savedProcess.getProcessId());
                node.setNodeOrder(nodeRequest.getNodeOrder());
                node.setPostId(nodeRequest.getPostId());
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
}
