package com.operatorchoreography.generator.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 工作流连接表
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Getter
@Setter
@TableName("workflow_connections")
@Schema(description = "工作流连接表")
public class WorkflowConnections implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "连接ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "工作流ID")
    @TableField("workflow_id")
    private Long workflowId;

    @Schema(description = "源节点ID")
    @TableField("source_node_id")
    private Long sourceNodeId;

    @Schema(description = "源端口")
    @TableField("source_port")
    private String sourcePort;

    @Schema(description = "目标节点ID")
    @TableField("target_node_id")
    private Long targetNodeId;

    @Schema(description = "目标端口")
    @TableField("target_port")
    private String targetPort;

    @Schema(description = "连接类型: DATA,CONTROL,ERROR")
    @TableField("connection_type")
    private String connectionType;

    @Schema(description = "连接条件表达式")
    @TableField("condition_expression")
    private String conditionExpression;

    @Schema(description = "数据映射配置")
    @TableField("data_mapping")
    private String dataMapping;

    @Schema(description = "连接线样式")
    @TableField("style")
    private String style;

    @Schema(description = "是否删除")
    @TableField("if_delete")
    private Boolean ifDelete;

    @Schema(description = "创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
