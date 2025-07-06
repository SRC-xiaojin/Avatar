-- ========================================
-- 算子编排系统 - 增强版数据库设计
-- 支持零代码算子配置和管理
-- ========================================

-- 1. 算子类别表 - 定义算子的大类别
CREATE TABLE IF NOT EXISTS operator_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '类别ID',
    category_code VARCHAR(50) NOT NULL UNIQUE COMMENT '类别编码',
    category_name VARCHAR(100) NOT NULL COMMENT '类别名称',
    description TEXT COMMENT '类别描述',
    icon VARCHAR(255) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    if_delete tinyint(1) unsigned DEFAULT '0' COMMENT '0 未删除 1 已删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='算子类别表';

-- 2. 算子模板表 - 定义各种算子类型的模板
CREATE TABLE IF NOT EXISTS operator_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    category_id BIGINT NOT NULL COMMENT '所属类别',
    template_code VARCHAR(100) NOT NULL UNIQUE COMMENT '模板编码',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    description TEXT COMMENT '模板描述',
    icon VARCHAR(255) COMMENT '图标',
    version VARCHAR(20) DEFAULT '1.0.0' COMMENT '版本号',
    
    -- 执行相关
    executor_class VARCHAR(500) COMMENT '执行器类名',
    executor_method VARCHAR(100) COMMENT '执行方法',
    
    -- 输入输出定义
    input_schema JSON COMMENT '输入数据结构定义',
    output_schema JSON COMMENT '输出数据结构定义',
    
    -- 其他属性
    is_async TINYINT DEFAULT 0 COMMENT '是否异步执行',
    timeout_seconds INT DEFAULT 30 COMMENT '超时时间(秒)',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    
    status TINYINT DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (category_id) REFERENCES operator_categories(id),
    INDEX idx_category (category_id),
    INDEX idx_template_code (template_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='算子模板表';

-- 3. 算子参数定义表 - 定义算子模板的参数
CREATE TABLE IF NOT EXISTS operator_template_params (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '参数ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    param_key VARCHAR(100) NOT NULL COMMENT '参数键',
    param_name VARCHAR(200) NOT NULL COMMENT '参数名称',
    param_type VARCHAR(50) NOT NULL COMMENT '参数类型: string,number,boolean,object,array',
    param_category VARCHAR(50) NOT NULL COMMENT '参数类别: input,output,config',
    
    -- 验证规则
    is_required TINYINT DEFAULT 0 COMMENT '是否必填',
    default_value TEXT COMMENT '默认值',
    validation_rules JSON COMMENT '验证规则',
    
    if_delete tinyint(1) unsigned DEFAULT '0' COMMENT '0 未删除 1 已删除',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (template_id) REFERENCES operator_templates(id) ON DELETE CASCADE,
    UNIQUE KEY uk_template_param (template_id, param_key),
    INDEX idx_template (template_id),
    INDEX idx_category (param_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='算子参数定义表';

-- 4. 工作流表
CREATE TABLE IF NOT EXISTS workflows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '工作流ID',
    workflow_code VARCHAR(100) NOT NULL UNIQUE COMMENT '工作流编码',
    workflow_name VARCHAR(200) NOT NULL COMMENT '工作流名称',
    description TEXT COMMENT '工作流描述',
    version VARCHAR(20) DEFAULT '1.0.0' COMMENT '版本号',
    
    -- 状态管理
    status VARCHAR(50) DEFAULT 'DRAFT' COMMENT '状态: DRAFT,PUBLISHED,ARCHIVED',
    
    -- 执行配置
    execution_mode VARCHAR(50) DEFAULT 'SYNC' COMMENT '执行模式: SYNC,ASYNC',
    max_execution_time INT DEFAULT 300 COMMENT '最大执行时间(秒)',
    
    -- 元数据
    tags JSON COMMENT '标签',
    variables JSON COMMENT '工作流变量',
    
    if_delete tinyint(1) unsigned DEFAULT '0' COMMENT '0 未删除 1 已删除',
    
    created_by VARCHAR(100) COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(100) COMMENT '更新人',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_workflow_code (workflow_code),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工作流表';

-- 5. 工作流节点表 - 工作流中的算子实例
CREATE TABLE IF NOT EXISTS workflow_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '节点ID',
    workflow_id BIGINT NOT NULL COMMENT '工作流ID',
    node_code VARCHAR(100) NOT NULL COMMENT '节点编码',
    node_name VARCHAR(200) NOT NULL COMMENT '节点名称',
    template_id BIGINT NOT NULL COMMENT '算子模板ID',
    
    -- 位置信息
    position_x DOUBLE DEFAULT 0 COMMENT 'X坐标',
    position_y DOUBLE DEFAULT 0 COMMENT 'Y坐标',
    width DOUBLE DEFAULT 100 COMMENT '宽度',
    height DOUBLE DEFAULT 60 COMMENT '高度',
    
    -- 样式
    style JSON COMMENT '样式配置',
    
    -- 执行配置
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    execution_order INT DEFAULT 0 COMMENT '执行顺序',
    
    -- 错误处理
    on_error VARCHAR(50) DEFAULT 'STOP' COMMENT '错误处理: STOP,CONTINUE,RETRY',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    
    if_delete tinyint(1) unsigned DEFAULT '0' COMMENT '0 未删除 1 已删除',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (workflow_id) REFERENCES workflows(id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES operator_templates(id),
    UNIQUE KEY uk_workflow_node (workflow_id, node_code),
    INDEX idx_workflow (workflow_id),
    INDEX idx_template (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工作流节点表';

-- 6. 节点参数配置表 - 节点的参数实际值
CREATE TABLE IF NOT EXISTS workflow_node_params (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '参数配置ID',
    node_id BIGINT NOT NULL COMMENT '节点ID',
    param_key VARCHAR(100) NOT NULL COMMENT '参数键',
    param_value TEXT COMMENT '参数值',
    param_type VARCHAR(50) NOT NULL COMMENT '参数类型',
    
    -- 数据来源
    value_source VARCHAR(50) DEFAULT 'STATIC' COMMENT '值来源: STATIC,VARIABLE,EXPRESSION,INPUT',
    source_expression TEXT COMMENT '取值表达式',
    
    if_delete tinyint(1) unsigned DEFAULT '0' COMMENT '0 未删除 1 已删除',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (node_id) REFERENCES workflow_nodes(id) ON DELETE CASCADE,
    UNIQUE KEY uk_node_param (node_id, param_key),
    INDEX idx_node (node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='节点参数配置表';

-- 7. 工作流连接表 - 节点间的连接关系
CREATE TABLE IF NOT EXISTS workflow_connections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '连接ID',
    workflow_id BIGINT NOT NULL COMMENT '工作流ID',
    
    -- 连接信息
    source_node_id BIGINT NOT NULL COMMENT '源节点ID',
    source_port VARCHAR(100) COMMENT '源端口',
    target_node_id BIGINT NOT NULL COMMENT '目标节点ID',
    target_port VARCHAR(100) COMMENT '目标端口',
    
    -- 连接类型和条件
    connection_type VARCHAR(50) DEFAULT 'DATA' COMMENT '连接类型: DATA,CONTROL,ERROR',
    condition_expression TEXT COMMENT '连接条件表达式',
    
    -- 数据映射
    data_mapping JSON COMMENT '数据映射配置',
    
    -- 样式
    style JSON COMMENT '连接线样式',

    if_delete tinyint(1) unsigned DEFAULT '0' COMMENT '0 未删除 1 已删除',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (workflow_id) REFERENCES workflows(id) ON DELETE CASCADE,
    FOREIGN KEY (source_node_id) REFERENCES workflow_nodes(id) ON DELETE CASCADE,
    FOREIGN KEY (target_node_id) REFERENCES workflow_nodes(id) ON DELETE CASCADE,
    INDEX idx_workflow (workflow_id),
    INDEX idx_source (source_node_id),
    INDEX idx_target (target_node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工作流连接表';

-- 8. 工作流执行记录表
CREATE TABLE IF NOT EXISTS workflow_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '执行ID',
    workflow_id BIGINT NOT NULL COMMENT '工作流ID',
    execution_id VARCHAR(100) NOT NULL UNIQUE COMMENT '执行实例ID',
    
    -- 执行信息
    trigger_type VARCHAR(50) COMMENT '触发类型: MANUAL,SCHEDULE,API,EVENT',
    trigger_by VARCHAR(100) COMMENT '触发人',
    
    -- 状态信息
    status VARCHAR(50) DEFAULT 'RUNNING' COMMENT '状态: WAITING,RUNNING,SUCCESS,FAILED,CANCELLED',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    duration_ms BIGINT COMMENT '执行时长(毫秒)',
    
    -- 输入输出
    input_data JSON COMMENT '输入数据',
    output_data JSON COMMENT '输出数据',
    
    -- 错误信息
    error_message TEXT COMMENT '错误信息',
    error_stack TEXT COMMENT '错误堆栈',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (workflow_id) REFERENCES workflows(id),
    INDEX idx_workflow (workflow_id),
    INDEX idx_execution_id (execution_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工作流执行记录表';

-- 9. 节点执行记录表
CREATE TABLE IF NOT EXISTS node_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '节点执行ID',
    execution_id VARCHAR(100) NOT NULL COMMENT '工作流执行ID',
    node_id BIGINT NOT NULL COMMENT '节点ID',
    
    -- 执行信息
    execution_order INT COMMENT '执行顺序',
    status VARCHAR(50) DEFAULT 'WAITING' COMMENT '状态: WAITING,RUNNING,SUCCESS,FAILED,SKIPPED',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    duration_ms BIGINT COMMENT '执行时长(毫秒)',
    
    -- 数据
    input_data JSON COMMENT '输入数据',
    output_data JSON COMMENT '输出数据',
    
    -- 错误信息
    error_message TEXT COMMENT '错误信息',
    error_stack TEXT COMMENT '错误堆栈',
    retry_count INT DEFAULT 0 COMMENT '已重试次数',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (node_id) REFERENCES workflow_nodes(id),
    INDEX idx_execution (execution_id),
    INDEX idx_node (node_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='节点执行记录表'; 
