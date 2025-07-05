# 算子编排系统前端

基于Vue 3.3 + Vite + Element Plus开发的算子编排系统前端应用。

## 技术栈

- **Vue 3.3**: 渐进式JavaScript框架
- **Vite 4.4**: 快速构建工具
- **Vue Router 4.2**: 路由管理
- **Pinia 2.1**: 状态管理
- **Element Plus 2.3**: UI组件库
- **Axios 1.5**: HTTP客户端
- **Sass**: CSS预处理器

## 功能特性

### 核心页面

1. **工作台 (Dashboard)**
   - 系统概览和统计信息
   - 快速操作入口
   - 最近活动时间线

2. **算子管理 (Operator Management)**
   - 算子列表查看和搜索
   - 算子创建、编辑、删除
   - 算子配置管理
   - 按类型过滤算子

3. **编排管理 (Workflow Management)**
   - 工作流列表管理
   - 工作流状态控制
   - 执行结果查看
   - 工作流详情预览

4. **编排设计器 (Operator Designer)**
   - 可视化拖拽设计界面
   - 算子库面板
   - 属性配置面板
   - 实时执行和调试

### 算子类型

- **转换算子**: 数据格式转换、字段映射、类型转换
- **条件算子**: 分支逻辑控制、条件表达式
- **输入算子**: 数据输入端点
- **输出算子**: 数据输出端点

## 项目结构

```
frontend/
├── public/                     # 静态资源
├── src/
│   ├── api/                   # API接口
│   │   ├── index.js          # Axios配置
│   │   └── operator.js       # 算子相关API
│   ├── components/           # 公共组件
│   ├── views/               # 页面组件
│   │   ├── Dashboard.vue    # 工作台
│   │   ├── OperatorManagement.vue      # 算子管理
│   │   ├── WorkflowManagement.vue      # 编排管理
│   │   └── OperatorDesigner.vue        # 编排设计器
│   ├── router/              # 路由配置
│   │   └── index.js
│   ├── style.css           # 全局样式
│   ├── App.vue             # 根组件
│   └── main.js             # 入口文件
├── index.html              # HTML模板
├── vite.config.js         # Vite配置
└── package.json           # 项目配置
```

## 开发指南

### 环境要求
- Node.js 16.0+
- npm 8.0+

### 安装和启动

1. **安装依赖**
   ```bash
   cd frontend
   npm install
   ```

2. **启动开发服务器**
   ```bash
   npm run dev
   ```

3. **访问应用**
   - 前端地址: http://localhost:3000
   - 自动代理后端API到: http://localhost:8080

### 构建部署

1. **构建生产版本**
   ```bash
   npm run build
   ```

2. **预览构建结果**
   ```bash
   npm run preview
   ```

## 使用说明

### 工作台
- 查看系统整体统计信息
- 快速创建新的算子编排
- 查看最近的系统活动

### 算子管理
- **创建算子**: 点击"新建算子"按钮，选择算子类型并配置参数
- **编辑算子**: 在列表中点击"编辑"按钮修改算子配置
- **搜索算子**: 使用搜索框按名称查找算子
- **过滤算子**: 按算子类型进行过滤

### 编排设计器
- **添加算子**: 从左侧算子库拖拽算子到画布
- **配置算子**: 选中算子后在右侧属性面板编辑配置
- **保存编排**: 点击"保存编排"按钮保存工作流
- **执行编排**: 点击"执行编排"查看执行结果

### 编排管理
- **查看工作流**: 浏览所有已创建的工作流
- **执行工作流**: 点击"执行"按钮运行工作流
- **查看详情**: 点击"查看"按钮查看工作流详细信息
- **编辑工作流**: 点击"编辑"跳转到设计器

## 算子配置示例

### 转换算子配置

```json
{
  "fieldMapping": "{\"user_name\": \"name\", \"user_age\": \"age\"}",
  "typeConversion": "{\"age\": \"integer\"}"
}
```

### 条件算子配置

```json
{
  "condition": "age >= 18 && status == 'active'"
}
```

## 组件说明

### OperatorDesigner 设计器组件

**主要功能**:
- 拖拽式算子编排
- 实时属性配置
- 执行结果展示

**核心方法**:
- `onOperatorDragStart`: 处理算子拖拽开始
- `onCanvasDrop`: 处理算子拖放到画布
- `selectNode`: 选择算子节点
- `saveWorkflow`: 保存工作流
- `executeWorkflow`: 执行工作流

### API服务

**请求拦截器**:
- 统一添加请求头
- 请求日志记录

**响应拦截器**:
- 统一错误处理
- 成功响应数据提取

## 样式规范

### CSS类命名
- 采用BEM命名规范
- 组件样式使用scoped作用域
- 全局样式定义在style.css

### 响应式设计
- 使用Element Plus的栅格系统
- 支持移动端适配
- 断点设置: 768px

## 扩展开发

### 添加新页面
1. 在`src/views/`目录下创建Vue组件
2. 在`src/router/index.js`中添加路由配置
3. 在主导航中添加菜单项

### 添加新的算子类型
1. 在设计器的`operatorCategories`中添加新类型
2. 在属性面板中添加对应的配置表单
3. 更新后端算子执行器

### 自定义主题
- 通过Element Plus的主题定制功能
- 修改CSS变量定义
- 使用Sass变量统一管理

## 调试说明

### 开发工具
- Vue DevTools: 组件状态调试
- 浏览器DevTools: 网络请求调试
- Console日志: API请求响应日志

### 常见问题

1. **跨域问题**: 
   - 通过Vite代理配置解决
   - 确保后端CORS配置正确

2. **组件状态更新**:
   - 检查响应式数据定义
   - 确认事件绑定正确

3. **路由跳转**:
   - 使用`$router.push()`方法
   - 检查路由配置是否正确

## 性能优化

### 代码分割
- 路由懒加载
- 组件按需导入

### 资源优化
- 图片压缩和格式优化
- CSS和JS文件压缩
- 开启Gzip压缩

### 缓存策略
- HTTP缓存设置
- 浏览器缓存利用
- API响应缓存

## 部署说明

### 静态部署
```bash
# 构建
npm run build

# 部署到Nginx
cp -r dist/* /usr/share/nginx/html/
```

### Docker部署
```dockerfile
FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

## 贡献指南

1. Fork项目
2. 创建特性分支: `git checkout -b feature/new-feature`
3. 提交更改: `git commit -am 'Add new feature'`
4. 推送分支: `git push origin feature/new-feature`
5. 提交Pull Request 