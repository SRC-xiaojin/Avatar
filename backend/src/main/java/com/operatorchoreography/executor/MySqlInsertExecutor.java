package com.operatorchoreography.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.operatorchoreography.common.util.UuidUtils;
import com.operatorchoreography.generator.model.OperatorTemplateParams;
import com.operatorchoreography.generator.service.OperatorTemplateParamsService;
import com.operatorchoreography.service.DynamicDataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * MySQL插入执行器
 * 执行MySQL插入操作
 * 支持根据templateId动态获取数据源配置
 * 支持ON DUPLICATE KEY UPDATE解决冲突
 */
@Slf4j
@Component
public class MySqlInsertExecutor implements BaseExecutor {

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;
    @Autowired
    private OperatorTemplateParamsService operatorTemplateParamsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExecutorResult insert(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    /**
     * 根据inputData和table构建插入语句，使用ON DUPLICATE KEY UPDATE
     * @param inputData 输入数据，包含insertData和可选的updateData、excludeFields等
     * @param templateId 模板ID
     * @return 执行结果
     * @throws Exception 执行异常
     */
    public ExecutorResult insertWithUpsert(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行MySQL插入(带冲突处理): templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 查询模板参数
            List<OperatorTemplateParams> params = operatorTemplateParamsService.getParametersByTemplateId(templateId);
            if (params == null || params.isEmpty()) {
                throw new RuntimeException("未找到模板参数配置: templateId=" + templateId);
            }

            // 获取动态JdbcTemplate
            JdbcTemplate jdbcTemplate = dynamicDataSourceService.getJdbcTemplate(params);
            
            // TODO 获取参数 需要修改根据输入的scheme来获取insertData
            @SuppressWarnings("unchecked")
            Map<String, Object> insertData = (Map<String, Object>) inputData.get("inputData");
            
            // 可选参数：指定更新数据（如果为空则使用insertData）
            @SuppressWarnings("unchecked")
            Map<String, Object> updateData = (Map<String, Object>) inputData.getOrDefault("updateData", insertData);
            
            // 可选参数：排除更新的字段（通常是主键、创建时间等）
            @SuppressWarnings("unchecked")
            List<String> excludeFields = (List<String>) inputData.getOrDefault("excludeFields", new ArrayList<>());
            
            // 可选参数：表名（优先从inputData获取）
            String table = (String) inputData.get("table");
            
            // 如果inputData中没有table，从模板参数获取
            if (table == null || table.trim().isEmpty()) {
                for (OperatorTemplateParams param : params) {
                    if ("table".equals(param.getParamKey())) {
                        table = param.getDefaultValue();
                        break;
                    }
                }
            }
            
            if (table == null || table.trim().isEmpty()) {
                throw new IllegalArgumentException("表名不能为空");
            }
            
            if (insertData == null || insertData.isEmpty()) {
                throw new IllegalArgumentException("插入数据不能为空");
            }
            
            log.info("执行MySQL插入(带冲突处理): templateId={}, table={}", templateId, table);
            log.debug("插入数据: {}", insertData);
            log.debug("更新数据: {}", updateData);
            log.debug("排除字段: {}", excludeFields);
            
            // 构建SQL和参数
            SqlWithParams sqlWithParams = buildInsertWithUpsertSql(table, insertData, updateData, excludeFields);
            
            log.info("执行插入SQL: {}", sqlWithParams.sql);
            log.debug("SQL参数: {}", sqlWithParams.parameters);
            
            // 执行插入操作
            KeyHolder keyHolder = new GeneratedKeyHolder();
            int affectedRows = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sqlWithParams.sql, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < sqlWithParams.parameters.size(); i++) {
                    ps.setObject(i + 1, sqlWithParams.parameters.get(i));
                }
                return ps;
            }, keyHolder);
            
            // 获取生成的主键ID
            Long insertedId = null;
            Number key = keyHolder.getKey();
            if (key != null) {
                insertedId = key.longValue();
            }
            
            Map<String, Object> insertResult = new HashMap<>();
            insertResult.put("affectedRows", affectedRows);
            insertResult.put("insertedId", insertedId);
            insertResult.put("table", table);
            insertResult.put("sql", sqlWithParams.sql);
            insertResult.put("templateId", templateId);
            insertResult.put("isUpsert", true);
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("insertResult", insertResult);
            outputData.put("insertedId", insertedId);
            outputData.put("affectedRows", affectedRows);
            outputData.put("message", "MySQL插入(带冲突处理)执行成功");
            
            log.info("MySQL插入(带冲突处理)执行成功: templateId={}, 影响行数={}, 插入ID={}", 
                    templateId, affectedRows, insertedId);
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("MySQL插入(带冲突处理)执行成功")
                                .withMetadata("upsertOperation", true)
                                .withMetadata("sqlStatement", sqlWithParams.sql);
            
        } catch (Exception e) {
            log.error("MySQL插入(带冲突处理)执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("MySQL插入(带冲突处理)执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("MySQL插入(带冲突处理)执行失败: " + e.getMessage())
                                .withMetadata("upsertOperation", true);
        }
    }

    /**
     * 构建INSERT ... ON DUPLICATE KEY UPDATE SQL语句
     * @param table 表名
     * @param insertData 插入数据
     * @param updateData 更新数据
     * @param excludeFields 排除更新的字段
     * @return SQL语句和参数
     */
    private SqlWithParams buildInsertWithUpsertSql(String table, Map<String, Object> insertData, 
                                                   Map<String, Object> updateData, List<String> excludeFields) {
        List<Object> parameters = new ArrayList<>();
        Set<String> excludeSet = new HashSet<>(excludeFields);
        excludeSet.add("id");
        
        // 如果没有提供id，则生成一个
        insertData.put("id", UuidUtils.generateUuid());
        
        // 构建INSERT部分
        List<String> columns = new ArrayList<>(insertData.keySet());
        String columnsPart = columns.stream()
                .map(col -> "`" + col + "`")
                .collect(Collectors.joining(", "));
        
        String valuesPart = columns.stream()
                .map(col -> "?")
                .collect(Collectors.joining(", "));
        
        // 添加插入参数
        for (String column : columns) {
            parameters.add(insertData.get(column));
        }

        
        // 构建ON DUPLICATE KEY UPDATE部分
        List<String> updateParts = new ArrayList<>();
        for (Map.Entry<String, Object> entry : updateData.entrySet()) {
            String column = entry.getKey();
            // 跳过排除的字段
            if (excludeSet.contains(column)) {
                continue;
            }
            updateParts.add("`" + column + "` = ?");
            parameters.add(entry.getValue());
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO `").append(table).append("` (");
        sql.append(columnsPart);
        sql.append(") VALUES (");
        sql.append(valuesPart);
        sql.append(")");
        
        if (!updateParts.isEmpty()) {
            sql.append(" ON DUPLICATE KEY UPDATE ");
            sql.append(String.join(", ", updateParts));
        }
        
        return new SqlWithParams(sql.toString(), parameters);
    }

    /**
     * SQL和参数的包装类
     */
    private static class SqlWithParams {
        final String sql;
        final List<Object> parameters;
        
        SqlWithParams(String sql, List<Object> parameters) {
            this.sql = sql;
            this.parameters = parameters;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行MySQL插入: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 查询模板参数
            List<OperatorTemplateParams> params = operatorTemplateParamsService.getParametersByTemplateId(templateId);
            if (params == null || params.isEmpty()) {
                throw new RuntimeException("未找到模板参数配置: templateId=" + templateId);
            }

            // 获取动态JdbcTemplate
            JdbcTemplate jdbcTemplate = dynamicDataSourceService.getJdbcTemplate(params);
            
            // 从输入数据获取参数（优先从inputData获取，如果没有则使用模板默认值）
            Map<String, Object> insertData = (Map<String, Object>) inputData.get("insertData");
            // 从模板参数中获取表名和SQL语句
            String table = null;
            String sqlStatement = null;
            for (OperatorTemplateParams param : params) {
                if ("table".equals(param.getParamKey())) {
                    table = param.getDefaultValue();
                    break;
                }
                if ("sql_statement".equals(param.getParamKey())) {
                    sqlStatement = param.getDefaultValue();
                    break;
                }
            }
            
            
            if (insertData.isEmpty()) {
                log.error("插入数据为空: templateId={}", templateId);
                throw new IllegalArgumentException("插入数据不能为空");
            }
            
            log.info("执行MySQL插入: templateId={}, table={}", templateId, table);
            log.debug("插入数据: {}", insertData);
            
            // 构建插入SQL语句
            String sql;
            List<Object> parameters = new ArrayList<>();
            
            sql = sqlStatement;
            
            log.info("执行插入SQL: {}", sql);
            log.debug("SQL参数: {}", parameters);
            
            // 执行插入操作
            KeyHolder keyHolder = new GeneratedKeyHolder();
            int affectedRows = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }
                return ps;
            }, keyHolder);
            
            // 获取生成的主键ID
            Object insertedId = null;
            if (keyHolder.getKey() != null) {
                insertedId = keyHolder.getKey();
            }
            
            Map<String, Object> insertResult = new HashMap<>();
            insertResult.put("affectedRows", affectedRows);
            insertResult.put("insertedId", insertedId);
            insertResult.put("table", table);
            insertResult.put("sql", sql);
            insertResult.put("templateId", templateId);
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("insertResult", insertResult);
            outputData.put("insertedId", insertedId);
            outputData.put("affectedRows", affectedRows);
            outputData.put("message", "MySQL插入执行成功");
            
            log.info("MySQL插入执行成功: templateId={}, 影响行数={}, 插入ID={}", 
                    templateId, affectedRows, insertedId);
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("MySQL插入执行成功");
            
        } catch (Exception e) {
            log.error("MySQL插入执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("MySQL插入执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("MySQL插入执行失败: " + e.getMessage());
        }
    }
} 