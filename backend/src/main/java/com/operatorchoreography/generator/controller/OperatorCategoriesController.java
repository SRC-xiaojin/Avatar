package com.operatorchoreography.generator.controller;

import com.operatorchoreography.generator.model.OperatorCategories;
import com.operatorchoreography.generator.service.OperatorCategoriesService;
import com.operatorchoreography.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * <p>
 * 算子类别表 前端控制器
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Tag(name = "算子类别管理", description = "算子类别相关的API接口")
@RestController
@RequestMapping("/operator-categories")
public class OperatorCategoriesController {

    @Autowired
    private OperatorCategoriesService operatorCategoriesService;

    /**
     * 获取所有算子类别
     */
    @Operation(summary = "获取所有算子类别", description = "获取系统中所有算子类别的列表")
    @GetMapping
    public Result<List<OperatorCategories>> getAllCategories() {
        List<OperatorCategories> categories = operatorCategoriesService.list();
        return Result.success(categories);
    }

    /**
     * 根据ID获取算子类别
     */
    @Operation(summary = "根据ID获取算子类别", description = "根据类别ID获取具体的算子类别信息")
    @GetMapping("/{id}")
    public Result<OperatorCategories> getCategoryById(@PathVariable Long id) {
        OperatorCategories category = operatorCategoriesService.getById(id);
        if (category == null) {
            return Result.error(404, "算子类别不存在");
        }
        return Result.success(category);
    }

    /**
     * 创建算子类别
     */
    @Operation(summary = "创建算子类别", description = "创建新的算子类别")
    @PostMapping
    public Result<OperatorCategories> createCategory(@RequestBody OperatorCategories category) {
        try {
            boolean success = operatorCategoriesService.save(category);
            if (success) {
                return Result.success(category, "算子类别创建成功");
            } else {
                return Result.error("算子类别创建失败");
            }
        } catch (Exception e) {
            return Result.error("创建算子类别时发生错误: " + e.getMessage());
        }
    }

    /**
     * 更新算子类别
     */
    @Operation(summary = "更新算子类别", description = "更新指定的算子类别信息")
    @PutMapping("/{id}")
    public Result<OperatorCategories> updateCategory(@PathVariable Long id, @RequestBody OperatorCategories category) {
        try {
            category.setId(id);
            boolean success = operatorCategoriesService.updateById(category);
            if (success) {
                return Result.success(category, "算子类别更新成功");
            } else {
                return Result.error(404, "算子类别不存在或更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新算子类别时发生错误: " + e.getMessage());
        }
    }

    /**
     * 删除算子类别
     */
    @Operation(summary = "删除算子类别", description = "删除指定的算子类别")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        try {
            boolean success = operatorCategoriesService.removeById(id);
            if (success) {
                return Result.success(null, "算子类别删除成功");
            } else {
                return Result.error(404, "算子类别不存在或删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除算子类别时发生错误: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用算子类别
     */
    @Operation(summary = "启用/禁用算子类别", description = "切换算子类别的启用状态")
    @PutMapping("/{id}/toggle-status")
    public Result<OperatorCategories> toggleCategoryStatus(@PathVariable Long id) {
        try {
            OperatorCategories category = operatorCategoriesService.getById(id);
            if (category == null) {
                return Result.error(404, "算子类别不存在");
            }
            
            category.setStatus(!category.getStatus());
            boolean success = operatorCategoriesService.updateById(category);
            if (success) {
                String message = category.getStatus() ? "算子类别已启用" : "算子类别已禁用";
                return Result.success(category, message);
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新状态时发生错误: " + e.getMessage());
        }
    }
}
