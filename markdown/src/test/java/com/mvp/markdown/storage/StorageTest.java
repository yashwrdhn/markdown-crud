package com.mvp.markdown.storage;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest {

    @TempDir
    Path tempVault;

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        FileStore fileStore = new LocalFileStore();
        MetadataStore metadataStore = new SidecarMetadataStore();
        storageService = new StorageService(tempVault, fileStore, metadataStore);
    }

    void createMarkDown(String fileName, String content)
            throws IOException, JSONException {
        Files.writeString(
                storageService.getResolvedPath(Path.of(fileName)),
                content
        );
    }

    void createMetadata(String fileName, UUID uuid)
            throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);

        Files.writeString(
                storageService.getResolvedPath(
                        Path.of(fileName + ".meta.json")
                ),
                jsonObject.toString()
        );
    }

    @Test
    void testVaultPathValidation() {
        assertTrue(Files.exists(tempVault));
        assertTrue(Files.isDirectory(tempVault));
    }

    @Test
    void testFileCreation() throws JSONException, IOException {
        String fileName = "test-file.md";
        String content = "This is a test file.";
        UUID uuid = UUID.randomUUID();

        createMarkDown(fileName, content);
        createMetadata(fileName, uuid);

        storageService.createFile(Path.of(fileName));

        Path filePath = tempVault.resolve(fileName);

        assertTrue(
                Files.exists(filePath),
                "File should have been created in the vault"
        );
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

        assertNotNull(doc);
        assertEquals(uuid, doc.getUuid());
        assertEquals(Path.of(fileName), doc.getPath());
    }

    @Test
    void testFileRename() throws IOException {
        String srcFileName = "test-file.md";
        String targetFileName = "test-rename.md";
        UUID uuid = UUID.randomUUID();

        createMarkDown(srcFileName, "test content");
        createMetadata(srcFileName, uuid);

        storageService.rename(
                Path.of(srcFileName),
                Path.of(targetFileName)
        );

        assertTrue(storageService.exists(Path.of(targetFileName)));
        assertFalse(storageService.exists(Path.of(srcFileName)));
    }

    @Test
    void testFileDelete() throws IOException {
        String fileName = "test-rename.md";

        createMarkDown(fileName, "test content");

        storageService.delete(Path.of(fileName));

        assertFalse(storageService.exists(Path.of(fileName)));
    }

    @Test
    void testListRecursiveFiltersCorrectly() throws IOException {
        // vault/
        // ├── note1.md
        // ├── ignore.txt
        // └── nested/
        //     └── note2.md

        Path nested = tempVault.resolve("nested");
        Files.createDirectories(nested);

        Files.createFile(tempVault.resolve("note1.md"));
        Files.createFile(tempVault.resolve("ignore.txt"));
        Files.createFile(nested.resolve("note2.md"));

        List<StorageService.FileMetadata> result = storageService.list();

        assertEquals(2, result.size());

//        assertTrue(result.contains(Path.of("note1.md")));
//        assertTrue(result.contains(Path.of("nested", "note2.md")));
//        assertFalse(result.contains(Path.of("ignore.txt")));
    }
}