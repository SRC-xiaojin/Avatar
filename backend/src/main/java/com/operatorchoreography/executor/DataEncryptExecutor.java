package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 数据加密执行器
 * 数据加密处理
 */
@Slf4j
@Component
public class DataEncryptExecutor implements BaseExecutor {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public ExecutorResult encrypt(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行数据加密: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            String plainText = (String) inputData.get("plainText");
            String encryptionKey = (String) inputData.get("encryptionKey");
            String encryptionType = (String) inputData.get("encryptionType");
            
            if (plainText == null || plainText.isEmpty()) {
                log.error("明文数据为空: templateId={}", templateId);
                throw new IllegalArgumentException("明文数据不能为空");
            }
            
            // 默认加密类型
            if (encryptionType == null) encryptionType = "BASE64";
            
            log.debug("数据加密参数: encryptionType={}, plainTextLength={}", 
                    encryptionType, plainText.length());
            
            String encryptedText;
            
            switch (encryptionType.toUpperCase()) {
                case "BASE64":
                    // Base64编码
                    encryptedText = Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
                    log.debug("使用BASE64编码完成");
                    break;
                case "AES":
                    // AES加密
                    encryptedText = encryptWithAES(plainText, encryptionKey);
                    log.debug("使用AES加密完成");
                    break;
                case "SIMPLE":
                    // 简单加密（仅用于测试）
                    encryptedText = simpleEncrypt(plainText);
                    log.debug("使用简单加密完成");
                    break;
                default:
                    throw new IllegalArgumentException("不支持的加密类型: " + encryptionType);
            }
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("encryptedText", encryptedText);
            outputData.put("originalText", plainText);
            outputData.put("encryptionType", encryptionType);
            outputData.put("message", "数据加密执行成功");
            
            log.info("数据加密成功: templateId={}, 加密类型: {}, 原文长度: {}, 密文长度: {}", 
                    templateId, encryptionType, plainText.length(), encryptedText.length());
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("数据加密执行成功")
                                .withMetadata("encryptionType", encryptionType)
                                .withMetadata("originalLength", plainText.length())
                                .withMetadata("encryptedLength", encryptedText.length())
                                .withMetadata("keyProvided", encryptionKey != null);
            
        } catch (Exception e) {
            log.error("数据加密执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("数据加密执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("数据加密执行失败: " + e.getMessage());
        }
    }
    
    /**
     * AES加密
     */
    private String encryptWithAES(String plainText, String key) throws Exception {
        if (key == null || key.isEmpty()) {
            key = "defaultkey123456"; // 默认密钥（16字节）
        }
        
        // 确保密钥长度为16字节
        if (key.length() < 16) {
            key = String.format("%-16s", key).replace(' ', '0');
        } else if (key.length() > 16) {
            key = key.substring(0, 16);
        }
        
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    /**
     * 简单加密（仅用于测试）
     */
    private String simpleEncrypt(String plainText) {
        StringBuilder encrypted = new StringBuilder();
        for (char c : plainText.toCharArray()) {
            // 简单的字符偏移
            encrypted.append((char) (c + 3));
        }
        return Base64.getEncoder().encodeToString(encrypted.toString().getBytes(StandardCharsets.UTF_8));
    }
} 