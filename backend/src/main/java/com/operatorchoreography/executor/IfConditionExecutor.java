package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * 条件分支执行器
 * 根据条件进行分支处理
 */
@Slf4j
@Component
public class IfConditionExecutor implements BaseExecutor {

    public ExecutorResult evaluate(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行条件分支: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 获取条件表达式和相关数据
            String condition = (String) inputData.get("condition");
            Object conditionData = inputData.get("inputData");
            Object trueValue = inputData.get("trueValue");
            Object falseValue = inputData.get("falseValue");
            
            if (condition == null) {
                log.error("条件表达式为空: templateId={}", templateId);
                throw new IllegalArgumentException("条件表达式不能为空");
            }
            
            log.debug("条件分支参数: condition={}, conditionData={}, trueValue={}, falseValue={}", 
                    condition, conditionData, trueValue, falseValue);
            
            boolean conditionResult = evaluateCondition(condition, conditionData);
            Object outputResult = conditionResult ? trueValue : falseValue;
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("result", outputResult);
            outputData.put("conditionResult", conditionResult);
            outputData.put("condition", condition);
            outputData.put("message", "条件分支执行成功");
            
            log.info("条件分支执行成功: templateId={}, 条件结果={}, 输出值={}", 
                    templateId, conditionResult, outputResult);
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("条件分支执行成功")
                                .withMetadata("condition", condition)
                                .withMetadata("conditionResult", conditionResult)
                                .withMetadata("selectedBranch", conditionResult ? "true" : "false");
            
        } catch (Exception e) {
            log.error("条件分支执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("条件分支执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("条件分支执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, Object data) {
        if (condition == null || data == null) {
            return false;
        }
        
        // 简单的条件评估逻辑，实际项目中可以使用表达式引擎如SpEL
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            
            // 支持简单的比较操作
            if (condition.contains("==")) {
                String[] parts = condition.split("==");
                String field = parts[0].trim();
                String value = parts[1].trim().replace("'", "").replace("\"", "");
                return value.equals(String.valueOf(dataMap.get(field)));
            } else if (condition.contains(">")) {
                String[] parts = condition.split(">");
                String field = parts[0].trim();
                double value = Double.parseDouble(parts[1].trim());
                Object fieldValue = dataMap.get(field);
                return fieldValue instanceof Number && ((Number) fieldValue).doubleValue() > value;
            } else if (condition.contains("<")) {
                String[] parts = condition.split("<");
                String field = parts[0].trim();
                double value = Double.parseDouble(parts[1].trim());
                Object fieldValue = dataMap.get(field);
                return fieldValue instanceof Number && ((Number) fieldValue).doubleValue() < value;
            }
        }
        
        return false;
    }
} 