package com.mvp.markdown.service;

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
    public void delete(Path markdownFile) throws IOException {}

    @Override
    public void rename(Path oldMarkdownFile, Path newMarkdownFile) throws IOException {

    }


}
