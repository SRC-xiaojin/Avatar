-- ========================================
-- JOLT映射规则示例配置
-- 为算子模板添加JOLT数据转换规则
-- ========================================

-- 为数据映射算子(template_id=1)添加JOLT规则示例

-- 删除已存在的mapping_rules配置
DELETE FROM operator_template_params 
WHERE template_id = 1 AND param_key = 'mapping_rules' AND param_category = 'config';

-- 示例1: 简单字段映射和重命名
INSERT INTO operator_template_params (
    template_id, param_key, param_name, param_type, param_category, 
    is_required, default_value, description
) VALUES (
    1, 'mapping_rules', 'JOLT映射规则', 'text', 'config', 1,
    '[
        {
            "operation": "shift",
            "spec": {
                "name": "userName",
                "age": "userAge",
                "email": "contact.email",
                "phone": "contact.phone",
                "address": {
                    "street": "address.streetName",
                    "city": "address.cityName",
                    "zipcode": "address.postalCode"
                }
            }
        }
    ]',
    '用户信息映射规则 - 将用户基本信息进行字段重命名和结构调整'
);

-- 为其他模板添加不同的JOLT规则示例

-- 示例2: 复杂数据转换规则 (为模板ID=2创建)
INSERT INTO operator_template_params (
    template_id, param_key, param_name, param_type, param_category, 
    is_required, default_value, description
) VALUES (
    2, 'mapping_rules', 'JOLT映射规则', 'text', 'config', 1,
    '[
        {
            "operation": "shift",
            "spec": {
                "users": {
                    "*": {
                        "name": "people[&1].fullName",
                        "age": "people[&1].years",
                        "skills": {
                            "*": "people[&2].abilities[&]"
                        }
                    }
                }
            }
        },
        {
            "operation": "default",
            "spec": {
                "people": {
                    "*": {
                        "status": "active",
                        "createdAt": "${#getCurrentTimestamp()}"
                    }
                }
            }
        }
    ]',
    '用户列表转换规则 - 数组转换和默认值设置'
);

-- 示例3: 条件转换和计算规则
INSERT INTO operator_template_params (
    template_id, param_key, param_name, param_type, param_category, 
    is_required, default_value, description
) VALUES (
    3, 'mapping_rules', 'JOLT映射规则', 'text', 'config', 1,
    '[
        {
            "operation": "shift",
            "spec": {
                "order": {
                    "id": "orderId",
                    "items": {
                        "*": {
                            "name": "orderItems[&1].productName",
                            "price": "orderItems[&1].unitPrice",
                            "quantity": "orderItems[&1].qty"
                        }
                    },
                    "customer": "customerInfo",
                    "total": "orderTotal"
                }
            }
        },
        {
            "operation": "modify-overwrite-beta",
            "spec": {
                "orderItems": {
                    "*": {
                        "subtotal": "=multiply(@(1,unitPrice),@(1,qty))"
                    }
                }
            }
        }
    ]',
    '订单数据转换规则 - 包含计算逻辑的复杂转换'
);

-- 示例4: 扁平化数据转换
INSERT INTO operator_template_params (
    template_id, param_key, param_name, param_type, param_category, 
    is_required, default_value, description
) VALUES (
    4, 'mapping_rules', 'JOLT映射规则', 'text', 'config', 1,
    '[
        {
            "operation": "shift",
            "spec": {
                "user": {
                    "profile": {
                        "firstName": "first_name",
                        "lastName": "last_name",
                        "contactInfo": {
                            "email": "email_address",
                            "phone": "phone_number"
                        }
                    },
                    "preferences": {
                        "language": "preferred_language",
                        "timezone": "user_timezone"
                    }
                }
            }
        }
    ]',
    '嵌套结构扁平化规则 - 将深层嵌套结构转换为平面结构'
);

-- 为数据映射算子添加其他相关参数

-- 输入数据参数
INSERT IGNORE INTO operator_template_params (
    template_id, param_key, param_name, param_type, param_category, 
    is_required, default_value, description, validation_rules
) VALUES (
    1, 'input_data', '输入数据', 'object', 'input', 1, '{}',
    '需要进行JOLT转换的原始数据', 
    '{"type": "object", "required": true}'
);

-- 输出数据参数
INSERT IGNORE INTO operator_template_params (
    template_id, param_key, param_name, param_type, param_category, 
    is_required, default_value, description
) VALUES (
    1, 'output_data', '输出数据', 'object', 'output', 0, null,
    'JOLT转换后的结果数据'
);

-- 错误处理配置
INSERT IGNORE INTO operator_template_params (
    template_id, param_key, param_name, param_type, param_category, 
    is_required, default_value, description
) VALUES (
    1, 'error_handling', '错误处理方式', 'string', 'config', 0, 'log',
    '转换失败时的处理方式: log(记录日志), throw(抛出异常), ignore(忽略错误)'
);

-- 调试模式配置
INSERT IGNORE INTO operator_template_params (
    template_id, param_key, param_name, param_type, param_category, 
    is_required, default_value, description
) VALUES (
    1, 'debug_mode', '调试模式', 'boolean', 'config', 0, 'false',
    '是否启用调试模式，启用后会输出详细的转换过程信息'
);

-- 提交更改
COMMIT; 