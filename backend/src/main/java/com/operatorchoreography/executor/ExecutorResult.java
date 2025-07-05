package com.operatorchoreography.executor;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * 执行器执行结果
 * 
 * @author OperatorChoreography
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorResult {
    
    /**
     * 执行状态
     */
    private ExecutorStatus status;
    
    /**
     * 执行是否成功
     */
    private boolean success;
    
    /**
     * 输出数据
     */
    private Map<String, Object> outputData;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 执行开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 执行结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long executionTimeMs;
    
    /**
     * 节点ID
     */
    private Long nodeId;
    
    /**
     * 节点名称
     */
    private String nodeName;
    
    /**
     * 模板ID
     */
    private Long templateId;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 执行器类名
     */
    private String executorClass;
    
    /**
     * 执行器方法名
     */
    private String executorMethod;
    
    /**
     * 执行日志
     */
    private String executionLog;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> metadata;
    
    /**
     * 创建成功结果
     */
    public static ExecutorResult success(Map<String, Object> outputData) {
        return ExecutorResult.builder()
                .status(ExecutorStatus.SUCCESS)
                .success(true)
                .outputData(outputData != null ? outputData : new HashMap<>())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .executionTimeMs(0L)
                .metadata(new HashMap<>())
                .build();
    }
    
    /**
     * 创建成功结果（带耗时）
     */
    public static ExecutorResult success(Map<String, Object> outputData, LocalDateTime startTime, LocalDateTime endTime) {
        long executionTime = java.time.Duration.between(startTime, endTime).toMillis();
        return ExecutorResult.builder()
                .status(ExecutorStatus.SUCCESS)
                .success(true)
                .outputData(outputData != null ? outputData : new HashMap<>())
                .startTime(startTime)
                .endTime(endTime)
                .executionTimeMs(executionTime)
                .metadata(new HashMap<>())
                .build();
    }
    
    /**
     * 创建失败结果
     */
    public static ExecutorResult failure(String errorMessage) {
        return ExecutorResult.builder()
                .status(ExecutorStatus.FAILED)
                .success(false)
                .outputData(new HashMap<>())
                .errorMessage(errorMessage)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .executionTimeMs(0L)
                .metadata(new HashMap<>())
                .build();
    }
    
    /**
     * 创建失败结果（带错误代码）
     */
    public static ExecutorResult failure(String errorCode, String errorMessage) {
        return ExecutorResult.builder()
                .status(ExecutorStatus.FAILED)
                .success(false)
                .outputData(new HashMap<>())
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .executionTimeMs(0L)
                .metadata(new HashMap<>())
                .build();
    }
    
    /**
     * 创建失败结果（带耗时）
     */
    public static ExecutorResult failure(String errorMessage, LocalDateTime startTime, LocalDateTime endTime) {
        long executionTime = java.time.Duration.between(startTime, endTime).toMillis();
        return ExecutorResult.builder()
                .status(ExecutorStatus.FAILED)
                .success(false)
                .outputData(new HashMap<>())
                .errorMessage(errorMessage)
                .startTime(startTime)
                .endTime(endTime)
                .executionTimeMs(executionTime)
                .metadata(new HashMap<>())
                .build();
    }
    
    /**
     * 创建失败结果（带异常）
     */
    public static ExecutorResult failure(String errorMessage, Exception exception) {
        return ExecutorResult.builder()
                .status(ExecutorStatus.FAILED)
                .success(false)
                .outputData(new HashMap<>())
                .errorMessage(errorMessage + ": " + exception.getMessage())
                .errorCode(exception.getClass().getSimpleName())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .executionTimeMs(0L)
                .metadata(new HashMap<>())
                .build();
    }
    
    /**
     * 创建超时结果
     */
    public static ExecutorResult timeout(LocalDateTime startTime, LocalDateTime endTime) {
        long executionTime = java.time.Duration.between(startTime, endTime).toMillis();
        return ExecutorResult.builder()
                .status(ExecutorStatus.TIMEOUT)
                .success(false)
                .outputData(new HashMap<>())
                .errorMessage("执行超时")
                .errorCode("TIMEOUT")
                .startTime(startTime)
                .endTime(endTime)
                .executionTimeMs(executionTime)
                .metadata(new HashMap<>())
                .build();
    }
    
    /**
     * 创建取消结果
     */
    public static ExecutorResult cancelled() {
        return ExecutorResult.builder()
                .status(ExecutorStatus.CANCELLED)
                .success(false)
                .outputData(new HashMap<>())
                .errorMessage("执行被取消")
                .errorCode("CANCELLED")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .executionTimeMs(0L)
                .metadata(new HashMap<>())
                .build();
    }
    
    /**
     * 设置节点信息
     */
    public ExecutorResult withNodeInfo(Long nodeId, String nodeName) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        return this;
    }
    
    /**
     * 设置模板信息
     */
    public ExecutorResult withTemplateInfo(Long templateId, String templateName) {
        this.templateId = templateId;
        this.templateName = templateName;
        return this;
    }
    
    /**
     * 设置执行器信息
     */
    public ExecutorResult withExecutorInfo(String executorClass, String executorMethod) {
        this.executorClass = executorClass;
        this.executorMethod = executorMethod;
        return this;
    }
    
    /**
     * 添加执行日志
     */
    public ExecutorResult withLog(String log) {
        this.executionLog = log;
        return this;
    }
    
    /**
     * 添加元数据
     */
    public ExecutorResult withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
    
    /**
     * 添加输出数据
     */
    public ExecutorResult withOutputData(String key, Object value) {
        if (this.outputData == null) {
            this.outputData = new HashMap<>();
        }
        this.outputData.put(key, value);
        return this;
    }
    
    /**
     * 转换为Map格式（向后兼容）
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", this.status != null ? this.status.name() : "UNKNOWN");
        result.put("success", this.success);
        result.put("outputData", this.outputData != null ? this.outputData : new HashMap<>());
        result.put("errorMessage", this.errorMessage);
        result.put("errorCode", this.errorCode);
        result.put("startTime", this.startTime);
        result.put("endTime", this.endTime);
        result.put("executionTimeMs", this.executionTimeMs);
        result.put("nodeId", this.nodeId);
        result.put("nodeName", this.nodeName);
        result.put("templateId", this.templateId);
        result.put("templateName", this.templateName);
        result.put("executorClass", this.executorClass);
        result.put("executorMethod", this.executorMethod);
        result.put("executionLog", this.executionLog);
        result.put("metadata", this.metadata != null ? this.metadata : new HashMap<>());
        return result;
    }
    
    /**
     * 从Map创建ExecutorResult（向后兼容）
     */
    public static ExecutorResult fromMap(Map<String, Object> map) {
        if (map == null) {
            return ExecutorResult.failure("Invalid input data");
        }
        
        ExecutorResult.ExecutorResultBuilder builder = ExecutorResult.builder();
        
        // 解析状态
        Object status = map.get("status");
        if (status instanceof String) {
            try {
                builder.status(ExecutorStatus.valueOf((String) status));
                builder.success("SUCCESS".equals(status));
            } catch (IllegalArgumentException e) {
                builder.status(ExecutorStatus.FAILED);
                builder.success(false);
            }
        }
        
        // 解析其他字段
        builder.outputData((Map<String, Object>) map.get("outputData"));
        builder.errorMessage((String) map.get("errorMessage"));
        builder.errorCode((String) map.get("errorCode"));
        builder.startTime((LocalDateTime) map.get("startTime"));
        builder.endTime((LocalDateTime) map.get("endTime"));
        builder.executionTimeMs((Long) map.get("executionTimeMs"));
        builder.nodeId((Long) map.get("nodeId"));
        builder.nodeName((String) map.get("nodeName"));
        builder.templateId((Long) map.get("templateId"));
        builder.templateName((String) map.get("templateName"));
        builder.executorClass((String) map.get("executorClass"));
        builder.executorMethod((String) map.get("executorMethod"));
        builder.executionLog((String) map.get("executionLog"));
        builder.metadata((Map<String, Object>) map.get("metadata"));
        
        return builder.build();
    }
} 