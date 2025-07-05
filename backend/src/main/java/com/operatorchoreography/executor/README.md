# 算子执行器说明

## 概述

算子执行器系统提供了一套完整的算子执行框架，支持动态调用各种类型的执行器来处理不同的业务逻辑。

## 已实现的执行器

### 1. 数据处理类执行器

#### DataMappingExecutor - 数据映射执行器
- **功能**: 字段映射和数据转换
- **输入参数**:
  - `input_data`: 输入数据对象
  - `mapping_rules`: 映射规则数组，每个规则包含：
    - `sourceField`: 源字段名
    - `targetField`: 目标字段名
    - `transformation`: 转换类型（direct/toString/toUpperCase/toLowerCase/toInt/toDouble）
- **输出**: 映射后的数据

#### JsonParseExecutor - JSON解析执行器
- **功能**: 解析JSON字符串为对象
- **输入参数**:
  - `jsonString`: 要解析的JSON字符串
- **输出**: 解析后的对象

#### JsonStringifyExecutor - JSON生成执行器
- **功能**: 将对象转换为JSON字符串
- **输入参数**:
  - `inputData`: 要转换的对象
- **输出**: JSON字符串

### 2. 控制流执行器

#### IfConditionExecutor - 条件分支执行器
- **功能**: 根据条件进行分支处理
- **输入参数**:
  - `condition`: 条件表达式（支持 ==、>、< 操作）
  - `inputData`: 条件判断的数据
  - `trueValue`: 条件为真时的返回值
  - `falseValue`: 条件为假时的返回值
- **输出**: 根据条件返回相应的值

### 3. 服务调用执行器

#### HttpRequestExecutor - HTTP请求执行器
- **功能**: 发送HTTP请求调用外部服务
- **输入参数**:
  - `request_url`: 请求URL（必填）
  - `request_method`: 请求方法（GET/POST/PUT/DELETE，默认GET）
  - `request_headers`: 请求头对象
  - `request_body`: 请求体
- **输出**: HTTP响应数据和状态码

### 4. 数据库执行器

#### MySqlQueryExecutor - MySQL查询执行器
- **功能**: 执行MySQL查询操作
- **输入参数**:
  - `sql_statement`: SQL查询语句（必填）
  - `query_params`: 查询参数对象
- **输出**: 查询结果数组和行数

### 5. 工具函数执行器

#### StringFormatExecutor - 字符串格式化执行器
- **功能**: 格式化字符串内容
- **输入参数**:
  - `inputString`: 输入字符串（必填）
  - `formatData`: 格式化数据
  - `formatType`: 格式化类型（template/printf/trim/upper/lower）
- **输出**: 格式化后的字符串

#### DataValidateExecutor - 数据验证执行器
- **功能**: 数据格式和内容验证
- **输入参数**:
  - `inputData`: 待验证的数据
  - `validationRules`: 验证规则数组，每个规则包含：
    - `field`: 字段名
    - `type`: 字段类型（string/number/boolean/email/phone/date）
    - `required`: 是否必填
    - `pattern`: 正则表达式
    - `minValue`/`maxValue`: 数值范围
- **输出**: 验证结果和错误信息

#### MathCalculateExecutor - 数学计算执行器
- **功能**: 数学表达式计算
- **输入参数**:
  - `expression`: 数学表达式（必填）
  - `variables`: 变量对象
- **输出**: 计算结果

## 使用方法

### 1. 通过API调用
```bash
POST /operator-templates/{id}/test
{
  "input_data": {"name": "张三", "age": 25},
  "mapping_rules": [
    {
      "sourceField": "name",
      "targetField": "username",
      "transformation": "toString"
    }
  ]
}
```

### 2. 自定义执行器
1. 实现 `BaseExecutor` 接口
2. 添加 `@Component` 注解
3. 实现 `execute` 方法
4. 在数据库中配置相应的模板记录

### 3. 执行器管理
- 执行器通过 `ExecutorManager` 进行统一管理
- 支持动态类加载和方法调用
- 自动处理异常和错误信息

## 错误处理

所有执行器都遵循统一的错误处理格式：
```json
{
  "success": false,
  "error": "错误描述信息"
}
```

成功执行的格式：
```json
{
  "success": true,
  "message": "执行成功信息",
  "具体的输出字段": "输出值"
}
```

## 扩展开发

要添加新的执行器：
1. 继承 `BaseExecutor` 接口
2. 实现具体的业务逻辑
3. 添加必要的参数验证
4. 在数据库中配置模板信息
5. 编写单元测试 