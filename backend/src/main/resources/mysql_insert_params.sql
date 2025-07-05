-- ========================================
-- MySQL插入算子参数定义
-- 为MySQL插入算子添加必要的参数配置
-- ========================================

-- 为MySQL插入算子(ID=9)添加参数定义
INSERT IGNORE INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required, default_value, validation_rules) VALUES

-- 1. 数据源配置参数
(9, 'datasource', '数据源配置', 'object', 'config', 1, 
'{"url": "jdbc:mysql://192.168.0.25:3306/operator_choreography?useSSL=false&serverTimezone=UTC&characterEncoding=utf8", "driver-class-name": "com.mysql.cj.jdbc.Driver", "username": "root", "password": "123456"}',
'{"type": "object", "properties": {"url": {"type": "string", "description": "数据库连接URL", "required": true}, "driver-class-name": {"type": "string", "description": "数据库驱动类名", "required": true}, "username": {"type": "string", "description": "数据库用户名", "required": true}, "password": {"type": "string", "description": "数据库密码", "required": true}}}'),

-- 2. 目标表名参数
(9, 'table', '目标表名', 'string', 'config', 1, 
'table_name',
'{"type": "string", "minLength": 1, "maxLength": 100, "description": "数据库表名，只能包含字母、数字和下划线"}'),

-- 3. SQL语句参数
(9, 'sql_statement', 'SQL插入语句', 'string', 'config', 1, 
'INSERT INTO table_name (column1, column2) VALUES (?, ?)',
'{"type": "string", "minLength": 10, "description": "SQL插入语句，支持预编译参数"}'),

-- 4. 插入数据参数（输入）
(9, 'insert_data', '插入数据', 'object', 'input', 1, 
'{}',
'{"type": "object", "description": "要插入的数据对象，字段名应与SQL语句中的参数对应"}'),

-- 5. 插入结果参数（输出）
(9, 'insert_result', '插入结果', 'object', 'output', 0, 
'{"success": true, "insertedId": 1, "affectedRows": 1}',
'{"type": "object", "properties": {"success": {"type": "boolean", "description": "插入是否成功"}, "insertedId": {"type": "number", "description": "插入记录的主键ID"}, "affectedRows": {"type": "number", "description": "影响的行数"}}}'),

-- 6. 事务控制参数
(9, 'use_transaction', '使用事务', 'boolean', 'config', 0, 
'true',
'{"type": "boolean", "description": "是否在事务中执行插入操作"}'),

-- 7. 批量插入支持参数
(9, 'batch_insert', '批量插入', 'boolean', 'config', 0, 
'false',
'{"type": "boolean", "description": "是否支持批量插入模式"}'),

-- 8. 超时配置参数
(9, 'timeout_seconds', '执行超时时间', 'number', 'config', 0, 
'30',
'{"type": "number", "minimum": 1, "maximum": 300, "description": "SQL执行超时时间，单位：秒"}');

-- 查询验证插入结果
SELECT 
    t.template_name,
    p.param_key,
    p.param_name,
    p.param_type,
    p.param_category,
    p.is_required,
    p.default_value
FROM operator_templates t
JOIN operator_template_params p ON t.id = p.template_id
WHERE t.id = 9
ORDER BY p.param_category, p.id; 