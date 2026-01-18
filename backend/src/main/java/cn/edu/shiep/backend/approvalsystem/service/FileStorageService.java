package cn.edu.shiep.backend.approvalsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 保存上传的文件
     * @param file 上传的文件
     * @return 文件的访问URL
     */
    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 返回文件访问URL（相对路径）
        return "/uploads/" + fileName;
    }

    /**
     * 获取文件的完整路径
     */
    public Path loadFileAsPath(String fileName) {
        return Paths.get(uploadDir).resolve(fileName);
    }
}
