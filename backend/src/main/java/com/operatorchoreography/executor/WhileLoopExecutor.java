package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.time.LocalDateTime;

/**
 * WHILE循环执行器
 * 根据条件循环执行
 */
@Slf4j
@Component
public class WhileLoopExecutor implements BaseExecutor {

    private static final int MAX_ITERATIONS = 1000; // 最大循环次数，防止无限循环

    // 由于while是Java关键字，使用executeWhile方法名
    @SuppressWarnings("unchecked")
    public ExecutorResult executeWhile(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行WHILE循环: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            Map<String, Object> conditionConfig = (Map<String, Object>) inputData.get("condition");
            Map<String, Object> currentData = new HashMap<>((Map<String, Object>) inputData.get("inputData"));
            Map<String, Object> loopLogic = (Map<String, Object>) inputData.get("loopLogic");
            Integer maxIterations = (Integer) inputData.get("maxIterations");
            
            if (conditionConfig == null) {
                log.error("循环条件配置为空: templateId={}", templateId);
                throw new IllegalArgumentException("循环条件配置不能为空");
            }
            
            // 设置最大循环次数
            int maxIter = (maxIterations != null && maxIterations > 0) ? 
                         Math.min(maxIterations, MAX_ITERATIONS) : MAX_ITERATIONS;
            
            log.info("WHILE循环开始: templateId={}, 最大迭代次数={}", templateId, maxIter);
            log.debug("初始条件配置: {}", conditionConfig);
            log.debug("初始数据: {}", currentData);
            
            List<Map<String, Object>> iterations = new ArrayList<>();
            int iterationCount = 0;
            boolean hasLogicErrors = false;
            
            // 执行WHILE循环
            while (evaluateCondition(conditionConfig, currentData) && iterationCount < maxIter) {
                log.debug("执行循环迭代: iteration={}", iterationCount);
                
                try {
                    Map<String, Object> iterationData = new HashMap<>();
                    iterationData.put("iteration", iterationCount);
                    iterationData.put("inputData", new HashMap<>(currentData));
                    
                    // 执行循环体逻辑
                    if (loopLogic != null) {
                        currentData = processLoopBody(currentData, loopLogic, iterationCount);
                        iterationData.put("outputData", new HashMap<>(currentData));
                    } else {
                        // 默认逻辑：增加计数器
                        Integer counter = (Integer) currentData.get("counter");
                        if (counter == null) {
                            counter = 0;
                        }
                        currentData.put("counter", counter + 1);
                        iterationData.put("outputData", new HashMap<>(currentData));
                    }
                    
                    iterationData.put("status", "success");
                    iterations.add(iterationData);
                } catch (Exception e) {
                    log.warn("循环体执行错误: iteration={}, error={}", iterationCount, e.getMessage());
                    Map<String, Object> errorIteration = new HashMap<>();
                    errorIteration.put("iteration", iterationCount);
                    errorIteration.put("status", "error");
                    errorIteration.put("error", e.getMessage());
                    iterations.add(errorIteration);
                    hasLogicErrors = true;
                }
                
                iterationCount++;
            }
            
            // 检查是否因为最大循环次数而结束
            boolean reachedMaxIterations = iterationCount >= maxIter;
            boolean conditionStillTrue = evaluateCondition(conditionConfig, currentData);
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("finalResult", currentData);
            outputData.put("iterations", iterationCount);
            outputData.put("iterationDetails", iterations);
            outputData.put("reachedMaxIterations", reachedMaxIterations);
            outputData.put("conditionStillTrue", conditionStillTrue);
            outputData.put("hasLogicErrors", hasLogicErrors);
            outputData.put("message", "WHILE循环执行成功");
            
            if (reachedMaxIterations && conditionStillTrue) {
                log.warn("WHILE循环达到最大迭代次数: templateId={}, iterations={}", templateId, iterationCount);
                outputData.put("warning", "循环达到最大迭代次数限制");
            }
            
            log.info("WHILE循环执行完成: templateId={}, 总迭代次数={}, 是否达到最大次数={}, 条件仍为真={}", 
                    templateId, iterationCount, reachedMaxIterations, conditionStillTrue);
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("WHILE循环执行成功")
                                .withMetadata("iterations", iterationCount)
                                .withMetadata("reachedMaxIterations", reachedMaxIterations)
                                .withMetadata("conditionStillTrue", conditionStillTrue)
                                .withMetadata("hasLogicErrors", hasLogicErrors)
                                .withMetadata("maxIterationsLimit", maxIter);
            
        } catch (Exception e) {
            log.error("WHILE循环执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("WHILE循环执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("WHILE循环执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 评估循环条件
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateCondition(Map<String, Object> conditionConfig, Map<String, Object> currentData) {
        try {
            String field = (String) conditionConfig.get("field");
            String operator = (String) conditionConfig.get("operator");
            Object expectedValue = conditionConfig.get("value");
            
            if (field == null || operator == null) {
                log.warn("条件配置不完整: field={}, operator={}", field, operator);
                return false;
            }
            
            Object actualValue = currentData.get(field);
            
            switch (operator.toLowerCase()) {
                case "eq":
                case "equals":
                    return Objects.equals(actualValue, expectedValue);
                case "ne":
                case "not_equals":
                    return !Objects.equals(actualValue, expectedValue);
                case "gt":
                case "greater_than":
                    return compareValues(actualValue, expectedValue) > 0;
                case "gte":
                case "greater_than_or_equals":
                    return compareValues(actualValue, expectedValue) >= 0;
                case "lt":
                case "less_than":
                    return compareValues(actualValue, expectedValue) < 0;
                case "lte":
                case "less_than_or_equals":
                    return compareValues(actualValue, expectedValue) <= 0;
                default:
                    log.warn("未知的条件操作符: {}", operator);
                    return false;
            }
        } catch (Exception e) {
            log.error("条件评估失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 比较两个值
     */
    @SuppressWarnings("unchecked")
    private int compareValues(Object actual, Object expected) {
        if (actual == null && expected == null) return 0;
        if (actual == null) return -1;
        if (expected == null) return 1;
        
        if (actual instanceof Number && expected instanceof Number) {
            double actualNum = ((Number) actual).doubleValue();
            double expectedNum = ((Number) expected).doubleValue();
            return Double.compare(actualNum, expectedNum);
        }
        
        if (actual instanceof Comparable && expected instanceof Comparable) {
            return ((Comparable<Object>) actual).compareTo(expected);
        }
        
        return actual.toString().compareTo(expected.toString());
    }
    
    /**
     * 处理循环体
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> processLoopBody(Map<String, Object> currentData, 
                                              Map<String, Object> loopLogic, 
                                              int iteration) {
        Map<String, Object> newData = new HashMap<>(currentData);
        
        try {
            String operation = (String) loopLogic.get("operation");
            
            if ("increment".equals(operation)) {
                // 递增操作
                String field = (String) loopLogic.get("field");
                Object stepObj = loopLogic.get("step");
                int step = stepObj instanceof Number ? ((Number) stepObj).intValue() : 1;
                
                if (field != null) {
                    Object currentValue = newData.get(field);
                    int current = currentValue instanceof Number ? ((Number) currentValue).intValue() : 0;
                    newData.put(field, current + step);
                }
            }
        } catch (Exception e) {
            log.warn("循环体处理失败: {}", e.getMessage());
        }
        
        return newData;
    }
} 