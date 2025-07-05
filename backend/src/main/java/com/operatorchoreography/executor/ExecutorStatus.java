package com.operatorchoreography.executor;

/**
 * 执行器执行状态枚举
 * 
 * @author OperatorChoreography
 * @since 2024-01-01
 */
public enum ExecutorStatus {
    
    /**
     * 执行成功
     */
    SUCCESS("执行成功"),
    
    /**
     * 执行失败
     */
    FAILED("执行失败"),
    
    /**
     * 执行超时
     */
    TIMEOUT("执行超时"),
    
    /**
     * 执行被取消
     */
    CANCELLED("执行被取消"),
    
    /**
     * 正在执行
     */
    RUNNING("正在执行"),
    
    /**
     * 等待执行
     */
    PENDING("等待执行"),
    
    /**
     * 执行跳过
     */
    SKIPPED("执行跳过"),
    
    /**
     * 未知状态
     */
    UNKNOWN("未知状态");
    
    private final String description;
    
    ExecutorStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 是否为成功状态
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    /**
     * 是否为失败状态
     */
    public boolean isFailure() {
        return this == FAILED || this == TIMEOUT || this == CANCELLED;
    }
    
    /**
     * 是否为最终状态（已完成）
     */
    public boolean isFinal() {
        return this == SUCCESS || this == FAILED || this == TIMEOUT || this == CANCELLED || this == SKIPPED;
    }
    
    /**
     * 是否为运行中状态
     */
    public boolean isRunning() {
        return this == RUNNING || this == PENDING;
    }
} 