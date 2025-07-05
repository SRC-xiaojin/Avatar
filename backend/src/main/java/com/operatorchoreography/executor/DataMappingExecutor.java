package com.operatorchoreography.executor;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.operatorchoreography.generator.model.OperatorTemplateParams;
import com.operatorchoreography.generator.service.OperatorTemplateParamsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 数据映射执行器
 * 基于JOLT规则进行数据转换，映射规则从数据库读取
 */
@Slf4j
@Component
public class DataMappingExecutor implements BaseExecutor {

    @Autowired
    private OperatorTemplateParamsService operatorTemplateParamsService;

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行JOLT数据映射: templateId={}", templateId);
        log.debug("输入数据: {}", inputData);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 从数据库获取JOLT映射规则
            String joltSpec = getMappingRulesFromDatabase(templateId);
            if (joltSpec == null || joltSpec.trim().isEmpty()) {
                log.warn("未找到JOLT映射规则: templateId={}", templateId);
                LocalDateTime endTime = LocalDateTime.now();
                return ExecutorResult.failure("未配置JOLT映射规则", new RuntimeException("未配置JOLT映射规则"))
                                    .withTemplateInfo(templateId, null)
                                    .withLog("未配置JOLT映射规则");
            }
            
            log.debug("JOLT规则长度: {}", joltSpec.length());
            
            // 获取输入数据 - 支持多种输入格式
            Object inputToTransform = getInputData(inputData);
            
            if (inputToTransform == null) {
                log.warn("输入数据为空: templateId={}", templateId);
                LocalDateTime endTime = LocalDateTime.now();
                return ExecutorResult.failure("输入数据为空", new RuntimeException("输入数据为空"))
                                    .withTemplateInfo(templateId, null)
                                    .withLog("输入数据为空");
            }
            
            log.debug("待转换数据类型: {}", inputToTransform.getClass().getSimpleName());
            
            // 执行JOLT转换
            Object transformedData = executeJoltTransformation(joltSpec, inputToTransform);
            
            log.info("JOLT数据映射执行成功: templateId={}, 输入类型: {}, 输出类型: {}", 
                    templateId, inputToTransform.getClass().getSimpleName(), 
                    transformedData != null ? transformedData.getClass().getSimpleName() : "null");
            // 如果输出是Map结构，确保转换为Map对象
            Map<String, Object> finalOutputData;
            if (transformedData instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapData = (Map<String, Object>) transformedData;
                finalOutputData = mapData;
            } else {
                // 如果不是Map，包装成Map
                finalOutputData = new HashMap<>();
                finalOutputData.put("result", transformedData);
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(finalOutputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("JOLT数据映射执行成功")
                                .withMetadata("joltSpecLength", joltSpec.length())
                                .withMetadata("inputType", inputToTransform.getClass().getSimpleName())
                                .withMetadata("outputType", transformedData != null ? transformedData.getClass().getSimpleName() : "null")
                                .withMetadata("hasTransformedData", transformedData != null);
            
        } catch (Exception e) {
            log.error("JOLT数据映射执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("JOLT数据映射执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("JOLT数据映射执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 从数据库获取JOLT映射规则
     */
    private String getMappingRulesFromDatabase(Long templateId) {
        try {
            QueryWrapper<OperatorTemplateParams> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("template_id", templateId)
                       .eq("param_key", "mapping_rules")
                       .eq("param_category", "config");
            
            OperatorTemplateParams param = operatorTemplateParamsService.getOne(queryWrapper);
            
            if (param != null && param.getDefaultValue() != null) {
                log.debug("从数据库获取到JOLT规则: templateId={}", templateId);
                return param.getDefaultValue();
            }
            
            log.warn("数据库中未找到mapping_rules配置: templateId={}", templateId);
            return null;
            
        } catch (Exception e) {
            log.error("从数据库读取JOLT规则失败: templateId={}", templateId, e);
            return null;
        }
    }
    
    /**
     * 获取输入数据
     */
    private Object getInputData(Map<String, Object> inputData) {
        // 优先使用 inputData 字段
        if (inputData.containsKey("inputData")) {
            return inputData.get("inputData");
        }else{
            throw new RuntimeException("未找到inputData字段");
        }
        
    }
    
    /**
     * 执行JOLT转换
     */
    private Object executeJoltTransformation(String joltSpecJson, Object inputData) throws Exception {
        try {
            // 解析JOLT规则
            List<Object> joltSpec = JsonUtils.jsonToList(joltSpecJson);
            log.debug("解析JOLT规则成功，规则数量: {}", joltSpec.size());
            
            // 创建JOLT转换链
            Chainr chainr = Chainr.fromSpec(joltSpec);
            
            // 执行转换
            Object transformedData = chainr.transform(inputData);
            
            log.debug("JOLT转换执行完成,transformedData: {}", transformedData);
            return transformedData;
            
        } catch (Exception e) {
            log.error("JOLT转换执行失败", e);
            throw new RuntimeException("JOLT转换失败: " + e.getMessage(), e);
        }
    }
} 