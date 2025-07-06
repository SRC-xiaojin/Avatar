package com.operatorchoreography.generator.service.impl;

import com.operatorchoreography.generator.model.OperatorTemplateParams;
import com.operatorchoreography.generator.mapper.OperatorTemplateParamsMapper;
import com.operatorchoreography.generator.service.OperatorTemplateParamsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 算子参数定义表 服务实现类
 * </p>
 *
 * @author MyBatis-Plus Generator
 * @since 2025-06-30
 */
@Service
public class OperatorTemplateParamsServiceImpl extends ServiceImpl<OperatorTemplateParamsMapper, OperatorTemplateParams> implements OperatorTemplateParamsService {

    @Override
    public List<OperatorTemplateParams> getParametersByTemplateId(Long templateId) {
        QueryWrapper<OperatorTemplateParams> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("template_id", templateId).eq("if_delete", false);
        return this.list(queryWrapper);
    }

    @Override
    public boolean removeByTemplateId(Long templateId) {
        QueryWrapper<OperatorTemplateParams> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("template_id", templateId).eq("if_delete", false);
        return this.remove(queryWrapper);
    }

}
