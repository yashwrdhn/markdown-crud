package com.mvp.markdown.repository;


import com.mvp.markdown.index.DocumentMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest // Loads your full application context, including your repository
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/postgres",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres"
})
class DocumentRepositoryIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DocumentRepository repository;

    @BeforeEach
    void setUp() {
        System.setProperty("user.timezone","Asia/Kolkata");

        // Ensures the table exists in your local environment before executing tests
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS documents (
                uuid UUID PRIMARY KEY,
                path TEXT NOT NULL,
                title TEXT NOT NULL,
                word_count INTEGER NOT NULL,
                search_text TEXT NOT NULL,
                search_vector TSVECTOR
            );
        """);

        jdbcTemplate.update("""
                TRUNCATE TABLE documents
                """);
    }

    @Test
    void shouldSaveAndFindDocumentByUuid() {
        UUID docId = UUID.randomUUID();
        DocumentMetadata metadata = new DocumentMetadata(
                "Learning Docker",
                Path.of("/docs/docker.md"),
                docId,
                450,
                "Docker run vs exec explanation"
        );

        repository.save(metadata);
        Optional<DocumentMetadata> found = repository.findByUuid(docId);

        assertThat(found).isPresent();
        assertThat(found.get().getUuid()).isEqualTo(docId);
        assertThat(found.get().getTitle()).isEqualTo("Learning Docker");
    }

    @Test
    void shouldReturnEmptyOptionalWhenDocumentDoesNotExist() {
        Optional<DocumentMetadata> found = repository.findByUuid(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateExistingDocumentSuccessfully() {
        UUID docId = UUID.randomUUID();
        DocumentMetadata original = new DocumentMetadata(
                "Title A", Path.of("/a.md"), docId, 10, "Text A"
        );
        repository.save(original);

        DocumentMetadata updated = new DocumentMetadata(
                "Title B", Path.of("/b.md"), docId, 20, "Text B"
        );

        repository.update(updated);
        Optional<DocumentMetadata> found = repository.findByUuid(docId);

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Title B");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentDocument() {
        DocumentMetadata missingDoc = new DocumentMetadata(
                "Ghost", Path.of("/ghost.md"), UUID.randomUUID(), 0, "No text"
        );

        assertThatThrownBy(() -> repository.update(missingDoc))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Document not found:");
    }

    @Test
    void shouldDeleteDocumentSuccessfully() {
        UUID docId = UUID.randomUUID();
        DocumentMetadata metadata = new DocumentMetadata(
                "To Delete", Path.of("/delete.md"), docId, 5, "Some content"
        );
        repository.save(metadata);

        repository.delete(docId);

        assertThat(repository.findByUuid(docId)).isEmpty();
    }

    @Test
    void shouldReturnDocumentBasedOnSearchQuery(){
        UUID docId = UUID.randomUUID();
        DocumentMetadata metadata = new DocumentMetadata(
                "To Delete", Path.of("/delete.md"), docId, 5, "Some content"
        );
        repository.save(metadata);
        List<DocumentMetadata> docs = repository.search("content");
        assertThat(docs).hasSize(1);
        assertThat(docs.getFirst().getUuid()).isEqualTo(docId);
    }

    @Test
    void shouldReturnZeroDocumentsBasedOnNonExistentSearchQuery(){
        UUID docId = UUID.randomUUID();
        DocumentMetadata metadata = new DocumentMetadata(
                "To Delete", Path.of("/delete.md"), docId, 5, "Some content"
        );
        repository.save(metadata);
        List<DocumentMetadata> docs = repository.search("this text doesnt exist");
        assertThat(docs).isEmpty();

    }
}