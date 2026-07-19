package com.mvp.markdown.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class SidecarMetadataStore implements MetadataStore {


    @Autowired
    private ObjectMapper objectMapper;

    private static Logger log = LoggerFactory.getLogger(SidecarMetadataStore.class);

    @Override
    public boolean exists(Path markdownFile) throws IOException {
        Path metaPath = markdownFile.resolveSibling(markdownFile.getFileName().toString() + ".meta.json");
        return Files.exists(metaPath) ;
    }

    @Override
    public UUID read(Path markdownFile) throws IOException {
        Path metaPath = markdownFile.resolveSibling(markdownFile.getFileName().toString() + ".meta.json");

        if (!Files.exists(metaPath)) {
            throw new IOException("Metadata file not found: " + metaPath);
        }

        JsonNode rootNode = objectMapper.readTree(metaPath.toFile());

        String uuidString = rootNode.path("uuid").asText();

        if (uuidString.isBlank()) {
            throw new IOException("UUID missing in metadata file: " + metaPath);
        }

        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid UUID in metadata file: " + metaPath, e);
        }
    }

    @Override
    public void write(Path markdownFile, UUID uuid) throws IOException {
        Path metaPath = markdownFile.resolveSibling(markdownFile.getFileName().toString() + ".meta.json");
        Map<String, String> mp  = new HashMap<>();
        mp.put("uuid", uuid.toString());
        Files.write(metaPath, objectMapper.writeValueAsBytes(mp));

    }

    @Override
    public void delete(Path markdownFile) throws IOException {
        Path metaPath = markdownFile.resolveSibling(markdownFile.getFileName().toString() + ".meta.json");

        // deleteIfExists returns true if the file existed and was deleted
        // It returns false if the file was not found
        boolean deleted = Files.deleteIfExists(metaPath);

        if (deleted) {
            log.info("Deleted: {}", metaPath);
        } else {
            log.warn("File not found, nothing to delete: {}", metaPath);
        }

    }

    @Override
    public void rename(Path oldMarkdownFile, Path newMarkdownFile) throws IOException {
        Path source = oldMarkdownFile.resolveSibling(oldMarkdownFile.getFileName().toString() + ".meta.json");
        Path target = newMarkdownFile.resolveSibling(newMarkdownFile.getFileName().toString() + ".meta.json");

        // 1. Check if source exists
        if (Files.notExists(source)) {
            throw new java.nio.file.NoSuchFileException(source.toString(), null, "Source file does not exist.");
        }

        // 2. Check if target already exists
        if (Files.exists(target)) {
            throw new java.nio.file.FileAlreadyExistsException(target.toString(), null, "Target file name already exists.");
        }

        Path targetParent = target.getParent();
        if (targetParent != null && Files.notExists(targetParent)) {
            Files.createDirectories(targetParent);
        }

        // 3. Perform the move
        Files.move(source, target);
    }


}
