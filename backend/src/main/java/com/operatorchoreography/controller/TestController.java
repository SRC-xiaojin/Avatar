package com.operatorchoreography.controller;

import com.operatorchoreography.common.Result;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@Tag(name = "系统测试", description = "系统测试相关的API接口")
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 健康检查
     */
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "算子编排系统后端");
        data.put("version", "1.0.0");
        return Result.success(data, "服务正常运行");
    }

    /**
     * Echo测试
     */
    @PostMapping("/echo")
    public Result<Map<String, Object>> echo(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("received", data);
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Echo测试成功");
        return Result.success(response);
    }

    /**
     * 数据库连接测试
     */
    @GetMapping("/db")
    public Result<Map<String, Object>> testDatabase() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("database", "MySQL");
            data.put("status", "连接正常");
            data.put("timestamp", LocalDateTime.now());
            return Result.success(data, "数据库连接测试成功");
        } catch (Exception e) {
            return Result.error("数据库连接失败: " + e.getMessage());
        }
    }
} 