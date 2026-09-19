package com.mvp.markdown.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class StorageService {


    private final MetadataStore metadataStore;
    private final FileStore fileStore;
    private final Path root;
    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    public StorageService(Path root, FileStore fileStore, MetadataStore metadataStore) {
        this.root = root;
        this.metadataStore = metadataStore;
        this.fileStore = fileStore;

        log.info("Root: {}", root);
        log.info("Root exists: {}", Files.exists(root));
        log.info("Root is directory: {}", Files.isDirectory(root));
        log.info("Root writable: {}", Files.isWritable(root));

    }

    public Path getResolvedPath(Path relativePath) {

        // add this separately as a validate metric for other methods such as create, read, write.
//        if (!relativePath.toString().endsWith(".md") && !relativePath.toString().endsWith(".json")) {
//            throw new IllegalArgumentException("Only .md files are supported");
//        }


        Path fullPath = root.resolve(relativePath).normalize();

        log.info("Resolved full path: {}", fullPath);

        if (relativePath == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

//        if (relativePath.isAbsolute()) {
//            throw new IllegalArgumentException("Absolute paths are not allowed");
//        }


        if (!fullPath.startsWith(root)) {
            throw new SecurityException("Path traversal attempt detected!");
        }
        return fullPath;
    }

    public boolean exists(Path filePath) throws IOException {
        Path relativePath = getResolvedPath(filePath);
        return fileStore.exists(relativePath) && metadataStore.exists(relativePath);
    }


    public void createFile(Path filePath) throws IOException {
        Path path = getResolvedPath(filePath);

        try {
            fileStore.createFile(path);
            log.info("Markdown file created, writing metadata");
            metadataStore.write(path, UUID.randomUUID());
            log.info("| File created: {}",path);

        } catch (IOException e) {
            log.error("| Failed to create file: ", e);
            throw e;
        }
    }

    public void createDirectory(Path dirPath) throws IOException {
        Path path = getResolvedPath(dirPath);

        try {
            fileStore.createDirectory(path);

        } catch (IOException e) {
            log.error("| Failed to create file: ", e);
            throw e;
        }
    }

    public void write(Path relativeFilePath, String content) throws IOException {

        Path fullPath = getResolvedPath(relativeFilePath);
        fileStore.writeFile(fullPath, content);

    }

    public Document read(Path relativeFilePath) throws IOException {
        Path fullPath = getResolvedPath(relativeFilePath);

        String content = fileStore.readFile(fullPath);
        UUID uuid = metadataStore.read(fullPath);

        return new Document(uuid, relativeFilePath, content);
    }


    public void delete(Path relativeFilePath) throws IOException {
        Path filePath = getResolvedPath(relativeFilePath);

        // deleteIfExists returns true if the file existed and was deleted
        // It returns false if the file was not found
        boolean deleted = fileStore.deleteFile(filePath);

        if (deleted) {
            metadataStore.delete(filePath);
            log.info("Deleted: {}", filePath);
        } else {
            log.warn("File not found, nothing to delete: {}", filePath);
            throw new IOException("File not found, nothing to delete");
        }

    }


    public void rename(Path oldFilePath, Path newFilePath) throws IOException {
        Path sourceFilePath = getResolvedPath(oldFilePath);
        Path targetFilePath = getResolvedPath(newFilePath);

        fileStore.move(sourceFilePath, targetFilePath);
        metadataStore.rename(sourceFilePath, targetFilePath);
    }

    public record DocumentMetadata(
            UUID uuid,
            String path
    ) {}

    public record FileMetadata(
            String name,
            String path,
            boolean directory,
            UUID uuid,
            List<FileMetadata> children
    ) {}

    public List<FileMetadata> list() throws IOException {
        Path fullPath = getResolvedPath(root);

        if (Files.notExists(fullPath) || !Files.isDirectory(fullPath)) {
            throw new IllegalArgumentException(
                    "Path must be an existing directory: " + fullPath
            );
        }

        return listDirectory(fullPath);
    }

    private List<FileMetadata> listDirectory(Path directory) throws IOException {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .map(path -> {
                        try {
                            Path relativePath = root.relativize(path);
                            String name = path.getFileName().toString();

                            if (Files.isDirectory(path)) {
                                return new FileMetadata(
                                        name,
                                        relativePath.toString(),
                                        true,
                                        null,
                                        listDirectory(path)
                                );
                            }

                            if (Files.isRegularFile(path)
                                    && name.endsWith(".md")) {

                                UUID uuid = metadataStore.read(path);

                                return new FileMetadata(
                                        name,
                                        relativePath.toString(),
                                        false,
                                        uuid,
                                        List.of()
                                );
                            }

                            return null;

                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    public Optional<Document> findByUuid(UUID uuid) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .map(path -> {
                        try {
                            UUID documentUuid = metadataStore.read(path);

                            if (!documentUuid.equals(uuid)) {
                                return null;
                            }

                            Path relativePath = root.relativize(path);
                            String content = Files.readString(path);

                            return new Document(
                                    documentUuid,
                                    relativePath,
                                    content
                            );

                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst();
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }


}
