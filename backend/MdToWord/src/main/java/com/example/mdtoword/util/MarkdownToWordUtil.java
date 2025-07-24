package com.example.mdtoword.util;


import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.style.*;
import com.deepoove.poi.plugin.markdown.MarkdownRenderData;
import com.deepoove.poi.plugin.markdown.MarkdownRenderPolicy;
import com.deepoove.poi.plugin.markdown.MarkdownStyle;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class MarkdownToWordUtil {

    /**
     * 将Markdown内容转换为Word字节数组
     *
     * @param markdownContent Markdown内容
     * @return Word文档字节数组
     */
    public byte[] convertMarkdownToWordBytes(String markdownContent) throws IOException {
        MarkdownRenderData markdownData = new MarkdownRenderData();
        markdownData.setMarkdown(markdownContent);

        // 设置Markdown样式
        MarkdownStyle style = createMarkdownStyle();
        markdownData.setStyle(style);

        // 绑定数据
        Map<String, Object> data = new HashMap<>();
        data.put("md", markdownData);

        // 配置渲染策略
        Configure config = Configure.builder()
                .bind("md", new MarkdownRenderPolicy())
                .build();

        // 使用模板进行渲染
        ClassPathResource resource = new ClassPathResource("templates/markdown_template.docx");

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XWPFTemplate.compile(resource.getInputStream(), config)
                    .render(data)
                    .write(outputStream);

            return outputStream.toByteArray();
        }
    }

    /**
     * 创建Markdown样式配置
     *
     * @return Markdown样式对象
     */
    private MarkdownStyle createMarkdownStyle() {
        MarkdownStyle style = MarkdownStyle.newStyle();

        // 不显示标题编号
        style.setShowHeaderNumber(false);

        // 设置表格头部样式
        RowStyle headerStyle = new RowStyle();
        CellStyle cellStyle = new CellStyle();
        cellStyle.setBackgroundColor("cccccc"); // 灰色背景

        Style textStyle = new Style();
        textStyle.setColor("000000"); // 黑色文字
        textStyle.setBold(true); // 加粗
        textStyle.setFontSize(12); // 字体大小12

        cellStyle.setVertAlign(XWPFTableCell.XWPFVertAlign.CENTER); // 垂直居中
        cellStyle.setDefaultParagraphStyle(ParagraphStyle.builder().withDefaultTextStyle(textStyle).build());
        headerStyle.setDefaultCellStyle(cellStyle);
        style.setTableHeaderStyle(headerStyle);

        // 设置表格边框样式
        BorderStyle borderStyle = new BorderStyle();
        borderStyle.setColor("000000"); // 黑色边框
        borderStyle.setSize(3); // 边框大小
        borderStyle.setType(XWPFTable.XWPFBorderType.SINGLE); // 实线边框
        style.setTableBorderStyle(borderStyle);

        // 设置引用样式
        ParagraphStyle quoteStyle = new ParagraphStyle();
        quoteStyle.setSpacingBeforeLines(0.5d);
        quoteStyle.setSpacingAfterLines(0.5d);

        Style quoteTextStyle = new Style();
        quoteTextStyle.setColor("000000");
        quoteTextStyle.setFontSize(8);
        quoteTextStyle.setItalic(true);
        quoteStyle.setDefaultTextStyle(quoteTextStyle);
        style.setQuoteStyle(quoteStyle);

        return style;
    }
}
