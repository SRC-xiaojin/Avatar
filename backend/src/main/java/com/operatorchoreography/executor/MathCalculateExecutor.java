package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * 数学计算执行器
 * 数学表达式计算
 */
@Slf4j
@Component
public class MathCalculateExecutor implements BaseExecutor {

    private final ScriptEngine scriptEngine;
    
    public MathCalculateExecutor() {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.scriptEngine = manager.getEngineByName("JavaScript");
    }

    public ExecutorResult calculate(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行数学计算: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            String expression = (String) inputData.get("expression");
            Map<String, Object> variables = (Map<String, Object>) inputData.get("variables");
            
            if (expression == null || expression.trim().isEmpty()) {
                log.error("数学表达式为空: templateId={}", templateId);
                throw new IllegalArgumentException("数学表达式不能为空");
            }
            
            log.debug("原始表达式: {}, 变量: {}", expression, variables);
            
            // 处理变量替换
            String processedExpression = expression;
            if (variables != null) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    String varName = entry.getKey();
                    Object varValue = entry.getValue();
                    if (varValue instanceof Number) {
                        processedExpression = processedExpression.replace(varName, varValue.toString());
                    }
                }
            }
            
            log.debug("处理后表达式: {}", processedExpression);
            
            // 安全检查：只允许数学运算符
            if (!isSafeExpression(processedExpression)) {
                log.error("表达式包含不安全字符: {}, templateId={}", processedExpression, templateId);
                throw new IllegalArgumentException("表达式包含不安全的字符");
            }
            
            // 计算表达式
            Object calcResult = scriptEngine.eval(processedExpression);
            double finalResult = 0.0;
            
            if (calcResult instanceof Number) {
                finalResult = ((Number) calcResult).doubleValue();
            }
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("result", finalResult);
            outputData.put("expression", expression);
            outputData.put("processedExpression", processedExpression);
            outputData.put("variables", variables);
            outputData.put("message", "数学计算成功");
            
            log.info("数学计算成功: templateId={}, 结果={}", templateId, finalResult);
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("数学计算成功")
                                .withMetadata("expression", expression)
                                .withMetadata("result", finalResult)
                                .withMetadata("variableCount", variables != null ? variables.size() : 0);
            
        } catch (Exception e) {
            log.error("数学计算失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("数学计算失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("数学计算失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查表达式是否安全（只包含数学运算符）
     */
    private boolean isSafeExpression(String expression) {
        // 只允许数字、基本运算符、括号、小数点
        String safePattern = "^[0-9+\\-*/().\\s]+$";
        return expression.matches(safePattern);
    }
} 