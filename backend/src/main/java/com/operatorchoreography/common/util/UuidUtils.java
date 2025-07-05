package com.operatorchoreography.common.util;

import java.util.UUID;

public class UuidUtils {

    /**
     * 生成不带-的UUID字符串
     *
     * @return a UUID string without hyphens
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
} 