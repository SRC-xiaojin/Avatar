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
 * MySQL更新执行器
 * 执行MySQL更新操作
 * 支持根据templateId动态获取数据源配置
 */
@Slf4j
@Component
public class MySqlUpdateExecutor implements BaseExecutor {

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;
    @Autowired
    private OperatorTemplateParamsService operatorTemplateParamsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExecutorResult update(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行MySQL更新: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 查询模板参数
            List<OperatorTemplateParams> params = operatorTemplateParamsService.getParametersByTemplateId(templateId);
            if (params == null || params.isEmpty()) {
                throw new RuntimeException("未找到模板参数配置: templateId=" + templateId);
            }

            // 获取动态JdbcTemplate
            JdbcTemplate jdbcTemplate = dynamicDataSourceService.getJdbcTemplate(params);
            
            // 获取更新数据和条件
            String table = (String) inputData.get("table");
            String sqlStatement = (String) inputData.get("sql_statement");
            Map<String, Object> updateData = (Map<String, Object>) inputData.get("updateData");
            Map<String, Object> whereCondition = (Map<String, Object>) inputData.get("whereCondition");
            Map<String, Object> datasource = (Map<String, Object>) inputData.get("datasource");
            
            if (updateData == null || updateData.isEmpty()) {
                log.error("更新数据为空: templateId={}", templateId);
                throw new IllegalArgumentException("更新数据不能为空");
            }
            
            log.info("执行MySQL更新: templateId={}, table={}", templateId, table);
            log.debug("更新数据: {}", updateData);
            log.debug("WHERE条件: {}", whereCondition);
            
            // 如果提供了自定义数据源，使用自定义数据源
            boolean useCustomDataSource = false;
            if (datasource != null && !datasource.isEmpty()) {
                String customIdentifier = "template_" + templateId + "_custom";
                jdbcTemplate = dynamicDataSourceService.getJdbcTemplate(datasource, customIdentifier);
                useCustomDataSource = true;
                log.info("使用自定义数据源: templateId={}", templateId);
            }
            
            // 构建更新SQL语句
            String sql;
            List<Object> parameters = new ArrayList<>();
            boolean isCustomSql = false;
            
            if (sqlStatement != null && !sqlStatement.trim().isEmpty()) {
                // 使用自定义SQL语句
                sql = sqlStatement;
                isCustomSql = true;
                // 先添加SET子句的参数
                for (Object value : updateData.values()) {
                    parameters.add(value);
                }
                // 再添加WHERE子句的参数
                if (whereCondition != null) {
                    for (Object value : whereCondition.values()) {
                        parameters.add(value);
                    }
                }
            } else if (table != null && !table.trim().isEmpty()) {
                // 自动生成更新SQL
                StringBuilder sqlBuilder = new StringBuilder("UPDATE ");
                sqlBuilder.append("`").append(table).append("`").append(" SET ");
                
                // 构建SET子句
                List<String> updateFields = new ArrayList<>(updateData.keySet());
                for (int i = 0; i < updateFields.size(); i++) {
                    if (i > 0) sqlBuilder.append(", ");
                    String field = updateFields.get(i);
                    sqlBuilder.append("`").append(field).append("`").append(" = ?");
                    parameters.add(updateData.get(field));
                }
                
                // 构建WHERE子句
                if (whereCondition != null && !whereCondition.isEmpty()) {
                    sqlBuilder.append(" WHERE ");
                    List<String> whereFields = new ArrayList<>(whereCondition.keySet());
                    for (int i = 0; i < whereFields.size(); i++) {
                        if (i > 0) sqlBuilder.append(" AND ");
                        String field = whereFields.get(i);
                        sqlBuilder.append("`").append(field).append("`").append(" = ?");
                        parameters.add(whereCondition.get(field));
                    }
                } else {
                    // 为了安全，不允许无条件更新
                    throw new IllegalArgumentException("更新操作必须包含WHERE条件，防止误更新全表数据");
                }
                
                sql = sqlBuilder.toString();
            } else {
                throw new IllegalArgumentException("必须提供table参数或sql_statement参数");
            }
            
            log.info("执行更新SQL: {}", sql);
            log.debug("SQL参数: {}", parameters);
            
            // 执行更新操作
            int affectedRows = jdbcTemplate.update(sql, parameters.toArray());
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("affectedRows", affectedRows);
            outputData.put("table", table);
            outputData.put("sql", sql);
            outputData.put("updateData", updateData);
            outputData.put("whereCondition", whereCondition);
            outputData.put("templateId", templateId);
            outputData.put("message", "MySQL更新执行成功");
            
            log.info("MySQL更新执行成功: templateId={}, 影响行数={}, 字段数={}", 
                    templateId, affectedRows, updateData.size());
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("MySQL更新执行成功")
                                .withMetadata("affectedRows", affectedRows)
                                .withMetadata("updateFieldsCount", updateData.size())
                                .withMetadata("whereConditionsCount", whereCondition != null ? whereCondition.size() : 0)
                                .withMetadata("parameterCount", parameters.size())
                                .withMetadata("isCustomSql", isCustomSql)
                                .withMetadata("useCustomDataSource", useCustomDataSource)
                                .withMetadata("sqlLength", sql.length());
            
        } catch (Exception e) {
            log.error("MySQL更新执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("MySQL更新执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("MySQL更新执行失败: " + e.getMessage());
        }
    }
} 