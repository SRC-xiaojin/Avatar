package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.time.LocalDateTime;

/**
 * 字符串格式化执行器
 * 格式化字符串内容
 */
@Slf4j
@Component
public class StringFormatExecutor implements BaseExecutor {

    public ExecutorResult format(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行字符串格式化: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            String inputString = (String) inputData.get("inputString");
            Map<String, Object> formatData = (Map<String, Object>) inputData.get("formatData");
            String formatType = (String) inputData.getOrDefault("formatType", "template");
            
            if (inputString == null) {
                log.error("输入字符串为空: templateId={}", templateId);
                throw new IllegalArgumentException("输入字符串不能为空");
            }
            
            log.debug("格式化类型: {}, 输入字符串长度: {}", formatType, inputString.length());
            
            String formattedString;
            
            switch (formatType) {
                case "template":
                    // 模板格式化，替换 ${key} 形式的占位符
                    formattedString = formatTemplate(inputString, formatData);
                    break;
                case "printf":
                    // Printf风格格式化
                    formattedString = formatPrintf(inputString, formatData);
                    break;
                case "trim":
                    // 去除空格
                    formattedString = inputString.trim();
                    break;
                case "upper":
                    // 转大写
                    formattedString = inputString.toUpperCase();
                    break;
                case "lower":
                    // 转小写
                    formattedString = inputString.toLowerCase();
                    break;
                default:
                    formattedString = inputString;
            }
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("formattedString", formattedString);
            outputData.put("originalString", inputString);
            outputData.put("formatType", formatType);
            outputData.put("message", "字符串格式化成功");
            
            log.info("字符串格式化成功: templateId={}, 格式化类型={}, 输出长度={}", 
                    templateId, formatType, formattedString.length());
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("字符串格式化成功")
                                .withMetadata("formatType", formatType)
                                .withMetadata("originalLength", inputString.length())
                                .withMetadata("formattedLength", formattedString.length());
            
        } catch (Exception e) {
            log.error("字符串格式化失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("字符串格式化失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("字符串格式化失败: " + e.getMessage());
        }
    }
    
    /**
     * 模板格式化，替换 ${key} 占位符
     */
    private String formatTemplate(String template, Map<String, Object> data) {
        if (data == null) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
    
    /**
     * Printf风格格式化
     */
    private String formatPrintf(String format, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return format;
        }
        
        Object[] values = data.values().toArray();
        return String.format(format, values);
    }
} 