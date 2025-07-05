package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.time.LocalDateTime;

/**
 * 数据验证执行器
 * 数据格式和内容验证
 */
@Slf4j
@Component
public class DataValidateExecutor implements BaseExecutor {

    public ExecutorResult validate(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行数据验证: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        List<String> errors = new ArrayList<>();
        
        try {
            Map<String, Object> data = (Map<String, Object>) inputData.get("inputData");
            List<Map<String, Object>> validationRules = (List<Map<String, Object>>) inputData.get("validationRules");
            
            if (data == null) {
                log.error("输入数据为空: templateId={}", templateId);
                errors.add("输入数据不能为空");
            } else if (validationRules != null) {
                log.debug("数据验证规则数量: {}, 数据字段数量: {}", validationRules.size(), data.size());
                
                // 执行验证规则
                for (Map<String, Object> rule : validationRules) {
                    String field = (String) rule.get("field");
                    String type = (String) rule.get("type");
                    Boolean required = (Boolean) rule.getOrDefault("required", false);
                    String pattern = (String) rule.get("pattern");
                    Object minValue = rule.get("minValue");
                    Object maxValue = rule.get("maxValue");
                    
                    Object value = data.get(field);
                    
                    log.debug("验证字段: {}, 类型: {}, 必填: {}, 值: {}", field, type, required, value);
                    
                    // 必填验证
                    if (required && (value == null || value.toString().trim().isEmpty())) {
                        errors.add("字段 " + field + " 不能为空");
                        continue;
                    }
                    
                    if (value != null) {
                        // 类型验证
                        if (!validateType(value, type)) {
                            errors.add("字段 " + field + " 类型不正确，期望类型: " + type);
                        }
                        
                        // 正则表达式验证
                        if (pattern != null && !validatePattern(value.toString(), pattern)) {
                            errors.add("字段 " + field + " 格式不正确");
                        }
                        
                        // 数值范围验证
                        if (minValue != null || maxValue != null) {
                            if (!validateRange(value, minValue, maxValue)) {
                                errors.add("字段 " + field + " 值超出范围");
                            }
                        }
                    }
                }
            }
            
            boolean isValid = errors.isEmpty();
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("isValid", isValid);
            outputData.put("errors", errors);
            outputData.put("validatedData", data);
            outputData.put("message", isValid ? "数据验证成功" : "数据验证失败");
            
            log.info("数据验证完成: templateId={}, 验证结果={}, 错误数量={}", 
                    templateId, isValid, errors.size());
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog(isValid ? "数据验证成功" : "数据验证失败")
                                .withMetadata("validationResult", isValid)
                                .withMetadata("errorCount", errors.size())
                                .withMetadata("ruleCount", validationRules != null ? validationRules.size() : 0);
            
        } catch (Exception e) {
            log.error("数据验证执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("数据验证执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("数据验证执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证数据类型
     */
    private boolean validateType(Object value, String expectedType) {
        if (expectedType == null) {
            return true;
        }
        
        switch (expectedType.toLowerCase()) {
            case "string":
                return value instanceof String;
            case "number":
            case "integer":
                return value instanceof Number || isNumeric(value.toString());
            case "boolean":
                return value instanceof Boolean || 
                       "true".equalsIgnoreCase(value.toString()) || 
                       "false".equalsIgnoreCase(value.toString());
            case "email":
                return validateEmail(value.toString());
            case "phone":
                return validatePhone(value.toString());
            case "date":
                return validateDate(value.toString());
            default:
                return true;
        }
    }
    
    /**
     * 验证正则表达式
     */
    private boolean validatePattern(String value, String pattern) {
        try {
            return Pattern.matches(pattern, value);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证数值范围
     */
    private boolean validateRange(Object value, Object minValue, Object maxValue) {
        try {
            double numValue = Double.parseDouble(value.toString());
            
            if (minValue != null) {
                double min = Double.parseDouble(minValue.toString());
                if (numValue < min) {
                    return false;
                }
            }
            
            if (maxValue != null) {
                double max = Double.parseDouble(maxValue.toString());
                if (numValue > max) {
                    return false;
                }
            }
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 验证是否为数字
     */
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 验证邮箱格式
     */
    private boolean validateEmail(String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        return Pattern.matches(emailPattern, email);
    }
    
    /**
     * 验证手机号格式
     */
    private boolean validatePhone(String phone) {
        String phonePattern = "^1[3-9]\\d{9}$";
        return Pattern.matches(phonePattern, phone);
    }
    
    /**
     * 验证日期格式
     */
    private boolean validateDate(String date) {
        try {
            // 简单的日期格式验证，实际项目中可以使用更复杂的日期解析
            String datePattern = "^\\d{4}-\\d{2}-\\d{2}$";
            return Pattern.matches(datePattern, date);
        } catch (Exception e) {
            return false;
        }
    }
} 