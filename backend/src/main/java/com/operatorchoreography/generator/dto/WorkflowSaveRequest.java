package com.operatorchoreography.generator.dto;

import lombok.Data;
import java.util.List;

/**
 * 工作流保存请求DTO
 * 包含工作流、节点和连线的完整数据
 */
@Data
public class WorkflowSaveRequest {
    
    /**
     * 工作流基本信息
     */
    private WorkflowData workflow;
    
    /**
     * 节点数据列表
     */
    private List<NodeData> nodes;
    
    /**
     * 连线数据列表
     */
    private List<ConnectionData> connections;
    
    @Data
    public static class WorkflowData {
        private Long id;
        private String workflowName;
        private String workflowCode;
        private String description;
        private String status;
        private String version;
        private String executionMode;
        private Integer maxExecutionTime;
        private String tags;
        private String variables;
    }
    
    @Data
    public static class NodeData {
        private Long id;
        private String nodeCode;
        private String nodeName;
        private Long templateId;
        private Double positionX;
        private Double positionY;
        private Double width;
        private Double height;
        private String style;
        private Boolean isEnabled;
        private Integer executionOrder;
        private String onError;
        private Integer retryCount;
        private String nodeConfig;
        
        // 前端画布节点ID，用于映射连线
        private Integer canvasNodeId;
    }
    
    @Data
    public static class ConnectionData {
        private Long id;
        private Integer sourceCanvasNodeId;  // 前端画布节点ID
        private Integer targetCanvasNodeId;  // 前端画布节点ID
        private String connectionType;
    }
} 