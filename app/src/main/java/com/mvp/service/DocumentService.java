package com.mvp.service;

import com.mvp.markdown.index.DocumentMetadata;
import com.mvp.markdown.index.Indexer;
import com.mvp.markdown.parser.MarkdownParser;
import com.mvp.markdown.parser.ParsedDocument;
import com.mvp.markdown.storage.Document;
import com.mvp.markdown.storage.StorageService;
import com.mvp.repository.DocumentRepository;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class DocumentService {

    private final StorageService storageService;
    private final DocumentRepository documentRepository;
    private final MarkdownParser parser;
    private final Indexer indexer;

    public DocumentService(StorageService storageService, DocumentRepository documentRepository) {
        this.storageService = storageService;
        this.documentRepository = documentRepository;
        this.parser = new MarkdownParser();
        this.indexer = new Indexer();
    }

    public Document createDocument(String relativePath, String content)
            throws IOException {

        Path path = Path.of(relativePath);
        System.out.println("path packed in request " + path);
        storageService.createFile(path);
        storageService.write(path, content);

        Document document = storageService.read(path);
        DocumentMetadata metadata = indexIt(document);
        System.out.println("path fetched from document " + document.getPath());
        documentRepository.save(metadata);

        return document;
    }

    public Optional<Document> getDocument(UUID uuid) throws IOException {
        return storageService.findByUuid(uuid);
    }

    public void updateDocument(UUID uuid, String content) throws IOException {
        Document document = getRequiredDocument(uuid);
        storageService.write(document.getPath(), content);

        Document updatedDocument = storageService.read(document.getPath());
        indexIt(updatedDocument);
    }

    public void deleteDocument(UUID uuid) throws IOException {
        Document document = getRequiredDocument(uuid);
        storageService.delete(document.getPath());
        documentRepository.delete(uuid);
    }

    public void renameDocument(UUID uuid, String newPath) throws IOException {
        Document document = getRequiredDocument(uuid);
        storageService.rename(
                document.getPath(),
                Path.of(newPath)
        );
        documentRepository.updatePath(uuid, Path.of(newPath));
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


    private DocumentMetadata indexIt(Document document){
        ParsedDocument parsedDocument = parser.parse(document);
        return indexer.index(parsedDocument);
    }
}