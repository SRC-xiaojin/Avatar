package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * HTTP请求执行器
 * 发送HTTP请求调用外部服务
 */
@Slf4j
@Component
public class HttpRequestExecutor implements BaseExecutor {

    private final RestTemplate restTemplate = new RestTemplate();

    public ExecutorResult request(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行HTTP请求: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            String url = (String) inputData.get("request_url");
            String method = (String) inputData.getOrDefault("request_method", "GET");
            Map<String, String> headers = (Map<String, String>) inputData.get("request_headers");
            Object requestBody = inputData.get("request_body");
            
            if (url == null || url.trim().isEmpty()) {
                log.error("请求URL为空: templateId={}", templateId);
                throw new IllegalArgumentException("请求URL不能为空");
            }
            
            log.info("HTTP请求详情: templateId={}, url={}, method={}", templateId, url, method);
            log.debug("请求头: {}", headers);
            log.debug("请求体: {}", requestBody);
            
            // 构建请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }
            
            // 构建请求实体
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, httpHeaders);
            
            // 发送请求
            ResponseEntity<Object> response;
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            
            response = restTemplate.exchange(url, httpMethod, requestEntity, Object.class);
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("response", response.getBody());
            outputData.put("statusCode", response.getStatusCodeValue());
            outputData.put("headers", response.getHeaders().toSingleValueMap());
            outputData.put("message", "HTTP请求执行成功");
            
            log.info("HTTP请求执行成功: templateId={}, url={}, statusCode={}", 
                    templateId, url, response.getStatusCodeValue());
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("HTTP请求执行成功")
                                .withMetadata("url", url)
                                .withMetadata("method", method)
                                .withMetadata("statusCode", response.getStatusCodeValue())
                                .withMetadata("responseSize", response.getBody() != null ? response.getBody().toString().length() : 0);
            
        } catch (Exception e) {
            log.error("HTTP请求执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("HTTP请求执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("HTTP请求执行失败: " + e.getMessage());
        }
    }
} 