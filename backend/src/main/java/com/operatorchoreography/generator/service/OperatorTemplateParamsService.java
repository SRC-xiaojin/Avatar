package com.operatorchoreography.generator.service;

import com.operatorchoreography.generator.model.OperatorTemplateParams;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 算子参数定义表 服务类
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
public interface OperatorTemplateParamsService extends IService<OperatorTemplateParams> {

    /**
     * 根据模板ID获取参数列表
     * @param templateId 模板ID
     * @return 参数列表
     */
    List<OperatorTemplateParams> getParametersByTemplateId(Long templateId);

    /**
     * 根据模板ID删除参数
     * @param templateId 模板ID
     * @return 删除结果
     */
    boolean removeByTemplateId(Long templateId);

}
