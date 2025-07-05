package com.operatorchoreography.executor;

import java.util.Map;

/**
 * 算子执行器基础接口
 * 所有算子执行器都必须实现此接口
 */
public interface BaseExecutor {
    
    /**
     * 执行算子逻辑
     * @param inputData 输入数据
     * @param templateId 模板ID
     * @return 执行结果
     * @throws Exception 执行异常
     */
    ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception;
} 