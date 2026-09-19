package com.mvp.controller;

import com.mvp.markdown.storage.Document;
import com.mvp.markdown.storage.StorageService;
import com.mvp.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public Document createDocument(
            @RequestBody CreateDocumentRequest request
    ) throws IOException {

        return documentService.createDocument(
                request.path(),
                request.content()
        );
    }

//    @GetMapping("/documents")
//    public List<DocumentService.DocumentSummary> listDocuments()
//            throws IOException {
//
//        return documentService.listDocuments();
//    }

    @GetMapping("/documents/{uuid}")
    public ResponseEntity<Document> getDocument(
            @PathVariable UUID uuid
    ) throws IOException {

        return documentService.getDocument(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/documents/{uuid}")
    public ResponseEntity<Void> updateDocument(
            @PathVariable UUID uuid,
            @RequestBody UpdateDocumentRequest request
    ) throws IOException {
        documentService.updateDocument(uuid, request.content());
        return ResponseEntity.noContent().build();

    }

    @DeleteMapping("/documents/{uuid}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID uuid
    ) throws IOException {
        documentService.deleteDocument(uuid);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/directories")
    @ResponseStatus(HttpStatus.CREATED)
    public void createDirectory(
            @RequestBody CreateDirectoryRequest request
    ) throws IOException {
        documentService.createDirectory(request.path());
    }

    @GetMapping("/files")
    public List<StorageService.FileMetadata> getTree()
            throws IOException {
        return documentService.getTree();
    }

    @PatchMapping("/documents/{uuid}")
    public ResponseEntity<Void> renameDocument(
            @PathVariable UUID uuid,
            @RequestBody RenameDocumentRequest request
    ) throws IOException {

        documentService.renameDocument(uuid, request.newPath());
        return ResponseEntity.noContent().build();
    }

    public record CreateDocumentRequest(
            String path,
            String content
    ) {}


    public record CreateDirectoryRequest( String path ) {}
    public record UpdateDocumentRequest(String content) {}
    public record RenameDocumentRequest(String newPath) {}

}