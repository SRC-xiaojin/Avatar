package com.operatorchoreography.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * JSON生成执行器
 * 将对象转换为JSON字符串
 */
@Slf4j
@Component
public class JsonStringifyExecutor implements BaseExecutor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExecutorResult stringify(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行JSON序列化: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            Object input = inputData.get("inputData");
            
            if (input == null) {
                log.error("输入数据为空: templateId={}", templateId);
                throw new IllegalArgumentException("输入数据不能为空");
            }
            
            // 将对象转换为JSON字符串
            String jsonString = objectMapper.writeValueAsString(input);
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("jsonString", jsonString);
            outputData.put("message", "JSON生成成功");
            
            log.info("JSON序列化成功: templateId={}, 输出长度={}", templateId, jsonString.length());
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("JSON序列化成功");
            
        } catch (Exception e) {
            log.error("JSON序列化失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("JSON生成失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("JSON序列化失败: " + e.getMessage());
        }
    }
} 