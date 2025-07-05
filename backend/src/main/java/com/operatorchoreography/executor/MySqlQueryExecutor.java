package com.operatorchoreography.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.operatorchoreography.generator.model.OperatorTemplateParams;
import com.operatorchoreography.generator.service.OperatorTemplateParamsService;
import com.operatorchoreography.service.DynamicDataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;

/**
 * MySQL查询执行器
 * 执行MySQL查询操作
 * 支持根据templateId动态获取数据源配置
 */
@Slf4j
@Component
public class MySqlQueryExecutor implements BaseExecutor {

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;
    @Autowired
    private OperatorTemplateParamsService operatorTemplateParamsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExecutorResult query(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行MySQL查询: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 查询模板参数
            List<OperatorTemplateParams> params = operatorTemplateParamsService.getParametersByTemplateId(templateId);
            if (params == null || params.isEmpty()) {
                throw new RuntimeException("未找到模板参数配置: templateId=" + templateId);
            }

            // 获取动态JdbcTemplate
            JdbcTemplate jdbcTemplate = dynamicDataSourceService.getJdbcTemplate(params);
            
            String sqlStatement = (String) inputData.get("sql_statement");
            Map<String, Object> queryParams = (Map<String, Object>) inputData.get("query_params");
            Map<String, Object> datasource = (Map<String, Object>) inputData.get("datasource");
            
            if (sqlStatement == null || sqlStatement.trim().isEmpty()) {
                log.error("SQL语句为空: templateId={}", templateId);
                throw new IllegalArgumentException("SQL语句不能为空");
            }
            
            log.info("执行SQL查询: templateId={}, sql={}", templateId, sqlStatement);
            log.debug("查询参数: {}", queryParams);
            
            // 如果提供了自定义数据源，使用自定义数据源
            boolean useCustomDataSource = false;
            if (datasource != null && !datasource.isEmpty()) {
                String customIdentifier = "template_" + templateId + "_custom";
                jdbcTemplate = dynamicDataSourceService.getJdbcTemplate(datasource, customIdentifier);
                useCustomDataSource = true;
                log.info("使用自定义数据源: templateId={}", templateId);
            }
            
            // 执行查询
            List<Map<String, Object>> queryResult;
            boolean hasParameters = false;
            
            if (queryParams != null && !queryParams.isEmpty()) {
                // 支持参数化查询
                Object[] paramsArray = queryParams.values().toArray();
                hasParameters = true;
                log.debug("使用参数化查询: 参数数量={}", paramsArray.length);
                queryResult = jdbcTemplate.queryForList(sqlStatement, paramsArray);
            } else {
                // 无参数查询
                log.debug("使用无参数查询");
                queryResult = jdbcTemplate.queryForList(sqlStatement);
            }
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("queryResult", queryResult);
            outputData.put("rowCount", queryResult.size());
            outputData.put("sql", sqlStatement);
            outputData.put("templateId", templateId);
            outputData.put("message", "MySQL查询执行成功");
            
            log.info("MySQL查询执行成功: templateId={}, 返回行数={}, SQL长度={}", 
                    templateId, queryResult.size(), sqlStatement.length());
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("MySQL查询执行成功")
                                .withMetadata("rowCount", queryResult.size())
                                .withMetadata("sqlLength", sqlStatement.length())
                                .withMetadata("hasParameters", hasParameters)
                                .withMetadata("parameterCount", queryParams != null ? queryParams.size() : 0)
                                .withMetadata("useCustomDataSource", useCustomDataSource);
            
        } catch (Exception e) {
            log.error("MySQL查询执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("MySQL查询执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("MySQL查询执行失败: " + e.getMessage());
        }
    }
} 