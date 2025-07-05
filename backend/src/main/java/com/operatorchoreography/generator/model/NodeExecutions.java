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
 * 节点执行记录表
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Getter
@Setter
@TableName("node_executions")
@Schema(description = "节点执行记录表")
public class NodeExecutions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "节点执行ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "工作流执行ID")
    @TableField("execution_id")
    private String executionId;

    @Schema(description = "节点ID")
    @TableField("node_id")
    private Long nodeId;

    @Schema(description = "执行顺序")
    @TableField("execution_order")
    private Integer executionOrder;

    @Schema(description = "状态: WAITING,RUNNING,SUCCESS,FAILED,SKIPPED")
    @TableField("status")
    private String status;

    @Schema(description = "开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @Schema(description = "执行时长(毫秒)")
    @TableField("duration_ms")
    private Long durationMs;

    @Schema(description = "输入数据")
    @TableField("input_data")
    private String inputData;

    @Schema(description = "输出数据")
    @TableField("output_data")
    private String outputData;

    @Schema(description = "错误信息")
    @TableField("error_message")
    private String errorMessage;

    @Schema(description = "错误堆栈")
    @TableField("error_stack")
    private String errorStack;

    @Schema(description = "已重试次数")
    @TableField("retry_count")
    private Integer retryCount;

    @Schema(description = "创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
