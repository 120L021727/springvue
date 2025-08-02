package com.example.mdtoword.controller;

import com.example.mdtoword.exception.BusinessException;
import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.service.ConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/converter")
public class ConverterController {
    @Autowired
    private final ConverterService converterService;
    private static final Logger logger = LoggerFactory.getLogger(ConverterController.class);

    public ConverterController(ConverterService converterService) {
        this.converterService = converterService;
    }

    @PostMapping("/markdown-to-word")
    public ResponseEntity<byte[]> convertMarkdownToWord(@RequestBody String markdownContent) {
        // 校验输入
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            logger.warn("收到空的 Markdown 输入");
            throw new BusinessException("Markdown content cannot be null or empty");
        }

        // 调用服务层转换
        byte[] wordFile = converterService.convertMarkdownToWord(markdownContent);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=converted.docx");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        return new ResponseEntity<>(wordFile, headers, HttpStatus.OK);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Result<String>> health() {
        return ResponseEntity.ok(Result.success("Converter service is running"));
    }
}
