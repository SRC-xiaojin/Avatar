# MyBatis-Plus 代码生成器使用说明

## 功能介绍

MyBatis-Plus 代码生成器可以快速生成数据库表对应的实体类、Mapper接口、Service接口及实现类、Controller控制器等代码，大大提高开发效率。

## 生成内容

运行代码生成器将自动生成以下代码文件：

### 1. 实体类 (Entity)
- 位置：`src/main/java/com/operatorchoreography/model/`
- 特点：
  - 使用 `@TableName` 注解映射表名
  - 使用 `@TableId` 注解标识主键
  - 使用 `@TableField` 注解映射字段
  - 使用 `@TableFill` 注解实现自动填充
  - 集成 Lombok 注解（`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`）

### 2. Mapper接口
- 位置：`src/main/java/com/operatorchoreography/mapper/`
- 特点：
  - 继承 `BaseMapper<T>` 提供基础CRUD操作
  - 使用 `@Mapper` 注解标识
  - 生成对应的XML映射文件

### 3. Service接口
- 位置：`src/main/java/com/operatorchoreography/service/`
- 特点：
  - 继承 `IService<T>` 提供基础服务方法

### 4. Service实现类
- 位置：`src/main/java/com/operatorchoreography/service/impl/`
- 特点：
  - 继承 `ServiceImpl<Mapper, Entity>`
  - 实现对应的Service接口

### 5. Controller控制器
- 位置：`src/main/java/com/operatorchoreography/controller/`
- 特点：
  - 使用 `@RestController` 注解
  - 提供基础的RESTful API

### 6. XML映射文件
- 位置：`src/main/resources/mapper/`
- 特点：
  - 包含BaseResultMap和BaseColumnList
  - 可添加自定义SQL语句

## 使用步骤

### 1. 准备工作
确保数据库环境正常：
```bash
# 启动MySQL服务
# 确保 operator_choreography 数据库存在且包含相关表
```

### 2. 配置数据库连接
修改 `MybatisPlusGenerator.java` 中的数据库连接信息：
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/operator_choreography?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
private static final String DB_USERNAME = "root";
private static final String DB_PASSWORD = "your_password";  // 修改为实际密码
```

### 3. 选择要生成的表
修改表名数组（默认生成所有表）：
```java
private static final String[] TABLE_NAMES = {
    "operators", 
    "operator_config", 
    "workflows", 
    "operator_connections"
};
```

### 4. 运行生成器
```bash
# 在IDE中直接运行 MybatisPlusGenerator.main() 方法
# 或者使用Maven命令
mvn test-compile exec:java -Dexec.mainClass="com.operatorchoreography.generator.MybatisPlusGenerator"
```

### 5. 检查生成结果
生成完成后，检查以下目录：
- `src/main/java/com/operatorchoreography/model/` - 实体类
- `src/main/java/com/operatorchoreography/mapper/` - Mapper接口
- `src/main/java/com/operatorchoreography/service/` - Service接口
- `src/main/java/com/operatorchoreography/service/impl/` - Service实现
- `src/main/java/com/operatorchoreography/controller/` - Controller
- `src/main/resources/mapper/` - XML映射文件

## 生成示例

### 生成的实体类示例
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("operators")
public class Operators implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

### 生成的Mapper接口示例
```java
@Mapper
public interface OperatorsMapper extends BaseMapper<Operators> {
    // 继承BaseMapper，自动拥有基础CRUD方法
    // 可以在这里添加自定义查询方法
}
```

### 生成的Service接口示例
```java
public interface OperatorsService extends IService<Operators> {
    // 继承IService，自动拥有基础服务方法
    // 可以在这里添加自定义业务方法
}
```

## 注意事项

### 1. 文件覆盖
- 代码生成器会覆盖现有同名文件
- 建议在生成前备份重要的自定义代码
- 或者将自定义代码放在不同的包或类中

### 2. 数据类型映射
- 大部分MySQL数据类型能正确映射到Java类型
- 特殊类型（如JSON、枚举等）可能需要手动调整
- 日期时间类型使用 `java.time` 包

### 3. 字段命名
- 数据库字段使用下划线命名（snake_case）
- Java字段自动转换为驼峰命名（camelCase）
- 如：`created_at` → `createdAt`

### 4. 自动填充
- `created_at` 字段配置为插入时自动填充
- `updated_at` 字段配置为插入和更新时自动填充
- 需要配合 `MybatisPlusConfig` 中的 `MetaObjectHandler` 使用

### 5. 与现有代码的兼容性
- 生成的代码可能与手写代码有差异
- 建议逐步迁移，而不是一次性替换所有代码
- 可以先生成新表的代码，再逐步重构旧代码

## 自定义配置

### 1. 修改包名
```java
private static final String PARENT_PACKAGE = "com.yourcompany.yourproject";
```

### 2. 修改作者信息
```java
private static final String AUTHOR = "Your Name";
```

### 3. 自定义模板
可以创建自定义的Velocity模板来定制生成的代码格式。

### 4. 添加表前缀过滤
```java
.addTablePrefix("t_", "sys_") // 过滤表前缀
```

## 常见问题

### Q: 生成的代码编译报错？
A: 检查导入的包是否正确，特别是MyBatis-Plus相关注解

### Q: 自动填充不生效？
A: 确保 `MybatisPlusConfig` 类中的 `MetaObjectHandler` 配置正确

### Q: 生成的字段类型不正确？
A: 可以手动修改实体类中的字段类型，或者在数据库中调整字段类型

### Q: 想要保留自定义代码怎么办？
A: 建议使用继承或组合的方式扩展生成的代码，避免直接修改生成的文件

## 扩展建议

1. **创建基础类**：为所有实体类创建公共的基础类（如BaseEntity）
2. **添加校验注解**：在实体类中添加JSR-303校验注解
3. **自定义查询方法**：在Mapper接口中添加复杂查询方法
4. **DTO转换**：创建DTO类并实现与Entity的转换方法
5. **API文档**：为Controller添加Swagger注解 