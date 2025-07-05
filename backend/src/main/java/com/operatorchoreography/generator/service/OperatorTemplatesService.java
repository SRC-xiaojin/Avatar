package com.operatorchoreography.generator.service;

import com.operatorchoreography.generator.model.OperatorTemplates;
import com.operatorchoreography.executor.ExecutorResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 算子模板表 服务类
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
public interface OperatorTemplatesService extends IService<OperatorTemplates> {

    /**
     * 根据类别获取算子模板
     */
    List<OperatorTemplates> getTemplatesByCategory(Long categoryId);

    /**
     * 根据模板编码获取算子模板
     */
    OperatorTemplates getByTemplateCode(String templateCode);

    /**
     * 测试算子模板执行
     */
    ExecutorResult testTemplateExecution(Long templateId, Map<String, Object> inputData);

    /**
     * 获取算子模板的参数定义
     */
    List<Map<String, Object>> getTemplateParams(Long templateId);
}
