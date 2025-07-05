-- 算子模板执行器配置示例
-- 这个文件展示了如何在operator_templates表中配置执行器类和方法

-- 1. 数据加密算子模板
INSERT INTO operator_templates (
    category_id, 
    template_code, 
    template_name, 
    description, 
    icon, 
    version, 
    executor_class, 
    executor_method, 
    input_schema, 
    output_schema, 
    is_async, 
    timeout_seconds, 
    retry_count, 
    status
) VALUES (
    1,
    'DATA_ENCRYPT',
    '数据加密',
    '对输入数据进行AES加密处理',
    'lock',
    '1.0.0',
    'com.operatorchoreography.executor.DataEncryptExecutor',
    'encrypt',
    '{"type":"object","properties":{"data":{"type":"string"},"algorithm":{"type":"string","default":"AES"}}}',
    '{"type":"object","properties":{"encryptedData":{"type":"string"},"algorithm":{"type":"string"}}}',
    false,
    30,
    3,
    true
);

-- 2. 数据解密算子模板
INSERT INTO operator_templates (
    category_id, 
    template_code, 
    template_name, 
    description, 
    icon, 
    version, 
    executor_class, 
    executor_method, 
    input_schema, 
    output_schema, 
    is_async, 
    timeout_seconds, 
    retry_count, 
    status
) VALUES (
    1,
    'DATA_DECRYPT',
    '数据解密',
    '对加密数据进行解密处理',
    'unlock',
    '1.0.0',
    'com.operatorchoreography.executor.DataDecryptExecutor',
    'decrypt',
    '{"type":"object","properties":{"encryptedData":{"type":"string"},"algorithm":{"type":"string","default":"AES"}}}',
    '{"type":"object","properties":{"data":{"type":"string"},"algorithm":{"type":"string"}}}',
    false,
    30,
    3,
    true
);

-- 3. HTTP请求算子模板
INSERT INTO operator_templates (
    category_id, 
    template_code, 
    template_name, 
    description, 
    icon, 
    version, 
    executor_class, 
    executor_method, 
    input_schema, 
    output_schema, 
    is_async, 
    timeout_seconds, 
    retry_count, 
    status
) VALUES (
    2,
    'HTTP_REQUEST',
    'HTTP请求',
    '发送HTTP请求并获取响应',
    'link',
    '1.0.0',
    'com.operatorchoreography.executor.HttpRequestExecutor',
    'execute',
    '{"type":"object","properties":{"url":{"type":"string"},"method":{"type":"string","default":"GET"},"headers":{"type":"object"},"body":{"type":"string"}}}',
    '{"type":"object","properties":{"statusCode":{"type":"integer"},"body":{"type":"string"},"headers":{"type":"object"}}}',
    true,
    60,
    3,
    true
);

-- 4. 数据映射算子模板
INSERT INTO operator_templates (
    category_id, 
    template_code, 
    template_name, 
    description, 
    icon, 
    version, 
    executor_class, 
    executor_method, 
    input_schema, 
    output_schema, 
    is_async, 
    timeout_seconds, 
    retry_count, 
    status
) VALUES (
    3,
    'DATA_MAPPING',
    '数据映射',
    '使用JOLT规则进行数据结构转换',
    'transform',
    '1.0.0',
    'com.operatorchoreography.executor.DataMappingExecutor',
    'transform',
    '{"type":"object","properties":{"data":{"type":"object"},"mappingRules":{"type":"object"}}}',
    '{"type":"object","properties":{"transformedData":{"type":"object"}}}',
    false,
    45,
    2,
    true
);

-- 5. 数据验证算子模板
INSERT INTO operator_templates (
    category_id, 
    template_code, 
    template_name, 
    description, 
    icon, 
    version, 
    executor_class, 
    executor_method, 
    input_schema, 
    output_schema, 
    is_async, 
    timeout_seconds, 
    retry_count, 
    status
) VALUES (
    3,
    'DATA_VALIDATE',
    '数据验证',
    '根据JSON Schema验证数据格式',
    'check-circle',
    '1.0.0',
    'com.operatorchoreography.executor.DataValidateExecutor',
    'validate',
    '{"type":"object","properties":{"data":{"type":"object"},"schema":{"type":"object"}}}',
    '{"type":"object","properties":{"isValid":{"type":"boolean"},"errors":{"type":"array"}}}',
    false,
    20,
    1,
    true
);

-- 6. MySQL查询算子模板
INSERT INTO operator_templates (
    category_id, 
    template_code, 
    template_name, 
    description, 
    icon, 
    version, 
    executor_class, 
    executor_method, 
    input_schema, 
    output_schema, 
    is_async, 
    timeout_seconds, 
    retry_count, 
    status
) VALUES (
    4,
    'MYSQL_QUERY',
    'MySQL查询',
    '执行MySQL查询操作',
    'database',
    '1.0.0',
    'com.operatorchoreography.executor.MySqlQueryExecutor',
    'query',
    '{"type":"object","properties":{"sql":{"type":"string"},"parameters":{"type":"array"}}}',
    '{"type":"object","properties":{"resultSet":{"type":"array"},"rowCount":{"type":"integer"}}}',
    false,
    60,
    2,
    true
);

-- 7. MySQL插入算子模板
INSERT INTO operator_templates (
    category_id, 
    template_code, 
    template_name, 
    description, 
    icon, 
    version, 
    executor_class, 
    executor_method, 
    input_schema, 
    output_schema, 
    is_async, 
    timeout_seconds, 
    retry_count, 
    status
) VALUES (
    4,
    'MYSQL_INSERT',
    'MySQL插入',
    '执行MySQL插入操作',
    'plus-circle',
    '1.0.0',
    'com.operatorchoreography.executor.MySqlInsertExecutor',
    'insert',
    '{"type":"object","properties":{"tableName":{"type":"string"},"data":{"type":"object"}}}',
    '{"type":"object","properties":{"insertedId":{"type":"integer"},"affectedRows":{"type":"integer"}}}',
    false,
    30,
    2,
    true
);

-- 8. 条件判断算子模板
INSERT INTO operator_templates (
    category_id, 
    template_code, 
    template_name, 
    description, 
    icon, 
    version, 
    executor_class, 
    executor_method, 
    input_schema, 
    output_schema, 
    is_async, 
    timeout_seconds, 
    retry_count, 
    status
) VALUES (
    5,
    'IF_CONDITION',
    '条件判断',
    '根据条件表达式进行分支判断',
    'fork',
    '1.0.0',
    'com.operatorchoreography.executor.IfConditionExecutor',
    'evaluate',
    '{"type":"object","properties":{"condition":{"type":"string"},"data":{"type":"object"}}}',
    '{"type":"object","properties":{"result":{"type":"boolean"},"branch":{"type":"string"}}}',
    false,
    15,
    1,
    true
);

-- 9. 循环控制算子模板
INSERT INTO operator_templates (
    category_id, 
    template_code, 
    template_name, 
    description, 
    icon, 
    version, 
    executor_class, 
    executor_method, 
    input_schema, 
    output_schema, 
    is_async, 
    timeout_seconds, 
    retry_count, 
    status
) VALUES (
    5,
    'FOR_LOOP',
    '循环控制',
    '对数组数据进行循环处理',
    'repeat',
    '1.0.0',
    'com.operatorchoreography.executor.ForLoopExecutor',
    'loop',
    '{"type":"object","properties":{"items":{"type":"array"},"processor":{"type":"object"}}}',
    '{"type":"object","properties":{"processedItems":{"type":"array"},"totalCount":{"type":"integer"}}}',
    false,
    120,
    2,
    true
);

-- 10. 日期格式化算子模板
INSERT INTO operator_templates (
    category_id, 
    template_code, 
    template_name, 
    description, 
    icon, 
    version, 
    executor_class, 
    executor_method, 
    input_schema, 
    output_schema, 
    is_async, 
    timeout_seconds, 
    retry_count, 
    status
) VALUES (
    6,
    'DATE_FORMAT',
    '日期格式化',
    '对日期进行格式化处理',
    'calendar',
    '1.0.0',
    'com.operatorchoreography.executor.DateFormatExecutor',
    'format',
    '{"type":"object","properties":{"date":{"type":"string"},"inputFormat":{"type":"string"},"outputFormat":{"type":"string"}}}',
    '{"type":"object","properties":{"formattedDate":{"type":"string"},"originalDate":{"type":"string"}}}',
    false,
    10,
    1,
    true
);

-- 查询所有配置的执行器模板
SELECT 
    id,
    template_code,
    template_name,
    executor_class,
    executor_method,
    is_async,
    timeout_seconds,
    retry_count,
    status
FROM operator_templates 
WHERE status = true
ORDER BY category_id, template_code; 