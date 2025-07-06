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
 * 算子参数定义表
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Getter
@Setter
@TableName("operator_template_params")
@Schema(description = "算子参数定义表")
public class OperatorTemplateParams implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "参数ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "模板ID")
    @TableField("template_id")
    private Long templateId;

    @Schema(description = "参数键")
    @TableField("param_key")
    private String paramKey;

    @Schema(description = "参数名称")
    @TableField("param_name")
    private String paramName;

    @Schema(description = "参数类型: string,number,boolean,object,array")
    @TableField("param_type")
    private String paramType;

    @Schema(description = "参数类别: input,output,config")
    @TableField("param_category")
    private String paramCategory;

    @Schema(description = "是否必填")
    @TableField("is_required")
    private Boolean isRequired;

    @Schema(description = "默认值")
    @TableField("default_value")
    private String defaultValue;

    @Schema(description = "验证规则")
    @TableField("validation_rules")
    private String validationRules;

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
