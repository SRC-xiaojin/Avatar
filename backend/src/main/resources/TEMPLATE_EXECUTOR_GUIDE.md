# 算子模板执行器配置指南

## 概述

本指南说明如何通过 `templateId` 获取算子的执行类（executor_class）和执行方法（executor_method），以及如何在工作流测试中正确调用这些执行器。

## 功能特点

✅ **动态执行器调用**：根据模板ID自动查找对应的执行器类和方法  
✅ **完整性验证**：自动验证模板配置的完整性和有效性  
✅ **错误处理**：完善的异常处理和错误日志记录  
✅ **回退机制**：当执行器不可用时自动使用默认逻辑  
✅ **调试支持**：详细的执行日志便于问题排查

## 核心实现

### 1. 数据库表结构

算子模板表 `operator_templates` 中的关键字段：

```sql
-- 执行器配置字段
executor_class VARCHAR(500)    -- 执行器类的完整类名
executor_method VARCHAR(100)   -- 执行器方法名
status BOOLEAN                 -- 模板启用状态
is_async BOOLEAN              -- 是否异步执行
timeout_seconds INT           -- 超时时间（秒）
retry_count INT               -- 重试次数
```

### 2. 工作流执行流程

```
工作流测试 → 获取节点 → 查询模板 → 验证配置 → 调用执行器 → 返回结果
    ↓              ↓          ↓         ↓          ↓         ↓
testWorkflow → WorkflowNodes → OperatorTemplates → 验证 → ExecutorManager → 输出
```

### 3. 核心方法

#### 主要执行方法
```java
// 根据模板ID执行节点
private Object executeNodeByTemplate(WorkflowNodes node, Map<String, Object> context, Map<String, Object> params)

// 验证模板执行器配置
private boolean isTemplateExecutorValid(OperatorTemplates template)

// 执行默认节点逻辑
private Object executeDefaultNodeLogic(WorkflowNodes node, Map<String, Object> context, Map<String, Object> params)
```

## 配置示例

### 1. 数据加密算子配置

```sql
INSERT INTO operator_templates (
    template_code, 
    template_name, 
    executor_class, 
    executor_method,
    status
) VALUES (
    'DATA_ENCRYPT',
    '数据加密',
    'com.operatorchoreography.executor.DataEncryptExecutor',
    'encrypt',
    true
);
```

### 2. HTTP请求算子配置

```sql
INSERT INTO operator_templates (
    template_code, 
    template_name, 
    executor_class, 
    executor_method,
    is_async,
    timeout_seconds,
    status
) VALUES (
    'HTTP_REQUEST',
    'HTTP请求',
    'com.operatorchoreography.executor.HttpRequestExecutor',
    'execute',
    true,
    60,
    true
);
```

## 使用说明

### 1. 配置算子模板

首先在数据库中配置算子模板：

```sql
-- 运行示例配置脚本
SOURCE operator_template_executor_examples.sql;
```

### 2. 创建工作流

在前端设计器中：
1. 拖拽算子到画布
2. 选择对应的算子模板
3. 配置节点参数
4. 建立节点连接关系

### 3. 测试工作流

在工作流管理页面：
1. 点击"测试"按钮
2. 输入测试数据
3. 点击"开始测试"
4. 查看执行结果

## 执行器要求

### 1. 执行器类结构

**新版本执行器结构**（推荐）：

```java
@Slf4j
@Component
public class YourExecutor implements BaseExecutor {
    
    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 执行业务逻辑
            Object processedData = processData(inputData);
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("data", processedData);
            outputData.put("message", "执行成功");
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("执行成功");
                                
        } catch (Exception e) {
            log.error("执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 可选的业务方法
     */
    public ExecutorResult yourBusinessMethod(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }
}
```

**旧版本执行器结构**（兼容性保留）：

```java
@Component
public class YourLegacyExecutor {
    
    /**
     * 旧版本方法仍然支持，但建议迁移到新版本
     * @param inputData 输入数据
     * @param templateId 模板ID
     * @return 执行结果
     */
    public Map<String, Object> yourMethod(Map<String, Object> inputData, Long templateId) {
        // 执行逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", processedData);
        return result;
    }
}
```

### 2. 返回值格式

**新版本执行器**必须返回 `ExecutorResult` 对象：

```java
// 成功结果
return ExecutorResult.success(outputData, startTime, endTime)
                    .withTemplateInfo(templateId, templateName)
                    .withLog("执行成功");

// 失败结果
return ExecutorResult.failure("执行失败", exception)
                    .withTemplateInfo(templateId, templateName)
                    .withLog("执行失败详情");
```

**ExecutorResult 包含的字段：**
- `status`: 执行状态枚举（SUCCESS/FAILED/TIMEOUT/CANCELLED等）
- `success`: 是否成功（boolean）
- `outputData`: 输出数据（Map<String, Object>）
- `errorMessage`: 错误信息
- `errorCode`: 错误代码
- `startTime/endTime`: 执行时间
- `executionTimeMs`: 执行耗时（毫秒）
- `nodeId/nodeName`: 节点信息
- `templateId/templateName`: 模板信息
- `executorClass/executorMethod`: 执行器信息
- `executionLog`: 执行日志
- `metadata`: 扩展元数据

**向后兼容性**：现有的 Map 格式仍然支持，系统会自动转换为 ExecutorResult。

## 错误处理

### 1. 模板不存在

```
错误信息: "模板不存在: templateId=123"
处理方式: 使用默认节点逻辑
```

### 2. 模板已禁用

```
错误信息: "模板已禁用: templateId=123, templateName=数据加密"
处理方式: 使用默认节点逻辑
```

### 3. 执行器信息不完整

```
错误信息: "模板执行器信息不完整: templateId=123, executorClass=null"
处理方式: 使用默认节点逻辑
```

### 4. 执行器调用失败

```
错误信息: "执行器调用失败: templateId=123, 错误: ClassNotFoundException"
处理方式: 返回错误信息，继续执行其他节点
```

## 调试和监控

### 1. 执行日志

系统使用 SLF4J 输出详细的执行信息，支持不同日志级别：

**信息日志 (INFO)**：
```
2024-01-01 10:00:00.123 [INFO ] === 算子模板执行信息 ===
2024-01-01 10:00:00.124 [INFO ] 模板ID: 1
2024-01-01 10:00:00.125 [INFO ] 模板名称: 数据加密
2024-01-01 10:00:00.126 [INFO ] 模板编码: DATA_ENCRYPT
2024-01-01 10:00:00.127 [INFO ] 执行器类: com.operatorchoreography.executor.DataEncryptExecutor
2024-01-01 10:00:00.128 [INFO ] 执行方法: encrypt
2024-01-01 10:00:00.129 [INFO ] 是否启用: true
2024-01-01 10:00:00.130 [INFO ] 是否异步: false
2024-01-01 10:00:00.131 [INFO ] 超时时间: 30秒
2024-01-01 10:00:00.132 [INFO ] 重试次数: 3
2024-01-01 10:00:00.133 [INFO ] ========================
```

**警告日志 (WARN)**：
```
2024-01-01 10:00:01.234 [WARN ] 模板不存在: templateId=123
2024-01-01 10:00:01.235 [WARN ] 模板已禁用: templateId=456, templateName=数据加密
2024-01-01 10:00:01.236 [WARN ] 模板执行器信息不完整: templateId=789, templateName=HTTP请求, executorClass=null, executorMethod=execute
```

**错误日志 (ERROR)**：
```
2024-01-01 10:00:02.345 [ERROR] 节点执行失败: 数据加密节点, 错误: ClassNotFoundException
2024-01-01 10:00:02.346 [ERROR] 执行器调用失败: templateId=123, 错误: Method not found
```

### 2. 日志配置

可以在 `application.yml` 中配置日志级别：

```yaml
logging:
  level:
    com.operatorchoreography.generator.service.impl.WorkflowsServiceImpl: DEBUG
    com.operatorchoreography.executor: INFO
    root: INFO
  
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%level] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%level] %logger{36} - %msg%n"
    
  file:
    name: logs/workflow-executor.log
    max-size: 10MB
    max-history: 30
```

日志级别说明：
- **DEBUG**：显示所有调试信息
- **INFO**：显示正常执行信息
- **WARN**：显示警告信息（如模板不存在）
- **ERROR**：显示错误信息（如执行失败）

### 3. 测试结果

测试完成后会返回详细的执行报告：

```json
{
    "testId": "test-1-1704067200000",
    "workflowId": 1,
    "status": "SUCCESS",
    "nodeCount": 3,
    "connectionCount": 2,
    "executionQueue": [...],       // 节点执行顺序
    "nodeResults": [...],          // 每个节点的执行结果
    "finalOutput": {...}           // 最终输出数据
}
```

## 最佳实践

### 1. 模板命名规范

- 使用有意义的模板编码：`DATA_ENCRYPT`, `HTTP_REQUEST`
- 模板名称使用中文：`数据加密`, `HTTP请求`
- 执行器类名遵循包命名规范

### 2. 错误处理

- 执行器方法应该捕获并处理所有可能的异常
- 返回统一格式的错误信息
- 记录详细的错误日志便于调试

### 3. 日志记录

- 使用 `@Slf4j` 注解而不是 `System.out.println`
- 使用参数化日志避免字符串拼接：`log.info("模板ID: {}", templateId)`
- 异常日志应包含堆栈信息：`log.error("执行失败", e)`
- 根据日志重要性选择合适的日志级别（DEBUG/INFO/WARN/ERROR）
- 定期清理和归档日志文件

### 4. 性能优化

- 合理设置超时时间和重试次数
- 对于耗时操作考虑使用异步执行
- 避免在执行器中进行阻塞操作

### 5. 测试验证

- 每个算子模板都应该有对应的单元测试
- 在生产环境部署前进行完整的工作流测试
- 定期验证模板配置的有效性

## 故障排除

### 常见问题

1. **执行器类找不到**
   - 检查类路径和包名是否正确
   - 确认执行器类已添加 `@Component` 注解

2. **方法签名不匹配**
   - 确认方法参数为 `(Map<String, Object> inputData, Long templateId)`
   - 确认返回值类型为 `Map<String, Object>`

3. **模板配置错误**
   - 检查 `executor_class` 和 `executor_method` 字段
   - 确认 `status` 字段为 `true`

4. **超时问题**
   - 适当增加 `timeout_seconds` 值
   - 检查执行器内部是否有死循环或阻塞

## 执行器重构

### 从 Map 到 ExecutorResult 迁移

系统正在从 `Map<String, Object>` 返回值迁移到 `ExecutorResult` 对象结构，以提供更好的类型安全性和扩展性。

#### 重构状态

**已完成重构：**
- ✅ **JsonParseExecutor** - JSON解析执行器
- ✅ **JsonStringifyExecutor** - JSON序列化执行器
- ✅ **MySqlInsertExecutor** - MySQL插入执行器（包括insertWithUpsert方法）
- ✅ **StringFormatExecutor** - 字符串格式化执行器
- ✅ **MathCalculateExecutor** - 数学计算执行器
- ✅ **DataValidateExecutor** - 数据验证执行器
- ✅ **HttpRequestExecutor** - HTTP请求执行器
- ✅ **DateFormatExecutor** - 日期格式化执行器
- ✅ **IfConditionExecutor** - 条件判断执行器
- ✅ **DataEncryptExecutor** - 数据加密执行器
- ✅ **DataDecryptExecutor** - 数据解密执行器
- ✅ **DataMappingExecutor** - 数据映射执行器
- ✅ **CoordinateConvertExecutor** - 坐标转换执行器
- ✅ **ForLoopExecutor** - For循环执行器
- ✅ **WhileLoopExecutor** - While循环执行器

**待重构执行器：**
- ⏳ **MySqlQueryExecutor** - MySQL查询执行器
- ⏳ **MySqlUpdateExecutor** - MySQL更新执行器
- ⏳ **MySqlDeleteExecutor** - MySQL删除执行器

#### 重构指南

详细的重构步骤和示例请参考：[执行器重构指南](./EXECUTOR_REFACTOR_GUIDE.md)

#### 向后兼容性

- 现有的 Map 格式执行器仍然可以正常工作
- ExecutorManager 会自动将 Map 结果转换为 ExecutorResult
- 建议新开发的执行器直接使用 ExecutorResult 格式

## 扩展功能

### 已实现功能

- ✅ 支持统一的 ExecutorResult 返回值结构
- ✅ 支持自动执行时间记录
- ✅ 支持链式方法调用设置结果属性
- ✅ 支持 SLF4J 日志记录
- ✅ 支持向后兼容的 Map 格式转换

### 未来计划

- [ ] 支持执行器版本管理
- [ ] 支持执行器热加载
- [ ] 支持分布式执行器调用
- [ ] 支持执行器性能监控
- [ ] 支持执行器依赖注入配置
- [ ] 支持执行器结果缓存
- [ ] 支持执行器并行执行优化

---

更多信息请参考：
- [执行器重构指南](./EXECUTOR_REFACTOR_GUIDE.md)
- [ExecutorManager 使用指南](./EXECUTOR_MANAGER_GUIDE.md)
- [工作流测试指南](./WORKFLOW_TEST_GUIDE.md)
- [算子开发规范](./OPERATOR_DEVELOPMENT_GUIDE.md) 