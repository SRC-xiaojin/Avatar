package com.operatorchoreography.generator.controller;

import com.operatorchoreography.generator.model.WorkflowConnections;
import com.operatorchoreography.generator.service.WorkflowConnectionsService;
import com.operatorchoreography.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * <p>
 * 工作流连接表 前端控制器
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Tag(name = "工作流连接管理", description = "工作流连接相关的API接口")
@RestController
@RequestMapping("/workflow-connections")
public class WorkflowConnectionsController {

    @Autowired
    private WorkflowConnectionsService workflowConnectionsService;

    /**
     * 获取工作流的所有连接
     */
    @Operation(summary = "获取工作流的所有连接", description = "根据工作流ID获取该工作流的所有连接关系")
    @GetMapping("/workflow/{workflowId}")
    public Result<List<WorkflowConnections>> getConnectionsByWorkflow(@PathVariable Long workflowId) {
        try {
            List<WorkflowConnections> connections = workflowConnectionsService.getConnectionsByWorkflowId(workflowId);
            return Result.success(connections);
        } catch (Exception e) {
            return Result.error("获取工作流连接时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取工作流连接
     */
    @Operation(summary = "根据ID获取工作流连接", description = "根据连接ID获取具体的工作流连接信息")
    @GetMapping("/{id}")
    public Result<WorkflowConnections> getConnectionById(@PathVariable Long id) {
        WorkflowConnections connection = workflowConnectionsService.getById(id);
        if (connection == null) {
            return Result.error(404, "工作流连接不存在");
        }
        return Result.success(connection);
    }

    /**
     * 创建工作流连接
     */
    @Operation(summary = "创建工作流连接", description = "在工作流中创建新的连接关系")
    @PostMapping
    public Result<WorkflowConnections> createConnection(@RequestBody WorkflowConnections connection) {
        try {
            boolean success = workflowConnectionsService.save(connection);
            if (success) {
                return Result.success(connection, "工作流连接创建成功");
            } else {
                return Result.error("工作流连接创建失败");
            }
        } catch (Exception e) {
            return Result.error("创建工作流连接时发生错误: " + e.getMessage());
        }
    }

    /**
     * 批量创建工作流连接
     */
    @Operation(summary = "批量创建工作流连接", description = "批量创建多个工作流连接关系")
    @PostMapping("/batch")
    public Result<List<WorkflowConnections>> createConnections(@RequestBody List<WorkflowConnections> connections) {
        try {
            boolean success = workflowConnectionsService.saveBatch(connections);
            if (success) {
                return Result.success(connections, "工作流连接批量创建成功");
            } else {
                return Result.error("工作流连接批量创建失败");
            }
        } catch (Exception e) {
            return Result.error("批量创建工作流连接时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新工作流连接
     */
    @Operation(summary = "更新工作流连接", description = "更新指定的工作流连接信息")
    @PutMapping("/{id}")
    public Result<WorkflowConnections> updateConnection(@PathVariable Long id, @RequestBody WorkflowConnections connection) {
        try {
            connection.setId(id);
            boolean success = workflowConnectionsService.updateById(connection);
            if (success) {
                return Result.success(connection, "工作流连接更新成功");
            } else {
                return Result.error(404, "工作流连接不存在或更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新工作流连接时发生错误: " + e.getMessage());
        }
    }

    /**
     * 删除工作流连接
     */
    @Operation(summary = "删除工作流连接", description = "删除指定的工作流连接")
    @DeleteMapping("/{id}")
    public Result<Void> deleteConnection(@PathVariable Long id) {
        try {
            boolean success = workflowConnectionsService.removeById(id);
            if (success) {
                return Result.success(null, "工作流连接删除成功");
            } else {
                return Result.error(404, "工作流连接不存在或删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除工作流连接时发生错误: " + e.getMessage());
        }
    }

    /**
     * 删除工作流的所有连接
     */
    @Operation(summary = "删除工作流的所有连接", description = "删除指定工作流的所有连接关系")
    @DeleteMapping("/workflow/{workflowId}")
    public Result<Void> deleteConnectionsByWorkflow(@PathVariable Long workflowId) {
        try {
            boolean success = workflowConnectionsService.deleteByWorkflowId(workflowId);
            if (success) {
                return Result.success(null, "工作流连接删除成功");
            } else {
                return Result.error("工作流连接删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除工作流连接时发生错误: " + e.getMessage());
        }
    }
}
