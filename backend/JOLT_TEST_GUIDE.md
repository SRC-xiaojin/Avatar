# JOLT数据转换测试指南

## 概述

本指南介绍如何使用系统的JOLT数据转换测试功能，包括预定义的测试样例、自定义测试以及批量测试功能。

## JOLT简介

JOLT（JSON转JSON语言）是一个Java库，用于将一种JSON结构转换为另一种JSON结构。它使用声明式转换规范来描述如何映射输入数据到输出数据。

### JOLT主要操作类型

1. **shift** - 字段映射和重命名
2. **default** - 设置默认值
3. **remove** - 删除字段
4. **sort** - 排序
5. **cardinality** - 修改数据类型
6. **modify-overwrite-beta** - 计算和修改字段值

## 快速开始

### 1. 启动服务

确保后端服务已启动并运行在默认端口（8080）。

### 2. 导入测试数据

执行以下SQL文件导入测试数据：

```bash
# 导入JOLT测试数据
mysql -u root -p operator_choreography < backend/src/main/resources/jolt_test_data.sql
```

### 3. 访问测试页面

- 后端API测试：`http://localhost:8080/swagger-ui/index.html`
- 前端测试页面：`http://localhost:3000/jolt-test`（如果前端已启动）

## API接口使用

### 1. 获取测试样例

```bash
GET /jolt-test/samples
```

返回所有预定义的JOLT测试样例。

### 2. 直接测试JOLT转换

```bash
POST /jolt-test/transform
Content-Type: application/json

{
  "inputData": {
    "name": "张三",
    "age": 25,
    "email": "zhangsan@example.com"
  },
  "joltSpec": "[{\"operation\": \"shift\", \"spec\": {\"name\": \"userName\", \"age\": \"userAge\", \"email\": \"userEmail\"}}]"
}
```

### 3. 验证JOLT规则

```bash
POST /jolt-test/validate
Content-Type: application/json

{
  "joltSpec": "[{\"operation\": \"shift\", \"spec\": {\"name\": \"userName\"}}]"
}
```

### 4. 运行预定义测试样例

```bash
POST /jolt-test/samples/{sampleId}/run
```

### 5. 批量运行所有测试

```bash
POST /jolt-test/samples/run-all
```

## 测试样例说明

### 样例1: 简单字段映射

**输入数据:**
```json
{
  "name": "张三",
  "age": 25,
  "email": "zhangsan@example.com"
}
```

**JOLT规则:**
```json
[
  {
    "operation": "shift",
    "spec": {
      "name": "userName",
      "age": "userAge",
      "email": "userEmail"
    }
  }
]
```

**期望输出:**
```json
{
  "userName": "张三",
  "userAge": 25,
  "userEmail": "zhangsan@example.com"
}
```

### 样例2: 嵌套结构映射

**输入数据:**
```json
{
  "user": {
    "profile": {
      "firstName": "张",
      "lastName": "三",
      "contact": {
        "email": "zhangsan@example.com",
        "phone": "13800138000"
      }
    }
  }
}
```

**JOLT规则:**
```json
[
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
]
```

**期望输出:**
```json
{
  "first_name": "张",
  "last_name": "三",
  "email_address": "zhangsan@example.com",
  "phone_number": "13800138000"
}
```

### 样例3: 数组转换

**输入数据:**
```json
{
  "users": [
    {
      "name": "张三",
      "age": 25,
      "city": "北京"
    },
    {
      "name": "李四",
      "age": 30,
      "city": "上海"
    }
  ]
}
```

**JOLT规则:**
```json
[
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
]
```

**期望输出:**
```json
{
  "people": [
    {
      "fullName": "张三",
      "years": 25,
      "location": "北京"
    },
    {
      "fullName": "李四",
      "years": 30,
      "location": "上海"
    }
  ]
}
```

### 样例4: 默认值设置

**输入数据:**
```json
{
  "name": "张三",
  "age": 25
}
```

**JOLT规则:**
```json
[
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
]
```

**期望输出:**
```json
{
  "userName": "张三",
  "userAge": 25,
  "status": "active",
  "role": "user",
  "createTime": "2024-01-01"
}
```

## 使用算子模板测试

系统还支持通过算子模板进行JOLT转换测试。测试数据中包含了以下测试模板：

- **模板ID 10**: 简单字段映射测试
- **模板ID 11**: 嵌套结构映射测试  
- **模板ID 12**: 数组转换测试
- **模板ID 13**: 默认值设置测试
- **模板ID 14**: 复杂转换测试

### 使用算子模板测试

```bash
POST /operator-templates/{templateId}/test
Content-Type: application/json

{
  "name": "张三",
  "age": 25,
  "email": "zhangsan@example.com"
}
```

例如，测试简单字段映射：

```bash
POST /operator-templates/10/test
Content-Type: application/json

{
  "name": "张三",
  "age": 25,
  "email": "zhangsan@example.com"
}
```

## 前端测试页面功能

### 主要功能

1. **预定义样例测试** - 选择预定义的测试样例进行快速测试
2. **自定义测试** - 手动输入数据和JOLT规则进行测试
3. **实时验证** - 验证JSON格式和JOLT规则语法
4. **结果对比** - 对比实际结果与期望结果
5. **批量测试** - 一键运行所有测试样例
6. **JSON格式化** - 自动格式化JSON数据便于阅读

### 使用步骤

1. 访问前端测试页面
2. 选择"预定义样例"或"自定义测试"
3. 在预定义样例中选择一个测试用例，或在自定义测试中输入数据
4. 点击"执行转换"按钮运行测试
5. 查看转换结果和对比结果
6. 使用右下角的批量测试按钮运行所有样例

## 常见问题

### 1. JOLT规则语法错误

**问题**: 转换失败，提示语法错误
**解决**: 
- 使用"验证规则"按钮检查JOLT规则语法
- 确保JSON格式正确
- 参考官方JOLT文档和示例

### 2. 输入数据格式错误

**问题**: JSON解析失败
**解决**: 
- 使用"验证JSON"按钮检查输入数据格式
- 使用"格式化JSON"按钮自动格式化数据

### 3. 转换结果与期望不符

**问题**: 转换成功但结果不是期望的
**解决**: 
- 检查JOLT规则的逻辑
- 使用"对比结果"标签页查看差异
- 参考样例逐步调试规则

### 4. 数组索引问题

**问题**: 数组转换时索引错误
**解决**: 
- 理解JOLT数组索引语法：`&1`, `&2`, `&3`等
- `&1`表示当前数组索引，`&2`表示上一级数组索引
- 参考数组转换样例

## 扩展和定制

### 添加新的测试样例

1. 在`JoltTestController.createTestSamples()`方法中添加新样例
2. 或者在数据库中插入新的测试模板和参数配置

### 自定义JOLT规则

参考JOLT官方文档创建更复杂的转换规则：
- [JOLT GitHub](https://github.com/bazaarvoice/jolt)
- [JOLT文档](https://github.com/bazaarvoice/jolt/blob/master/README.md)

## 技术细节

### 后端实现

- **JoltTestController**: 提供JOLT测试相关的REST API
- **DataMappingExecutor**: 实际执行JOLT转换的执行器
- **依赖库**: `jolt-core` 和 `json-utils`

### 前端实现

- **JoltTest.vue**: JOLT测试的前端页面
- **功能**: 测试样例管理、实时转换、结果对比、批量测试

### 数据库配置

- **operator_templates**: 存储测试模板信息
- **operator_template_params**: 存储JOLT规则和参数配置

## 性能优化建议

1. **大数据集测试**: 对于大型JSON数据，建议分批处理
2. **复杂规则**: 避免过度复杂的嵌套转换，可以分步进行
3. **缓存规则**: 频繁使用的JOLT规则可以考虑缓存编译结果

## 版本兼容性

- **JOLT版本**: 0.1.7
- **Java版本**: 17+
- **Spring Boot版本**: 3.2.5 