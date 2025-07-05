package com.operatorchoreography.generator.service.impl;

import com.operatorchoreography.generator.model.WorkflowConnections;
import com.operatorchoreography.generator.mapper.WorkflowConnectionsMapper;
import com.operatorchoreography.generator.service.WorkflowConnectionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 工作流连接表 服务实现类
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Service
public class WorkflowConnectionsServiceImpl extends ServiceImpl<WorkflowConnectionsMapper, WorkflowConnections> implements WorkflowConnectionsService {

    @Override
    public List<WorkflowConnections> getConnectionsByWorkflowId(Long workflowId) {
        QueryWrapper<WorkflowConnections> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("workflow_id", workflowId);
        return this.list(queryWrapper);
    }

    @Override
    public boolean deleteByWorkflowId(Long workflowId) {
        QueryWrapper<WorkflowConnections> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("workflow_id", workflowId);
        return this.remove(queryWrapper);
    }
}
