package com.mvp.markdown.repository;

import com.mvp.markdown.index.DocumentMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static final RowMapper<DocumentMetadata> DOCUMENT_METADATA_ROW_MAPPER = (rs, rowNum) -> new DocumentMetadata(
            rs.getString("title"),
            Path.of(rs.getString("path")),
            rs.getObject("uuid", UUID.class),
            rs.getInt("word_count"),
            rs.getString("search_text")
    );

    void save(DocumentMetadata documentMetadata) {
        String sql = """
            INSERT INTO documents (
                uuid,
                path,
                title,
                word_count,
                search_text,
                search_vector
            )
            VALUES (?, ?, ?, ?, ?, to_tsvector('english', ?))
            """;

        jdbcTemplate.update(sql,
            documentMetadata.getUuid(),
            documentMetadata.getPath().toString(),
            documentMetadata.getTitle(),
            documentMetadata.getWordCount(),
            documentMetadata.getSearchText(),
            documentMetadata.getSearchText()
        );
    }

    void update(DocumentMetadata documentMetadata) {
        String sql = """
                UPDATE documents
                SET
                    path = ?,
                    title = ?,
                    word_count = ?,
                    search_text = ?,
                    search_vector = to_tsvector('english', ?)
                where uuid = ?
                """;
        int rows = jdbcTemplate.update(sql,
                documentMetadata.getPath().toString(),
                documentMetadata.getTitle(),
                documentMetadata.getWordCount(),
                documentMetadata.getSearchText(),
                documentMetadata.getSearchText(),
                documentMetadata.getUuid()
        );
        if(rows == 0){
            throw new IllegalStateException(
                    "Document not found:" + documentMetadata.getUuid()
            );
        }
    }

    void delete(UUID uuid) {
        String sql = """
                DELETE FROM documents
                WHERE uuid = ?
                """;
        jdbcTemplate.update(sql, uuid);
    }

    Optional<DocumentMetadata> findByUuid(UUID uuid) {
        String sql = """
            SELECT
                uuid,
                path,
                title,
                word_count,
                search_text
            FROM documents
            WHERE uuid = ?
            """;

        List<DocumentMetadata> results = jdbcTemplate.query(sql, DOCUMENT_METADATA_ROW_MAPPER, uuid);

        return results.stream().findFirst();
    }

    public List<DocumentMetadata> search(String searchQuery) {
        String sql = """
                SELECT
                    uuid,
                    path,
                    title,
                    word_count,
                    search_text
                FROM documents
                WHERE search_vector @@ plainto_tsquery('english', ?)
                """;
        return jdbcTemplate.query(sql, DOCUMENT_METADATA_ROW_MAPPER, searchQuery);
    }
}
