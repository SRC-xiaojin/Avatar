# 日志配置和使用指南

## 日志配置概述

本项目使用 Logback 作为日志框架，配合 Slf4j 接口和 Lombok 的 @Slf4j 注解来实现日志记录功能。

## 日志配置文件

### logback-spring.xml 配置

位置：`src/main/resources/logback-spring.xml`

#### 日志文件分类：
1. **主日志文件**: `logs/operator-choreography.log` - 记录所有INFO级别以上的日志
2. **错误日志文件**: `logs/operator-choreography-error.log` - 仅记录ERROR级别的日志
3. **算子执行日志**: `logs/operator-choreography-executor.log` - 专门记录算子执行相关的日志

#### 日志滚动策略：
- 按天分割日志文件
- 单个文件最大100MB
- 保留30天的历史日志

#### 环境配置：
- **开发环境** (`dev`): DEBUG级别，输出到控制台和文件
- **生产环境** (`prod`): INFO级别，仅输出到文件

## 日志级别配置

### 包级别配置
- `com.operatorchoreography.executor` - DEBUG级别（算子执行器详细日志）
- `com.operatorchoreography.generator.controller` - INFO级别（控制器日志）
- `com.operatorchoreography.generator.service` - INFO级别（服务层日志）
- `com.operatorchoreography.generator.mapper` - DEBUG级别（MyBatis日志）
- `org.springframework.jdbc.core` - DEBUG级别（SQL日志）

## 已配置日志的组件

### 1. 执行器管理器 (ExecutorManager)
- 算子执行开始/结束日志
- 执行器类加载日志
- 方法调用日志
- 执行时间统计
- 异常详细记录

### 2. 算子执行器 (各种Executor)
- 算子执行开始/结束日志
- 输入参数记录（DEBUG级别）
- 执行结果记录
- 异常处理日志

#### 已配置的执行器：
- `DataMappingExecutor` - 数据映射执行器
- `JsonParseExecutor` - JSON解析执行器
- `HttpRequestExecutor` - HTTP请求执行器

### 3. 服务层 (OperatorTemplatesServiceImpl)
- 算子模板测试执行日志
- 模板查找和验证日志
- 执行器选择逻辑日志

### 4. 控制器层 (OperatorTemplatesController)
- API请求接收日志
- 请求参数记录（DEBUG级别）
- 响应结果日志
- 异常处理日志

## 日志使用示例

### 基本使用
```java
@Slf4j
@Component
public class YourExecutor implements BaseExecutor {
    
    @Override
    public Map<String, Object> execute(Map<String, Object> inputData, Long templateId) {
        log.info("开始执行算子: templateId={}", templateId);
        log.debug("输入参数: {}", inputData);
        
        try {
            // 业务逻辑
            log.info("算子执行成功: templateId={}", templateId);
        } catch (Exception e) {
            log.error("算子执行失败: templateId={}", templateId, e);
        }
    }
}
```

### 日志级别选择
- **ERROR**: 系统错误、异常情况
- **WARN**: 警告信息、潜在问题
- **INFO**: 重要业务流程、状态变更
- **DEBUG**: 详细调试信息、参数内容

## 日志查看和监控

### 开发环境
```bash
# 查看实时日志
tail -f logs/operator-choreography.log

# 查看错误日志
tail -f logs/operator-choreography-error.log

# 查看算子执行日志
tail -f logs/operator-choreography-executor.log
```

### 日志分析
```bash
# 查找特定模板的执行日志
grep "templateId=1" logs/operator-choreography-executor.log

# 查找执行失败的日志
grep "执行失败" logs/operator-choreography-error.log

# 查看性能统计
grep "耗时" logs/operator-choreography.log
```

## 性能监控

日志中记录的关键性能指标：
- **执行时间**: 每个算子的执行耗时
- **API响应时间**: 控制器层的总响应时间
- **异常频率**: 通过错误日志监控系统健康状况

## 扩展日志配置

### 添加新的执行器日志
1. 在执行器类上添加 `@Slf4j` 注解
2. 在关键方法中添加日志记录
3. 遵循现有的日志格式和级别规范

### 添加新的业务日志
1. 创建专门的日志文件配置
2. 在 `logback-spring.xml` 中添加相应的 appender
3. 配置特定包的日志级别

## 注意事项

1. **避免日志泄露敏感信息**：不要在日志中记录密码、密钥等敏感数据
2. **控制日志量**：DEBUG级别的详细日志仅在开发环境使用
3. **异常日志**：异常信息要包含足够的上下文信息
4. **性能考虑**：在高频执行的代码中谨慎使用DEBUG级别的日志

## 故障排查

### 常见问题
1. **日志不输出**: 检查日志级别配置
2. **日志文件过大**: 调整滚动策略和保留天数
3. **性能问题**: 检查是否有过多的DEBUG日志在生产环境输出

### 调试步骤
1. 检查 `logback-spring.xml` 配置
2. 验证 `@Slf4j` 注解是否正确添加
3. 确认日志级别设置
4. 查看控制台输出确认日志框架正常工作 