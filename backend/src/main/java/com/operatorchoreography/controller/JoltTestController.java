package com.operatorchoreography.controller;

import com.operatorchoreography.common.Result;
import com.operatorchoreography.executor.DataMappingExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;

/**
 * JOLT数据映射测试控制器
 * 提供JOLT转换功能的测试接口和示例
 */
@Tag(name = "JOLT测试", description = "JOLT数据转换测试相关的API接口")
@Slf4j
@RestController
@RequestMapping("/jolt-test")
public class JoltTestController {

    @Autowired
    private DataMappingExecutor dataMappingExecutor;

    /**
     * 获取JOLT测试样例列表
     */
    @Operation(summary = "获取JOLT测试样例", description = "获取预定义的JOLT转换测试样例")
    @GetMapping("/samples")
    public Result<List<Map<String, Object>>> getTestSamples() {
        List<Map<String, Object>> samples = createTestSamples();
        return Result.success(samples, "获取JOLT测试样例成功");
    }

    /**
     * 根据样例ID获取具体的测试数据
     */
    @Operation(summary = "获取测试样例详情", description = "根据样例ID获取具体的输入数据、JOLT规则和期望输出")
    @GetMapping("/samples/{sampleId}")
    public Result<Map<String, Object>> getTestSample(@PathVariable String sampleId) {
        Map<String, Object> sample = getTestSampleById(sampleId);
        if (sample == null) {
            return Result.error(404, "测试样例不存在");
        }
        return Result.success(sample, "获取测试样例详情成功");
    }

    /**
     * 直接测试JOLT转换
     */
    @Operation(summary = "直接测试JOLT转换", description = "提供JOLT规则和输入数据进行直接转换测试")
    @PostMapping("/transform")
    public Result<Map<String, Object>> testJoltTransform(@RequestBody Map<String, Object> testData) {
        log.info("接收到JOLT直接转换测试请求,testData: {}", testData);
        
        try {
            Object inputData = testData.get("inputData");
            Object joltSpec = testData.get("joltSpec");
            
            if (inputData == null || joltSpec == null) {
                return Result.error("请提供inputData和joltSpec参数");
            }
            
            Map<String, Object> result = executeJoltTransformation(joltSpec, inputData);
            return Result.success(result, "JOLT转换测试成功");
            
        } catch (Exception e) {
            log.error("JOLT转换测试失败", e);
            return Result.error("JOLT转换测试失败: " + e.getMessage());
        }
    }

    /**
     * 运行预定义的测试样例
     */
    @Operation(summary = "运行测试样例", description = "运行指定的预定义测试样例")
    @PostMapping("/samples/{sampleId}/run")
    public Result<Map<String, Object>> runTestSample(@PathVariable String sampleId) {
        log.info("运行JOLT测试样例: {}", sampleId);
        
        try {
            Map<String, Object> sample = getTestSampleById(sampleId);
            if (sample == null) {
                return Result.error(404, "测试样例不存在");
            }
            
            Object inputData = sample.get("inputData");
            String joltSpec = (String) sample.get("joltSpec");
            Object expectedOutput = sample.get("expectedOutput");
            
            // 执行转换
            long startTime = System.currentTimeMillis();
            Map<String, Object> transformResult = executeJoltTransformation(joltSpec, inputData);
            long endTime = System.currentTimeMillis();
            
            Object actualOutput = transformResult.get("transformedData");
            
            // 比较结果
            boolean isMatch = compareResults(expectedOutput, actualOutput);
            
            Map<String, Object> result = new HashMap<>();
            result.put("sampleId", sampleId);
            result.put("sampleName", sample.get("name"));
            result.put("inputData", inputData);
            result.put("expectedOutput", expectedOutput);
            result.put("actualOutput", actualOutput);
            result.put("isMatch", isMatch);
            result.put("joltSpec", joltSpec);
            result.put("executionTime", endTime - startTime);
            
            String message = isMatch ? "测试样例运行成功，结果匹配" : "测试样例运行完成，但结果不匹配";
            return Result.success(result, message);
            
        } catch (Exception e) {
            log.error("运行测试样例失败: {}", sampleId, e);
            return Result.error("运行测试样例失败: " + e.getMessage());
        }
    }

    /**
     * 运行所有测试样例
     */
    @Operation(summary = "运行所有测试样例", description = "批量运行所有预定义的测试样例")
    @PostMapping("/samples/run-all")
    public Result<Map<String, Object>> runAllTestSamples() {
        log.info("运行所有JOLT测试样例");
        
        try {
            List<Map<String, Object>> samples = createTestSamples();
            List<Map<String, Object>> results = new ArrayList<>();
            int passCount = 0;
            int totalCount = samples.size();
            
            for (Map<String, Object> sample : samples) {
                try {
                    String sampleId = (String) sample.get("id");
                    Result<Map<String, Object>> sampleResult = runTestSample(sampleId);
                    
                    if (sampleResult.isSuccess()) {
                        Map<String, Object> data = sampleResult.getData();
                        results.add(data);
                        if ((Boolean) data.get("isMatch")) {
                            passCount++;
                        }
                    }
                } catch (Exception e) {
                    log.error("运行测试样例失败: {}", sample.get("id"), e);
                }
            }
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalCount", totalCount);
            summary.put("passCount", passCount);
            summary.put("failCount", totalCount - passCount);
            summary.put("passRate", totalCount > 0 ? (double) passCount / totalCount * 100 : 0);
            summary.put("results", results);
            
            return Result.success(summary, String.format("批量测试完成，通过率: %.1f%% (%d/%d)", 
                summary.get("passRate"), passCount, totalCount));
            
        } catch (Exception e) {
            log.error("批量运行测试样例失败", e);
            return Result.error("批量运行测试样例失败: " + e.getMessage());
        }
    }

    /**
     * 验证JOLT规则语法
     */
    @Operation(summary = "验证JOLT规则", description = "验证JOLT规则的语法正确性")
    @PostMapping("/validate")
    public Result<Map<String, Object>> validateJoltSpec(@RequestBody Map<String, Object> request) {
        String joltSpec = (String) request.get("joltSpec");
        
        if (joltSpec == null || joltSpec.trim().isEmpty()) {
            return Result.error("请提供JOLT规则");
        }
        
        try {
            Map<String, Object> result = validateJoltSyntax(joltSpec);
            return Result.success(result, "JOLT规则验证完成");
        } catch (Exception e) {
            log.error("JOLT规则验证失败", e);
            return Result.error("JOLT规则验证失败: " + e.getMessage());
        }
    }

    // ========== 私有方法 ==========

    /**
     * 创建测试样例数据
     */
    private List<Map<String, Object>> createTestSamples() {
        List<Map<String, Object>> samples = new ArrayList<>();
        
        // 样例1: 简单字段映射
        Map<String, Object> sample1 = new HashMap<>();
        sample1.put("id", "simple-mapping");
        sample1.put("name", "简单字段映射");
        sample1.put("description", "将用户基本信息字段进行重命名");
        sample1.put("inputData", Map.of("name", "张三", "age", 25, "email", "zhangsan@example.com"));
        sample1.put("joltSpec", "[{\"operation\": \"shift\", \"spec\": {\"name\": \"userName\", \"age\": \"userAge\", \"email\": \"userEmail\"}}]");
        sample1.put("expectedOutput", Map.of("userName", "张三", "userAge", 25, "userEmail", "zhangsan@example.com"));
        samples.add(sample1);
        
        // 样例2: 嵌套结构映射
        Map<String, Object> sample2 = new HashMap<>();
        sample2.put("id", "nested-mapping");
        sample2.put("name", "嵌套结构映射");
        sample2.put("description", "将嵌套的用户信息结构进行扁平化处理");
        sample2.put("inputData", Map.of(
            "user", Map.of(
                "profile", Map.of(
                    "firstName", "张",
                    "lastName", "三",
                    "contact", Map.of(
                        "email", "zhangsan@example.com",
                        "phone", "13800138000"
                    )
                )
            )
        ));
        sample2.put("joltSpec", "[{\"operation\": \"shift\", \"spec\": {\"user\": {\"profile\": {\"firstName\": \"first_name\", \"lastName\": \"last_name\", \"contact\": {\"email\": \"email_address\", \"phone\": \"phone_number\"}}}}}]");
        sample2.put("expectedOutput", Map.of(
            "first_name", "张",
            "last_name", "三", 
            "email_address", "zhangsan@example.com",
            "phone_number", "13800138000"
        ));
        samples.add(sample2);
        
        // 样例3: 数组转换
        Map<String, Object> sample3 = new HashMap<>();
        sample3.put("id", "array-transformation");
        sample3.put("name", "数组数据转换");
        sample3.put("description", "将用户列表进行结构转换");
        sample3.put("inputData", Map.of(
            "users", Arrays.asList(
                Map.of("name", "张三", "age", 25, "city", "北京"),
                Map.of("name", "李四", "age", 30, "city", "上海")
            )
        ));
        sample3.put("joltSpec", "[{\"operation\": \"shift\", \"spec\": {\"users\": {\"*\": {\"name\": \"people[&1].fullName\", \"age\": \"people[&1].years\", \"city\": \"people[&1].location\"}}}}]");
        sample3.put("expectedOutput", Map.of(
            "people", Arrays.asList(
                Map.of("fullName", "张三", "years", 25, "location", "北京"),
                Map.of("fullName", "李四", "years", 30, "location", "上海")
            )
        ));
        samples.add(sample3);
        
        // 样例4: 默认值设置
        Map<String, Object> sample4 = new HashMap<>();
        sample4.put("id", "default-values");
        sample4.put("name", "默认值设置");
        sample4.put("description", "为数据添加默认值");
        sample4.put("inputData", Map.of("name", "张三", "age", 25));
        sample4.put("joltSpec", "[{\"operation\": \"shift\", \"spec\": {\"name\": \"userName\", \"age\": \"userAge\"}}, {\"operation\": \"default\", \"spec\": {\"status\": \"active\", \"role\": \"user\"}}]");
        sample4.put("expectedOutput", Map.of(
            "userName", "张三",
            "userAge", 25,
            "status", "active",
            "role", "user"
        ));
        samples.add(sample4);
        
        return samples;
    }
    
    /**
     * 根据ID获取测试样例
     */
    private Map<String, Object> getTestSampleById(String sampleId) {
        List<Map<String, Object>> samples = createTestSamples();
        return samples.stream()
                .filter(sample -> sampleId.equals(sample.get("id")))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 执行JOLT转换（内部方法）
     */
    private Map<String, Object> executeJoltTransformation(Object joltSpecJson, Object inputData) throws Exception {
        try {
            com.bazaarvoice.jolt.Chainr chainr = com.bazaarvoice.jolt.Chainr.fromSpec(joltSpecJson);
            Object transformedData = chainr.transform(inputData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("transformedData", transformedData);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "JOLT转换失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 验证JOLT规则语法
     */
    private Map<String, Object> validateJoltSyntax(String joltSpecJson) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 尝试解析JOLT规则
            List<Object> joltSpec = com.bazaarvoice.jolt.JsonUtils.jsonToList(joltSpecJson);
            
            // 尝试创建转换链
            com.bazaarvoice.jolt.Chainr.fromSpec(joltSpec);
            
            result.put("valid", true);
            result.put("message", "JOLT规则语法正确");
            result.put("specCount", joltSpec.size());
            
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "JOLT规则语法错误: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }
    
    /**
     * 比较结果
     */
    private boolean compareResults(Object expected, Object actual) {
        try {
            if (expected == null && actual == null) {
                return true;
            }
            if (expected == null || actual == null) {
                return false;
            }
            
            // 转换为JSON字符串进行比较
            String expectedJson = com.bazaarvoice.jolt.JsonUtils.toJsonString(expected);
            String actualJson = com.bazaarvoice.jolt.JsonUtils.toJsonString(actual);
            
            return expectedJson.equals(actualJson);
            
        } catch (Exception e) {
            log.error("比较结果时发生错误", e);
            return false;
        }
    }
} 