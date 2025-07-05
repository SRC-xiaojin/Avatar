package com.operatorchoreography.generator.controller;

import com.operatorchoreography.generator.model.WorkflowNodes;
import com.operatorchoreography.generator.service.WorkflowNodesService;
import com.operatorchoreography.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 工作流节点表 前端控制器
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Tag(name = "工作流节点管理", description = "工作流节点相关的API接口")
@RestController
@RequestMapping("/workflow-nodes")
public class WorkflowNodesController {

    @Autowired
    private WorkflowNodesService workflowNodesService;

    /**
     * 获取工作流的所有节点
     */
    @Operation(summary = "获取工作流的所有节点", description = "根据工作流ID获取该工作流的所有节点")
    @GetMapping("/workflow/{workflowId}")
    public Result<List<WorkflowNodes>> getNodesByWorkflow(@PathVariable Long workflowId) {
        try {
            List<WorkflowNodes> nodes = workflowNodesService.getNodesByWorkflowId(workflowId);
            return Result.success(nodes);
        } catch (Exception e) {
            return Result.error("获取工作流节点时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取工作流节点
     */
    @Operation(summary = "根据ID获取工作流节点", description = "根据节点ID获取具体的工作流节点信息")
    @GetMapping("/{id}")
    public Result<WorkflowNodes> getNodeById(@PathVariable Long id) {
        WorkflowNodes node = workflowNodesService.getById(id);
        if (node == null) {
            return Result.error(404, "工作流节点不存在");
        }
        return Result.success(node);
    }

    /**
     * 创建工作流节点
     */
    @Operation(summary = "创建工作流节点", description = "在工作流中创建新的节点")
    @PostMapping
    public Result<WorkflowNodes> createNode(@RequestBody WorkflowNodes node) {
        try {
            boolean success = workflowNodesService.save(node);
            if (success) {
                return Result.success(node, "工作流节点创建成功");
            } else {
                return Result.error("工作流节点创建失败");
            }
        } catch (Exception e) {
            return Result.error("创建工作流节点时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新工作流节点
     */
    @Operation(summary = "更新工作流节点", description = "更新指定的工作流节点信息")
    @PutMapping("/{id}")
    public Result<WorkflowNodes> updateNode(@PathVariable Long id, @RequestBody WorkflowNodes node) {
        try {
            node.setId(id);
            boolean success = workflowNodesService.updateById(node);
            if (success) {
                return Result.success(node, "工作流节点更新成功");
            } else {
                return Result.error(404, "工作流节点不存在或更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新工作流节点时发生错误: " + e.getMessage());
        }
    }

    /**
     * 删除工作流节点
     */
    @Operation(summary = "删除工作流节点", description = "删除指定的工作流节点")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNode(@PathVariable Long id) {
        try {
            boolean success = workflowNodesService.removeById(id);
            if (success) {
                return Result.success(null, "工作流节点删除成功");
            } else {
                return Result.error(404, "工作流节点不存在或删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除工作流节点时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新节点位置
     */
    @Operation(summary = "更新节点位置", description = "更新工作流节点在画布上的位置")
    @PutMapping("/{id}/position")
    public Result<WorkflowNodes> updateNodePosition(@PathVariable Long id, @RequestBody Map<String, Object> position) {
        try {
            WorkflowNodes node = workflowNodesService.getById(id);
            if (node == null) {
                return Result.error(404, "工作流节点不存在");
            }
            
            if (position.containsKey("positionX")) {
                node.setPositionX(Double.valueOf(position.get("positionX").toString()));
            }
            if (position.containsKey("positionY")) {
                node.setPositionY(Double.valueOf(position.get("positionY").toString()));
            }
            
            boolean success = workflowNodesService.updateById(node);
            if (success) {
                return Result.success(node, "节点位置更新成功");
            } else {
                return Result.error("节点位置更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新节点位置时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取节点的参数配置
     */
    @Operation(summary = "获取节点的参数配置", description = "获取指定节点的所有参数配置")
    @GetMapping("/{id}/params")
    public Result<List<Map<String, Object>>> getNodeParams(@PathVariable Long id) {
        try {
            List<Map<String, Object>> params = workflowNodesService.getNodeParams(id);
            return Result.success(params);
        } catch (Exception e) {
            return Result.error("获取节点参数时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新节点的参数配置
     */
    @Operation(summary = "更新节点的参数配置", description = "批量更新指定节点的参数配置")
    @PutMapping("/{id}/params")
    public Result<String> updateNodeParams(@PathVariable Long id, @RequestBody List<Map<String, Object>> params) {
        try {
            boolean success = workflowNodesService.updateNodeParams(id, params);
            if (success) {
                return Result.success("节点参数更新成功");
            } else {
                return Result.error("节点参数更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新节点参数时发生错误: " + e.getMessage());
        }
    }
}
