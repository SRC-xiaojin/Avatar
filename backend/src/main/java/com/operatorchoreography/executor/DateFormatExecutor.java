package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期格式化执行器
 * 日期时间格式转换
 */
@Slf4j
@Component
public class DateFormatExecutor implements BaseExecutor {

    public ExecutorResult format(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行日期格式化: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            String inputDate = (String) inputData.get("inputDate");
            String inputFormat = (String) inputData.get("inputFormat");
            String outputFormat = (String) inputData.get("outputFormat");
            
            // 默认格式
            if (inputFormat == null) inputFormat = "yyyy-MM-dd HH:mm:ss";
            if (outputFormat == null) outputFormat = "yyyy-MM-dd";
            
            log.debug("日期格式化参数: inputDate={}, inputFormat={}, outputFormat={}", 
                    inputDate, inputFormat, outputFormat);
            
            String formattedDate;
            boolean usedCurrentTime = false;
            
            if (inputDate == null || inputDate.trim().isEmpty()) {
                // 如果没有输入日期，使用当前时间
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
                formattedDate = now.format(outputFormatter);
                usedCurrentTime = true;
                log.debug("使用当前时间进行格式化");
            } else {
                // 解析输入日期并格式化
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inputFormat);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
                
                LocalDateTime dateTime = LocalDateTime.parse(inputDate, inputFormatter);
                formattedDate = dateTime.format(outputFormatter);
                log.debug("解析输入日期并格式化: {} -> {}", inputDate, formattedDate);
            }
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("formattedDate", formattedDate);
            outputData.put("originalDate", inputDate);
            outputData.put("inputFormat", inputFormat);
            outputData.put("outputFormat", outputFormat);
            outputData.put("message", "日期格式化执行成功");
            
            log.info("日期格式化成功: templateId={}, 格式化后: {}", templateId, formattedDate);
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("日期格式化执行成功")
                                .withMetadata("inputFormat", inputFormat)
                                .withMetadata("outputFormat", outputFormat)
                                .withMetadata("usedCurrentTime", usedCurrentTime)
                                .withMetadata("formattedDate", formattedDate);
            
        } catch (DateTimeParseException e) {
            log.error("日期解析失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("日期解析失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("日期解析失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("日期格式化执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("日期格式化执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("日期格式化执行失败: " + e.getMessage());
        }
    }
} 