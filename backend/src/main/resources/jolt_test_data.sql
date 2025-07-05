-- ========================================
-- JOLT测试数据配置
-- 为不同模板创建JOLT转换规则的测试数据
-- ========================================

-- 清除已存在的测试数据
DELETE FROM operator_template_params WHERE template_id IN (10, 11, 12, 13, 14);
DELETE FROM operator_templates WHERE id IN (10, 11, 12, 13, 14);

-- 创建JOLT测试模板

-- 测试模板1: 简单字段映射
INSERT INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method, input_schema, output_schema, status) VALUES
(10, 1, 'JOLT_SIMPLE_MAPPING', '简单字段映射测试', 'JOLT简单字段重命名和映射测试', 'com.operatorchoreography.executor.DataMappingExecutor', 'execute',
'{"type": "object", "properties": {"name": {"type": "string"}, "age": {"type": "number"}, "email": {"type": "string"}}}',
'{"type": "object", "properties": {"userName": {"type": "string"}, "userAge": {"type": "number"}, "userEmail": {"type": "string"}}}', 1);

-- 测试模板2: 嵌套结构映射
INSERT INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method, input_schema, output_schema, status) VALUES
(11, 1, 'JOLT_NESTED_MAPPING', '嵌套结构映射测试', 'JOLT嵌套结构扁平化映射测试', 'com.operatorchoreography.executor.DataMappingExecutor', 'execute',
'{"type": "object", "properties": {"user": {"type": "object"}}}',
'{"type": "object", "properties": {"first_name": {"type": "string"}, "last_name": {"type": "string"}}}', 1);

-- 测试模板3: 数组转换
INSERT INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method, input_schema, output_schema, status) VALUES
(12, 1, 'JOLT_ARRAY_TRANSFORM', '数组转换测试', 'JOLT数组结构转换测试', 'com.operatorchoreography.executor.DataMappingExecutor', 'execute',
'{"type": "object", "properties": {"users": {"type": "array"}}}',
'{"type": "object", "properties": {"people": {"type": "array"}}}', 1);

-- 测试模板4: 默认值设置
INSERT INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method, input_schema, output_schema, status) VALUES
(13, 1, 'JOLT_DEFAULT_VALUES', '默认值设置测试', 'JOLT添加默认值测试', 'com.operatorchoreography.executor.DataMappingExecutor', 'execute',
'{"type": "object", "properties": {"name": {"type": "string"}, "age": {"type": "number"}}}',
'{"type": "object", "properties": {"userName": {"type": "string"}, "userAge": {"type": "number"}, "status": {"type": "string"}}}', 1);

-- 测试模板5: 复杂转换
INSERT INTO operator_templates (id, category_id, template_code, template_name, description, executor_class, executor_method, input_schema, output_schema, status) VALUES
(14, 1, 'JOLT_COMPLEX_TRANSFORM', '复杂转换测试', 'JOLT复杂数据结构转换测试', 'com.operatorchoreography.executor.DataMappingExecutor', 'execute',
'{"type": "object", "properties": {"orders": {"type": "array"}}}',
'{"type": "object", "properties": {"orderList": {"type": "array"}}}', 1);

-- 为测试模板添加JOLT规则参数

-- 简单字段映射规则
INSERT INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required, default_value, description) VALUES
(10, 'mapping_rules', 'JOLT映射规则', 'text', 'config', 1,
'[
    {
        "operation": "shift",
        "spec": {
            "name": "userName",
            "age": "userAge",
            "email": "userEmail"
        }
    }
]',
'简单字段重命名规则 - 将name映射为userName，age映射为userAge，email映射为userEmail');

-- 嵌套结构映射规则
INSERT INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required, default_value, description) VALUES
(11, 'mapping_rules', 'JOLT映射规则', 'text', 'config', 1,
'[
    {
        "operation": "shift",
        "spec": {
            "user": {
                "profile": {
                    "firstName": "first_name",
                    "lastName": "last_name",
                    "contact": {
                        "email": "email_address",
                        "phone": "phone_number"
                    }
                }
            }
        }
    }
]',
'嵌套结构扁平化规则 - 将深层嵌套的用户信息转换为平面结构');

-- 数组转换规则
INSERT INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required, default_value, description) VALUES
(12, 'mapping_rules', 'JOLT映射规则', 'text', 'config', 1,
'[
    {
        "operation": "shift",
        "spec": {
            "users": {
                "*": {
                    "name": "people[&1].fullName",
                    "age": "people[&1].years",
                    "city": "people[&1].location"
                }
            }
        }
    }
]',
'数组转换规则 - 将users数组转换为people数组，并重命名字段');

-- 默认值设置规则
INSERT INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required, default_value, description) VALUES
(13, 'mapping_rules', 'JOLT映射规则', 'text', 'config', 1,
'[
    {
        "operation": "shift",
        "spec": {
            "name": "userName",
            "age": "userAge"
        }
    },
    {
        "operation": "default",
        "spec": {
            "status": "active",
            "role": "user",
            "createTime": "2024-01-01"
        }
    }
]',
'默认值设置规则 - 映射字段并添加默认值');

-- 复杂转换规则
INSERT INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required, default_value, description) VALUES
(14, 'mapping_rules', 'JOLT映射规则', 'text', 'config', 1,
'[
    {
        "operation": "shift",
        "spec": {
            "orders": {
                "*": {
                    "id": "orderList[&1].orderId",
                    "amount": "orderList[&1].orderAmount",
                    "status": "orderList[&1].orderStatus",
                    "customer": {
                        "name": "orderList[&2].customerName",
                        "email": "orderList[&2].customerEmail"
                    },
                    "items": {
                        "*": {
                            "productId": "orderList[&3].products[&1].id",
                            "productName": "orderList[&3].products[&1].name",
                            "quantity": "orderList[&3].products[&1].qty",
                            "price": "orderList[&3].products[&1].unitPrice"
                        }
                    }
                }
            }
        }
    }
]',
'复杂订单数据转换规则 - 包含嵌套对象和数组的复杂转换');

-- 为每个测试模板添加输入和输出参数定义

-- 通用输入参数
INSERT INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required, default_value, description) VALUES
(10, 'input_data', '输入数据', 'object', 'input', 1, '{"name": "张三", "age": 25, "email": "zhangsan@example.com"}', '需要进行JOLT转换的输入数据'),
(11, 'input_data', '输入数据', 'object', 'input', 1, '{"user": {"profile": {"firstName": "张", "lastName": "三", "contact": {"email": "zhangsan@example.com", "phone": "13800138000"}}}}', '嵌套结构输入数据'),
(12, 'input_data', '输入数据', 'object', 'input', 1, '{"users": [{"name": "张三", "age": 25, "city": "北京"}, {"name": "李四", "age": 30, "city": "上海"}]}', '包含用户数组的输入数据'),
(13, 'input_data', '输入数据', 'object', 'input', 1, '{"name": "张三", "age": 25}', '需要添加默认值的输入数据'),
(14, 'input_data', '输入数据', 'object', 'input', 1, '{"orders": [{"id": 1, "amount": 100, "status": "paid", "customer": {"name": "张三", "email": "zhangsan@example.com"}, "items": [{"productId": 101, "productName": "商品A", "quantity": 2, "price": 50}]}]}', '复杂订单数据');

-- 通用输出参数
INSERT INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required, default_value, description) VALUES
(10, 'output_data', '输出数据', 'object', 'output', 0, null, 'JOLT转换后的输出数据'),
(11, 'output_data', '输出数据', 'object', 'output', 0, null, 'JOLT转换后的输出数据'),
(12, 'output_data', '输出数据', 'object', 'output', 0, null, 'JOLT转换后的输出数据'),
(13, 'output_data', '输出数据', 'object', 'output', 0, null, 'JOLT转换后的输出数据'),
(14, 'output_data', '输出数据', 'object', 'output', 0, null, 'JOLT转换后的输出数据');

-- 添加测试配置参数
INSERT INTO operator_template_params (template_id, param_key, param_name, param_type, param_category, is_required, default_value, description) VALUES
(10, 'debug_mode', '调试模式', 'boolean', 'config', 0, 'false', '是否启用调试模式'),
(11, 'debug_mode', '调试模式', 'boolean', 'config', 0, 'false', '是否启用调试模式'),
(12, 'debug_mode', '调试模式', 'boolean', 'config', 0, 'false', '是否启用调试模式'),
(13, 'debug_mode', '调试模式', 'boolean', 'config', 0, 'false', '是否启用调试模式'),
(14, 'debug_mode', '调试模式', 'boolean', 'config', 0, 'false', '是否启用调试模式');

-- 提交更改
COMMIT; 