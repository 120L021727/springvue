package com.example.mdtoword.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传工具类
 * 处理头像文件的上传和存储
 * 
 * @author 坤坤
 * @since 2025-08-09
 */
@Component
public class FileUploadUtil {

    @Value("${file.upload.path:./uploads/avatars/}")
    private String uploadPath;

    @Value("${file.upload.max-size:104857600}")
    private long maxSize; // 100MB

    // 富文本图片上传基础目录
    @Value("${file.rte-upload.path:./uploads/rte/}")
    private String rteUploadBasePath;

    /**
     * 上传头像文件
     * 
     * @param file 上传的文件
     * @return 文件访问URL
     * @throws IOException 文件操作异常
     */
    public String uploadAvatar(MultipartFile file) throws IOException {
        // 1. 简单验证文件
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 2. 检查文件大小（100MB）
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过100MB");
        }

        // 3. 创建上传目录
        createUploadDirectory();

        // 4. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;

        // 5. 保存文件
        Path filePath = Paths.get(uploadPath, fileName);
        Files.copy(file.getInputStream(), filePath);

        // 6. 返回文件访问路径
        return "/api/file/avatar/" + fileName;
    }

    /**
     * 上传富文本图片
     * 目录：./uploads/rte/yyyy/MM/uuid.ext
     * 返回：/api/file/rte/yyyy/MM/uuid.ext
     */
    public String uploadRteImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过100MB");
        }

        java.time.LocalDate now = java.time.LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = java.util.UUID.randomUUID().toString() + "." + fileExtension;

        // 目标目录
        Path dir = Paths.get(rteUploadBasePath, year, month);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path path = dir.resolve(fileName);
        Files.copy(file.getInputStream(), path);

        return String.format("/api/file/rte/%s/%s/%s", year, month, fileName);
    }

    /**
     * 创建上传目录
     */
    private void createUploadDirectory() throws IOException {
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "jpg"; // 默认扩展名
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
} 