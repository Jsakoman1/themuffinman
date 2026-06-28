package com.themuffinman.app.location.service;

import com.themuffinman.app.location.dto.DatabaseTableStatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDatabaseMetricsService {

    private final JdbcTemplate jdbcTemplate;

    public long getDatabaseSizeBytes() {
        Long size = jdbcTemplate.queryForObject("select pg_database_size(current_database())", Long.class);
        return size == null ? 0L : size;
    }

    public List<DatabaseTableStatusDTO> getTableStatuses() {
        List<String> tableNames = jdbcTemplate.queryForList("""
                select table_name
                from information_schema.tables
                where table_schema = 'public'
                  and table_type = 'BASE TABLE'
                  and table_name <> 'flyway_schema_history'
                order by table_name
                """, String.class);

        return tableNames.stream()
                .map(this::toTableStatus)
                .toList();
    }

    private DatabaseTableStatusDTO toTableStatus(String tableName) {
        long rowCount = countRows(tableName);
        return DatabaseTableStatusDTO.builder()
                .tableName(tableName)
                .rowCount(rowCount)
                .build();
    }

    private long countRows(String tableName) {
        if (!tableName.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Unsupported table name: " + tableName);
        }

        Long count = jdbcTemplate.queryForObject("select count(*) from " + tableName, Long.class);
        return count == null ? 0L : count;
    }
}
