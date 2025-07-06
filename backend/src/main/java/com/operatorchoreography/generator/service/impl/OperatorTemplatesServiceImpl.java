package com.operatorchoreography.generator.service.impl;

import com.operatorchoreography.generator.model.OperatorTemplates;
import com.operatorchoreography.generator.mapper.OperatorTemplatesMapper;
import com.operatorchoreography.generator.service.OperatorTemplatesService;
import com.operatorchoreography.generator.service.OperatorTemplateParamsService;
import com.operatorchoreography.generator.model.OperatorTemplateParams;
import com.operatorchoreography.executor.ExecutorManager;
import com.operatorchoreography.executor.ExecutorResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * <p>
 * 算子模板表 服务实现类
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Slf4j
@Service
public class OperatorTemplatesServiceImpl extends ServiceImpl<OperatorTemplatesMapper, OperatorTemplates> implements OperatorTemplatesService {

    @Autowired
    private OperatorTemplateParamsService operatorTemplateParamsService;
    
    @Autowired
    private ExecutorManager executorManager;

    @Override
    public List<OperatorTemplates> getTemplatesByCategory(Long categoryId) {
        QueryWrapper<OperatorTemplates> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", categoryId)
                   .eq("if_delete", false)
                   .eq("status", true)
                   .orderByAsc("sort_order");
        return this.list(queryWrapper);
    }

    @Override
    public OperatorTemplates getByTemplateCode(String templateCode) {
        QueryWrapper<OperatorTemplates> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("template_code", templateCode).eq("if_delete", false);
        return this.getOne(queryWrapper);
    }

    @Override
    public ExecutorResult testTemplateExecution(Long templateId, Map<String, Object> inputData) {
        log.info("开始测试算子模板执行: templateId={}", templateId);
        log.debug("测试输入数据: {}", inputData);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            QueryWrapper<OperatorTemplates> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", templateId).eq("if_delete", false);
            OperatorTemplates template = this.getOne(queryWrapper);
            if (template == null) {
                log.error("算子模板不存在: templateId={}", templateId);
                LocalDateTime endTime = LocalDateTime.now();
                return ExecutorResult.failure("算子模板不存在", startTime, endTime)
                        .withTemplateInfo(templateId, null);
            }
            
            log.info("找到算子模板: templateId={}, templateCode={}, templateName={}", 
                    templateId, template.getTemplateCode(), template.getTemplateName());
            
            String executorClass = template.getExecutorClass();
            String executorMethod = template.getExecutorMethod();
            
            if (executorClass == null || executorClass.trim().isEmpty()) {
                return ExecutorResult.failure("执行器类或者方法不存在")
                        .withTemplateInfo(templateId, null);
            } else {
                log.info("使用真实执行器: templateId={}, executorClass={}, executorMethod={}", 
                        templateId, executorClass, executorMethod);
                
                // 使用真实的执行器
                ExecutorResult executorResult = executorManager.executeOperator(executorClass, executorMethod, inputData, templateId);
                
                // 补充模板信息
                executorResult.withTemplateInfo(templateId, template.getTemplateName());
                
                log.info("真实执行器执行完成: templateId={}", templateId);
                
                return executorResult;
            }
            
        } catch (Exception e) {
            log.error("算子模板测试执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            
            return ExecutorResult.failure("算子执行失败: " + e.getMessage(), e)
                    .withTemplateInfo(templateId, null);
        }
    }

    @Override
    public List<Map<String, Object>> getTemplateParams(Long templateId) {
        QueryWrapper<OperatorTemplateParams> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("template_id", templateId)
                   .eq("if_delete", false)
                   .orderByAsc("id");
        
        List<OperatorTemplateParams> params = operatorTemplateParamsService.list(queryWrapper);
        
        return params.stream().map(param -> {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", param.getId());
            paramMap.put("paramKey", param.getParamKey());
            paramMap.put("paramName", param.getParamName());
            paramMap.put("paramType", param.getParamType());
            paramMap.put("paramCategory", param.getParamCategory());
            paramMap.put("defaultValue", param.getDefaultValue());
            paramMap.put("isRequired", param.getIsRequired());
            paramMap.put("validationRules", param.getValidationRules());
            return paramMap;
        }).collect(Collectors.toList());
    }
}
