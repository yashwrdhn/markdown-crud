package com.mvp.markdown.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public interface MetadataStore {
    UUID read(Path markdownFile) throws IOException;
    void write(Path markdownFile, UUID uuid) throws IOException;
    void delete(Path markdownFile) throws IOException;
    void rename(Path oldMarkdownFile, Path newMarkdownFile) throws IOException;
    boolean exists(Path markdownFile) throws IOException;
}
