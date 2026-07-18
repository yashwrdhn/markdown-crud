package com.mvp.markdown.service;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStore {

    void createFile(Path path) throws IOException;

    String readFile(Path path) throws IOException;

    void writeFile(Path path, String content) throws IOException;

    void deleteFile(Path path) throws IOException;

    void move(Path source, Path target) throws IOException;

    boolean exists(Path path);

    void createDirectories(Path path) throws IOException;
}