package com.mvp.markdown.index;

import com.mvp.markdown.parser.Heading;
import com.mvp.markdown.parser.ParsedDocument;
import org.springframework.stereotype.Service;

@Service
public class Indexer {

    public DocumentMetadata index(ParsedDocument document) {

        String title = extractTitle(document);

        int wordCount = countWords(document.getPlainText());

        return new DocumentMetadata(
                title,
                document.getPath(),
                document.getUuid(),
                wordCount,
                document.getPlainText()
        );
    }

    private String extractTitle(ParsedDocument document) {

        if (!document.getHeadings().isEmpty()) {
            return document.getHeadings().getFirst().getText();
        }

        String filename = document.getPath().getFileName().toString();

        int dot = filename.lastIndexOf('.');

        return dot == -1 ? filename : filename.substring(0, dot);
    }

    private int countWords(String text) {

        if (text == null || text.isBlank()) {
            return 0;
        }

        return text.trim().split("\\s+").length;
    }
}