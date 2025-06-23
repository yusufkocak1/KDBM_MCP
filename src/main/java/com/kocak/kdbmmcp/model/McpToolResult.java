package com.kocak.kdbmmcp.model;

/**
 * MCP araç çalıştırma sonuçları için model sınıfı
 */
public class McpToolResult {
    private Object data;
    private String error;

    // Default constructor
    public McpToolResult() {
    }

    // All-args constructor
    public McpToolResult(Object data, String error) {
        this.data = data;
        this.error = error;
    }

    // Getter and setter methods
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    // Builder pattern implementation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final McpToolResult instance = new McpToolResult();

        public Builder data(Object data) {
            instance.setData(data);
            return this;
        }

        public Builder error(String error) {
            instance.setError(error);
            return this;
        }

        public McpToolResult build() {
            return instance;
        }
    }
}
