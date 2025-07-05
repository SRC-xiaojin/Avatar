package com.operatorchoreography.common;

import java.util.List;
import java.util.Map;

// 列表类型和Map类型
public class FlexibleBean<T> {
    private T data;
    
    public FlexibleBean(T data) {
        validateType(data);
        this.data = data;
    }
    
    private void validateType(T data) {
        if (!(data instanceof Map) && !(data instanceof List)) {
            throw new IllegalArgumentException(
                "Data must be either Map<String, Object> or List<Object>");
        }
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        validateType(data);
        this.data = data;
    }
    
    // 工厂方法
    public static FlexibleBean<Map<String, Object>> ofMap(Map<String, Object> map) {
        return new FlexibleBean<>(map);
    }
    
    public static FlexibleBean<List<Object>> ofList(List<Object> list) {
        return new FlexibleBean<>(list);
    }
}

