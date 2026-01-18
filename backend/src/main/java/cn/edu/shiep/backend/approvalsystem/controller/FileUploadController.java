package cn.edu.shiep.backend.approvalsystem.controller;

import cn.edu.shiep.backend.approvalsystem.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // 上传文件
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件为空");
            }

            String fileUrl = fileStorageService.storeFile(file);
            return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
                put("url", fileUrl);
                put("fileName", file.getOriginalFilename());
            }});
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        }
    }

    // 下载文件（通过路径参数）
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) {
        try {
            // 从路径中提取文件名（例如：/uploads/xxx.pdf -> xxx.pdf）
            String fileName = path;
            if (path.contains("/")) {
                fileName = path.substring(path.lastIndexOf("/") + 1);
            }
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
