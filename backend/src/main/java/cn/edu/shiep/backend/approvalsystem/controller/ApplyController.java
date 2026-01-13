package cn.edu.shiep.backend.approvalsystem.controller;

import cn.edu.shiep.backend.approvalsystem.dto.ApplyDTO;
import cn.edu.shiep.backend.approvalsystem.dto.request.ApplyRequest;
import cn.edu.shiep.backend.approvalsystem.security.services.UserDetailsImpl;
import cn.edu.shiep.backend.approvalsystem.service.ApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applies")
public class ApplyController {

    @Autowired
    private ApplyService applyService;

    // 创建申请（草稿）
    @PostMapping
    public ResponseEntity<ApplyDTO> createApply(@RequestBody ApplyRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        Long userId = userDetailsImpl.getId();

        ApplyDTO response = applyService.createApply(request, userId);
        return ResponseEntity.ok(response);
    }

    // 提交申请
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApplyDTO> submitApply(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        Long userId = userDetailsImpl.getId();

        ApplyDTO response = applyService.submitApply(id, userId);
        return ResponseEntity.ok(response);
    }

    // 撤回申请
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdrawApply(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        Long userId = userDetailsImpl.getId();

        applyService.withdrawApply(id, userId);
        return ResponseEntity.noContent().build();
    }

    // 获取我的申请列表
    @GetMapping("/my")
    public ResponseEntity<List<ApplyDTO>> getMyApplies(@AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        Long userId = userDetailsImpl.getId();

        List<ApplyDTO> applies = applyService.getMyApplies(userId);
        return ResponseEntity.ok(applies);
    }

    // 获取申请详情
    @GetMapping("/{id}")
    public ResponseEntity<ApplyDTO> getApplyDetail(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        Long userId = userDetailsImpl.getId();

        ApplyDTO apply = applyService.getApplyDetail(id, userId);
        return ResponseEntity.ok(apply);
    }
}
