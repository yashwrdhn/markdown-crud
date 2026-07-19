package com.mvp.markdown.index;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class DocumentMetadata {
    private final String title;
    private final Path path;
    private final UUID uuid;
    private final int wordCount;
    private final String searchWords;


    public DocumentMetadata(String title, Path path, UUID uuid, int wordCount, String searchWords) {
        this.title = title;
        this.path = path;
        this.uuid = uuid;
        this.wordCount = wordCount;
        this.searchWords = searchWords;
    }

    public String getTitle() { return title; }
    public Path getPath() { return path; }
    public UUID getUuid() { return uuid; }
    public int getWordCount() { return wordCount; }
    public String getSearchWords() { return searchWords; }

}
