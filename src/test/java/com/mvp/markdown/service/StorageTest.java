package com.mvp.markdown.service;

import com.mvp.markdown.AppConfigs;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StorageTest {

    @Autowired
    private StorageService storageService;

    @MockitoBean
    private AppConfigs appConfigs;


    void createMarkDown(String fileName, String content) throws IOException, JSONException {
        Files.writeString(storageService.getResolvedPath(Path.of(fileName)), content);
    }

    void createMetadata(String fileName, UUID uuid) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        Files.writeString(storageService.getResolvedPath(Path.of(fileName + ".meta.json")), jsonObject.toString());
    }

    @Test
    void testVaultPathValidation() {
        Path path = appConfigs.getResolvedVaultPath();

        // Validate that the directory exists (or was created by @PostConstruct)
        assertTrue(Files.exists(path), "Vault directory should exist");
        assertTrue(Files.isDirectory(path), "Vault path should be a directory");
    }

    @Test
    void testFileCreation() throws JSONException, IOException {
        String fileName = "test-file.md";
        String content = "This is a test file.";
        UUID uuid = UUID.randomUUID();
        createMarkDown(fileName, content);
        createMetadata(fileName, uuid);

        storageService.create(Path.of(fileName));

        Path filePath = appConfigs.getResolvedVaultPath().resolve(fileName);
        assertTrue(Files.exists(filePath), "File should have been created in the vault");
    }

    @Test
    void testFileExists() throws IOException, JSONException {
        String fileName = "test-file.md";
        String content = "This is a test file.";
        UUID uuid = UUID.randomUUID();
        createMarkDown(fileName, content);
        createMetadata(fileName, uuid);
        assertTrue(storageService.exists(Path.of(fileName)));
    }

    @Test
    void testFileRead() throws IOException, JSONException {

        String fileName = "test-file.md";
        String content = "This is a test file.";
        UUID uuid = UUID.randomUUID();
        createMarkDown(fileName, content);
        createMetadata(fileName, uuid);

        Document doc = storageService.read(Path.of(fileName));

        // 3. Assert
        assertNotNull(doc);
        assertEquals(uuid, doc.getUuid());
        assertEquals(Path.of(fileName), doc.getPath());
    }

    @Test
    void testFileRename() throws IOException {
        String srcFileName = "test-file.md";
        String targetFileName = "test-rename.md";
        storageService.rename(Path.of(srcFileName), Path.of(targetFileName));

        assertTrue(storageService.exists(Path.of(targetFileName)));
        assertTrue(!storageService.exists(Path.of(srcFileName)));
    }

    @Test
    void testFileDelete() throws IOException {
        String fileName = "test-rename.md";
        storageService.delete(Path.of(fileName));
        assertTrue(!storageService.exists(Path.of(fileName)));
    }

    @TempDir
    Path tempVault;

    @Test
    void testListRecursiveFiltersCorrectly() throws IOException {
        // 1. Arrange: Create a nested structure
        // vault/
        // ├── note1.md
        // ├── ignore.txt
        // └── nested/
        //     └── note2.md
        Path dir = tempVault.resolve("nested");
        Files.createDirectories(dir);

        Files.createFile(tempVault.resolve("note1.md"));
        Files.createFile(tempVault.resolve("ignore.txt"));
        Files.createFile(dir.resolve("note2.md"));

        Mockito.when(appConfigs.getResolvedVaultPath()).thenReturn(tempVault);
        // 2. Act: Call the list method
        // Note: You may need to mock appConfigs if you aren't passing the path directly
        List<Path> result = storageService.list(Path.of(""));
        System.out.println(result);
        // 3. Assert
        assertEquals(2, result.size(), "Should only contain the 2 markdown files");
        assertTrue(result.contains(Path.of("note1.md")), "Should contain note1.md");
        assertTrue(result.contains(Path.of("nested/note2.md")), "Should contain nested/note2.md");
        assertFalse(result.contains(Path.of("ignore.txt")), "Should ignore .txt files");
    }
}