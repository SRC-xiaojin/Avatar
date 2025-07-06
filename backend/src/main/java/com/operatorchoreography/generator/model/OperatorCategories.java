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
 * 算子类别表
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Getter
@Setter
@TableName("operator_categories")
@Schema(description = "算子类别表")
public class OperatorCategories implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "类别ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "类别编码")
    @TableField("category_code")
    private String categoryCode;

    @Schema(description = "类别名称")
    @TableField("category_name")
    private String categoryName;

    @Schema(description = "类别描述")
    @TableField("description")
    private String description;

    @Schema(description = "图标")
    @TableField("icon")
    private String icon;

    @Schema(description = "排序")
    @TableField("sort_order")
    private Integer sortOrder;

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
