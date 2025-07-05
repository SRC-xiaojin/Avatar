-- ========================================
-- MySQL插入算子参数定义 - 逐条插入版本
-- 避免JSON格式问题的手动插入方式
-- ========================================

-- 1. 插入datasource参数
INSERT INTO operator_template_params 
(template_id, param_key, param_name, param_type, param_category, is_required, default_value) 
VALUES 
(9, 'datasource', '数据源配置', 'object', 'config', 1, 
CONCAT(
'{"url": "jdbc:mysql://192.168.0.25:3306/operator_choreography?useSSL=false&serverTimezone=UTC&characterEncoding=utf8", ',
'"driver-class-name": "com.mysql.cj.jdbc.Driver", ',
'"username": "root", ',
'"password": "123456"}'
)
);

-- 2. 插入table参数
INSERT INTO operator_template_params 
(template_id, param_key, param_name, param_type, param_category, is_required, default_value) 
VALUES 
(9, 'table', '目标表名', 'string', 'config', 1, 'users');

-- 3. 插入sql_statement参数
INSERT INTO operator_template_params 
(template_id, param_key, param_name, param_type, param_category, is_required, default_value) 
VALUES 
(9, 'sql_statement', 'SQL插入语句', 'string', 'config', 1, 'INSERT INTO users (name, email, age) VALUES (?, ?, ?)');

-- 查询验证结果
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
ORDER BY p.param_key; 