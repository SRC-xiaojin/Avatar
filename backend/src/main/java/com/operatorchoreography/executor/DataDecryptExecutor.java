package com.operatorchoreography.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 数据解密执行器
 * 数据解密处理
 */
@Slf4j
@Component
public class DataDecryptExecutor implements BaseExecutor {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public ExecutorResult decrypt(Map<String, Object> inputData, Long templateId) throws Exception {
        return execute(inputData, templateId);
    }

    @Override
    public ExecutorResult execute(Map<String, Object> inputData, Long templateId) throws Exception {
        log.info("开始执行数据解密: templateId={}", templateId);
        
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            String encryptedText = (String) inputData.get("encryptedText");
            String encryptionKey = (String) inputData.get("encryptionKey");
            String encryptionType = (String) inputData.get("encryptionType");
            
            if (encryptedText == null || encryptedText.isEmpty()) {
                log.error("加密数据为空: templateId={}", templateId);
                throw new IllegalArgumentException("加密数据不能为空");
            }
            
            if (encryptionType == null) encryptionType = "BASE64";
            
            log.debug("数据解密参数: encryptionType={}, encryptedTextLength={}", 
                    encryptionType, encryptedText.length());
            
            String plainText;
            
            switch (encryptionType.toUpperCase()) {
                case "BASE64":
                    // Base64解码
                    byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
                    plainText = new String(decodedBytes, StandardCharsets.UTF_8);
                    log.debug("使用BASE64解码完成");
                    break;
                case "AES":
                    // AES解密
                    plainText = decryptWithAES(encryptedText, encryptionKey);
                    log.debug("使用AES解密完成");
                    break;
                case "SIMPLE":
                    // 简单解密（仅用于测试）
                    plainText = simpleDecrypt(encryptedText);
                    log.debug("使用简单解密完成");
                    break;
                default:
                    throw new IllegalArgumentException("不支持的解密类型: " + encryptionType);
            }
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("plainText", plainText);
            outputData.put("originalEncryptedText", encryptedText);
            outputData.put("encryptionType", encryptionType);
            outputData.put("message", "数据解密执行成功");
            
            log.info("数据解密成功: templateId={}, 解密类型: {}, 密文长度: {}, 明文长度: {}", 
                    templateId, encryptionType, encryptedText.length(), plainText.length());
            
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.success(outputData, startTime, endTime)
                                .withTemplateInfo(templateId, null)
                                .withLog("数据解密执行成功")
                                .withMetadata("encryptionType", encryptionType)
                                .withMetadata("encryptedLength", encryptedText.length())
                                .withMetadata("plainLength", plainText.length())
                                .withMetadata("keyProvided", encryptionKey != null);
            
        } catch (Exception e) {
            log.error("数据解密执行失败: templateId={}", templateId, e);
            LocalDateTime endTime = LocalDateTime.now();
            return ExecutorResult.failure("数据解密执行失败", e)
                                .withTemplateInfo(templateId, null)
                                .withLog("数据解密执行失败: " + e.getMessage());
        }
    }
    
    /**
     * AES解密
     */
    private String decryptWithAES(String encryptedText, String key) throws Exception {
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
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * 简单解密（仅用于测试）
     */
    private String simpleDecrypt(String encryptedText) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        String encoded = new String(decodedBytes, StandardCharsets.UTF_8);
        
        StringBuilder decrypted = new StringBuilder();
        for (char c : encoded.toCharArray()) {
            // 反向字符偏移
            decrypted.append((char) (c - 3));
        }
        return decrypted.toString();
    }
} 