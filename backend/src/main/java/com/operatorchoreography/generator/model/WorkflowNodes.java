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
 * 工作流节点表
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Getter
@Setter
@TableName("workflow_nodes")
@Schema(description = "工作流节点表")
public class WorkflowNodes implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "节点ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "工作流ID")
    @TableField("workflow_id")
    private Long workflowId;

    @Schema(description = "节点编码")
    @TableField("node_code")
    private String nodeCode;

    @Schema(description = "节点名称")
    @TableField("node_name")
    private String nodeName;

    @Schema(description = "算子模板ID")
    @TableField("template_id")
    private Long templateId;

    @Schema(description = "X坐标")
    @TableField("position_x")
    private Double positionX;

    @Schema(description = "Y坐标")
    @TableField("position_y")
    private Double positionY;

    @Schema(description = "宽度")
    @TableField("width")
    private Double width;

    @Schema(description = "高度")
    @TableField("height")
    private Double height;

    @Schema(description = "样式配置")
    @TableField("style")
    private String style;

    @Schema(description = "是否启用")
    @TableField("is_enabled")
    private Boolean isEnabled;

    @Schema(description = "执行顺序")
    @TableField("execution_order")
    private Integer executionOrder;

    @Schema(description = "错误处理: STOP,CONTINUE,RETRY")
    @TableField("on_error")
    private String onError;

    @Schema(description = "重试次数")
    @TableField("retry_count")
    private Integer retryCount;

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
