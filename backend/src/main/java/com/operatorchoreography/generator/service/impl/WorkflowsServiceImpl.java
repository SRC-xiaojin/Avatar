package com.operatorchoreography.generator.service.impl;

import com.operatorchoreography.generator.model.Workflows;
import com.operatorchoreography.generator.model.WorkflowNodes;
import com.operatorchoreography.generator.model.WorkflowConnections;
import com.operatorchoreography.generator.model.OperatorTemplates;
import com.operatorchoreography.generator.mapper.WorkflowsMapper;
import com.operatorchoreography.generator.service.WorkflowsService;
import com.operatorchoreography.generator.service.WorkflowNodesService;
import com.operatorchoreography.generator.service.WorkflowConnectionsService;
import com.operatorchoreography.generator.service.OperatorTemplatesService;
import com.operatorchoreography.generator.dto.WorkflowSaveRequest;
import com.operatorchoreography.executor.ExecutorManager;
import com.operatorchoreography.executor.ExecutorResult;
import com.operatorchoreography.executor.ExecutorStatus;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 工作流表 服务实现类
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Slf4j
@Service
public class WorkflowsServiceImpl extends ServiceImpl<WorkflowsMapper, Workflows> implements WorkflowsService {

    @Autowired
    private WorkflowNodesService workflowNodesService;

    @Autowired
    private WorkflowConnectionsService workflowConnectionsService;

    @Autowired
    private OperatorTemplatesService operatorTemplatesService;

    @Autowired
    private ExecutorManager executorManager;

    @Override
    public Map<String, Object> executeWorkflow(Long workflowId, Map<String, Object> inputData) {
        // 模拟工作流执行逻辑
        Workflows workflow = this.getById(workflowId);
        if (workflow == null) {
            throw new RuntimeException("工作流不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("executionId", UUID.randomUUID().toString());
        result.put("workflowId", workflowId);
        result.put("workflowName", workflow.getWorkflowName());
        result.put("status", "SUCCESS");
        result.put("startTime", LocalDateTime.now());
        result.put("endTime", LocalDateTime.now().plusSeconds(5));
        result.put("durationMs", 5000L);
        result.put("inputData", inputData);
        result.put("outputData", Map.of("processedCount", 100, "result", "执行成功"));
        
        return result;
    }

    @Override
    public boolean validateWorkflow(Long workflowId) {
        // 模拟工作流验证逻辑
        Workflows workflow = this.getById(workflowId);
        if (workflow == null) {
            return false;
        }
        
        // 简单验证：检查工作流是否为激活状态
        return "ACTIVE".equals(workflow.getStatus());
    }

    @Override
    public List<Map<String, Object>> getExecutionHistory(Long workflowId) {
        // 模拟执行历史数据
        List<Map<String, Object>> history = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> execution = new HashMap<>();
            execution.put("executionId", "exec-" + workflowId + "-" + i);
            execution.put("status", i % 2 == 0 ? "SUCCESS" : "FAILED");
            execution.put("startTime", LocalDateTime.now().minusDays(i));
            execution.put("endTime", LocalDateTime.now().minusDays(i).plusMinutes(5));
            execution.put("durationMs", 5000L + i * 1000);
            history.add(execution);
        }
        
        return history;
    }

    @Override
    public Map<String, Object> testWorkflow(Long workflowId, Map<String, Object> inputData) {
        LocalDateTime startTime = LocalDateTime.now();
        String testId = "test-" + workflowId + "-" + System.currentTimeMillis();
        
        try {
            // 1. 验证工作流是否存在
            Workflows workflow = this.getById(workflowId);
            if (workflow == null) {
                throw new RuntimeException("工作流不存在: " + workflowId);
            }

            // 2. 获取工作流的所有节点
            QueryWrapper<WorkflowNodes> nodeQuery = new QueryWrapper<>();
            nodeQuery.eq("workflow_id", workflowId);
            List<WorkflowNodes> nodes = workflowNodesService.list(nodeQuery);
            
            if (nodes.isEmpty()) {
                throw new RuntimeException("工作流中没有节点");
            }

            // 3. 获取工作流的所有连接关系
            QueryWrapper<WorkflowConnections> connectionQuery = new QueryWrapper<>();
            connectionQuery.eq("workflow_id", workflowId);
            List<WorkflowConnections> connections = workflowConnectionsService.list(connectionQuery);

            // 4. 构建节点执行队列（拓扑排序）
            List<WorkflowNodes> executionQueue = buildExecutionQueue(nodes, connections);

            // 5. 初始化执行结果
            Map<String, Object> executionContext = new HashMap<>(inputData);
            List<ExecutorResult> nodeResults = new ArrayList<>();
            
            // 6. 按队列顺序执行节点
            for (WorkflowNodes node : executionQueue) {
                try {
                    ExecutorResult nodeResult = executeNodeByTemplate(node, executionContext,new HashMap<>());
                    nodeResults.add(nodeResult);
                    
                    // 将节点输出合并到执行上下文中
                    if (nodeResult.isSuccess()) {

                        // 将节点输出中的outputData合并到执行上下文中
                        executionContext.put("inputData", nodeResult.getOutputData());
                    }
                    
                } catch (Exception e) {
                    // 节点执行失败

                    ExecutorResult errorResult = ExecutorResult.failure(ExecutorStatus.FAILED.getDescription(), e.getMessage());
                    nodeResults.add(errorResult);
                    
                    // 测试模式下继续执行其他节点
                    log.error("节点执行失败: {}, 错误: {}", node.getNodeName(), e.getMessage());
                }
            }

            // 7. 构建测试结果
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            
            Map<String, Object> result = new HashMap<>();
            result.put("testId", testId);
            result.put("workflowId", workflowId);
            result.put("workflowName", workflow.getWorkflowName());
            result.put("status", "SUCCESS");
            result.put("startTime", startTime);
            result.put("endTime", endTime);
            result.put("durationMs", durationMs);
            result.put("inputData", inputData);
            result.put("nodeCount", nodes.size());
            result.put("connectionCount", connections.size());
            result.put("executionQueue", executionQueue.stream().map(n -> 
                Map.of("id", n.getId(), "name", n.getNodeName(), "templateId", n.getTemplateId())
            ).toList());
            result.put("nodeResults", nodeResults);
            result.put("finalOutput", executionContext);
            
            return result;
            
        } catch (Exception e) {
            // 测试失败
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            
            Map<String, Object> result = new HashMap<>();
            result.put("testId", testId);
            result.put("workflowId", workflowId);
            result.put("status", "FAILED");
            result.put("startTime", startTime);
            result.put("endTime", endTime);
            result.put("durationMs", durationMs);
            result.put("inputData", inputData);
            result.put("error", e.getMessage());
            result.put("stackTrace", Arrays.toString(e.getStackTrace()));
            
            return result;
        }
    }

    /**
     * 构建节点执行队列（拓扑排序）
     */
    private List<WorkflowNodes> buildExecutionQueue(List<WorkflowNodes> nodes, List<WorkflowConnections> connections) {
        // 构建邻接表和入度表
        Map<Long, List<Long>> adjacencyList = new HashMap<>();
        Map<Long, Integer> inDegree = new HashMap<>();
        Map<Long, WorkflowNodes> nodeMap = new HashMap<>();
        
        // 初始化
        for (WorkflowNodes node : nodes) {
            nodeMap.put(node.getId(), node);
            adjacencyList.put(node.getId(), new ArrayList<>());
            inDegree.put(node.getId(), 0);
        }
        
        // 构建图
        for (WorkflowConnections connection : connections) {
            Long fromNodeId = connection.getSourceNodeId();
            Long toNodeId = connection.getTargetNodeId();
            
            if (nodeMap.containsKey(fromNodeId) && nodeMap.containsKey(toNodeId)) {
                adjacencyList.get(fromNodeId).add(toNodeId);
                inDegree.put(toNodeId, inDegree.get(toNodeId) + 1);
            }
        }
        
        // 拓扑排序
        Queue<Long> queue = new LinkedList<>();
        List<WorkflowNodes> result = new ArrayList<>();
        
        // 找到所有入度为0的节点（起始节点）
        for (Map.Entry<Long, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }
        
        // 如果没有入度为0的节点，选择第一个节点作为起始点
        if (queue.isEmpty() && !nodes.isEmpty()) {
            queue.offer(nodes.get(0).getId());
        }
        
        while (!queue.isEmpty()) {
            Long currentNodeId = queue.poll();
            WorkflowNodes currentNode = nodeMap.get(currentNodeId);
            if (currentNode != null) {
                result.add(currentNode);
                
                // 更新相邻节点的入度
                for (Long neighborId : adjacencyList.get(currentNodeId)) {
                    inDegree.put(neighborId, inDegree.get(neighborId) - 1);
                    if (inDegree.get(neighborId) == 0) {
                        queue.offer(neighborId);
                    }
                }
            }
        }
        
        // 如果还有节点没有被处理（可能存在环），将剩余节点添加到队列末尾
        for (WorkflowNodes node : nodes) {
            if (!result.contains(node)) {
                result.add(node);
            }
        }
        
        return result;
    }

    /**
     * 执行单个节点
     */
    private Map<String, Object> executeNode(WorkflowNodes node, Map<String, Object> context) {
        Map<String, Object> result = new HashMap<>();
        result.put("nodeId", node.getId());
        result.put("nodeName", node.getNodeName());
        result.put("templateId", node.getTemplateId());
        result.put("startTime", LocalDateTime.now());
        
        try {
            // 初始化节点参数
            Map<String, Object> params = new HashMap<>();
            params.put("nodeCode", node.getNodeCode());
            params.put("positionX", node.getPositionX());
            params.put("positionY", node.getPositionY());
            params.put("executionOrder", node.getExecutionOrder());
            
            // 根据模板ID执行不同的逻辑
            ExecutorResult output = executeNodeByTemplate(node, context, params);
            
            result.put("status", "SUCCESS");
            result.put("output", output);
            result.put("endTime", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
            result.put("endTime", LocalDateTime.now());
        }
        
        return result;
    }

    /**
     * 根据模板ID执行节点
     */
    private ExecutorResult executeNodeByTemplate(WorkflowNodes node, Map<String, Object> context, Map<String, Object> params) {
        Long templateId = node.getTemplateId();
        
        // 根据模板ID调用对应的执行器
        if (templateId != null && executorManager != null && operatorTemplatesService != null) {
            try {
                // 通过templateId获取算子模板信息
                OperatorTemplates template = operatorTemplatesService.getById(templateId);
                
                if (isTemplateExecutorValid(template)) {
                    // 打印详细的执行信息（用于调试）
                    logTemplateExecution(template, templateId);
                    
                    // 获取执行器类名和方法名
                    String executorClass = template.getExecutorClass();
                    String executorMethod = template.getExecutorMethod();
                    
                    // 调用执行器
                    ExecutorResult executorResult = executorManager.executeOperator(executorClass, executorMethod, context, templateId);
                    
                    // 设置节点信息
                    executorResult.withNodeInfo(node.getId(), node.getNodeName())
                                 .withTemplateInfo(templateId, template.getTemplateName());
                    
                    // 转换为Map格式返回（保持向后兼容）
                    return executorResult;
                } else {
                    if (template == null) {
                        log.warn("模板不存在: templateId={}", templateId);
                    } else if (!template.getStatus()) {
                        log.warn("模板已禁用: templateId={}, templateName={}", templateId, template.getTemplateName());
                    } else {
                        log.warn("模板执行器信息不完整: templateId={}, templateName={}, executorClass={}, executorMethod={}", 
                                templateId, template.getTemplateName(), template.getExecutorClass(), template.getExecutorMethod());
                    }
                }
                
            } catch (Exception e) {
                log.error("执行器调用失败: templateId={}, 错误: {}", templateId, e.getMessage(), e);
                
                // 返回错误信息而不是抛出异常
                ExecutorResult errorResult = ExecutorResult.failure("执行器调用失败", e)
                                                          .withNodeInfo(node.getId(), node.getNodeName())
                                                          .withTemplateInfo(templateId, null);
                return errorResult;
            }
        }
        
        return ExecutorResult.failure("执行器未配置")
                    .withNodeInfo(node.getId(), node.getNodeName())
                    .withTemplateInfo(templateId, null);
    }

    /**
     * 验证模板执行器配置是否完整
     */
    private boolean isTemplateExecutorValid(OperatorTemplates template) {
        if (template == null) {
            return false;
        }
        
        String executorClass = template.getExecutorClass();
        String executorMethod = template.getExecutorMethod();
        
        return executorClass != null && !executorClass.trim().isEmpty() &&
               executorMethod != null && !executorMethod.trim().isEmpty() &&
               template.getStatus() != null && template.getStatus();
    }

    /**
     * 打印模板执行信息（用于调试）
     */
    private void logTemplateExecution(OperatorTemplates template, Long templateId) {
        if (template != null) {
            log.info("=== 算子模板执行信息 ===");
            log.info("模板ID: {}", templateId);
            log.info("模板名称: {}", template.getTemplateName());
            log.info("模板编码: {}", template.getTemplateCode());
            log.info("执行器类: {}", template.getExecutorClass());
            log.info("执行方法: {}", template.getExecutorMethod());
            log.info("是否启用: {}", template.getStatus());
            log.info("是否异步: {}", template.getIsAsync());
            log.info("超时时间: {}秒", template.getTimeoutSeconds());
            log.info("重试次数: {}", template.getRetryCount());
            log.info("========================");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Workflows saveCompleteWorkflow(WorkflowSaveRequest request) {
        try {
            log.info("开始保存完整工作流数据...");
            
            // 1. 保存或更新工作流基本信息
            Workflows workflow = saveWorkflowBasicInfo(request.getWorkflow());
            Long workflowId = workflow.getId();
            
            log.info("工作流基本信息保存成功，ID: {}", workflowId);
            
            // 2. 删除现有的节点和连线（如果是更新操作）
            if (request.getWorkflow().getId() != null) {
                cleanExistingNodesAndConnections(workflowId);
            }
            
            // 3. 保存节点并创建节点ID映射
            Map<Integer, Long> nodeIdMapping = saveWorkflowNodes(workflowId, request.getNodes());
            
            log.info("节点保存成功，节点数量: {}", request.getNodes().size());
            
            // 4. 保存连线
            saveWorkflowConnections(workflowId, request.getConnections(), nodeIdMapping);
            
            log.info("连线保存成功，连线数量: {}", request.getConnections().size());
            
            log.info("完整工作流保存成功: {}", workflow.getWorkflowName());
            
            return workflow;
            
        } catch (Exception e) {
            log.error("保存完整工作流失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存工作流失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存工作流基本信息
     */
    private Workflows saveWorkflowBasicInfo(WorkflowSaveRequest.WorkflowData workflowData) {
        Workflows workflow = new Workflows();
        
        // 设置基本信息
        if (workflowData.getId() != null) {
            workflow.setId(workflowData.getId());
        }
        workflow.setWorkflowName(workflowData.getWorkflowName());
        workflow.setWorkflowCode(workflowData.getWorkflowCode());
        workflow.setDescription(workflowData.getDescription());
        workflow.setStatus(workflowData.getStatus() != null ? workflowData.getStatus() : "DRAFT");
        workflow.setVersion(workflowData.getVersion() != null ? workflowData.getVersion() : "1.0.0");
        workflow.setExecutionMode(workflowData.getExecutionMode() != null ? workflowData.getExecutionMode() : "SYNC");
        workflow.setMaxExecutionTime(workflowData.getMaxExecutionTime() != null ? workflowData.getMaxExecutionTime() : 300);
        workflow.setTags(workflowData.getTags());
        workflow.setVariables(workflowData.getVariables());
        
        // 保存或更新
        if (workflowData.getId() != null) {
            this.updateById(workflow);
        } else {
            this.save(workflow);
        }
        
        return workflow;
    }

    /**
     * 清理现有的节点和连线
     */
    private void cleanExistingNodesAndConnections(Long workflowId) {
        // 删除现有连线
        QueryWrapper<WorkflowConnections> connectionQuery = new QueryWrapper<>();
        connectionQuery.eq("workflow_id", workflowId);
        workflowConnectionsService.remove(connectionQuery);
        
        // 删除现有节点
        QueryWrapper<WorkflowNodes> nodeQuery = new QueryWrapper<>();
        nodeQuery.eq("workflow_id", workflowId);
        workflowNodesService.remove(nodeQuery);
        
        log.info("清理工作流 {} 的现有节点和连线完成", workflowId);
    }

    /**
     * 保存工作流节点
     */
    private Map<Integer, Long> saveWorkflowNodes(Long workflowId, List<WorkflowSaveRequest.NodeData> nodeDataList) {
        Map<Integer, Long> nodeIdMapping = new HashMap<>();
        
        if (nodeDataList == null || nodeDataList.isEmpty()) {
            return nodeIdMapping;
        }
        
        for (WorkflowSaveRequest.NodeData nodeData : nodeDataList) {
            WorkflowNodes node = new WorkflowNodes();
            
            node.setWorkflowId(workflowId);
            node.setNodeCode(nodeData.getNodeCode());
            node.setNodeName(nodeData.getNodeName());
            node.setTemplateId(nodeData.getTemplateId());
            node.setPositionX(nodeData.getPositionX() != null ? nodeData.getPositionX() : 0.0);
            node.setPositionY(nodeData.getPositionY() != null ? nodeData.getPositionY() : 0.0);
            node.setWidth(nodeData.getWidth() != null ? nodeData.getWidth() : 100.0);
            node.setHeight(nodeData.getHeight() != null ? nodeData.getHeight() : 60.0);
            node.setStyle(nodeData.getStyle());
            node.setIsEnabled(nodeData.getIsEnabled() != null ? nodeData.getIsEnabled() : true);
            node.setExecutionOrder(nodeData.getExecutionOrder() != null ? nodeData.getExecutionOrder() : 0);
            node.setOnError(nodeData.getOnError() != null ? nodeData.getOnError() : "STOP");
            node.setRetryCount(nodeData.getRetryCount() != null ? nodeData.getRetryCount() : 0);
            
            // 保存节点
            workflowNodesService.save(node);
            
            // 创建节点ID映射（前端画布ID -> 数据库ID）
            if (nodeData.getCanvasNodeId() != null) {
                nodeIdMapping.put(nodeData.getCanvasNodeId(), node.getId());
            }
        }
        
        return nodeIdMapping;
    }

    /**
     * 保存工作流连线
     */
    private void saveWorkflowConnections(Long workflowId, List<WorkflowSaveRequest.ConnectionData> connectionDataList, 
                                        Map<Integer, Long> nodeIdMapping) {
        if (connectionDataList == null || connectionDataList.isEmpty()) {
            return;
        }
        
        for (WorkflowSaveRequest.ConnectionData connectionData : connectionDataList) {
            WorkflowConnections connection = new WorkflowConnections();
            
            // 通过映射获取真实的数据库节点ID
            Long sourceNodeId = nodeIdMapping.get(connectionData.getSourceCanvasNodeId());
            Long targetNodeId = nodeIdMapping.get(connectionData.getTargetCanvasNodeId());
            
            if (sourceNodeId == null || targetNodeId == null) {
                log.warn("连线节点映射失败: sourceCanvasId={}, targetCanvasId={}, sourceDbId={}, targetDbId={}", 
                        connectionData.getSourceCanvasNodeId(), connectionData.getTargetCanvasNodeId(),
                        sourceNodeId, targetNodeId);
                continue;
            }
            
            connection.setWorkflowId(workflowId);
            connection.setSourceNodeId(sourceNodeId);
            connection.setTargetNodeId(targetNodeId);
            connection.setConnectionType(connectionData.getConnectionType() != null ? 
                                        connectionData.getConnectionType() : "DATA_FLOW");
            
            // 保存连线
            workflowConnectionsService.save(connection);
        }
    }
}

