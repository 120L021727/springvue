package com.example.mdtoword.controller;

import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.service.UserService;
import com.example.mdtoword.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Value("${file.upload.path:./uploads/avatars/}")
    private String uploadPath;

    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    public ResponseEntity<Result<String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Result.unauthorized("用户未登录"));
            }

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