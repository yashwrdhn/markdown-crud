package com.mvp.markdown.storage;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public interface FileStore {

    void createFile(Path path) throws IOException;

    String readFile(Path path) throws IOException;

    void writeFile(Path path, String content) throws IOException;

    boolean deleteFile(Path path) throws IOException;

    void move(Path source, Path target) throws IOException;

    boolean exists(Path path);

    void createDirectory(Path path) throws IOException;

}