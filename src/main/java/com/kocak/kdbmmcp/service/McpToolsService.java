package com.kocak.kdbmmcp.service;

import com.kocak.kdbmmcp.model.McpTool;
import com.kocak.kdbmmcp.model.McpToolResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class McpToolsService {

    @Autowired
    private DatabaseService databaseService;

    public List<McpTool> getAvailableTools() {
        return Arrays.asList(
                McpTool.builder()
                        .name("execute_query")
                        .description("SQL sorgusu çalıştırır ve sonuçları döndürür. SELECT, INSERT, UPDATE ve DELETE sorgularını destekler." +
                                "\n\nParametreler:\n" +
                                "- query (string, zorunlu): Çalıştırılacak SQL sorgusu. Örnek: 'SELECT * FROM users WHERE age > ?' veya 'INSERT INTO users (name, email) VALUES (?, ?)'.\n" +
                                "- params (array, opsiyonel): Sorgu içindeki ? işaretleri yerine geçecek parametreler. Örnek: [25] veya ['John', 'john@example.com'].\n\n" +
                                "Yanıt:\n" +
                                "- SELECT sorguları için: [{column1: value1, column2: value2, ...}, ...] formatında sonuç satırları.\n" +
                                "- INSERT/UPDATE/DELETE için: Etkilenen satır sayısı.")
                        .inputSchema(createQuerySchema())
                        .build(),
                McpTool.builder()
                        .name("get_tables")
                        .description("Veritabanındaki tüm tabloları listeler.\n\n" +
                                "Parametreler:\n" +
                                "- Parametre gerekmiyor.\n\n" +
                                "Yanıt:\n" +
                                "- [\"tablo1\", \"tablo2\", ...] formatında bir dizi olarak tablo adları.")
                        .inputSchema(new HashMap<>())
                        .build(),
                McpTool.builder()
                        .name("get_table_structure")
                        .description("Belirtilen tablonun yapısını detaylı olarak gösterir.\n\n" +
                                "Parametreler:\n" +
                                "- table (string, zorunlu): Yapısı incelenecek tablo adı. Örnek: 'users'.\n\n" +
                                "Yanıt:\n" +
                                "- columns: [{\"column_name\": \"id\", \"data_type\": \"integer\", \"is_nullable\": \"NO\", \"is_primary_key\": true, ...}, ...] formatında kolon bilgileri.\n" +
                                "- relations: [{\"table_name\": \"users\", \"column_name\": \"role_id\", \"foreign_table_name\": \"roles\", \"foreign_column_name\": \"id\"}, ...] formatında ilişki bilgileri.")
                        .inputSchema(createTableSchema())
                        .build(),
                McpTool.builder()
                        .name("get_table_relations")
                        .description("Belirtilen tablonun diğer tablolarla olan ilişkilerini gösterir.\n\n" +
                                "Parametreler:\n" +
                                "- table (string, zorunlu): İlişkileri incelenecek tablo adı. Örnek: 'orders'.\n\n" +
                                "Yanıt:\n" +
                                "- Tablonun diğer tablolara olan foreign key ilişkileri ve diğer tablolardan gelen ilişkiler.")
                        .inputSchema(createTableSchema())
                        .build(),
                McpTool.builder()
                        .name("get_db_diagram")
                        .description("Veritabanındaki tüm tabloların ve aralarındaki ilişkilerin diyagramını oluşturur.\n\n" +
                                "Parametreler:\n" +
                                "- Parametre gerekmiyor.\n\n" +
                                "Yanıt:\n" +
                                "- Tüm tablolar ve aralarındaki ilişkiler listesi.")
                        .inputSchema(new HashMap<>())
                        .build()
        );
    }

    public McpToolResult executeTool(String toolName, Map<String, Object> arguments) {
        switch (toolName) {
            case "execute_query":
                return executeQuery(arguments);
            case "get_tables":
                return getTables();
            case "get_table_structure":
                return getTableStructure(arguments);
            case "get_table_relations":
                return getTableRelations(arguments);
            case "get_db_diagram":
                return getDatabaseDiagram();
            default:
                throw new RuntimeException("Bilinmeyen tool: " + toolName);
        }
    }

    private McpToolResult executeQuery(Map<String, Object> arguments) {
        String query = (String) ((LinkedHashMap)arguments.get("args")).get("query");
        List<Object> params = (List<Object>) arguments.getOrDefault("params", new ArrayList<>());

        List<Map<String, Object>> results = databaseService.executeQuery(query, params);

        return McpToolResult.builder()
                .data(results)
                .build();
    }

    private Map<String, Object> createQuerySchema() {
        Map<String, Object> schema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> queryProp = new HashMap<>();
        queryProp.put("type", "string");
        queryProp.put("description", "SQL sorgusu");

        Map<String, Object> paramsProp = new HashMap<>();
        paramsProp.put("type", "array");
        paramsProp.put("description", "Sorgu parametreleri (opsiyonel)");

        properties.put("query", queryProp);
        properties.put("params", paramsProp);

        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", List.of("query"));

        return schema;
    }

    private Map<String, Object> createTableSchema() {
        Map<String, Object> schema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> tableProp = new HashMap<>();
        tableProp.put("type", "string");
        tableProp.put("description", "Tablo adı");

        properties.put("table", tableProp);

        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", List.of("table"));

        return schema;
    }

    private McpToolResult getTables() {
        try {
            List<String> tables = databaseService.getTableNames();
            return McpToolResult.builder()
                    .data(tables)
                    .build();
        } catch (Exception e) {
            return McpToolResult.builder()
                    .error("Tablolar alınırken hata: " + e.getMessage())
                    .build();
        }
    }

    private McpToolResult getTableStructure(Map<String, Object> arguments) {
        try {
            // Parametre çıkarma işlemi düzeltildi
            String tableName = null;
            if (arguments.containsKey("args")) {
                Map<String, Object> args = (Map<String, Object>) arguments.get("args");
                if (args.containsKey("table")) {
                    tableName = args.get("table").toString();
                }
            } else if (arguments.containsKey("table")) {
                tableName = arguments.get("table").toString();
            }

            if (tableName == null) {
                throw new RuntimeException("Tablo adı (table) parametresi gereklidir");
            }

            System.out.println("Tablo yapısı alınıyor: " + tableName);

            // Kolon bilgilerini al
            List<Map<String, Object>> columns = databaseService.getTableStructure(tableName);

            // İlişkileri al
            List<Map<String, Object>> relations = databaseService.getTableRelations(tableName);

            // Sonucu oluştur
            Map<String, Object> result = new HashMap<>();
            result.put("columns", columns);
            result.put("relations", relations);

            return McpToolResult.builder()
                    .data(result)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Hata detayı: " + e.getMessage());
            return McpToolResult.builder()
                    .error("Tablo yapısı alınırken hata: " + e.getMessage())
                    .build();
        }
    }

    private McpToolResult getTableRelations(Map<String, Object> arguments) {
        try {
            // Parametre çıkarma işlemi düzeltildi
            String tableName = null;
            if (arguments.containsKey("args")) {
                Map<String, Object> args = (Map<String, Object>) arguments.get("args");
                if (args.containsKey("table")) {
                    tableName = args.get("table").toString();
                }
            } else if (arguments.containsKey("table")) {
                tableName = arguments.get("table").toString();
            }

            if (tableName == null) {
                throw new RuntimeException("Tablo adı (table) parametresi gereklidir");
            }

            System.out.println("Tablo ilişkileri alınıyor: " + tableName);

            List<Map<String, Object>> relations = databaseService.getTableRelations(tableName);
            return McpToolResult.builder()
                    .data(relations)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Hata detayı: " + e.getMessage());
            return McpToolResult.builder()
                    .error("Tablo ilişkileri alınırken hata: " + e.getMessage())
                    .build();
        }
    }

    private McpToolResult getDatabaseDiagram() {
        try {
            List<Map<String, Object>> relations = databaseService.getAllTableRelations();
            // Tabloları al
            List<String> tables = databaseService.getTableNames();

            Map<String, Object> diagram = new HashMap<>();
            diagram.put("tables", tables);
            diagram.put("relations", relations);

            return McpToolResult.builder()
                    .data(diagram)
                    .build();
        } catch (Exception e) {
            return McpToolResult.builder()
                    .error("Veritabanı diyagramı alınırken hata: " + e.getMessage())
                    .build();
        }
    }
}