package com.operatorchoreography.generator.service;

import com.operatorchoreography.generator.model.WorkflowConnections;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 工作流连接表 服务类
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
public interface WorkflowConnectionsService extends IService<WorkflowConnections> {

    /**
     * 根据工作流ID获取所有连接
     */
    List<WorkflowConnections> getConnectionsByWorkflowId(Long workflowId);

    /**
     * 根据工作流ID删除所有连接
     */
    boolean deleteByWorkflowId(Long workflowId);
}
