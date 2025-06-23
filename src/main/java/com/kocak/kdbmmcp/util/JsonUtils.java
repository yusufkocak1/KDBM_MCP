package com.kocak.kdbmmcp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kocak.kdbmmcp.model.McpMessage;

/**
 * JSON işlemleri için yardımcı sınıf
 */
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Bir nesneyi JSON string'e dönüştürür
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * JSON string'i McpMessage nesnesine dönüştürür
     */
    public static McpMessage parseMessage(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, McpMessage.class);
    }

    /**
     * JSON string'i verilen tipe dönüştürür
     */
    public static <T> T fromJson(String json, Class<T> valueType) throws JsonProcessingException {
        return objectMapper.readValue(json, valueType);
    }
}
