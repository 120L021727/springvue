package com.example.mdtoword.controller;


import com.example.mdtoword.service.ConverterService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/converter")
public class ConverterController {
    @Autowired
    private final ConverterService converterService;


    public ConverterController(ConverterService converterService) {
        this.converterService = converterService;
    }

    @PostMapping("/markdown-to-word")
    public ResponseEntity<byte[]> convertMarkdownToWord(@RequestBody String markdownContent) {
        try {
            // 校验输入
            if (markdownContent == null || markdownContent.trim().isEmpty()) {
                return new ResponseEntity<>("Markdown content cannot be null or empty".getBytes(), HttpStatus.BAD_REQUEST);
            }

            // 调用服务层转换
            byte[] wordFile = converterService.convertMarkdownToWord(markdownContent);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=converted.docx");
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            return new ResponseEntity<>(wordFile, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(("Conversion failed: " + e.getMessage()).getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
