package com.kocak.kdbmmcp.config;

import com.kocak.kdbmmcp.model.McpMessage;
import com.kocak.kdbmmcp.model.McpTool;
import com.kocak.kdbmmcp.model.McpToolResult;
import com.kocak.kdbmmcp.service.DatabaseService;
import com.kocak.kdbmmcp.service.McpToolsService;
import com.kocak.kdbmmcp.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class McpWebSocketHandler extends TextWebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(McpWebSocketHandler.class);

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private McpToolsService mcpToolsService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("WebSocket bağlantısı kuruldu: {}", session.getId());
        // MCP bağlantısı kuruldu
        sendCapabilities(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("WebSocket bağlantısı kapatıldı: {}, sebep: {}", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            logger.debug("Mesaj alındı: {}", message.getPayload());
            McpMessage mcpMessage = JsonUtils.parseMessage(message.getPayload());
            handleMcpRequest(session, mcpMessage);
        } catch (Exception e) {
            logger.error("Mesaj işleme hatası: ", e);
            sendError(session, e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("WebSocket transport hatası: {}", exception.getMessage(), exception);
    }

    private void handleMcpRequest(WebSocketSession session, McpMessage message) {
        switch (message.getMethod()) {
            case "tools/list":
                sendAvailableTools(session, message);
                break;
            case "tools/call":
                executeTool(session, message);
                break;
            default:
                sendError(session, "Bilinmeyen method: " + message.getMethod());
        }
    }

    private void sendCapabilities(WebSocketSession session) {
        try {
            McpMessage response = McpMessage.builder()
                    .jsonrpc("2.0")
                    .method("capabilities")
                    .id(session.getId())
                    .params(Map.of("version", "1.0"))
                    .build();
            session.sendMessage(new TextMessage(JsonUtils.toJson(response)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAvailableTools(WebSocketSession session, McpMessage message) {
        try {
            List<McpTool> tools = mcpToolsService.getAvailableTools();
            McpMessage response = McpMessage.builder()
                    .jsonrpc("2.0")
                    .id(message.getId())
                    .method("tools/list/response")
                    .params(Map.of("tools", tools))
                    .build();
            session.sendMessage(new TextMessage(JsonUtils.toJson(response)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeTool(WebSocketSession session, McpMessage message) {
        try {
            Map<String, Object> params = (Map<String, Object>) message.getParams();
            String toolName = (String) params.get("toolName");

            McpToolResult result = mcpToolsService.executeTool(toolName, params);

            McpMessage response = McpMessage.builder()
                    .jsonrpc("2.0")
                    .id(message.getId())
                    .method("tools/call/response")
                    .params(Map.of("result", result))
                    .build();
            session.sendMessage(new TextMessage(JsonUtils.toJson(response)));
        } catch (Exception e) {
            sendError(session, "Tool çalıştırma hatası: " + e.getMessage());
        }
    }

    private void sendError(WebSocketSession session, String message) {
        try {
            McpMessage response = McpMessage.builder()
                    .jsonrpc("2.0")
                    .id(session.getId())
                    .method("error")
                    .params(Map.of("message", message))
                    .build();
            session.sendMessage(new TextMessage(JsonUtils.toJson(response)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}