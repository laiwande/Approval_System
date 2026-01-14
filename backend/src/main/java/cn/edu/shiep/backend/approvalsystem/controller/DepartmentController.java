package cn.edu.shiep.backend.approvalsystem.controller;

import cn.edu.shiep.backend.approvalsystem.entity.Department;
import cn.edu.shiep.backend.approvalsystem.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/departments")
public class DepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentRepository.findByDelFlag("0");
        return ResponseEntity.ok(departments);
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        department.setDelFlag("0");
        department.setCreateTime(LocalDateTime.now());
        if (department.getStatus() == null) {
            department.setStatus("0");
        }
        Department saved = departmentRepository.save(department);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{deptId}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long deptId) {
        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("部门不存在"));
        department.setDelFlag("2");
        departmentRepository.save(department);
        return ResponseEntity.ok("部门已删除");
    }
}
