package com.mvp.markdown.service;

import com.mvp.markdown.AppConfigs;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StorageTest {

    @Autowired
    private StorageService storageService;

    @Autowired
    private AppConfigs appConfigs;

    @Test
    void testVaultPathValidation() {
        Path path = appConfigs.getResolvedVaultPath();

        // Validate that the directory exists (or was created by @PostConstruct)
        assertTrue(Files.exists(path), "Vault directory should exist");
        assertTrue(Files.isDirectory(path), "Vault path should be a directory");
    }

    @Test
    void testFileCreation() {
        String fileName = "test-file.md";

        storageService.create(Path.of(fileName));

        Path filePath = appConfigs.getResolvedVaultPath().resolve(fileName);
        assertTrue(Files.exists(filePath), "File should have been created in the vault");
    }

    @Test
    void testFileRead() throws IOException, JSONException {
        // 1. Arrange: Create files programmatically in the test
        String fileName = "test-file.md";
        Files.writeString(storageService.getResolvedPath(Path.of(fileName)), "hello world");
        UUID uuid = UUID.randomUUID();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        Files.writeString(storageService.getResolvedPath(Path.of(fileName + ".meta.json")), jsonObject.toString());

        // 2. Act
        Document doc = storageService.read(Path.of(fileName));

        // 3. Assert
        assertNotNull(doc);
        assertEquals(uuid, doc.getUuid());
        assertEquals(Path.of(fileName), doc.getPath());
    }
}