package com.mvp.markdown.storage;


import java.nio.file.Path;
import java.util.UUID;

public class Document {
    private final UUID uuid;
    private final Path path;
    private final String content;

    public Document(UUID uuid, Path path, String content) {
        this.uuid = uuid;
        this.path = path;
        this.content = content;
    }

    // Getters
    public UUID getUuid() { return uuid; }
    public Path getPath() { return path; }
    public String getContent() { return content; }
}