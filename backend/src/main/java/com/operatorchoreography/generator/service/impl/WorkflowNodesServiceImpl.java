package com.operatorchoreography.generator.service.impl;

import com.operatorchoreography.generator.model.WorkflowNodes;
import com.operatorchoreography.generator.model.WorkflowNodeParams;
import com.operatorchoreography.generator.mapper.WorkflowNodesMapper;
import com.operatorchoreography.generator.service.WorkflowNodesService;
import com.operatorchoreography.generator.service.WorkflowNodeParamsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * <p>
 * 工作流节点表 服务实现类
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Service
public class WorkflowNodesServiceImpl extends ServiceImpl<WorkflowNodesMapper, WorkflowNodes> implements WorkflowNodesService {

    @Autowired
    private WorkflowNodeParamsService workflowNodeParamsService;

    @Override
    public List<WorkflowNodes> getNodesByWorkflowId(Long workflowId) {
        QueryWrapper<WorkflowNodes> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("workflow_id", workflowId)
                   .eq("if_delete", false)
                   .orderByAsc("id");
        return this.list(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getNodeParams(Long nodeId) {
        QueryWrapper<WorkflowNodeParams> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("node_id", nodeId).eq("if_delete", false);
        
        List<WorkflowNodeParams> params = workflowNodeParamsService.list(queryWrapper);
        
        return params.stream().map(param -> {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", param.getId());
            paramMap.put("nodeId", param.getNodeId());
            paramMap.put("paramKey", param.getParamKey());
            paramMap.put("paramValue", param.getParamValue());
            return paramMap;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean updateNodeParams(Long nodeId, List<Map<String, Object>> params) {
        try {
            // 先删除现有的参数配置
            QueryWrapper<WorkflowNodeParams> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("node_id", nodeId).eq("if_delete", false);
            workflowNodeParamsService.remove(deleteWrapper);
            
            // 添加新的参数配置
            for (Map<String, Object> paramMap : params) {
                WorkflowNodeParams param = new WorkflowNodeParams();
                param.setNodeId(nodeId);
                param.setParamKey((String) paramMap.get("paramKey"));
                param.setParamValue((String) paramMap.get("paramValue"));
                workflowNodeParamsService.save(param);
            }
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("更新节点参数失败: " + e.getMessage());
        }
    }
}
