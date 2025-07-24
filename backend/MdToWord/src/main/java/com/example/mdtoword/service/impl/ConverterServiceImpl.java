package com.example.mdtoword.service.impl;

import com.example.mdtoword.service.ConverterService;
import com.example.mdtoword.util.MarkdownToWordUtil;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Service
public class ConverterServiceImpl implements ConverterService {
    private static final Logger logger = LoggerFactory.getLogger(ConverterServiceImpl.class);

    private final MarkdownToWordUtil markdownToWordUtil;

    public ConverterServiceImpl(MarkdownToWordUtil markdownToWordUtil) {
        this.markdownToWordUtil = markdownToWordUtil;
    }

    @Override
    public byte[] convertMarkdownToWord(String markdownContent) {
        try {
            logger.info("开始转换Markdown内容，长度: {}", markdownContent.length());

            // 直接使用Deepoove POI进行转换
            byte[] wordBytes = markdownToWordUtil.convertMarkdownToWordBytes(markdownContent);

            logger.info("Markdown转换成功，生成字节数组长度: {}", wordBytes.length);
            return wordBytes;

        } catch (IOException e) {
            logger.error("转换过程中发生IO异常: {}", e.getMessage(), e);
            throw new RuntimeException("转换失败: 模板文件读取错误", e);
        } catch (Exception e) {
            logger.error("转换过程中发生未知异常: {}", e.getMessage(), e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }
}
