package com.kocak.kdbmmcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * MCP protokolü için mesaj modeli
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class McpMessage {
    private String jsonrpc;
    private String id;
    private String method;
    private Object params;

    // Default constructor
    public McpMessage() {
    }

    // All-args constructor
    public McpMessage(String jsonrpc, String id, String method, Object params) {
        this.jsonrpc = jsonrpc;
        this.id = id;
        this.method = method;
        this.params = params;
    }

    // Getter and setter methods
    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    // Builder pattern implementation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final McpMessage instance = new McpMessage();

        public Builder jsonrpc(String jsonrpc) {
            instance.setJsonrpc(jsonrpc);
            return this;
        }
        public Builder id(String id) {
            instance.setId(id);
            return this;
        }
        public Builder method(String method) {
            instance.setMethod(method);
            return this;
        }
        public Builder params(Object params) {
            instance.setParams(params);
            return this;
        }
        public McpMessage build() {
            return instance;
        }
    }
}
