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
 * 工作流表
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Getter
@Setter
@TableName("workflows")
@Schema(description = "工作流表")
public class Workflows implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "工作流ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "工作流编码")
    @TableField("workflow_code")
    private String workflowCode;

    @Schema(description = "工作流名称")
    @TableField("workflow_name")
    private String workflowName;

    @Schema(description = "工作流描述")
    @TableField("description")
    private String description;

    @Schema(description = "版本号")
    @TableField("version")
    private String version;

    @Schema(description = "状态: DRAFT,PUBLISHED,ARCHIVED")
    @TableField("status")
    private String status;

    @Schema(description = "执行模式: SYNC,ASYNC")
    @TableField("execution_mode")
    private String executionMode;

    @Schema(description = "最大执行时间(秒)")
    @TableField("max_execution_time")
    private Integer maxExecutionTime;

    @Schema(description = "标签")
    @TableField("tags")
    private String tags;

    @Schema(description = "工作流变量")
    @TableField("variables")
    private String variables;

    @Schema(description = "创建人")
    @TableField("created_by")
    private String createdBy;

    @Schema(description = "创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新人")
    @TableField("updated_by")
    private String updatedBy;

    @Schema(description = "更新时间")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
