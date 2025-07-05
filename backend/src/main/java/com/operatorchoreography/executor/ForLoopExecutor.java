package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.time.LocalDateTime;

/**
 * FOR循环执行器
 * 循环处理数据集合
 */
@Slf4j
@Component
public class ForLoopExecutor implements BaseExecutor {

    public ExecutorResult loop(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行FOR循环: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            List<Object> dataArray = (List<Object>) inputData.get("dataArray");
            String loopVariable = (String) inputData.get("loopVariable");
            Map<String, Object> loopLogic = (Map<String, Object>) inputData.get("loopLogic");
            
            if (dataArray == null) {
                log.error("数据数组为空: templateId={}", templateId);
                throw new IllegalArgumentException("数据数组不能为空");
            }
            
            if (loopVariable == null || loopVariable.trim().isEmpty()) {
                loopVariable = "item"; // 默认循环变量名
            }
            
            log.info("FOR循环执行: templateId={}, 数组长度={}, 循环变量={}", 
                    templateId, dataArray.size(), loopVariable);
            
            List<Map<String, Object>> results = new ArrayList<>();
            int index = 0;
            int processedCount = 0;
            int errorCount = 0;
            
            // 执行循环
            for (Object item : dataArray) {
                log.debug("处理循环项: index={}, item={}", index, item);
                
                try {
                    Map<String, Object> loopContext = new HashMap<>();
                    loopContext.put(loopVariable, item);
                    loopContext.put("index", index);
                    loopContext.put("isFirst", index == 0);
                    loopContext.put("isLast", index == dataArray.size() - 1);
                    
                    // 执行循环逻辑
                    Map<String, Object> itemResult = new HashMap<>();
                    itemResult.put("index", index);
                    itemResult.put("input", item);
                    itemResult.put("context", loopContext);
                    
                    // 如果有循环逻辑配置，可以在这里扩展
                    if (loopLogic != null) {
                        itemResult.put("processedData", processLoopLogic(item, loopLogic, loopContext));
                    } else {
                        itemResult.put("processedData", item);
                    }
                    
                    itemResult.put("status", "success");
                    results.add(itemResult);
                    processedCount++;
                } catch (Exception e) {
                    log.warn("循环项处理失败: index={}, error={}", index, e.getMessage());
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("index", index);
                    errorResult.put("input", item);
                    errorResult.put("status", "error");
                    errorResult.put("error", e.getMessage());
                    results.add(errorResult);
                    errorCount++;
                }
                
                index++;
            }
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("results", results);
            outputData.put("totalItems", dataArray.size());
            outputData.put("processedItems", processedCount);
            outputData.put("errorItems", errorCount);
            outputData.put("loopVariable", loopVariable);
            outputData.put("message", "FOR循环执行成功");
            
            log.info("FOR循环执行成功: templateId={}, 总项数={}, 成功项数={}, 失败项数={}", 
                    templateId, dataArray.size(), processedCount, errorCount);
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("FOR循环执行成功")
                                .withMetadata("totalItems", dataArray.size())
                                .withMetadata("processedItems", processedCount)
                                .withMetadata("errorItems", errorCount)
                                .withMetadata("loopVariable", loopVariable)
                                .withMetadata("hasLoopLogic", loopLogic != null);
            
        } catch (Exception e) {
            log.error("FOR循环执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("FOR循环执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("FOR循环执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理循环逻辑
     * @param item 当前循环项
     * @param loopLogic 循环逻辑配置
     * @param context 循环上下文
     * @return 处理结果
     */
    private Object processLoopLogic(Object item, Map<String, Object> loopLogic, Map<String, Object> context) {
        try {
            // 简单的映射逻辑
            String operation = (String) loopLogic.get("operation");
            
            if ("transform".equals(operation)) {
                // 数据转换
                Map<String, String> mapping = (Map<String, String>) loopLogic.get("mapping");
                if (mapping != null && item instanceof Map) {
                    Map<String, Object> itemMap = (Map<String, Object>) item;
                    Map<String, Object> transformed = new HashMap<>();
                    
                    for (Map.Entry<String, String> entry : mapping.entrySet()) {
                        String sourceKey = entry.getValue();
                        String targetKey = entry.getKey();
                        transformed.put(targetKey, itemMap.get(sourceKey));
                    }
                    
                    return transformed;
                }
            } else if ("filter".equals(operation)) {
                // 数据过滤
                String condition = (String) loopLogic.get("condition");
                // 简单的条件判断，可以扩展
                return item;
            }
            
            return item;
        } catch (Exception e) {
            log.warn("循环逻辑处理失败: {}", e.getMessage());
            return item;
        }
    }
} 