package com.operatorchoreography.generator.controller;

import com.operatorchoreography.generator.model.OperatorTemplateParams;
import com.operatorchoreography.generator.service.OperatorTemplateParamsService;
import com.operatorchoreography.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * <p>
 * 算子参数定义表 前端控制器
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Tag(name = "算子参数管理", description = "算子参数定义相关的API接口")
@RestController
@RequestMapping("/operator-template-params")
public class OperatorTemplateParamsController {

    @Autowired
    private OperatorTemplateParamsService operatorTemplateParamsService;

    /**
     * 根据模板ID获取参数列表
     */
    @Operation(summary = "根据模板ID获取参数列表", description = "获取指定模板的所有参数定义")
    @GetMapping("/template/{templateId}")
    public Result<List<OperatorTemplateParams>> getParamsByTemplateId(@PathVariable Long templateId) {
        try {
            List<OperatorTemplateParams> params = operatorTemplateParamsService.getParametersByTemplateId(templateId);
            return Result.success(params);
        } catch (Exception e) {
            return Result.error("获取参数列表时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取参数
     */
    @Operation(summary = "根据ID获取参数", description = "根据参数ID获取具体的参数信息")
    @GetMapping("/{id}")
    public Result<OperatorTemplateParams> getParamById(@PathVariable Long id) {
        OperatorTemplateParams param = operatorTemplateParamsService.getById(id);
        if (param == null) {
            return Result.error(404, "参数不存在");
        }
        return Result.success(param);
    }

    /**
     * 创建参数
     */
    @Operation(summary = "创建参数", description = "创建新的参数定义")
    @PostMapping
    public Result<OperatorTemplateParams> createParam(@RequestBody OperatorTemplateParams param) {
        try {
            boolean success = operatorTemplateParamsService.save(param);
            if (success) {
                return Result.success(param, "参数创建成功");
            } else {
                return Result.error("参数创建失败");
            }
        } catch (Exception e) {
            return Result.error("创建参数时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新参数
     */
    @Operation(summary = "更新参数", description = "更新指定的参数信息")
    @PutMapping("/{id}")
    public Result<OperatorTemplateParams> updateParam(@PathVariable Long id, @RequestBody OperatorTemplateParams param) {
        try {
            param.setId(id);
            boolean success = operatorTemplateParamsService.updateById(param);
            if (success) {
                return Result.success(param, "参数更新成功");
            } else {
                return Result.error(404, "参数不存在或更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新参数时发生错误: " + e.getMessage());
        }
    }

    /**
     * 删除参数
     */
    @Operation(summary = "删除参数", description = "删除指定的参数")
    @DeleteMapping("/{id}")
    public Result<Void> deleteParam(@PathVariable Long id) {
        try {
            boolean success = operatorTemplateParamsService.removeById(id);
            if (success) {
                return Result.success(null, "参数删除成功");
            } else {
                return Result.error(404, "参数不存在或删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除参数时发生错误: " + e.getMessage());
        }
    }

    /**
     * 批量保存模板参数
     */
    @Operation(summary = "批量保存模板参数", description = "批量保存或更新模板的参数定义")
    @PostMapping("/template/{templateId}/batch")
    public Result<List<OperatorTemplateParams>> batchSaveParams(@PathVariable Long templateId, @RequestBody List<OperatorTemplateParams> params) {
        try {
            // 设置模板ID
            params.forEach(param -> param.setTemplateId(templateId));
            
            // 先删除该模板的所有参数
            operatorTemplateParamsService.removeByTemplateId(templateId);
            
            // 批量保存新参数
            if (!params.isEmpty()) {
                boolean success = operatorTemplateParamsService.saveBatch(params);
                if (success) {
                    return Result.success(params, "参数批量保存成功");
                } else {
                    return Result.error("参数批量保存失败");
                }
            }
            
            return Result.success(params, "参数批量保存成功");
        } catch (Exception e) {
            return Result.error("批量保存参数时发生错误: " + e.getMessage());
        }
    }
}
