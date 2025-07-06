package com.operatorchoreography.generator.controller;

import com.operatorchoreography.generator.model.Workflows;
import com.operatorchoreography.generator.service.WorkflowsService;
import com.operatorchoreography.generator.dto.WorkflowSaveRequest;
import com.operatorchoreography.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 工作流表 前端控制器
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Tag(name = "工作流管理", description = "工作流相关的API接口")
@RestController
@RequestMapping("/workflows")
public class WorkflowsController {

    @Autowired
    private WorkflowsService workflowsService;

    /**
     * 获取所有工作流
     */
    @Operation(summary = "获取所有工作流", description = "获取系统中所有工作流的列表")
    @GetMapping
    public Result<List<Workflows>> getAllWorkflows() {
        List<Workflows> workflows = workflowsService.list();
        return Result.success(workflows);
    }

    /**
     * 根据ID获取工作流
     */
    @GetMapping("/{id}")
    public Result<Workflows> getWorkflowById(@PathVariable Long id) {
        Workflows workflow = workflowsService.getById(id);
        if (workflow == null) {
            return Result.error(404, "工作流不存在");
        }
        return Result.success(workflow);
    }

    /**
     * 创建工作流
     */
    @PostMapping
    public Result<Workflows> createWorkflow(@RequestBody Workflows workflow) {
        try {
            boolean success = workflowsService.save(workflow);
            if (success) {
                return Result.success(workflow, "工作流创建成功");
            } else {
                return Result.error("工作流创建失败");
            }
        } catch (Exception e) {
            return Result.error("创建工作流时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新工作流
     */
    @PutMapping("/{id}")
    public Result<Workflows> updateWorkflow(@PathVariable Long id, @RequestBody Workflows workflow) {
        try {
            workflow.setId(id);
            boolean success = workflowsService.updateById(workflow);
            if (success) {
                return Result.success(workflow, "工作流更新成功");
            } else {
                return Result.error(404, "工作流不存在或更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新工作流时发生错误: " + e.getMessage());
        }
    }

    /**
     * 删除工作流
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteWorkflow(@PathVariable Long id) {
        try {
            boolean success = workflowsService.removeById(id);
            if (success) {
                return Result.success(null, "工作流删除成功");
            } else {
                return Result.error(404, "工作流不存在或删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除工作流时发生错误: " + e.getMessage());
        }
    }

    /**
     * 执行工作流
     */
    @PostMapping("/{id}/execute")
    public Result<Map<String, Object>> executeWorkflow(@PathVariable Long id, @RequestBody Map<String, Object> inputData) {
        try {
            Map<String, Object> result = workflowsService.executeWorkflow(id, inputData);
            return Result.success(result, "工作流执行成功");
        } catch (Exception e) {
            return Result.error("执行工作流时发生错误: " + e.getMessage());
        }
    }

    /**
     * 验证工作流
     */
    @PostMapping("/{id}/validate")
    public Result<Map<String, Object>> validateWorkflow(@PathVariable Long id) {
        try {
            boolean isValid = workflowsService.validateWorkflow(id);
            Map<String, Object> result = Map.of(
                "valid", isValid,
                "message", isValid ? "工作流配置有效" : "工作流配置无效"
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("验证工作流时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取工作流执行历史
     */
    @GetMapping("/{id}/history")
    public Result<List<Map<String, Object>>> getWorkflowHistory(@PathVariable Long id) {
        try {
            List<Map<String, Object>> history = workflowsService.getExecutionHistory(id);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("获取工作流执行历史时发生错误: " + e.getMessage());
        }
    }

    /**
     * 测试工作流
     */
    @Operation(summary = "测试工作流", description = "使用指定输入数据测试工作流，通过队列方式顺序执行")
    @PostMapping("/{id}/test")
    public Result<Map<String, Object>> testWorkflow(@PathVariable Long id, @RequestBody Map<String, Object> inputData) {
        try {
            Map<String, Object> result = workflowsService.testWorkflow(id, inputData);
            return Result.success(result, "工作流测试完成");
        } catch (Exception e) {
            return Result.error("测试工作流时发生错误: " + e.getMessage());
        }
    }

    /**
     * 保存完整工作流（包含工作流、节点和连线数据）
     * 支持新建和更新操作，保证事务性
     */
    @Operation(summary = "保存完整工作流", description = "一次性保存工作流基本信息、节点数据和连线数据，保证事务性")
    @PostMapping("/save-complete")
    public Result<Workflows> saveCompleteWorkflow(@RequestBody WorkflowSaveRequest request) {
        try {
            Workflows workflow = workflowsService.saveCompleteWorkflow(request);
            return Result.success(workflow, "工作流保存成功");
        } catch (Exception e) {
            return Result.error("保存工作流时发生错误: " + e.getMessage());
        }
    }
}
