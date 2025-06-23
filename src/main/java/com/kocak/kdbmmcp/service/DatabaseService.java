package com.kocak.kdbmmcp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private String databaseType;

    /**
     * Veritabanı türünü belirler
     */
    private String getDatabaseType() {
        if (databaseType == null) {
            try {
                DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
                String productName = metaData.getDatabaseProductName().toLowerCase();

                if (productName.contains("postgresql")) {
                    databaseType = "postgresql";
                } else if (productName.contains("oracle")) {
                    databaseType = "oracle";
                } else if (productName.contains("mysql")) {
                    databaseType = "mysql";
                } else if (productName.contains("microsoft") || productName.contains("sql server")) {
                    databaseType = "sqlserver";
                } else {
                    databaseType = "unknown";
                }
            } catch (SQLException e) {
                databaseType = "unknown";
            }
        }
        return databaseType;
    }

    public List<Map<String, Object>> executeQuery(String query, List<Object> params) {
        try {
            // Loglama ekleyelim
            System.out.println("Çalıştırılacak sorgu: " + query);
            if (params != null && !params.isEmpty()) {
                System.out.println("Parametreler: " + params);

                // Türkçe karakter içeren parametreleri kontrol et
                List<Object> sanitizedParams = new ArrayList<>();
                for (Object param : params) {
                    if (param instanceof String) {
                        // String parametreleri UTF-8 formatında olduğundan emin olalım
                        String strParam = (String) param;
                        sanitizedParams.add(strParam);
                    } else {
                        sanitizedParams.add(param);
                    }
                }
                return jdbcTemplate.queryForList(query, sanitizedParams.toArray());
            } else {
                return jdbcTemplate.queryForList(query);
            }
        } catch (Exception e) {
            // Daha detaylı hata ayrıştırması
            String errorMessage = e.getMessage();
            if (errorMessage.contains("bad SQL grammar")) {
                // SQL hatası ayrıştırma
                String detailedMessage = "Sorgu hatası: ";

                // Tablo veya sütun ismi sorunu
                if (errorMessage.contains("Table") && errorMessage.contains("doesn't exist")) {
                    detailedMessage += "Belirtilen tablo bulunamadı. ";
                }
                if (errorMessage.contains("Unknown column")) {
                    detailedMessage += "Belirtilen sütun bulunamadı. ";
                }

                // Genel SQL yazım hatası
                detailedMessage += "SQL yazımını kontrol ediniz. Türkçe karakterlerin (ı,İ,ş,ç,ğ,ö,ü) sorgulamada sorun çıkarabileceğini unutmayınız.";
                throw new RuntimeException(detailedMessage + " Orijinal hata: " + errorMessage);
            }

            throw new RuntimeException("Sorgu çalıştırılırken hata: " + errorMessage);
        }
    }

    public int executeUpdate(String query, List<Object> params) {
        try {
            // Sorguyu ve parametreleri logla
            System.out.println("Çalıştırılacak güncelleme sorgusu: " + query);

            if (params != null && !params.isEmpty()) {
                System.out.println("Parametreler: " + params);

                // NULL değerleri ve Türkçe karakterleri düzgün işleme
                List<Object> sanitizedParams = new ArrayList<>();
                for (Object param : params) {
                    if (param instanceof String) {
                        // String parametreleri UTF-8 formatında olduğundan emin olalım
                        String strParam = (String) param;
                        sanitizedParams.add(strParam.isEmpty() ? null : strParam);
                    } else {
                        // NULL değeri olduğu gibi bırakıyoruz
                        sanitizedParams.add(param);
                    }
                }
                return jdbcTemplate.update(query, sanitizedParams.toArray());
            } else {
                return jdbcTemplate.update(query);
            }
        } catch (Exception e) {
            // Hata mesajını ayrıştır
            String errorMessage = e.getMessage();
            if (errorMessage.contains("bad SQL grammar")) {
                String detailedMessage = "Sorgu hatası: ";

                // Tablo veya sütun ismi sorunu
                if (errorMessage.contains("Table") && errorMessage.contains("doesn't exist")) {
                    detailedMessage += "Belirtilen tablo bulunamadı. ";
                }
                if (errorMessage.contains("Unknown column")) {
                    detailedMessage += "Belirtilen sütun bulunamadı. ";
                }
                if (errorMessage.contains("NULL")) {
                    detailedMessage += "NULL değeri uygun bir şekilde işlenemedi. ";
                }

                // Genel SQL yazım hatası
                detailedMessage += "SQL yazımını kontrol ediniz. Türkçe karakterlerin (ı,İ,ş,ç,ğ,ö,ü) sorgulamada sorun çıkarabileceğini unutmayınız.";
                throw new RuntimeException(detailedMessage + " Orijinal hata: " + errorMessage);
            }

            throw new RuntimeException("Güncelleme çalıştırılırken hata: " + errorMessage);
        }
    }

    public List<String> getTableNames() {
        String query;
        String dbType = getDatabaseType();

        switch (dbType) {
            case "postgresql":
                query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
                break;
            case "oracle":
                query = "SELECT table_name FROM user_tables";
                break;
            case "mysql":
                query = "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()";
                break;
            case "sqlserver":
                query = "SELECT table_name FROM information_schema.tables WHERE table_type = 'BASE TABLE'";
                break;
            default:
                throw new RuntimeException("Desteklenmeyen veritabanı türü: " + dbType);
        }

        return jdbcTemplate.queryForList(query, String.class);
    }

    public List<Map<String, Object>> getTableStructure(String tableName) {
        String dbType = getDatabaseType();

        switch (dbType) {
            case "postgresql":
                // PostgreSQL için - belirsiz sütun referanslarını düzeltiyoruz
                String queryPostgres = "SELECT c.column_name, c.data_type, c.is_nullable, c.column_default, " +
                        "CASE WHEN pk.column_name IS NOT NULL THEN true ELSE false END AS is_primary_key " +
                        "FROM information_schema.columns c " +
                        "LEFT JOIN ( " +
                        "    SELECT ku.table_name, ku.column_name " +
                        "    FROM information_schema.table_constraints AS tc " +
                        "    JOIN information_schema.key_column_usage AS ku ON tc.constraint_name = ku.constraint_name " +
                        "    WHERE tc.constraint_type = 'PRIMARY KEY' AND tc.table_schema = 'public' " +
                        ") pk ON c.table_name = pk.table_name AND c.column_name = pk.column_name " +
                        "WHERE c.table_name = ? AND c.table_schema = 'public' " +
                        "ORDER BY c.ordinal_position";
                return jdbcTemplate.queryForList(queryPostgres, tableName.toLowerCase());

            case "oracle":
                String queryOracle = "SELECT column_name, data_type, " +
                        "CASE WHEN nullable = 'Y' THEN 'YES' ELSE 'NO' END as is_nullable, " +
                        "data_default as column_default, " +
                        "CASE WHEN c.column_name IN (" +
                        "    SELECT column_name FROM user_cons_columns ucc " +
                        "    JOIN user_constraints uc ON ucc.constraint_name = uc.constraint_name " +
                        "    WHERE uc.constraint_type = 'P' AND ucc.table_name = ?" +
                        ") THEN 'true' ELSE 'false' END AS is_primary_key " +
                        "FROM user_tab_columns c " +
                        "WHERE c.table_name = ?";
                return jdbcTemplate.queryForList(queryOracle, tableName.toUpperCase(), tableName.toUpperCase());

            case "mysql":
                String queryMysql = "SELECT " +
                        "column_name, " +
                        "data_type, " +
                        "is_nullable, " +
                        "column_default, " +
                        "CASE WHEN column_key = 'PRI' THEN true ELSE false END as is_primary_key " +
                        "FROM information_schema.columns " +
                        "WHERE table_name = ? AND table_schema = DATABASE() " +
                        "ORDER BY ordinal_position";
                return jdbcTemplate.queryForList(queryMysql, tableName);

            case "sqlserver":
                String querySqlServer = "SELECT " +
                        "c.name AS column_name, " +
                        "t.name AS data_type, " +
                        "CASE WHEN c.is_nullable = 1 THEN 'YES' ELSE 'NO' END AS is_nullable, " +
                        "OBJECT_DEFINITION(c.default_object_id) AS column_default, " +
                        "CASE WHEN pk.column_id IS NOT NULL THEN 1 ELSE 0 END AS is_primary_key " +
                        "FROM sys.columns c " +
                        "INNER JOIN sys.tables tb ON c.object_id = tb.object_id " +
                        "INNER JOIN sys.types t ON c.user_type_id = t.user_type_id " +
                        "LEFT JOIN ( " +
                        "    SELECT ic.column_id, ic.object_id FROM sys.index_columns ic " +
                        "    INNER JOIN sys.indexes i ON ic.object_id = i.object_id AND ic.index_id = i.index_id " +
                        "    WHERE i.is_primary_key = 1 " +
                        ") pk ON c.column_id = pk.column_id AND c.object_id = pk.object_id " +
                        "WHERE tb.name = ?";
                return jdbcTemplate.queryForList(querySqlServer, tableName);

            default:
                throw new RuntimeException("Desteklenmeyen veritabanı türü: " + dbType);
        }
    }

    public List<Map<String, Object>> getTableRelations(String tableName) {
        String dbType = getDatabaseType();
        String query;

        switch (dbType) {
            case "postgresql":
                query = "SELECT " +
                        "  tc.table_schema AS schema_name, " +
                        "  tc.constraint_name, " +
                        "  tc.table_name, " +
                        "  kcu.column_name, " +
                        "  ccu.table_schema AS foreign_schema_name, " +
                        "  ccu.table_name AS foreign_table_name, " +
                        "  ccu.column_name AS foreign_column_name " +
                        "FROM information_schema.table_constraints AS tc " +
                        "JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name " +
                        "JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name " +
                        "WHERE tc.constraint_type = 'FOREIGN KEY' AND (tc.table_name = ? OR ccu.table_name = ?)";
                break;

            case "oracle":
                query = "SELECT " +
                        "  a.owner AS schema_name, " +
                        "  a.constraint_name, " +
                        "  a.table_name, " +
                        "  b.column_name, " +
                        "  c.owner AS foreign_schema_name, " +
                        "  c.table_name AS foreign_table_name, " +
                        "  d.column_name AS foreign_column_name " +
                        "FROM user_constraints a " +
                        "JOIN user_cons_columns b ON a.constraint_name = b.constraint_name " +
                        "JOIN user_constraints c ON a.r_constraint_name = c.constraint_name " +
                        "JOIN user_cons_columns d ON c.constraint_name = d.constraint_name " +
                        "WHERE a.constraint_type = 'R' AND (a.table_name = ? OR c.table_name = ?)";
                return jdbcTemplate.queryForList(query, tableName.toUpperCase(), tableName.toUpperCase());

            case "mysql":
                query = "SELECT " +
                        "  kcu.referenced_table_schema AS schema_name, " +
                        "  kcu.constraint_name, " +
                        "  kcu.table_name, " +
                        "  kcu.column_name, " +
                        "  kcu.referenced_table_schema AS foreign_schema_name, " +
                        "  kcu.referenced_table_name AS foreign_table_name, " +
                        "  kcu.referenced_column_name AS foreign_column_name " +
                        "FROM information_schema.key_column_usage kcu " +
                        "WHERE kcu.referenced_table_name IS NOT NULL " +
                        "AND (kcu.table_name = ? OR kcu.referenced_table_name = ?) " +
                        "AND kcu.table_schema = DATABASE()";
                break;

            case "sqlserver":
                query = "SELECT " +
                        "  schema_name(fk.schema_id) AS schema_name, " +
                        "  fk.name AS constraint_name, " +
                        "  object_name(fk.parent_object_id) AS table_name, " +
                        "  col1.name AS column_name, " +
                        "  schema_name(pk.schema_id) AS foreign_schema_name, " +
                        "  object_name(fk.referenced_object_id) AS foreign_table_name, " +
                        "  col2.name AS foreign_column_name " +
                        "FROM sys.foreign_keys fk " +
                        "INNER JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id " +
                        "INNER JOIN sys.columns col1 ON col1.column_id = fkc.parent_column_id AND col1.object_id = fk.parent_object_id " +
                        "INNER JOIN sys.columns col2 ON col2.column_id = fkc.referenced_column_id AND col2.object_id = fk.referenced_object_id " +
                        "INNER JOIN sys.tables pk ON fk.referenced_object_id = pk.object_id " +
                        "WHERE (object_name(fk.parent_object_id) = ? OR object_name(fk.referenced_object_id) = ?)";
                break;

            default:
                throw new RuntimeException("Desteklenmeyen veritabanı türü: " + dbType);
        }

        return jdbcTemplate.queryForList(query, tableName, tableName);
    }

    public List<Map<String, Object>> getAllTableRelations() {
        String dbType = getDatabaseType();
        String query;

        switch (dbType) {
            case "postgresql":
                query = "SELECT " +
                        "  tc.table_schema AS schema_name, " +
                        "  tc.constraint_name, " +
                        "  tc.table_name AS source_table, " +
                        "  kcu.column_name AS source_column, " +
                        "  ccu.table_schema AS target_schema, " +
                        "  ccu.table_name AS target_table, " +
                        "  ccu.column_name AS target_column " +
                        "FROM information_schema.table_constraints AS tc " +
                        "JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name " +
                        "JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name " +
                        "WHERE tc.constraint_type = 'FOREIGN KEY'";
                break;

            case "oracle":
                query = "SELECT " +
                        "  a.owner AS schema_name, " +
                        "  a.constraint_name, " +
                        "  a.table_name AS source_table, " +
                        "  b.column_name AS source_column, " +
                        "  c.owner AS target_schema, " +
                        "  c.table_name AS target_table, " +
                        "  d.column_name AS target_column " +
                        "FROM user_constraints a " +
                        "JOIN user_cons_columns b ON a.constraint_name = b.constraint_name " +
                        "JOIN user_constraints c ON a.r_constraint_name = c.constraint_name " +
                        "JOIN user_cons_columns d ON c.constraint_name = d.constraint_name " +
                        "WHERE a.constraint_type = 'R'";
                break;

            case "mysql":
                query = "SELECT " +
                        "  kcu.referenced_table_schema AS schema_name, " +
                        "  kcu.constraint_name, " +
                        "  kcu.table_name AS source_table, " +
                        "  kcu.column_name AS source_column, " +
                        "  kcu.referenced_table_schema AS target_schema, " +
                        "  kcu.referenced_table_name AS target_table, " +
                        "  kcu.referenced_column_name AS target_column " +
                        "FROM information_schema.key_column_usage kcu " +
                        "WHERE kcu.referenced_table_name IS NOT NULL " +
                        "AND kcu.table_schema = DATABASE()";
                break;

            case "sqlserver":
                query = "SELECT " +
                        "  schema_name(fk.schema_id) AS schema_name, " +
                        "  fk.name AS constraint_name, " +
                        "  object_name(fk.parent_object_id) AS source_table, " +
                        "  col1.name AS source_column, " +
                        "  schema_name(pk.schema_id) AS target_schema, " +
                        "  object_name(fk.referenced_object_id) AS target_table, " +
                        "  col2.name AS target_column " +
                        "FROM sys.foreign_keys fk " +
                        "INNER JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id " +
                        "INNER JOIN sys.columns col1 ON col1.column_id = fkc.parent_column_id AND col1.object_id = fk.parent_object_id " +
                        "INNER JOIN sys.columns col2 ON col2.column_id = fkc.referenced_column_id AND col2.object_id = fk.referenced_object_id " +
                        "INNER JOIN sys.tables pk ON fk.referenced_object_id = pk.object_id";
                break;

            default:
                throw new RuntimeException("Desteklenmeyen veritabanı türü: " + dbType);
        }

        return jdbcTemplate.queryForList(query);
    }
}
