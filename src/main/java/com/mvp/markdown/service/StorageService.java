package com.mvp.markdown.service;

import com.mvp.markdown.AppConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class StorageService {

    private final AppConfigs appConfigs;
    private final MetadataStore metadataStore;
//    private final FileStore fileStore;

    public StorageService(AppConfigs appConfigs, MetadataStore metadataStore) {
        this.appConfigs = appConfigs;
        this.metadataStore = metadataStore;
//        this.fileStore = fileStore;
    }

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    public Path getResolvedPath(Path relativePath) {
        Path root = appConfigs.getResolvedVaultPath();

        if (!relativePath.toString().endsWith(".md") && !relativePath.toString().endsWith(".json")) {
            throw new IllegalArgumentException("Only .md files are supported");
        }
        if (relativePath == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        if (relativePath.isAbsolute()) {
            throw new IllegalArgumentException("Absolute paths are not allowed");
        }

        Path fullPath = root.resolve(relativePath).normalize();
        log.info("Resolved full path: {}", fullPath);
        if (!fullPath.startsWith(root)) {
            throw new SecurityException("Path traversal attempt detected!");
        }
        return fullPath;
    }

    public void create(Path filePath) {

        try {
            Path path = getResolvedPath(filePath);
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                metadataStore.write(path, UUID.randomUUID());
                log.info("| File created: {}",path);
            } else {
                log.info("| File already exists: {}", path);
            }
        } catch (IOException e) {
            log.error("| Failed to create file: {}", e.getMessage());
        }
    }

    public Document read(Path relativeFilePath) throws IOException {
        Path fullPath = getResolvedPath(relativeFilePath);

        if (!Files.exists(fullPath)) {
            throw new FileNotFoundException("File not found: " + relativeFilePath);
        }

        String content = Files.readString(fullPath);
        UUID uuid = metadataStore.read(fullPath);

        return new Document(uuid, relativeFilePath, content);
    }


}
