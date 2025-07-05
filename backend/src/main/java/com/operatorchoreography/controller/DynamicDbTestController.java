package com.operatorchoreography.controller;

import com.operatorchoreography.common.Result;
import com.operatorchoreography.service.DynamicDataSourceService;
import com.operatorchoreography.generator.service.OperatorTemplateParamsService;
import com.operatorchoreography.generator.model.OperatorTemplateParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态数据库测试控制器
 * 测试动态数据源管理功能，使用基于URL的缓存键策略
 * 缓存键格式：host:port:database
 * 只缓存DataSource，JdbcTemplate实时创建
 */
@Slf4j
@Tag(name = "动态数据库测试", description = "测试动态数据库连接功能")
@RestController
@RequestMapping("/api/dynamic-db-test")
public class DynamicDbTestController {

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    @Autowired
    private OperatorTemplateParamsService operatorTemplateParamsService;

    /**
     * 测试通过模板ID获取数据库连接并执行查询
     * DataSource使用 host:port:database 作为缓存键，JdbcTemplate实时创建
     */
    @Operation(summary = "测试模板数据库连接")
    @GetMapping("/test-template/{templateId}")
    public Result<Object> testTemplateConnection(@PathVariable Long templateId,
                                                @RequestParam(defaultValue = "SELECT 1 as test_value") String sql) {
        try {
            log.info("测试模板数据库连接: templateId={}, sql={}", templateId, sql);
            
            // 获取模板参数
            List<OperatorTemplateParams> params = operatorTemplateParamsService.getParametersByTemplateId(templateId);
            if (params == null || params.isEmpty()) {
                throw new RuntimeException("未找到模板参数配置: templateId=" + templateId);
            }
            
            JdbcTemplate jdbcTemplate = dynamicDataSourceService.getJdbcTemplate(params);
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            
            Map<String, Object> response = new HashMap<>();
            response.put("templateId", templateId);
            response.put("sql", sql);
            response.put("result", result);
            response.put("message", "模板数据库连接测试成功");
            
            log.info("模板数据库连接测试成功: templateId={}, 结果数量={}", templateId, result.size());
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("模板数据库连接测试失败: templateId={}", templateId, e);
            return Result.error("模板数据库连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试自定义数据源连接
     * DataSource使用 host:port:database 作为缓存键，JdbcTemplate实时创建
     */
    @Operation(summary = "测试自定义数据源连接")
    @PostMapping("/test-custom")
    public Result<Object> testCustomConnection(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> datasourceConfig = (Map<String, Object>) request.get("datasourceConfig");
            String sql = (String) request.getOrDefault("sql", "SELECT 1 as test_value");
            String identifier = (String) request.getOrDefault("identifier", "custom_test");
            
            log.info("测试自定义数据库连接: identifier={}, sql={}", identifier, sql);
            
            JdbcTemplate jdbcTemplate = dynamicDataSourceService.getJdbcTemplate(datasourceConfig, identifier);
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            
            Map<String, Object> response = new HashMap<>();
            response.put("identifier", identifier);
            response.put("sql", sql);
            response.put("result", result);
            response.put("message", "自定义数据库连接测试成功");
            
            log.info("自定义数据库连接测试成功: identifier={}, 结果数量={}", identifier, result.size());
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("自定义数据库连接测试失败", e);
            return Result.error("自定义数据库连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存信息
     * 显示当前所有缓存的DataSource（按 host:port:database 分组）
     * JdbcTemplate不缓存，每次实时创建
     */
    @Operation(summary = "获取缓存信息")
    @GetMapping("/cache-info")
    public Result<Object> getCacheInfo() {
        try {
            log.info("获取数据源缓存信息");
            
            Map<String, Object> cacheInfo = dynamicDataSourceService.getCacheInfo();
            
            return Result.success(cacheInfo);
            
        } catch (Exception e) {
            log.error("获取缓存信息失败", e);
            return Result.error("获取缓存信息失败: " + e.getMessage());
        }
    }

    /**
     * 清理所有DataSource缓存
     */
    @Operation(summary = "清理所有缓存")
    @PostMapping("/clear-cache")
    public Result<Object> clearCache() {
        try {
            log.info("清理所有数据源缓存");
            
            dynamicDataSourceService.clearCache();
            
            Map<String, Object> response = Map.of(
                "message", "所有DataSource缓存已清理，JdbcTemplate无需清理（实时创建）",
                "timestamp", System.currentTimeMillis()
            );
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("清理缓存失败", e);
            return Result.error("清理缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清理指定缓存键的DataSource连接
     * @param cacheKey 缓存键，格式：host:port:database（通过查询参数传递）
     */
    @Operation(summary = "清理指定缓存键的连接")
    @DeleteMapping("/cache")
    public Result<Object> clearCacheByKey(@RequestParam String cacheKey) {
        try {
            log.info("清理指定缓存键: cacheKey={}", cacheKey);
            
            boolean removed = dynamicDataSourceService.clearCacheByKey(cacheKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("cacheKey", cacheKey);
            response.put("removed", removed);
            response.put("message", removed ? "DataSource缓存键清理成功" : "缓存键不存在");
            response.put("note", "JdbcTemplate实时创建，无需清理");
            response.put("timestamp", System.currentTimeMillis());
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("清理指定缓存键失败: cacheKey={}", cacheKey, e);
            return Result.error("清理指定缓存键失败: " + e.getMessage());
        }
    }

    /**
     * 测试多个模板使用相同数据库服务器的DataSource复用
     * JdbcTemplate每次都实时创建
     */
    @Operation(summary = "测试多个模板使用相同数据库服务器的连接复用")
    @PostMapping("/test-connection-reuse")
    public Result<Object> testConnectionReuse(@RequestBody Map<String, Object> request) {
        try {
            List<Long> templateIds = (List<Long>) request.get("templateIds");
            String sql = (String) request.getOrDefault("sql", "SELECT 1 as test_value");
            
            log.info("测试连接复用: templateIds={}, sql={}", templateIds, sql);
            
            Map<String, Object> response = new HashMap<>();
            Map<Long, Object> results = new HashMap<>();
            
            // 获取初始缓存信息
            Map<String, Object> initialCacheInfo = dynamicDataSourceService.getCacheInfo();
            
            // 依次测试每个模板
            for (Long templateId : templateIds) {
                try {
                    // 获取模板参数
                    List<OperatorTemplateParams> params = operatorTemplateParamsService.getParametersByTemplateId(templateId);
                    if (params == null || params.isEmpty()) {
                        throw new RuntimeException("未找到模板参数配置: templateId=" + templateId);
                    }
                    
                    JdbcTemplate jdbcTemplate = dynamicDataSourceService.getJdbcTemplate(params);
                    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
                    
                    Map<String, Object> templateResult = new HashMap<>();
                    templateResult.put("success", true);
                    templateResult.put("result", result);
                    templateResult.put("message", "成功");
                    results.put(templateId, templateResult);
                    
                } catch (Exception e) {
                    Map<String, Object> templateResult = new HashMap<>();
                    templateResult.put("success", false);
                    templateResult.put("error", e.getMessage());
                    results.put(templateId, templateResult);
                }
            }
            
            // 获取最终缓存信息
            Map<String, Object> finalCacheInfo = dynamicDataSourceService.getCacheInfo();
            
            response.put("templateIds", templateIds);
            response.put("sql", sql);
            response.put("results", results);
            response.put("initialCacheInfo", initialCacheInfo);
            response.put("finalCacheInfo", finalCacheInfo);
            response.put("message", "DataSource复用测试完成，JdbcTemplate每次实时创建");
            response.put("note", "观察DataSource缓存变化，JdbcTemplate不缓存");
            
            log.info("连接复用测试完成: templateIds={}, 初始缓存={}, 最终缓存={}", 
                    templateIds, 
                    initialCacheInfo.get("dataSourceCacheSize"),
                    finalCacheInfo.get("dataSourceCacheSize"));
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("连接复用测试失败", e);
            return Result.error("连接复用测试失败: " + e.getMessage());
        }
    }

    /**
     * 示例：获取测试数据源配置
     */
    @Operation(summary = "获取测试数据源配置")
    @GetMapping("/example-config")
    public Result<Object> getExampleConfig() {
        Map<String, Object> exampleConfig = Map.of(
            "datasourceConfig", Map.of(
                "url", "jdbc:mysql://192.168.1.100:3306/test_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8",
                "driver-class-name", "com.mysql.cj.jdbc.Driver",
                "username", "test_user",
                "password", "test_password",
                "maxPoolSize", 5,
                "minPoolSize", 1
            ),
            "sql", "SELECT NOW() as current_time, VERSION() as mysql_version",
            "identifier", "example_test",
            "description", "DataSource缓存键: 192.168.1.100:3306:test_db，JdbcTemplate实时创建",
            "cacheStrategy", Map.of(
                "DataSource", "缓存复用，减少连接池创建开销",
                "JdbcTemplate", "实时创建，轻量级无状态对象"
            ),
            "supportedUrlFormats", Map.of(
                "MySQL", "jdbc:mysql://host:port/database",
                "PostgreSQL", "jdbc:postgresql://host:port/database", 
                "SQLServer", "jdbc:sqlserver://host:port;databaseName=database",
                "Oracle", "jdbc:oracle:thin:@host:port:database"
            )
        );
        
        return Result.success(exampleConfig);
    }

    /**
     * 测试MySQL插入(带冲突处理)功能
     * 使用ON DUPLICATE KEY UPDATE解决主键或索引冲突
     */
    @Operation(summary = "测试MySQL插入(带冲突处理)")
    @PostMapping("/test-mysql-upsert/{templateId}")
    public Result<Object> testMySqlUpsert(@PathVariable Long templateId, 
                                         @RequestBody Map<String, Object> inputData) {
        try {
            log.info("测试MySQL插入(带冲突处理): templateId={}, inputData={}", templateId, inputData);
            
            // 调用MySQL插入执行器的upsert方法
            // 这里需要通过ExecutorManager调用，但由于我们没有ExecutorManager，直接调用服务
            // 实际应用中应该通过ExecutorManager.executeOperator调用
            
            // 构造测试用例说明
            Map<String, Object> response = new HashMap<>();
            response.put("templateId", templateId);
            response.put("inputData", inputData);
            response.put("message", "MySQL插入(带冲突处理)测试接口");
            response.put("note", "此接口用于演示功能，实际执行需要通过ExecutorManager");
            
            // 输入数据格式说明
            Map<String, Object> inputFormat = Map.of(
                "insertData", Map.of(
                    "id", 1,
                    "name", "张三",
                    "email", "zhangsan@example.com",
                    "age", 25,
                    "created_at", "2024-01-01 10:00:00"
                ),
                "updateData", Map.of(
                    "name", "张三(更新)",
                    "email", "zhangsan_new@example.com", 
                    "age", 26,
                    "updated_at", "2024-01-02 10:00:00"
                ),
                "excludeFields", List.of("id", "created_at"),
                "table", "users"
            );
            
            // 生成的SQL示例
            String sqlExample = """
                INSERT INTO `users` (`id`, `name`, `email`, `age`, `created_at`) 
                VALUES (?, ?, ?, ?, ?) 
                ON DUPLICATE KEY UPDATE 
                `name` = ?, `email` = ?, `age` = ?, `updated_at` = ?
                """;
            
            response.put("inputFormat", inputFormat);
            response.put("sqlExample", sqlExample);
            response.put("description", Map.of(
                "insertData", "要插入的数据，必填",
                "updateData", "冲突时更新的数据，可选，默认使用insertData",
                "excludeFields", "排除更新的字段，可选，通常包含主键、创建时间等",
                "table", "表名，可选，优先使用inputData中的值，否则使用模板参数"
            ));
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("MySQL插入(带冲突处理)测试失败: templateId={}", templateId, e);
            return Result.error("MySQL插入(带冲突处理)测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取MySQL插入(带冲突处理)的示例配置
     */
    @Operation(summary = "获取MySQL插入(带冲突处理)示例")
    @GetMapping("/mysql-upsert-example")
    public Result<Object> getMySqlUpsertExample() {
        Map<String, Object> example = Map.of(
            "description", "MySQL插入(带冲突处理)功能演示",
            "features", List.of(
                "自动构建INSERT语句",
                "ON DUPLICATE KEY UPDATE处理冲突",
                "支持排除特定字段更新",
                "支持自定义更新数据"
            ),
            "inputDataExample", Map.of(
                "basic", Map.of(
                    "insertData", Map.of(
                        "user_id", 123,
                        "username", "testuser",
                        "email", "test@example.com",
                        "status", 1,
                        "created_at", "2024-01-01 10:00:00"
                    ),
                    "table", "users"
                ),
                "advanced", Map.of(
                    "insertData", Map.of(
                        "user_id", 123,
                        "username", "testuser", 
                        "email", "test@example.com",
                        "status", 1,
                        "created_at", "2024-01-01 10:00:00"
                    ),
                    "updateData", Map.of(
                        "username", "testuser_updated",
                        "email", "test_updated@example.com",
                        "status", 2,
                        "updated_at", "2024-01-02 10:00:00"
                    ),
                    "excludeFields", List.of("user_id", "created_at"),
                    "table", "users"
                )
            ),
            "generatedSql", Map.of(
                "basic", "INSERT INTO `users` (`user_id`, `username`, `email`, `status`, `created_at`) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `user_id` = ?, `username` = ?, `email` = ?, `status` = ?, `created_at` = ?",
                "advanced", "INSERT INTO `users` (`user_id`, `username`, `email`, `status`, `created_at`) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `username` = ?, `email` = ?, `status` = ?, `updated_at` = ?"
            ),
            "useCases", List.of(
                "用户信息同步：插入新用户或更新现有用户信息",
                "配置管理：插入新配置或更新现有配置值", 
                "数据导入：批量导入数据时处理重复记录",
                "缓存更新：插入缓存数据或更新已存在的缓存"
            )
        );
        
        return Result.success(example);
    }
} 