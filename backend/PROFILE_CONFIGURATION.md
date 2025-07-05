# Spring Boot Profile 配置说明

## 概述

本项目使用Spring Boot的Profile功能来管理不同环境的配置，支持开发环境(dev)和生产环境(prod)的配置分离。

## 配置文件结构

```
src/main/resources/
├── application.yml          # 主配置文件，包含通用配置
├── application-dev.yml      # 开发环境专用配置
├── application-prod.yml     # 生产环境专用配置
└── logback-spring.xml       # 日志配置文件，支持Profile
```

## 当前激活的Profile

默认激活的Profile为 **dev**（开发环境），在 `application.yml` 中配置：

```yaml
spring:
  profiles:
    active: dev
```

## 开发环境 (dev) 配置

### 特性
- 数据库连接使用开发服务器
- 显示SQL语句和格式化
- 开放所有管理端点
- 启用Swagger UI
- DEBUG级别日志输出到控制台和文件

### 配置文件：`application-dev.yml`
- **数据源**：连接开发数据库 (192.168.0.25:3306)
- **连接池**：小规模连接池配置 (最大10个连接)
- **JPA**：显示和格式化SQL
- **管理端点**：开放所有端点 (`include: "*"`)
- **Swagger**：完全启用，包含标签和操作排序

### 日志配置 (logback-spring.xml)
```xml
<springProfile name="dev">
    <root level="DEBUG">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
        <appender-ref ref="ERROR_FILE" />
    </root>
</springProfile>
```

## 生产环境 (prod) 配置

### 特性
- 数据库连接使用环境变量
- 不显示SQL语句
- 限制管理端点访问
- 禁用Swagger UI
- INFO级别日志仅输出到文件

### 配置文件：`application-prod.yml`
- **数据源**：通过环境变量配置
- **连接池**：生产级连接池配置 (最大20个连接)
- **JPA**：不显示SQL
- **管理端点**：仅开放基本端点
- **Swagger**：完全禁用

### 环境变量
生产环境需要设置以下环境变量：
```bash
export DB_URL="jdbc:mysql://prod-server:3306/operator_choreography?useSSL=true&serverTimezone=UTC&characterEncoding=utf8"
export DB_USERNAME="prod_user"
export DB_PASSWORD="secure_password"
```

### 日志配置 (logback-spring.xml)
```xml
<springProfile name="prod">
    <root level="INFO">
        <appender-ref ref="FILE" />
        <appender-ref ref="ERROR_FILE" />
    </root>
</springProfile>
```

## 如何切换Profile

### 方法1：修改application.yml
```yaml
spring:
  profiles:
    active: prod  # 改为生产环境
```

### 方法2：命令行参数
```bash
# 启动开发环境
java -jar app.jar --spring.profiles.active=dev

# 启动生产环境
java -jar app.jar --spring.profiles.active=prod
```

### 方法3：环境变量
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

### 方法4：Maven运行时指定
```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 配置验证

### 检查当前激活的Profile
访问管理端点查看当前配置：
```bash
curl http://localhost:8080/actuator/env | grep "activeProfiles"
```

### 检查数据源配置
```bash
curl http://localhost:8080/actuator/configprops
```

### 检查日志级别
```bash
curl http://localhost:8080/actuator/loggers
```

## 开发环境特有功能

### Swagger UI
- 访问地址：http://localhost:8080/swagger-ui.html
- API文档：http://localhost:8080/v3/api-docs

### 管理端点
- 健康检查：http://localhost:8080/actuator/health
- 应用信息：http://localhost:8080/actuator/info
- 环境信息：http://localhost:8080/actuator/env
- Bean信息：http://localhost:8080/actuator/beans
- 日志配置：http://localhost:8080/actuator/loggers

## 配置优先级

Spring Boot配置的加载顺序（优先级从高到低）：
1. 命令行参数
2. 环境变量
3. application-{profile}.yml
4. application.yml

## 最佳实践

### 开发环境
- 使用本地或开发数据库
- 启用详细日志
- 开放调试端点
- 使用较小的连接池

### 生产环境
- 使用环境变量管理敏感配置
- 限制日志级别和端点访问
- 禁用调试功能
- 优化性能配置

### 安全考虑
- 生产环境不要在配置文件中硬编码密码
- 使用环境变量或外部配置管理工具
- 限制管理端点的访问权限
- 定期审查和更新配置

## 故障排查

### 常见问题
1. **Profile未生效**：检查配置文件名称和Profile名称是否匹配
2. **数据库连接失败**：验证数据源配置和网络连接
3. **日志级别不正确**：检查logback-spring.xml中的Profile配置
4. **端点无法访问**：确认management配置和Profile设置

### 调试步骤
1. 启动时检查控制台日志中的Profile信息
2. 访问 `/actuator/env` 验证配置
3. 检查 `/actuator/health` 确认组件状态
4. 查看应用日志文件排查具体问题 