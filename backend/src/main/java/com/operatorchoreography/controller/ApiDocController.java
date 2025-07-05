package com.operatorchoreography.controller;

import com.operatorchoreography.common.Result;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * API文档测试控制器
 */
@Tag(name = "API文档测试", description = "用于测试OpenAPI配置是否正常")
@RestController
@RequestMapping("/api-test")
public class ApiDocController {

    @Operation(summary = "测试接口", description = "用于验证OpenAPI配置和Swagger UI是否正常工作")
    @GetMapping("/test")
    public Result<Map<String, Object>> test() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "OpenAPI配置正常");
        data.put("swagger", "工作正常");
        data.put("timestamp", System.currentTimeMillis());
        return Result.success(data, "API文档测试成功");
    }

    @Operation(summary = "版本信息", description = "获取API版本信息")
    @GetMapping("/version")
    public Result<Map<String, Object>> version() {
        Map<String, Object> data = new HashMap<>();
        data.put("apiVersion", "1.0.0");
        data.put("springdocVersion", "2.3.0");
        data.put("description", "算子编排系统API");
        return Result.success(data);
    }
} 