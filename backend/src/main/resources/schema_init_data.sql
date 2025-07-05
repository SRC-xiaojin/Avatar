-- ========================================
-- 算子编排系统 - 初始化数据
-- ========================================

-- 插入算子类别
INSERT IGNORE INTO operator_categories (id, category_code, category_name, description, icon, sort_order) VALUES
(1, 'DATA_PROCESS', '数据处理算子', '数据映射、JSON解析等数据处理操作', 'icon-data', 1),
(2, 'CONTROL', '控制算子', '条件分支、循环等流程控制', 'icon-control', 2),
(3, 'SERVICE_CALL', '服务调用算子', 'HTTP接口请求、RPC调用等', 'icon-service', 3),
(4, 'DATABASE', '数据库算子', 'MySQL增删改查等数据库操作', 'icon-database', 4),
(5, 'FUNCTION', '函数算子', '字符串格式化、数据转换等工具函数', 'icon-function', 5);

-- ========================================
-- 1. 数据处理算子模板
-- ========================================

-- 数据映射算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(1, 1, 'DATA_MAPPING', '数据映射', '字段映射和数据转换', 'com.operatorchoreography.executor.DataMappingExecutor', 'execute',
'{"type": "object", "properties": {"inputData": {"type": "object", "description": "输入数据"}}}',
'{"type": "object", "properties": {"outputData": {"type": "object", "description": "映射后的数据"}}}');

-- JSON解析算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(2, 1, 'JSON_PARSE', 'JSON解析', '解析JSON字符串为对象', 'com.operatorchoreography.executor.JsonParseExecutor', 'parse',
'{"type": "object", "properties": {"jsonString": {"type": "string", "description": "JSON字符串"}}}',
'{"type": "object", "properties": {"parsedData": {"type": "object", "description": "解析后的对象"}}}');

-- JSON生成算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(3, 1, 'JSON_STRINGIFY', 'JSON生成', '将对象转换为JSON字符串', 'com.operatorchoreography.executor.JsonStringifyExecutor', 'stringify',
'{"type": "object", "properties": {"inputData": {"type": "object", "description": "输入对象"}}}',
'{"type": "object", "properties": {"jsonString": {"type": "string", "description": "JSON字符串"}}}');

-- ========================================
-- 2. 控制算子模板
-- ========================================

-- 条件分支算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(4, 2, 'IF_CONDITION', '条件分支', '根据条件进行分支处理', 'com.operatorchoreography.executor.IfConditionExecutor', 'evaluate',
'{"type": "object", "properties": {"inputData": {"type": "object", "description": "输入数据"}}}',
'{"type": "object", "properties": {"result": {"type": "object", "description": "输出结果"}, "conditionResult": {"type": "boolean", "description": "条件判断结果"}}}');

-- FOR循环算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(5, 2, 'FOR_LOOP', 'FOR循环', '循环处理数据集合', 'com.operatorchoreography.executor.ForLoopExecutor', 'loop',
'{"type": "object", "properties": {"dataArray": {"type": "array", "description": "输入数组"}, "loopVariable": {"type": "string", "description": "循环变量名"}}}',
'{"type": "object", "properties": {"results": {"type": "array", "description": "循环处理结果"}}}');

-- WHILE循环算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(6, 2, 'WHILE_LOOP', 'WHILE循环', '根据条件循环执行', 'com.operatorchoreography.executor.WhileLoopExecutor', 'executeWhile',
'{"type": "object", "properties": {"inputData": {"type": "object", "description": "输入数据"}}}',
'{"type": "object", "properties": {"finalResult": {"type": "object", "description": "最终结果"}, "iterations": {"type": "number", "description": "执行次数"}}}');

-- ========================================
-- 3. 服务调用算子模板
-- ========================================

-- HTTP请求算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(7, 3, 'HTTP_REQUEST', 'HTTP请求', '发送HTTP请求调用外部服务', 'com.operatorchoreography.executor.HttpRequestExecutor', 'request',
'{"type": "object", "properties": {"requestData": {"type": "object", "description": "请求数据"}}}',
'{"type": "object", "properties": {"response": {"type": "object", "description": "响应数据"}, "statusCode": {"type": "number", "description": "HTTP状态码"}}}');

-- ========================================
-- 4. 数据库算子模板
-- ========================================

-- MySQL查询算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(8, 4, 'MYSQL_QUERY', 'MySQL查询', '执行MySQL查询操作', 'com.operatorchoreography.executor.MySqlQueryExecutor', 'query',
'{"type": "object", "properties": {"queryParams": {"type": "object", "description": "查询参数"}}}',
'{"type": "object", "properties": {"queryResult": {"type": "array", "description": "查询结果"}, "rowCount": {"type": "number", "description": "行数"}}}');

-- MySQL插入算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(9, 4, 'MYSQL_INSERT', 'MySQL插入', '执行MySQL插入操作', 'com.operatorchoreography.executor.MySqlInsertExecutor', 'insert',
'{"type": "object", "properties": {"insertData": {"type": "object", "description": "插入数据"}}}',
'{"type": "object", "properties": {"insertResult": {"type": "object", "description": "插入结果"}, "insertedId": {"type": "number", "description": "插入的ID"}}}');

-- MySQL更新算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(10, 4, 'MYSQL_UPDATE', 'MySQL更新', '执行MySQL更新操作', 'com.operatorchoreography.executor.MySqlUpdateExecutor', 'update',
'{"type": "object", "properties": {"updateData": {"type": "object", "description": "更新数据"}}}',
'{"type": "object", "properties": {"updateResult": {"type": "object", "description": "更新结果"}, "affectedRows": {"type": "number", "description": "影响行数"}}}');

-- MySQL删除算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(11, 4, 'MYSQL_DELETE', 'MySQL删除', '执行MySQL删除操作', 'com.operatorchoreography.executor.MySqlDeleteExecutor', 'delete',
'{"type": "object", "properties": {"deleteParams": {"type": "object", "description": "删除参数"}}}',
'{"type": "object", "properties": {"deleteResult": {"type": "object", "description": "删除结果"}, "deletedRows": {"type": "number", "description": "删除行数"}}}');

-- ========================================
-- 5. 函数算子模板
-- ========================================

-- 字符串格式化算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(12, 5, 'STRING_FORMAT', '字符串格式化', '格式化字符串内容', 'com.operatorchoreography.executor.StringFormatExecutor', 'format',
'{"type": "object", "properties": {"inputString": {"type": "string", "description": "输入字符串"}, "formatData": {"type": "object", "description": "格式化数据"}}}',
'{"type": "object", "properties": {"formattedString": {"type": "string", "description": "格式化后的字符串"}}}');

-- 经纬度转换算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(13, 5, 'COORDINATE_CONVERT', '经纬度转换', '坐标系转换', 'com.operatorchoreography.executor.CoordinateConvertExecutor', 'convert',
'{"type": "object", "properties": {"longitude": {"type": "number", "description": "经度"}, "latitude": {"type": "number", "description": "纬度"}}}',
'{"type": "object", "properties": {"convertedLng": {"type": "number", "description": "转换后的经度"}, "convertedLat": {"type": "number", "description": "转换后的纬度"}}}');

-- 日期格式化算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(14, 5, 'DATE_FORMAT', '日期格式化', '日期时间格式转换', 'com.operatorchoreography.executor.DateFormatExecutor', 'format',
'{"type": "object", "properties": {"inputDate": {"type": "string", "description": "输入日期"}}}',
'{"type": "object", "properties": {"formattedDate": {"type": "string", "description": "格式化后的日期"}}}');

-- 数据加密算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(15, 5, 'DATA_ENCRYPT', '数据加密', '数据加密处理', 'com.operatorchoreography.executor.DataEncryptExecutor', 'encrypt',
'{"type": "object", "properties": {"plainText": {"type": "string", "description": "明文数据"}}}',
'{"type": "object", "properties": {"encryptedText": {"type": "string", "description": "加密后的数据"}}}');

-- 数据解密算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(16, 5, 'DATA_DECRYPT', '数据解密', '数据解密处理', 'com.operatorchoreography.executor.DataDecryptExecutor', 'decrypt',
'{"type": "object", "properties": {"encryptedText": {"type": "string", "description": "加密数据"}}}',
'{"type": "object", "properties": {"plainText": {"type": "string", "description": "解密后的明文"}}}');

-- 数据验证算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(17, 5, 'DATA_VALIDATE', '数据验证', '数据格式和内容验证', 'com.operatorchoreography.executor.DataValidateExecutor', 'validate',
'{"type": "object", "properties": {"inputData": {"type": "object", "description": "待验证数据"}}}',
'{"type": "object", "properties": {"isValid": {"type": "boolean", "description": "验证结果"}, "errors": {"type": "array", "description": "错误信息"}}}');

-- 数学计算算子
INSERT IGNORE INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method,
input_schema, output_schema) VALUES
(18, 5, 'MATH_CALCULATE', '数学计算', '数学表达式计算', 'com.operatorchoreography.executor.MathCalculateExecutor', 'calculate',
'{"type": "object", "properties": {"inputData": {"type": "object", "description": "计算数据"}}}',
'{"type": "object", "properties": {"result": {"type": "number", "description": "计算结果"}}}');

-- ========================================
-- 插入算子参数定义
-- ========================================

-- 数据映射算子参数
INSERT IGNORE INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required) VALUES
(1, 'input_data', '输入数据', 'object', 'input', 1),
(1, 'mapping_rules', '映射规则', 'array', 'config', 1),
(1, 'output_data', '输出数据', 'object', 'output', 0);

-- HTTP请求算子参数
INSERT IGNORE INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required) VALUES
(7, 'request_url', '请求URL', 'string', 'config', 1),
(7, 'request_method', '请求方法', 'string', 'config', 1),
(7, 'request_headers', '请求头', 'object', 'config', 0),
(7, 'request_body', '请求体', 'object', 'input', 0),
(7, 'response_data', '响应数据', 'object', 'output', 0);

-- MySQL查询算子参数
INSERT IGNORE INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required) VALUES
(8, 'sql_statement', 'SQL语句', 'string', 'config', 1),
(8, 'datasource', '数据源', 'string', 'config', 1),
(8, 'query_params', '查询参数', 'object', 'input', 0),
(8, 'query_result', '查询结果', 'array', 'output', 0);

-- 示例工作流
INSERT IGNORE INTO workflows (id, workflow_code, workflow_name, description, status, created_by) VALUES
(1, 'USER_DATA_PROCESS', '用户数据处理流程', '处理用户注册数据，包括格式转换、条件验证和数据存储', 'DRAFT', 'admin');

-- 示例工作流节点
INSERT IGNORE INTO workflow_nodes (id, workflow_id, node_code, node_name, template_id, position_x, position_y) VALUES
(1, 1, 'START_NODE', '开始节点', 1, 100, 100),
(2, 1, 'DATA_VALIDATE_NODE', '数据验证', 17, 300, 100),
(3, 1, 'AGE_CHECK_NODE', '年龄检查', 4, 500, 100),
(4, 1, 'SAVE_USER_NODE', '保存用户', 9, 700, 150),
(5, 1, 'SEND_EMAIL_NODE', '发送邮件', 7, 700, 50); 