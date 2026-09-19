package com.mvp.markdown.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;


public class LocalFileStore implements FileStore {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStore.class);



    @Override
    public void createFile(Path path) throws IOException {

        if (Files.notExists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        } else {
            throw new FileAlreadyExistsException(path.toString(), null, "Target file name already exists.");
        }

    }

    @Override
    public String readFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + path);
        }

        return Files.readString(path);
    }

    @Override
    public void writeFile(Path path, String content) throws IOException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + path);
        }
        Files.writeString(path, content);
    }

    @Override
    public boolean deleteFile(Path path) throws IOException {
        return Files.deleteIfExists(path);
    }

    @Override
    public boolean exists(Path filePath) {
        return Files.exists(filePath);
    }

    @Override
    public void createDirectory(Path path) throws IOException {
        if (path != null && Files.notExists(path)) {
            Files.createDirectories(path);
        }
        else{
            throw new DirectoryNotEmptyException(path.toString());
        }
    }

    @Override
    public void move(Path sourceFilePath, Path targetFilePath) throws IOException {
        if (Files.notExists(sourceFilePath)) {
            throw new NoSuchFileException(sourceFilePath.toString(), null, "Source file does not exist.");
        }

        if (Files.exists(targetFilePath)) {
            throw new FileAlreadyExistsException(targetFilePath.toString(), null, "Target file name already exists.");
        }

        Path targetParent = targetFilePath.getParent();
        if (targetParent != null && Files.notExists(targetParent)) {
            Files.createDirectories(targetParent);
        }

        Files.move(sourceFilePath, targetFilePath);

    }
}
