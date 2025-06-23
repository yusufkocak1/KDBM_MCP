package com.kocak.kdbmmcp.model;

import java.util.Map;

/**
 * MCP araçları için model sınıfı
 */
public class McpTool {
    private String name;
    private String description;
    private Map<String, Object> inputSchema;

    // Default constructor
    public McpTool() {
    }

    // All-args constructor
    public McpTool(String name, String description, Map<String, Object> inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }

    // Getter and setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }

    public void setInputSchema(Map<String, Object> inputSchema) {
        this.inputSchema = inputSchema;
    }

    // Builder pattern implementation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final McpTool instance = new McpTool();

        public Builder name(String name) {
            instance.setName(name);
            return this;
        }

        public Builder description(String description) {
            instance.setDescription(description);
            return this;
        }

        public Builder inputSchema(Map<String, Object> inputSchema) {
            instance.setInputSchema(inputSchema);
            return this;
        }

        public McpTool build() {
            return instance;
        }
    }
}
