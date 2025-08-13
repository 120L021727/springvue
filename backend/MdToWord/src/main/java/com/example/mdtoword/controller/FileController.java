package com.example.mdtoword.controller;

import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.service.UserService;
import com.example.mdtoword.util.FileUploadUtil;
import com.example.mdtoword.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件控制器
 * 处理头像上传和文件访问
 * 
 * @author 坤坤
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired
    private UserService userService;
    
    @Autowired
    private SecurityUtil securityUtil;

    @Value("${file.upload.path:./uploads/avatars/}")
    private String uploadPath;

    @Value("${file.rte-upload.path:./uploads/rte/}")
    private String rteUploadBasePath;

    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    public ResponseEntity<Result<String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 获取当前登录用户
            securityUtil.getCurrentUser(); // 验证用户已登录
            
            // 2. 上传文件
            String avatarUrl = fileUploadUtil.uploadAvatar(file);

            // 3. 更新用户头像信息
            userService.updateAvatar(avatarUrl);

            return ResponseEntity.ok(Result.success(avatarUrl, "头像上传成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Result.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500)
                .body(Result.error("文件上传失败：" + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Result.error("头像上传失败"));
        }
    }

    /**
     * 富文本图片上传
     */
    @PostMapping("/rte-upload")
    public ResponseEntity<?> uploadRteImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileUploadUtil.uploadRteImage(file);
            // 按 TinyMCE 约定返回 { location: url }
            java.util.Map<String, String> body = new java.util.HashMap<>();
            body.put("location", url);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Result.error("文件上传失败：" + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("图片上传失败"));
        }
    }

    /**
     * 访问富文本图片
     */
    @GetMapping("/rte/{year}/{month}/{fileName:.+}")
    public ResponseEntity<Resource> getRteImage(@PathVariable String year, @PathVariable String month, @PathVariable String fileName) {
        try {
            Path filePath = Paths.get(rteUploadBasePath).resolve(year).resolve(month).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType;
                try {
                    contentType = java.nio.file.Files.probeContentType(filePath);
                } catch (IOException ex) {
                    contentType = null;
                }
                if (contentType == null) {
                    String lower = fileName.toLowerCase();
                    if (lower.endsWith(".png")) contentType = MediaType.IMAGE_PNG_VALUE;
                    else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) contentType = MediaType.IMAGE_JPEG_VALUE;
                    else if (lower.endsWith(".gif")) contentType = MediaType.IMAGE_GIF_VALUE;
                    else if (lower.endsWith(".webp")) contentType = "image/webp";
                    else if (lower.endsWith(".svg")) contentType = "image/svg+xml";
                    else contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 访问头像文件
     */
    @GetMapping("/avatar/{fileName:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadPath).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 