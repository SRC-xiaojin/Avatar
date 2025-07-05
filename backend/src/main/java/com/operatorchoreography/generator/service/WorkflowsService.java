package com.operatorchoreography.generator.service;

import com.operatorchoreography.generator.model.Workflows;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 工作流表 服务类
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
public interface WorkflowsService extends IService<Workflows> {

    /**
     * 执行工作流
     */
    Map<String, Object> executeWorkflow(Long workflowId, Map<String, Object> inputData);

    /**
     * 验证工作流
     */
    boolean validateWorkflow(Long workflowId);

    /**
     * 获取工作流执行历史
     */
    List<Map<String, Object>> getExecutionHistory(Long workflowId);

    /**
     * 测试工作流
     */
    Map<String, Object> testWorkflow(Long workflowId, Map<String, Object> inputData);
}
