package com.mvp.markdown.storage;

import com.mvp.markdown.AppConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class StorageService {

    private final AppConfigs appConfigs;
    private final MetadataStore metadataStore;
//    private final FileStore fileStore;

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    public StorageService(AppConfigs appConfigs, MetadataStore metadataStore) {
        this.appConfigs = appConfigs;
        this.metadataStore = metadataStore;
//        this.fileStore = fileStore;
    }

    public Path getResolvedPath(Path relativePath) {
        Path root = appConfigs.getResolvedVaultPath();

        // add this separately as a validate metric for other methods such as create, read, write.
//        if (!relativePath.toString().endsWith(".md") && !relativePath.toString().endsWith(".json")) {
//            throw new IllegalArgumentException("Only .md files are supported");
//        }
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

    public boolean exists(Path relativePath) throws IOException {
        Path filePath = getResolvedPath(relativePath);
        return Files.exists(filePath) && metadataStore.exists(filePath);
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

    public void write(Path relativeFilePath, String content) throws IOException {

        Path fullPath = getResolvedPath(relativeFilePath);
        if (!Files.exists(fullPath)) {
            throw new FileNotFoundException("File not found: " + relativeFilePath);
        }

        Files.writeString(fullPath, content);

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


    public void delete(Path relativeFilePath) throws IOException {
        Path filePath = getResolvedPath(relativeFilePath);

        // deleteIfExists returns true if the file existed and was deleted
        // It returns false if the file was not found
        boolean deleted = Files.deleteIfExists(filePath);

        if (deleted) {
            metadataStore.delete(filePath);
            log.info("Deleted: {}", filePath);
        } else {
            log.warn("File not found, nothing to delete: {}", filePath);
        }

    }


    public void rename(Path oldFilePath, Path newFilePath) throws IOException {
        Path sourceFilePath = getResolvedPath(oldFilePath);
        Path targetFilePath = getResolvedPath(newFilePath);

        // 1. Check if source exists
        if (Files.notExists(sourceFilePath)) {
            throw new java.nio.file.NoSuchFileException(sourceFilePath.toString(), null, "Source file does not exist.");
        }

        // 2. Check if target already exists
        if (Files.exists(targetFilePath)) {
            throw new java.nio.file.FileAlreadyExistsException(targetFilePath.toString(), null, "Target file name already exists.");
        }

        Path targetParent = targetFilePath.getParent();
        if (targetParent != null && Files.notExists(targetParent)) {
            Files.createDirectories(targetParent);
        }

        // 3. Perform the move
        Files.move(sourceFilePath, targetFilePath);
        metadataStore.rename(sourceFilePath, targetFilePath);
    }

    public List<Path> list(Path relativeRoot) throws IOException {
        Path fullPath = getResolvedPath(relativeRoot);
        Path vaultRoot = appConfigs.getResolvedVaultPath();

        if (Files.notExists(fullPath) || !Files.isDirectory(fullPath)) {
            throw new IllegalArgumentException("Path must be an existing directory: " + relativeRoot);
        }

        try (Stream<Path> stream = Files.walk(fullPath)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .map(vaultRoot::relativize)
                    .collect(Collectors.toList());
        }
    }


}
