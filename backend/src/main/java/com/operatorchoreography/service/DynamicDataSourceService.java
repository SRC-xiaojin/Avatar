package com.operatorchoreography.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.operatorchoreography.generator.model.OperatorTemplateParams;
import com.operatorchoreography.generator.service.OperatorTemplateParamsService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动态数据源管理服务
 * 支持根据算子模板ID动态创建和管理数据库连接
 * 使用URL中的IP+端口+数据库名作为缓存键，实现DataSource复用
 * JdbcTemplate实时创建，不进行缓存
 */
@Slf4j
@Service
public class DynamicDataSourceService {

    @Autowired
    private OperatorTemplateParamsService operatorTemplateParamsService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 数据源缓存，键格式：host:port:database
    private final Map<String, DataSource> dataSourceCache = new ConcurrentHashMap<>();
    
    // URL解析正则表达式
    private static final Pattern URL_PATTERN = Pattern.compile(
        "jdbc:(?:mysql|mariadb|postgresql)://([^:/]+):(\\d+)/([^?;&]+)(?:[?;&].*)?", 
        Pattern.CASE_INSENSITIVE
    );
    
    // SQL Server URL解析正则表达式
    private static final Pattern SQLSERVER_PATTERN = Pattern.compile(
        "jdbc:sqlserver://([^:/;]+):(\\d+)(?:;.*?databaseName=([^;]+))?", 
        Pattern.CASE_INSENSITIVE
    );
    
    // Oracle URL解析正则表达式
    private static final Pattern ORACLE_PATTERN = Pattern.compile(
        "jdbc:oracle:thin:@([^:/]+):(\\d+):([^?;&]+)", 
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 根据模板ID获取JdbcTemplate（实时创建）
     * @param templateId 模板ID
     * @return JdbcTemplate实例
     * @throws Exception 获取失败异常
     */
    public JdbcTemplate getJdbcTemplate(List<OperatorTemplateParams> params) throws Exception {
        
        
        // 查找datasource参数
        OperatorTemplateParams datasourceParam = params.stream()
                .filter(p -> "datasource".equals(p.getParamKey()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到datasource参数配置"));
        
        // 解析datasource配置
        Map<String, Object> datasourceConfig = parseDataSourceConfig(datasourceParam.getDefaultValue());
        
        // 生成基于URL的缓存键
        String cacheKey = generateCacheKey(datasourceConfig);
        
        // 获取或创建数据源
        DataSource dataSource = getOrCreateDataSource(datasourceConfig, cacheKey);
        
        // 实时创建JdbcTemplate
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        log.info("成功创建JdbcTemplate: cacheKey={}", cacheKey);
        return jdbcTemplate;
    }

    /**
     * 根据自定义数据源配置获取JdbcTemplate（实时创建）
     * @param datasourceConfig 数据源配置
     * @param identifier 标识符（已弃用，使用URL生成缓存键）
     * @return JdbcTemplate实例
     * @throws Exception 创建失败异常
     */
    public JdbcTemplate getJdbcTemplate(Map<String, Object> datasourceConfig, String identifier) throws Exception {
        log.info("获取自定义JdbcTemplate: identifier={}", identifier);
        
        // 生成基于URL的缓存键
        String cacheKey = generateCacheKey(datasourceConfig);
        
        // 获取或创建数据源
        DataSource dataSource = getOrCreateDataSource(datasourceConfig, cacheKey);
        
        // 实时创建JdbcTemplate
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        log.info("成功创建自定义JdbcTemplate: identifier={}, cacheKey={}", identifier, cacheKey);
        return jdbcTemplate;
    }

    /**
     * 生成基于URL的缓存键
     * @param datasourceConfig 数据源配置
     * @return 缓存键，格式：host:port:database
     */
    private String generateCacheKey(Map<String, Object> datasourceConfig) throws Exception {
        String url = (String) datasourceConfig.get("url");
        if (url == null || url.trim().isEmpty()) {
            throw new RuntimeException("数据库连接URL不能为空");
        }
        
        String trimmedUrl = url.trim();
        String host = null;
        String port = null;
        String database = null;
        
        // 尝试匹配MySQL/PostgreSQL/MariaDB格式
        Matcher matcher = URL_PATTERN.matcher(trimmedUrl);
        if (matcher.find()) {
            host = matcher.group(1);
            port = matcher.group(2);
            database = matcher.group(3);
        } else {
            // 尝试匹配SQL Server格式
            matcher = SQLSERVER_PATTERN.matcher(trimmedUrl);
            if (matcher.find()) {
                host = matcher.group(1);
                port = matcher.group(2);
                database = matcher.group(3);
                if (database == null) {
                    database = "default"; // SQL Server可能没有指定数据库名
                }
            } else {
                // 尝试匹配Oracle格式
                matcher = ORACLE_PATTERN.matcher(trimmedUrl);
                if (matcher.find()) {
                    host = matcher.group(1);
                    port = matcher.group(2);
                    database = matcher.group(3);
                }
            }
        }
        
        if (host != null && port != null && database != null) {
            String cacheKey = host + ":" + port + ":" + database;
            log.debug("生成缓存键: url={}, cacheKey={}", url, cacheKey);
            return cacheKey;
        } else {
            // 如果无法解析，使用URL的hash作为备用方案
            String fallbackKey = "url_hash_" + Math.abs(url.hashCode());
            log.warn("无法解析URL，使用备用缓存键: url={}, fallbackKey={}", url, fallbackKey);
            return fallbackKey;
        }
    }

    /**
     * 解析数据源配置
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseDataSourceConfig(String datasourceConfigStr) throws Exception {
        if (datasourceConfigStr == null || datasourceConfigStr.trim().isEmpty()) {
            throw new RuntimeException("数据源配置为空");
        }
        
        try {
            return objectMapper.readValue(datasourceConfigStr, Map.class);
        } catch (Exception e) {
            log.error("解析数据源配置失败: {}", datasourceConfigStr, e);
            throw new RuntimeException("解析数据源配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取或创建数据源（支持缓存）
     */
    private DataSource getOrCreateDataSource(Map<String, Object> config, String cacheKey) throws Exception {
        // 先从缓存获取
        DataSource cachedDataSource = dataSourceCache.get(cacheKey);
        if (cachedDataSource != null) {
            log.debug("从缓存获取数据源: cacheKey={}", cacheKey);
            return cachedDataSource;
        }
        
        // 创建新的数据源
        DataSource dataSource = createDataSource(config, cacheKey);
        
        // 缓存数据源
        dataSourceCache.put(cacheKey, dataSource);
        
        return dataSource;
    }

    /**
     * 创建数据源
     */
    private DataSource createDataSource(Map<String, Object> config, String cacheKey) throws Exception {
        // 获取配置参数
        String url = (String) config.get("url");
        String driverClassName = (String) config.get("driver-class-name");
        String username = (String) config.get("username");
        String password = (String) config.get("password");
        
        if (url == null || url.trim().isEmpty()) {
            throw new RuntimeException("数据库连接URL不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("数据库用户名不能为空");
        }
        if (password == null) {
            password = ""; // 允许空密码
        }
        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            driverClassName = "com.mysql.cj.jdbc.Driver"; // 默认MySQL驱动
        }
        
        log.info("创建数据源: cacheKey={}, url={}, username={}", cacheKey, url, username);
        
        // 创建HikariCP配置
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        
        // 连接池配置
        hikariConfig.setMaximumPoolSize(getIntValue(config, "maxPoolSize", 10));
        hikariConfig.setMinimumIdle(getIntValue(config, "minPoolSize", 1));
        hikariConfig.setConnectionTimeout(getIntValue(config, "connectionTimeout", 30000));
        hikariConfig.setIdleTimeout(getIntValue(config, "idleTimeout", 600000));
        hikariConfig.setMaxLifetime(getIntValue(config, "maxLifetime", 1800000));
        hikariConfig.setLeakDetectionThreshold(getIntValue(config, "leakDetectionThreshold", 60000));
        
        // 连接测试
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setValidationTimeout(5000);
        
        // 连接池名称
        hikariConfig.setPoolName("DynamicPool-" + cacheKey);
        
        // 创建数据源
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        
        log.info("数据源创建成功: cacheKey={}, poolName={}", cacheKey, hikariConfig.getPoolName());
        return dataSource;
    }

    /**
     * 获取整数配置值
     */
    private int getIntValue(Map<String, Object> config, String key, int defaultValue) {
        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("配置值解析失败，使用默认值: key={}, value={}, default={}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * 获取缓存信息
     */
    public Map<String, Object> getCacheInfo() {
        Map<String, Object> cacheInfo = Map.of(
            "dataSourceCacheSize", dataSourceCache.size(),
            "cacheKeys", dataSourceCache.keySet(),
            "jdbcTemplateCache", "实时创建，无缓存"
        );
        
        log.info("缓存信息: {}", cacheInfo);
        return cacheInfo;
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        log.info("清理数据源缓存，当前缓存大小: dataSource={}", dataSourceCache.size());
        
        // 关闭数据源
        dataSourceCache.values().forEach(dataSource -> {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                log.info("关闭数据源: poolName={}", hikariDataSource.getPoolName());
                hikariDataSource.close();
            }
        });
        dataSourceCache.clear();
        
        log.info("数据源缓存清理完成");
    }

    /**
     * 清理指定缓存键的连接
     */
    public boolean clearCacheByKey(String cacheKey) {
        log.info("清理指定缓存键的连接: cacheKey={}", cacheKey);
        
        DataSource removedDataSource = dataSourceCache.remove(cacheKey);
        
        if (removedDataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) removedDataSource;
            log.info("关闭指定数据源: cacheKey={}, poolName={}", cacheKey, hikariDataSource.getPoolName());
            hikariDataSource.close();
        }
        
        boolean removed = removedDataSource != null;
        log.info("清理缓存键完成: cacheKey={}, removed={}", cacheKey, removed);
        return removed;
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void destroy() {
        log.info("应用关闭，清理数据源资源");
        clearCache();
    }
} 