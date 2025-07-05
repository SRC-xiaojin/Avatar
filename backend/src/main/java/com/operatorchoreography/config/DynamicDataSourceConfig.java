package com.operatorchoreography.config;

import lombok.Data;

/**
 * 动态数据源配置
 */
@Data
public class DynamicDataSourceConfig {
    
    /**
     * 数据库连接URL
     */
    private String url;
    
    /**
     * 数据库驱动类名
     */
    private String driverClassName;
    
    /**
     * 数据库用户名
     */
    private String username;
    
    /**
     * 数据库密码
     */
    private String password;
    
    /**
     * 连接池最大连接数
     */
    private Integer maxPoolSize = 10;
    
    /**
     * 连接池最小连接数
     */
    private Integer minPoolSize = 1;
    
    /**
     * 连接超时时间(毫秒)
     */
    private Integer connectionTimeout = 30000;
    
    /**
     * 空闲超时时间(毫秒)
     */
    private Integer idleTimeout = 600000;
    
    /**
     * 连接最大生命周期(毫秒)
     */
    private Integer maxLifetime = 1800000;
    
    /**
     * 连接泄漏检测阈值(毫秒)
     */
    private Integer leakDetectionThreshold = 60000;
} 