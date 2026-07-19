package com.mvp.markdown.parser;

import com.mvp.markdown.storage.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class MarkdownParserTest {

    private MarkdownParser parser;

    @BeforeEach
    void setUp() {
        parser = new MarkdownParser();
    }

    private Document createDoc(String content) {
        return new Document(UUID.randomUUID(), Paths.get("test.md"), content);
    }

    @Test
    void testEmptyDocument() {
        Document doc = createDoc("");
        ParsedDocument result = parser.parse(doc);
        assertThat(result.getHeadings()).isEmpty();
        assertThat(result.getCodeBlocks()).isEmpty();
        assertThat(result.getLinks()).isEmpty();
    }

    @Test
    void testSingleHeading() {
        Document doc = createDoc("# Hello World");
        ParsedDocument result = parser.parse(doc);
        assertThat(result.getHeadings()).hasSize(1);
        assertThat(result.getHeadings().get(0).getText()).isEqualTo("Hello World");
        assertThat(result.getHeadings().get(0).getLineNumber()).isEqualTo(1);
    }

    @Test
    void testMultipleHeadingsWithDifferentLevels() {
        Document doc = createDoc("# H1\n## H2\n### H3");
        ParsedDocument result = parser.parse(doc);
        assertThat(result.getHeadings()).hasSize(3);
        assertThat(result.getHeadings().get(0).getLevel()).isEqualTo(1);
        assertThat(result.getHeadings().get(2).getLevel()).isEqualTo(3);
    }

    @Test
    void testSingleCodeBlock() {
        Document doc = createDoc("```\nprint('hello')\n```");
        ParsedDocument result = parser.parse(doc);
        assertThat(result.getCodeBlocks()).hasSize(1);
        assertThat(result.getCodeBlocks().get(0).getContent()).contains("print('hello')");
    }

    @Test
    void testCodeBlockWithLanguage() {
        Document doc = createDoc("```java\nSystem.out.println();\n```");
        ParsedDocument result = parser.parse(doc);
        assertThat(result.getCodeBlocks().get(0).getLanguage()).isEqualTo("java");
    }

    @Test
    void testMultipleCodeBlocks() {
        Document doc = createDoc("```\n1\n```\nSome text\n```\n2\n```");
        ParsedDocument result = parser.parse(doc);
        assertThat(result.getCodeBlocks()).hasSize(2);
    }

    @Test
    void testSingleExternalLink() {
        Document doc = createDoc("[Google](https://google.com)");
        ParsedDocument result = parser.parse(doc);
        assertThat(result.getLinks()).hasSize(1);
        assertThat(result.getLinks().getFirst().getText()).isEqualTo("Google");
    }

    @Test
    void testMultipleLinksOnOneLine() {
        Document doc = createDoc("[A](https://a.com) and [B](https://b.com)");
        ParsedDocument result = parser.parse(doc);
        assertThat(result.getLinks()).hasSize(2);
    }

    @Test
    void testMixedDocument() {
        String content = "# Title\n[Link](https://test.com)\n```\ncode\n```";
        ParsedDocument result = parser.parse(createDoc(content));
        assertThat(result.getHeadings()).hasSize(1);
        assertThat(result.getLinks()).hasSize(1);
        assertThat(result.getCodeBlocks()).hasSize(1);
    }

    @Test
    void testHeadingsAndLinksIgnoredInsideCodeBlocks() {
        String content = "```\n# Not a heading\n[Not a link](https://test.com)\n```";
        ParsedDocument result = parser.parse(createDoc(content));
        assertThat(result.getHeadings()).isEmpty();
        assertThat(result.getLinks()).isEmpty();
        assertThat(result.getCodeBlocks()).hasSize(1);
    }

    @Test
    void testNoRecognizedConstructs() {
        Document doc = createDoc("Just some plain text without markdown.");
        ParsedDocument result = parser.parse(doc);
        assertThat(result.getHeadings()).isEmpty();
        assertThat(result.getLinks()).isEmpty();
        assertThat(result.getCodeBlocks()).isEmpty();
    }
}