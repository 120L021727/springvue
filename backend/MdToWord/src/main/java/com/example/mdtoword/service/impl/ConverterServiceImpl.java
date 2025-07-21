package com.example.mdtoword.service.impl;

import com.example.mdtoword.service.ConverterService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;


@Service
public class ConverterServiceImpl implements ConverterService {

    @Override
    public byte[] convertMarkdownToWord(String markdownContent) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownContent);

        // 使用Apache POI生成Word文档
        try (XWPFDocument wordDocument = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 将Markdown内容渲染为纯文本
            TextContentRenderer renderer = TextContentRenderer.builder().build();
            String plainText = renderer.render(document);

            // 将纯文本写入Word文档
            wordDocument.createParagraph().createRun().setText(plainText);

            // 将Word文档写入字节数组
            wordDocument.write(outputStream);
            return outputStream.toByteArray();
        }
        catch (Exception e) {
            e.printStackTrace();
            // 处理异常，返回空字节数组或抛出自定义异常
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }
}
