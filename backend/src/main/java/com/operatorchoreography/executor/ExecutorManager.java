package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 执行器管理器
 * 负责动态调用算子执行器
 */
@Slf4j
@Component
public class ExecutorManager {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * 动态执行算子
     * @param executorClass 执行器类名
     * @param executorMethod 执行器方法名
     * @param inputData 输入数据
     * @param templateId 模板ID
     * @return 执行结果
     * @throws Exception 执行异常
     */
    public ExecutorResult executeOperator(String executorClass, String executorMethod, 
                                         Map<String, Object> inputData, Long templateId) throws Exception {
        
        log.info("开始执行算子: templateId={}, executorClass={}, executorMethod={}", 
                templateId, executorClass, executorMethod);
        log.debug("输入参数: {}", inputData);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 获取执行器实例
            Class<?> clazz = Class.forName(executorClass);
            Object executor = applicationContext.getBean(clazz);
            log.debug("成功获取执行器实例: {}", executorClass);
            
            // 获取执行方法
            Method method;
            try {
                // 首先尝试调用指定的方法
                method = clazz.getMethod(executorMethod, Map.class, Long.class);
                log.debug("使用指定方法: {}", executorMethod);
            } catch (NoSuchMethodException e) {
                // 如果没有找到指定方法，则调用execute方法
                // method = clazz.getMethod("execute", Map.class, Long.class);
                log.error("指定方法不存在");
                throw new RuntimeException("指定方法不存在: " + e.getMessage());
            }
            
            // 执行方法
            Object result = method.invoke(executor, inputData, templateId);
            LocalDateTime endTime = LocalDateTime.now();
            
            // 处理不同类型的返回值
            ExecutorResult executorResult;
            if (result instanceof ExecutorResult) {
                // 新的ExecutorResult类型
                executorResult = (ExecutorResult) result;
                log.info("算子执行成功: templateId={}, executorClass={}, 耗时={}ms", 
                        templateId, executorClass, executorResult.getExecutionTimeMs());
                log.debug("执行结果: {}", executorResult);
            } else if (result instanceof Map) {
                // 兼容旧的Map格式
                Map<String, Object> resultMap = (Map<String, Object>) result;
                executorResult = ExecutorResult.success(resultMap, startTime, endTime);
                log.info("算子执行成功（Map格式兼容）: templateId={}, executorClass={}, 耗时={}ms", 
                        templateId, executorClass, executorResult.getExecutionTimeMs());
                log.debug("执行结果: {}", resultMap);
            } else {
                throw new RuntimeException("执行器返回结果格式错误");
            }
            
            // 设置执行器信息
            executorResult.withExecutorInfo(executorClass, executorMethod)
                         .withTemplateInfo(templateId, null);
            
            return executorResult;
            
        } catch (ClassNotFoundException e) {
            log.error("执行器类不存在: executorClass={}, templateId={}", executorClass, templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("执行器类不存在: " + executorClass, e)
                                .withExecutorInfo(executorClass, executorMethod)
                                .withTemplateInfo(templateId, null);
        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            log.error("执行器调用失败: templateId={}, executorClass={}, executorMethod={}", 
                     templateId, executorClass, executorMethod, e);
            return ExecutorResult.failure("执行器调用失败", e)
                                .withExecutorInfo(executorClass, executorMethod)
                                .withTemplateInfo(templateId, null);
        }
    }
    
    /**
     * 检查执行器是否存在
     * @param executorClass 执行器类名
     * @return 是否存在
     */
    public boolean isExecutorExists(String executorClass) {
        try {
            Class<?> clazz = Class.forName(executorClass);
            Object executor = applicationContext.getBean(clazz);
            return executor != null;
        } catch (Exception e) {
            return false;
        }
    }
} 