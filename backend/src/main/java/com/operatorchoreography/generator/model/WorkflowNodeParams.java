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
 * 节点参数配置表
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Getter
@Setter
@TableName("workflow_node_params")
@Schema(description = "节点参数配置表")
public class WorkflowNodeParams implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "参数配置ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "节点ID")
    @TableField("node_id")
    private Long nodeId;

    @Schema(description = "参数键")
    @TableField("param_key")
    private String paramKey;

    @Schema(description = "参数值")
    @TableField("param_value")
    private String paramValue;

    @Schema(description = "参数类型")
    @TableField("param_type")
    private String paramType;

    @Schema(description = "值来源: STATIC,VARIABLE,EXPRESSION,INPUT")
    @TableField("value_source")
    private String valueSource;

    @Schema(description = "取值表达式")
    @TableField("source_expression")
    private String sourceExpression;

    @Schema(description = "创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
