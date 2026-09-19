package com.mvp.service;

import com.mvp.markdown.storage.Document;
import com.mvp.markdown.storage.StorageService;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class DocumentService {

    private final StorageService storageService;

    public DocumentService(StorageService storageService) {
        this.storageService = storageService;
    }

    public Document createDocument(String relativePath, String content)
            throws IOException {

        Path path = Path.of(relativePath);

        storageService.createFile(path);
        storageService.write(path, content);

        return storageService.read(path);
    }

    public Optional<Document> getDocument(UUID uuid) throws IOException {
        return storageService.findByUuid(uuid);
    }

    public void updateDocument(UUID uuid, String content) throws IOException {
        Document document = getRequiredDocument(uuid);
        storageService.write(document.getPath(), content);
    }

    public void deleteDocument(UUID uuid) throws IOException {
        Document document = getRequiredDocument(uuid);
        storageService.delete(document.getPath());
    }

    public void renameDocument(UUID uuid, String newPath) throws IOException {
        Document document = getRequiredDocument(uuid);
        storageService.rename(
                document.getPath(),
                Path.of(newPath)
        );
    }

    public void createDirectory(String relativePath) throws IOException {
        storageService.createDirectory(Path.of(relativePath));
    }

    public List<StorageService.FileMetadata> getTree() throws IOException {
        return storageService.list();
    }

    private Document getRequiredDocument(UUID uuid) throws IOException {
        return storageService.findByUuid(uuid)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Document not found: " + uuid
                        )
                );
    }
}