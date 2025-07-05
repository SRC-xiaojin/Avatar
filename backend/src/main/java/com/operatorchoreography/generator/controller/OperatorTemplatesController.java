package com.operatorchoreography.generator.controller;

import com.operatorchoreography.generator.model.OperatorTemplates;
import com.operatorchoreography.generator.model.OperatorTemplateParams;
import com.operatorchoreography.generator.service.OperatorTemplatesService;
import com.operatorchoreography.generator.service.OperatorTemplateParamsService;
import com.operatorchoreography.common.Result;
import com.operatorchoreography.executor.ExecutorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * <p>
 * 算子模板表 前端控制器
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Tag(name = "算子模板管理", description = "算子模板相关的API接口")
@Slf4j
@RestController
@RequestMapping("/operator-templates")
public class OperatorTemplatesController {

    @Autowired
    private OperatorTemplatesService operatorTemplatesService;

    @Autowired
    private OperatorTemplateParamsService operatorTemplateParamsService;

    /**
     * 获取所有算子模板
     */
    @Operation(summary = "获取所有算子模板", description = "获取系统中所有算子模板的列表")
    @GetMapping
    public Result<List<OperatorTemplates>> getAllTemplates() {
        List<OperatorTemplates> templates = operatorTemplatesService.list();
        return Result.success(templates);
    }

    /**
     * 根据类别获取算子模板
     */
    @Operation(summary = "根据类别获取算子模板", description = "根据类别ID获取该类别下的所有算子模板")
    @GetMapping("/category/{categoryId}")
    public Result<List<OperatorTemplates>> getTemplatesByCategory(@PathVariable Long categoryId) {
        try {
            List<OperatorTemplates> templates = operatorTemplatesService.getTemplatesByCategory(categoryId);
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("获取算子模板时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取算子模板
     */
    @Operation(summary = "根据ID获取算子模板", description = "根据模板ID获取具体的算子模板信息")
    @GetMapping("/{id}")
    public Result<OperatorTemplates> getTemplateById(@PathVariable Long id) {
        OperatorTemplates template = operatorTemplatesService.getById(id);
        if (template == null) {
            return Result.error(404, "算子模板不存在");
        }
        return Result.success(template);
    }

    /**
     * 根据模板编码获取算子模板
     */
    @Operation(summary = "根据模板编码获取算子模板", description = "根据模板编码获取算子模板信息")
    @GetMapping("/code/{templateCode}")
    public Result<OperatorTemplates> getTemplateByCode(@PathVariable String templateCode) {
        try {
            OperatorTemplates template = operatorTemplatesService.getByTemplateCode(templateCode);
            if (template == null) {
                return Result.error(404, "算子模板不存在");
            }
            return Result.success(template);
        } catch (Exception e) {
            return Result.error("获取算子模板时发生错误: " + e.getMessage());
        }
    }

    /**
     * 创建算子模板
     */
    @Operation(summary = "创建算子模板", description = "创建新的算子模板")
    @PostMapping
    public Result<OperatorTemplates> createTemplate(@RequestBody OperatorTemplates template) {
        try {
            boolean success = operatorTemplatesService.save(template);
            if (success) {
                return Result.success(template, "算子模板创建成功");
            } else {
                return Result.error("算子模板创建失败");
            }
        } catch (Exception e) {
            return Result.error("创建算子模板时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新算子模板
     */
    @Operation(summary = "更新算子模板", description = "更新指定的算子模板信息")
    @PutMapping("/{id}")
    public Result<OperatorTemplates> updateTemplate(@PathVariable Long id, @RequestBody OperatorTemplates template) {
        try {
            template.setId(id);
            boolean success = operatorTemplatesService.updateById(template);
            if (success) {
                return Result.success(template, "算子模板更新成功");
            } else {
                return Result.error(404, "算子模板不存在或更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新算子模板时发生错误: " + e.getMessage());
        }
    }

    /**
     * 删除算子模板
     */
    @Operation(summary = "删除算子模板", description = "删除指定的算子模板")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        try {
            boolean success = operatorTemplatesService.removeById(id);
            if (success) {
                return Result.success(null, "算子模板删除成功");
            } else {
                return Result.error(404, "算子模板不存在或删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除算子模板时发生错误: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用算子模板
     */
    @Operation(summary = "启用/禁用算子模板", description = "切换算子模板的启用状态")
    @PutMapping("/{id}/toggle-status")
    public Result<OperatorTemplates> toggleTemplateStatus(@PathVariable Long id) {
        try {
            OperatorTemplates template = operatorTemplatesService.getById(id);
            if (template == null) {
                return Result.error(404, "算子模板不存在");
            }
            
            template.setStatus(!template.getStatus());
            boolean success = operatorTemplatesService.updateById(template);
            if (success) {
                String message = template.getStatus() ? "算子模板已启用" : "算子模板已禁用";
                return Result.success(template, message);
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新状态时发生错误: " + e.getMessage());
        }
    }

    /**
     * 测试算子模板执行
     */
    @Operation(summary = "测试算子模板执行", description = "使用提供的输入数据测试算子模板的执行")
    @PostMapping("/{id}/test")
    public Result<ExecutorResult> testTemplateExecution(@PathVariable Long id, @RequestBody Map<String, Object> inputData) {
        log.info("接收到算子模板测试请求: templateId={}", id);
        log.debug("请求参数: {}", inputData);
        
        try {
            // 执行测试
            ExecutorResult result = operatorTemplatesService.testTemplateExecution(id, inputData);
            
            log.info("算子模板测试完成: templateId={}, 执行状态={}, 耗时={}ms", 
                    id, result.getStatus(), result.getExecutionTimeMs());
            
            String message = result.isSuccess() ? 
                    "算子模板测试成功，耗时: " + result.getExecutionTimeMs() + "ms" :
                    "算子模板测试失败: " + result.getErrorMessage();
            
            return Result.success(result, message);
        } catch (Exception e) {
            log.error("算子模板测试失败: templateId={}", id, e);
            return Result.error("测试算子模板时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取算子模板的参数定义
     */
    @Operation(summary = "获取算子模板的参数定义", description = "获取指定算子模板的所有参数定义")
    @GetMapping("/{id}/params")
    public Result<List<Map<String, Object>>> getTemplateParams(@PathVariable Long id) {
        try {
            List<Map<String, Object>> params = operatorTemplatesService.getTemplateParams(id);
            return Result.success(params);
        } catch (Exception e) {
            return Result.error("获取算子参数时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取算子模板详细信息，包括参数
     */
    @GetMapping("/{id}/details")
    public Result<Map<String, Object>> getTemplateDetails(@PathVariable Long id) {
        try {
            OperatorTemplates template = operatorTemplatesService.getById(id);
            if (template == null) {
                return Result.error(404, "算子模板不存在");
            }

            List<OperatorTemplateParams> params = operatorTemplateParamsService.getParametersByTemplateId(id);
            
            Map<String, Object> details = new HashMap<>();
            details.put("template", template);
            
            // 根据参数键分组
            Map<String, List<OperatorTemplateParams>> paramsByKey = params.stream()
                .collect(java.util.stream.Collectors.groupingBy(OperatorTemplateParams::getParamKey));
            details.put("params", paramsByKey);
            
            return Result.success(details);
        } catch (Exception e) {
            return Result.error("获取算子模板详情时发生错误: " + e.getMessage());
        }
    }
}
