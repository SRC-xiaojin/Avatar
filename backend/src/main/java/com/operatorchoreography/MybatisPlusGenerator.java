package com.operatorchoreography;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器
 * 
 * 使用说明：
 * 1. 确保MySQL数据库已启动并创建了operator_choreography数据库
 * 2. 修改数据库连接信息（用户名、密码等）
 * 3. 运行main方法即可生成代码
 * 4. 生成的代码位于 src/main/java 目录下
 */
public class MybatisPlusGenerator {

    /**
     * 数据库配置
     */
    private static final String DB_URL = "jdbc:mysql://192.168.0.25:3306/operator_choreography?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "123456";

    /**
     * 包配置
     */
    private static final String PARENT_PACKAGE = "com.operatorchoreography";
    
    /**
     * 作者信息
     */
    private static final String AUTHOR = "MyBatis-Plus Generator";
    
    /**
     * 需要生成的表名
     */
    private static final String[] TABLE_NAMES = {
        "node_executions",
        "operator_categories",
        "operator_template_params",
        "operator_templates",

        "workflow_connections",
        "workflow_executions",
        "workflow_node_params",
        "workflow_nodes",
            "workflows"
    };

    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir");
        String outputDir = projectPath + "/src/main/java";
        String moduleName = "generator";
        String mapperXmlPath = projectPath + "/src/main/resources/" + moduleName + "/mapper";

        FastAutoGenerator.create(DB_URL, DB_USERNAME, DB_PASSWORD)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author(AUTHOR) // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .dateType(DateType.TIME_PACK) // 使用 java.time 包
                            .outputDir(outputDir) // 指定输出目录
                            .disableOpenDir(); // 禁止打开输出目录
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent(PARENT_PACKAGE) // 设置父包名
                            .moduleName(moduleName)
                            .entity("model") // 实体类包名
                            .mapper("mapper") // Mapper 接口包名
                            .service("service") // Service 接口包名
                            .serviceImpl("service.impl") // Service 实现类包名
                            .controller("controller") // Controller 包名
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml, mapperXmlPath)); // 设置mapperXml生成路径
                })
                // 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude(TABLE_NAMES) // 设置需要生成的表名
                            .addTablePrefix("") // 设置过滤表前缀
                            // Entity 策略配置
                            .entityBuilder()
                            .enableLombok() // 开启 lombok 模式
                            .enableTableFieldAnnotation() // 开启生成实体时生成字段注解
                            .naming(NamingStrategy.underline_to_camel) // 数据库表映射到实体的命名策略
                            .columnNaming(NamingStrategy.underline_to_camel) // 数据库表字段映射到实体的命名策略
                            .idType(IdType.AUTO) // 主键生成策略
                            .addTableFills(new Column("created_at", FieldFill.INSERT)) // 自动填充创建时间
                            .addTableFills(new Column("updated_at", FieldFill.INSERT_UPDATE)) // 自动填充更新时间
                            // Mapper 策略配置
                            .mapperBuilder()
                            .enableMapperAnnotation() // 开启 @Mapper 注解
                            .enableBaseResultMap() // 启用 BaseResultMap 生成
                            .enableBaseColumnList() // 启用 BaseColumnList 生成
                            // Service 策略配置
                            .serviceBuilder()
                            .formatServiceFileName("%sService") // 格式化 service 接口文件名称
                            .formatServiceImplFileName("%sServiceImpl") // 格式化 service 实现类文件名称
                            // Controller 策略配置
                            .controllerBuilder()
                            .enableRestStyle() // 开启生成@RestController 控制器
                            .enableHyphenStyle(); // 开启驼峰转连字符
                })
                // 使用 Velocity 引擎模板，默认的是 Velocity 引擎模板
                .templateEngine(new com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine())
                // 执行
                .execute();
        
        System.out.println("=================================");
        System.out.println("代码生成完成！");
        System.out.println("生成位置：" + outputDir);
        System.out.println("XML文件位置：" + mapperXmlPath);
        System.out.println("生成的表：" + String.join(", ", TABLE_NAMES));
        System.out.println("=================================");
        System.out.println("注意事项：");
        System.out.println("1. 生成的代码会覆盖现有文件，请注意备份");
        System.out.println("2. 建议生成后检查实体类的字段映射是否正确");
        System.out.println("3. 可能需要手动调整某些复杂的字段类型（如JSON字段）");
        System.out.println("=================================");
    }
} 