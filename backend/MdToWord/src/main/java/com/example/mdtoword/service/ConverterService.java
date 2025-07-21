package com.example.mdtoword.service;

import org.springframework.stereotype.Service;


public interface ConverterService {
    /**
     * 将Markdown内容转换为Word文件
     * @param markdownContent Markdown文本内容
     * @return Word文件的字节数组
     * @throws Exception 转换过程中可能抛出的异常
     */
    byte[] convertMarkdownToWord(String markdownContent);
}
