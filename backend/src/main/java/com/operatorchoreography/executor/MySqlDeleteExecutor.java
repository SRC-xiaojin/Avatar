package com.operatorchoreography.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.operatorchoreography.generator.model.OperatorTemplateParams;
import com.operatorchoreography.generator.service.OperatorTemplateParamsService;
import com.operatorchoreography.service.DynamicDataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

/**
 * MySQL删除执行器
 * 执行MySQL删除操作
 * 支持根据templateId动态获取数据源配置
 */
@Slf4j
@Component
public class MySqlDeleteExecutor implements BaseExecutor {

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;
    @Autowired
    private OperatorTemplateParamsService operatorTemplateParamsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExecutorResult delete(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行MySQL删除: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 查询模板参数
            List<OperatorTemplateParams> params = operatorTemplateParamsService.getParametersByTemplateId(templateId);
            if (params == null || params.isEmpty()) {
                throw new RuntimeException("未找到模板参数配置: templateId=" + templateId);
            }

            // 获取动态JdbcTemplate
            JdbcTemplate jdbcTemplate = dynamicDataSourceService.getJdbcTemplate(params);
            
            // 获取删除条件
            String table = (String) inputData.get("table");
            String sqlStatement = (String) inputData.get("sql_statement");
            Map<String, Object> deleteParams = (Map<String, Object>) inputData.get("deleteParams");
            Map<String, Object> whereCondition = (Map<String, Object>) inputData.get("whereCondition");
            Map<String, Object> datasource = (Map<String, Object>) inputData.get("datasource");
            
            // 合并删除参数和WHERE条件（deleteParams优先级更高）
            Map<String, Object> conditions = new HashMap<>();
            if (whereCondition != null) {
                conditions.putAll(whereCondition);
            }
            if (deleteParams != null) {
                conditions.putAll(deleteParams);
            }
            
            log.info("执行MySQL删除: templateId={}, table={}", templateId, table);
            log.debug("删除条件: {}", conditions);
            
            // 如果提供了自定义数据源，使用自定义数据源
            boolean useCustomDataSource = false;
            if (datasource != null && !datasource.isEmpty()) {
                String customIdentifier = "template_" + templateId + "_custom";
                jdbcTemplate = dynamicDataSourceService.getJdbcTemplate(datasource, customIdentifier);
                useCustomDataSource = true;
                log.info("使用自定义数据源: templateId={}", templateId);
            }
            
            // 构建删除SQL语句
            String sql;
            List<Object> parameters = new ArrayList<>();
            boolean isCustomSql = false;
            
            if (sqlStatement != null && !sqlStatement.trim().isEmpty()) {
                // 使用自定义SQL语句
                sql = sqlStatement;
                isCustomSql = true;
                // 添加条件参数
                if (conditions != null) {
                    for (Object value : conditions.values()) {
                        parameters.add(value);
                    }
                }
            } else if (table != null && !table.trim().isEmpty()) {
                // 自动生成删除SQL
                StringBuilder sqlBuilder = new StringBuilder("DELETE FROM ");
                sqlBuilder.append("`").append(table).append("`");
                
                // 构建WHERE子句
                if (conditions != null && !conditions.isEmpty()) {
                    sqlBuilder.append(" WHERE ");
                    List<String> conditionFields = new ArrayList<>(conditions.keySet());
                    for (int i = 0; i < conditionFields.size(); i++) {
                        if (i > 0) sqlBuilder.append(" AND ");
                        String field = conditionFields.get(i);
                        sqlBuilder.append("`").append(field).append("`").append(" = ?");
                        parameters.add(conditions.get(field));
                    }
                } else {
                    // 为了安全，不允许无条件删除
                    throw new IllegalArgumentException("删除操作必须包含WHERE条件，防止误删除全表数据");
                }
                
                sql = sqlBuilder.toString();
            } else {
                throw new IllegalArgumentException("必须提供table参数或sql_statement参数");
            }
            
            log.info("执行删除SQL: {}", sql);
            log.debug("SQL参数: {}", parameters);
            
            // 为了安全，检查是否有潜在的全表删除风险
            if (sql.toUpperCase().trim().matches("DELETE\\s+FROM\\s+`?\\w+`?\\s*$")) {
                throw new IllegalArgumentException("禁止执行无WHERE条件的删除操作");
            }
            
            // 执行删除操作
            int deletedRows = jdbcTemplate.update(sql, parameters.toArray());
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("deletedRows", deletedRows);
            outputData.put("table", table);
            outputData.put("sql", sql);
            outputData.put("conditions", conditions);
            outputData.put("templateId", templateId);
            outputData.put("message", "MySQL删除执行成功");
            
            log.info("MySQL删除执行成功: templateId={}, 删除行数={}, 条件数={}", 
                    templateId, deletedRows, conditions.size());
            
            // 检查大量删除的情况
            boolean massDelete = deletedRows > 1000;
            if (massDelete) {
                String warning = "删除了大量数据，请确认操作正确性";
                outputData.put("warning", warning);
                log.warn("删除了大量数据: templateId={}, deletedRows={}", templateId, deletedRows);
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("MySQL删除执行成功")
                                .withMetadata("deletedRows", deletedRows)
                                .withMetadata("conditionsCount", conditions.size())
                                .withMetadata("parameterCount", parameters.size())
                                .withMetadata("isCustomSql", isCustomSql)
                                .withMetadata("useCustomDataSource", useCustomDataSource)
                                .withMetadata("sqlLength", sql.length())
                                .withMetadata("massDelete", massDelete);
            
        } catch (Exception e) {
            log.error("MySQL删除执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("MySQL删除执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("MySQL删除执行失败: " + e.getMessage());
        }
    }
} 