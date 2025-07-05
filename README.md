# 算子编排系统

一个基于Vue3前端和Spring Boot后端的算子编排系统，支持数据转换和条件分支逻辑。

## 项目结构

```
OperatorChoreography/
├── backend/                 # Spring Boot后端
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │   ├── pom.xml
│   │   └── README.md
├── frontend/               # Vue3前端
│   ├── src/
│   │   ├── components/
│   │   ├── views/
│   │   └── utils/
│   ├── package.json
│   └── README.md
└── README.md
```

## 功能特性

### 算子类型
- **转换算子**: 数据格式转换、字段映射
- **条件算子**: 分支逻辑控制

### 核心功能
- 可视化算子编排
- 实时数据流处理
- 拖拽式界面设计
- 算子执行引擎

## 快速开始

### 环境要求
- Java 17+
- Node.js 16+
- MySQL 8.0+

### 数据库配置
1. 创建MySQL数据库：
```sql
CREATE DATABASE operator_choreography CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息

详细设置指南请参考: [backend/MYSQL_SETUP.md](backend/MYSQL_SETUP.md)

### 后端启动
```bash
cd backend
mvn spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

## API文档

后端服务默认运行在 http://localhost:8080
前端服务默认运行在 http://localhost:3000 