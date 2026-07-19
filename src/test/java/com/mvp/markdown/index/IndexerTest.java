package com.mvp.markdown.index;

import com.mvp.markdown.parser.*;
import com.mvp.markdown.storage.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class IndexerTest {

    private Indexer indexer;
    private final UUID docUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        indexer = new Indexer();
    }

    private ParsedDocument createParsedDoc(String title, String plainText, List<Heading> headings) {
        Document doc = new Document(docUuid, Paths.get("my-document.md"), "");
        return new ParsedDocument(docUuid, doc.getPath(), List.of(), headings, List.of(), plainText);
    }

    @Test
    void shouldExtractTitleFromFirstHeading() {
        List<Heading> headings = List.of(new Heading(1, "Main Title", 1));
        ParsedDocument doc = createParsedDoc("", "Some text", headings);

        DocumentMetadata result = indexer.index(doc);

        assertThat(result.getTitle()).isEqualTo("Main Title");
    }

    @Test
    void shouldFallbackToFilenameIfNoHeadingsExist() {
        // No headings provided
        ParsedDocument doc = createParsedDoc("", "Some text", List.of());

        DocumentMetadata result = indexer.index(doc);

        // Should strip the .md extension
        assertThat(result.getTitle()).isEqualTo("my-document");
    }

    @Test
    void shouldCalculateWordCountCorrectly() {
        String text = "This is a test document with six words.";
        ParsedDocument doc = createParsedDoc("", text, List.of());

        DocumentMetadata result = indexer.index(doc);

        assertThat(result.getWordCount()).isEqualTo(8);
    }

    @Test
    void shouldReturnZeroWordsForEmptyOrBlankText() {
        ParsedDocument doc = createParsedDoc("", "   ", List.of());

        DocumentMetadata result = indexer.index(doc);

        assertThat(result.getWordCount()).isEqualTo(0);
    }

    @Test
    void shouldPopulateMetadataFieldsCorrectly() {
        ParsedDocument doc = createParsedDoc("Title", "Content here", List.of());

        DocumentMetadata result = indexer.index(doc);

        assertThat(result.getUuid()).isEqualTo(docUuid);
        assertThat(result.getPath().toString()).contains("my-document.md");
        assertThat(result.getSearchText()).isEqualTo("Content here");
    }
}