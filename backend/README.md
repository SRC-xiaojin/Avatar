# 算子编排系统后端

基于Spring Boot 3.2.0开发的算子编排系统后端服务。

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.2.0
- **MyBatis-Plus**: 数据持久化框架
- **MyBatis**: SQL映射框架
- **MySQL**: 关系型数据库
- **Maven**: 项目管理
- **Lombok**: 简化代码
- **Jackson**: JSON处理

## 核心功能

### 算子类型

1. **转换算子 (TRANSFORM)**
   - 数据格式转换
   - 字段映射
   - 数据类型转换
   - 数据过滤

2. **条件算子 (CONDITION)**
   - 分支逻辑控制
   - 条件表达式求值
   - 支持数值和字符串比较
   - 支持逻辑操作符 (&&, ||)

3. **输入算子 (INPUT)**
   - 数据输入端点

4. **输出算子 (OUTPUT)**
   - 数据输出端点

### API接口

#### 算子管理
- `GET /api/operators` - 获取所有算子
- `GET /api/operators/{id}` - 根据ID获取算子
- `GET /api/operators/type/{type}` - 根据类型获取算子
- `GET /api/operators/search?keyword=xxx` - 搜索算子
- `POST /api/operators` - 创建算子
- `PUT /api/operators/{id}` - 更新算子
- `DELETE /api/operators/{id}` - 删除算子
- `POST /api/operators/{id}/validate` - 验证算子配置
- `POST /api/operators/{id}/execute` - 执行算子
- `GET /api/operators/types` - 获取算子类型列表

## 项目结构

```
backend/
├── src/main/java/com/operatorchoreography/
│   ├── OperatorChoreographyApplication.java   # 主启动类
│   ├── model/                                  # 实体模型
│   │   ├── Operator.java                      # 算子实体
│   │   ├── OperatorType.java                  # 算子类型枚举
│   │   ├── Workflow.java                      # 工作流实体
│   │   └── OperatorConnection.java            # 算子连接关系
│   ├── mapper/                                # MyBatis Mapper接口
│   │   ├── OperatorMapper.java                # 算子数据访问接口
│   │   ├── OperatorConfigMapper.java          # 算子配置数据访问接口
│   │   ├── WorkflowMapper.java                # 工作流数据访问接口
│   │   └── OperatorConnectionMapper.java      # 算子连接关系数据访问接口
│   ├── config/                                # 配置类
│   │   └── MybatisPlusConfig.java             # MyBatis-Plus配置
│   ├── service/                               # 服务层
│   │   ├── OperatorService.java               # 算子服务接口
│   │   ├── OperatorExecutor.java              # 算子执行器接口
│   │   └── impl/                              # 服务实现
│   │       ├── OperatorServiceImpl.java       # 算子服务实现
│   │       ├── TransformOperatorExecutor.java # 转换算子执行器
│   │       └── ConditionOperatorExecutor.java # 条件算子执行器
│   └── controller/                            # 控制器
│       └── OperatorController.java            # 算子控制器
├── src/main/resources/
│   ├── mapper/                                # MyBatis XML映射文件
│   │   └── OperatorMapper.xml                 # 算子复杂查询映射
│   ├── schema.sql                             # 数据库初始化脚本
│   └── application.yml                        # 应用配置
└── pom.xml                                   # Maven配置
```

## 启动说明

### 环境要求
- JDK 17或更高版本
- Maven 3.6或更高版本
- MySQL 8.0或更高版本

### 启动步骤

1. **准备MySQL数据库**
   ```sql
   CREATE DATABASE operator_choreography CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```

2. **克隆项目**
   ```bash
   git clone <repository-url>
   cd backend
   ```

3. **配置数据库连接**
   修改 `src/main/resources/application.yml` 中的数据库配置：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/operator_choreography?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
       username: your_username
       password: your_password
   ```

4. **安装依赖**
   ```bash
   mvn clean install
   ```

5. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

6. **访问应用**
   - API地址: http://localhost:8080
   - 健康检查: http://localhost:8080/actuator/health

### MySQL数据库配置
- 数据库名: `operator_choreography`
- 默认用户名: `root`
- 默认密码: `password`
- 字符集: `utf8mb4`
- 排序规则: `utf8mb4_general_ci`

## 算子配置示例

### 转换算子配置
```json
{
  "name": "用户数据转换",
  "type": "TRANSFORM",
  "description": "转换用户数据格式",
  "config": {
    "fieldMapping": "{\"user_name\": \"name\", \"user_age\": \"age\"}",
    "typeConversion": "{\"age\": \"integer\", \"score\": \"double\"}",
    "dataFilter": "{\"include\": [\"name\", \"age\", \"email\"]}"
  }
}
```

### 条件算子配置
```json
{
  "name": "年龄验证",
  "type": "CONDITION",
  "description": "验证用户年龄是否满足条件",
  "config": {
    "condition": "age >= 18 && status == 'active'"
  }
}
```

## 执行示例

### 执行转换算子
```bash
curl -X POST http://localhost:8080/api/operators/1/execute \
  -H "Content-Type: application/json" \
  -d '{
    "user_name": "张三",
    "user_age": "25",
    "email": "zhangsan@example.com"
  }'
```

### 执行条件算子
```bash
curl -X POST http://localhost:8080/api/operators/2/execute \
  -H "Content-Type: application/json" \
  -d '{
    "age": 25,
    "status": "active"
  }'
```

## 开发说明

### 添加新的算子类型

1. **更新算子类型枚举**
   ```java
   // 在 OperatorType.java 中添加新类型
   NEW_TYPE("新算子", "新算子描述")
   ```

2. **创建算子执行器**
   ```java
   @Service
   public class NewOperatorExecutor implements OperatorExecutor {
       @Override
       public Map<String, Object> execute(Operator operator, Map<String, Object> inputData) {
           // 实现执行逻辑
       }
       
       @Override
       public String getSupportedOperatorType() {
           return "NEW_TYPE";
       }
   }
   ```

### 扩展功能建议

1. **工作流执行引擎**
   - 实现算子之间的连接执行
   - 支持并行执行
   - 错误处理和重试机制

2. **数据持久化**
   - 添加数据迁移脚本
   - 数据库连接池优化

3. **监控和日志**
   - 添加执行日志记录
   - 性能监控指标
   - 错误告警机制

4. **安全性**
   - 用户认证和授权
   - API访问控制
   - 数据加密

## 测试

运行单元测试:
```bash
mvn test
```

## 打包部署

```bash
# 打包
mvn clean package

# 运行jar包
java -jar target/operator-choreography-backend-1.0.0.jar
```

## MyBatis-Plus 使用说明

### 数据访问层架构

项目已从Spring Data JPA迁移到MyBatis-Plus，提供以下优势：

1. **轻量级ORM**: 相比JPA更轻量，性能更好
2. **灵活的SQL**: 支持注解SQL和XML映射文件
3. **强大的条件构造器**: 类型安全的查询条件构建
4. **自动代码生成**: 减少重复代码编写
5. **分页插件**: 物理分页，性能优秀

### Mapper层说明

- **OperatorMapper**: 算子数据访问，继承BaseMapper获得基础CRUD
- **OperatorConfigMapper**: 算子配置数据访问，使用注解SQL
- **WorkflowMapper**: 工作流数据访问
- **OperatorConnectionMapper**: 算子连接关系数据访问

### 核心配置

```yaml
mybatis-plus:
  # 实体类包扫描
  type-aliases-package: com.operatorchoreography.model
  # XML映射文件路径  
  mapper-locations: classpath*:mapper/*.xml
  global-config:
    db-config:
      id-type: AUTO  # 主键自增
      field-strategy: NOT_NULL  # 字段策略
      table-underline: true  # 驼峰转下划线
  configuration:
    map-underscore-to-camel-case: true  # 下划线转驼峰
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL日志
```

### 使用示例

```java
// 基础CRUD操作（继承BaseMapper）
List<Operator> operators = operatorMapper.selectList(null);
Operator operator = operatorMapper.selectById(1L);
operatorMapper.insert(operator);
operatorMapper.updateById(operator);
operatorMapper.deleteById(1L);

// 条件查询
QueryWrapper<Operator> queryWrapper = new QueryWrapper<>();
queryWrapper.eq("type", OperatorType.TRANSFORM)
           .like("name", "数据")
           .orderByDesc("created_at");
List<Operator> result = operatorMapper.selectList(queryWrapper);

// 分页查询
Page<Operator> page = new Page<>(1, 10);
Page<Operator> result = operatorMapper.selectPage(page, queryWrapper);

// 注解SQL查询
@Select("SELECT * FROM operators WHERE type = #{type}")
List<Operator> findByType(@Param("type") OperatorType type);

// XML映射查询（复杂查询）
<select id="selectOperatorWithConfig" resultMap="OperatorWithConfigResultMap">
    SELECT o.*, oc.config_key, oc.config_value
    FROM operators o
    LEFT JOIN operator_config oc ON o.id = oc.operator_id
    WHERE o.id = #{id}
</select>
```

### 自动填充功能

配置类 `MybatisPlusConfig` 实现了 `MetaObjectHandler` 接口，自动处理时间字段：

```java
@Override
public void insertFill(MetaObject metaObject) {
    this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
    this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
}

@Override  
public void updateFill(MetaObject metaObject) {
    this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
}
```

### 实体类注解

```java
@TableName("operators")  // 指定表名
public class Operator {
    @TableId(value = "id", type = IdType.AUTO)  // 主键自增
    private Long id;
    
    @TableField("name")  // 字段映射
    private String name;
    
    @TableField(exist = false)  // 非数据库字段
    private Map<String, String> config;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)  // 自动填充
    private LocalDateTime createdAt;
}
```

### 数据库操作最佳实践

1. **事务管理**: 在Service层方法上使用 `@Transactional` 注解
2. **批量操作**: 使用MyBatis-Plus提供的批量方法提升性能
3. **条件构造**: 优先使用QueryWrapper而不是手写SQL
4. **分页查询**: 使用内置分页插件，避免内存分页
5. **SQL优化**: 复杂查询使用XML映射文件编写优化的SQL

## 代码生成器

项目提供了MyBatis-Plus代码生成器，可以快速生成数据库表对应的实体类、Mapper接口、Service接口及实现类、Controller控制器等代码。

### 使用方法

1. **配置数据库连接**
   修改 `src/test/java/com/operatorchoreography/generator/MybatisPlusGenerator.java` 中的数据库连接信息

2. **运行代码生成器**
   ```bash
   # 在IDE中运行 MybatisPlusGenerator.main() 方法
   # 或使用Maven命令
   mvn test-compile exec:java -Dexec.mainClass="com.operatorchoreography.generator.MybatisPlusGenerator"
   ```

3. **生成内容**
   - Entity实体类（使用MyBatis-Plus注解）
   - Mapper接口（继承BaseMapper）
   - Service接口和实现类
   - Controller控制器
   - XML映射文件

### 注意事项

- 代码生成器会覆盖现有同名文件，请先备份重要代码
- 生成后可能需要手动调整某些字段类型或业务逻辑
- 详细使用说明请参考：`src/test/java/com/operatorchoreography/generator/README.md`

### 生成的代码特点

- 实体类使用Lombok注解简化代码
- 自动配置字段映射和自动填充
- Mapper接口提供基础CRUD操作
- Service层提供业务逻辑封装
- Controller提供RESTful API接口 