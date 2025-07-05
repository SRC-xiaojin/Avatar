package com.operatorchoreography.generator.service;

import com.operatorchoreography.generator.model.WorkflowNodes;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 工作流节点表 服务类
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
public interface WorkflowNodesService extends IService<WorkflowNodes> {

    /**
     * 根据工作流ID获取节点列表
     */
    List<WorkflowNodes> getNodesByWorkflowId(Long workflowId);

    /**
     * 获取节点的参数配置
     */
    List<Map<String, Object>> getNodeParams(Long nodeId);

    /**
     * 更新节点的参数配置
     */
    boolean updateNodeParams(Long nodeId, List<Map<String, Object>> params);
}
