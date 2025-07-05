# 控制台中文乱码解决方案

## 问题描述

在Windows环境下运行Spring Boot应用时，控制台输出的中文可能显示为乱码。这是由于控制台编码与应用输出编码不匹配导致的。

## 解决方案

### 方案1：设置Windows控制台编码为UTF-8 (推荐)

#### 步骤1：修改控制台编码
```cmd
# 在命令行中执行
chcp 65001
```

#### 步骤2：设置JVM参数
```bash
# Maven方式启动
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"

# 或者使用环境变量
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
mvn spring-boot:run
```

#### 步骤3：验证设置
启动应用后，控制台应该正确显示中文。

### 方案2：使用GBK编码 (Windows兼容)

#### 设置环境变量
```cmd
# 设置控制台使用GBK编码
set CONSOLE_CHARSET=GBK
mvn spring-boot:run
```

### 方案3：IDE环境配置

#### IntelliJ IDEA
1. File → Settings → Editor → File Encodings
2. 设置以下选项为UTF-8：
   - Global Encoding: UTF-8
   - Project Encoding: UTF-8
   - Default encoding for properties files: UTF-8
3. Run/Debug Configurations → VM options:
   ```
   -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
   ```

#### Eclipse
1. Window → Preferences → General → Workspace
2. Text file encoding → Other → UTF-8
3. Run Configurations → Arguments → VM arguments:
   ```
   -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
   ```

#### VS Code
1. 文件 → 首选项 → 设置
2. 搜索 "encoding"
3. 设置 Files: Encoding 为 utf8

### 方案4：启动脚本配置

#### Windows批处理脚本 (startup.bat)
```batch
@echo off
chcp 65001 > nul
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Dspring.profiles.active=dev
echo 正在启动算子编排系统...
mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JAVA_OPTS%"
pause
```

#### PowerShell脚本 (startup.ps1)
```powershell
# 设置控制台编码为UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

# 设置环境变量
$env:JAVA_OPTS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Dspring.profiles.active=dev"

Write-Host "正在启动算子编排系统..." -ForegroundColor Green
mvn spring-boot:run -D"spring-boot.run.jvmArguments=$env:JAVA_OPTS"
```

### 方案5：永久性系统配置

#### Windows系统级别设置
1. 打开注册表编辑器 (regedit)
2. 导航到: `HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\Nls\CodePage`
3. 修改 `OEMCP` 值为 `65001` (UTF-8)
4. 重启系统

#### 环境变量设置
```cmd
# 在系统环境变量中添加
JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
```

## 测试验证

### 创建测试类
```java
@RestController
public class EncodingTestController {
    
    private static final Logger log = LoggerFactory.getLogger(EncodingTestController.class);
    
    @GetMapping("/test-encoding")
    public String testEncoding() {
        String testMessage = "测试中文编码：你好世界！";
        log.info("控制台输出测试：{}", testMessage);
        return testMessage;
    }
}
```

### 验证步骤
1. 启动应用
2. 访问：http://localhost:8080/test-encoding
3. 检查控制台输出是否正确显示中文
4. 检查日志文件中的中文是否正确

## 配置优先级

我们的logback配置支持通过环境变量灵活配置：

1. **CONSOLE_CHARSET环境变量**：如果设置了此变量，使用指定编码
2. **默认UTF-8**：如果未设置环境变量，使用UTF-8
3. **GBK备选**：可以手动设置为GBK适配Windows

## 常见问题排查

### 问题1：设置后仍然乱码
**解决方案**：
1. 检查IDE的编码设置
2. 确认JVM参数是否生效
3. 重启命令行窗口

### 问题2：部分中文正常，部分乱码
**解决方案**：
1. 检查日志框架配置
2. 确认应用中所有字符串都是UTF-8编码
3. 检查数据库连接编码

### 问题3：Maven插件启动正常，jar包启动乱码
**解决方案**：
```bash
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar your-app.jar
```

### 问题4：Linux/Mac环境正常，Windows乱码
**解决方案**：
使用条件配置，针对不同操作系统使用不同编码：
```cmd
# Windows
set CONSOLE_CHARSET=GBK

# Linux/Mac
export CONSOLE_CHARSET=UTF-8
```

## 推荐配置 (开箱即用)

对于开发团队，推荐使用以下配置：

### 1. 团队统一设置
所有开发者在项目根目录创建 `.env` 文件：
```
CONSOLE_CHARSET=UTF-8
JAVA_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
```

### 2. IDE项目配置
在项目的 `.vscode/settings.json` 或 `.idea` 配置中统一编码设置

### 3. 启动脚本
提供统一的启动脚本，自动设置编码参数

这样可以确保团队所有成员都有一致的中文显示效果。 