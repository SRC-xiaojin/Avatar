package com.operatorchoreography.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * JSON解析执行器
 * 解析JSON字符串为对象
 */
@Slf4j
@Component
public class JsonParseExecutor implements BaseExecutor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExecutorResult parse(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行JSON解析: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            String jsonString = (String) inputData.get("jsonString");
            
            if (jsonString == null || jsonString.trim().isEmpty()) {
                log.error("JSON字符串为空: templateId={}", templateId);
                throw new IllegalArgumentException("JSON字符串不能为空");
            }
            
            log.debug("待解析的JSON字符串长度: {}", jsonString.length());
            
            // 解析JSON字符串
            Object parsedData = objectMapper.readValue(jsonString, Object.class);
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("parsedData", parsedData);
            outputData.put("message", "JSON解析成功");
            
            log.info("JSON解析成功: templateId={}", templateId);
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("JSON解析成功");
            
        } catch (Exception e) {
            log.error("JSON解析失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("JSON解析失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("JSON解析失败: " + e.getMessage());
        }
    }
} 