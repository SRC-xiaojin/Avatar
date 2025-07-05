-- ========================================
-- MySQL插入算子参数定义 - 简化版本
-- 只包含核心的三个参数：datasource、table、sql_statement
-- ========================================

-- 清理可能已存在的参数（可选）
DELETE FROM operator_template_params WHERE template_id = 9 AND param_key IN ('datasource', 'table', 'sql_statement');

-- 为MySQL插入算子(ID=9)添加核心参数定义
INSERT INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required, default_value) VALUES

-- 1. 数据源配置参数
(9, 'datasource', '数据源配置', 'object', 'config', 1, 
'{"url": "jdbc:mysql://192.168.0.25:3306/operator_choreography?useSSL=false&serverTimezone=UTC&characterEncoding=utf8", "driver-class-name": "com.mysql.cj.jdbc.Driver", "username": "root", "password": "123456"}'),

-- 2. 目标表名参数
(9, 'table', '目标表名', 'string', 'config', 1, 'users'),

-- 3. SQL语句参数
(9, 'sql_statement', 'SQL插入语句', 'string', 'config', 1, 'INSERT INTO users (name, email, age) VALUES (?, ?, ?)');

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
WHERE t.id = 9 AND p.param_key IN ('datasource', 'table', 'sql_statement')
ORDER BY p.param_category, p.id; 