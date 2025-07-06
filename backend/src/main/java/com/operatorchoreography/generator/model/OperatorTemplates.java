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
 * 算子模板表
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Getter
@Setter
@TableName("operator_templates")
@Schema(description = "算子模板表")
public class OperatorTemplates implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "模板ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "所属类别")
    @TableField("category_id")
    private Long categoryId;

    @Schema(description = "模板编码")
    @TableField("template_code")
    private String templateCode;

    @Schema(description = "模板名称")
    @TableField("template_name")
    private String templateName;

    @Schema(description = "模板描述")
    @TableField("description")
    private String description;

    @Schema(description = "图标")
    @TableField("icon")
    private String icon;

    @Schema(description = "版本号")
    @TableField("version")
    private String version;

    @Schema(description = "执行器类名")
    @TableField("executor_class")
    private String executorClass;

    @Schema(description = "执行方法")
    @TableField("executor_method")
    private String executorMethod;

    @Schema(description = "输入数据结构定义")
    @TableField("input_schema")
    private String inputSchema;

    @Schema(description = "输出数据结构定义")
    @TableField("output_schema")
    private String outputSchema;

    @Schema(description = "是否异步执行")
    @TableField("is_async")
    private Boolean isAsync;

    @Schema(description = "超时时间(秒)")
    @TableField("timeout_seconds")
    private Integer timeoutSeconds;

    @Schema(description = "重试次数")
    @TableField("retry_count")
    private Integer retryCount;

    @Schema(description = "状态: 1启用 0禁用")
    @TableField("status")
    private Boolean status;

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
